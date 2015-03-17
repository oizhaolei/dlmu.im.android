package com.ruptech.chinatalk.adapter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.baidutranslate.openapi.TranslateClient;
import com.baidu.baidutranslate.openapi.callback.ITransResultCallback;
import com.baidu.baidutranslate.openapi.entity.TransResult;
import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.http.HttpConnection;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.sqlite.ChatProvider;
import com.ruptech.chinatalk.model.Chat;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.sqlite.TableContent;
import com.ruptech.chinatalk.sqlite.TableContent.ChatTable;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RequestVerifyTask;
import com.ruptech.chinatalk.task.impl.RetrieveMessageTask;
import com.ruptech.chinatalk.task.impl.TranslateAcceptTask;
import com.ruptech.chinatalk.task.impl.XmppRequestTranslateTask;
import com.ruptech.chinatalk.ui.FullScreenActivity;
import com.ruptech.chinatalk.ui.ImageViewActivity;
import com.ruptech.chinatalk.ui.setting.WebViewActivity;
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.TimeUtil;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.face.EmojiParser;
import com.ruptech.chinatalk.utils.face.ParseEmojiMsgUtil;
import com.ruptech.chinatalk.widget.CustomDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatAdapter extends CursorAdapter {
    private static final String TAG = ChatAdapter.class.getName();

    enum ChatType {
        MY_PHOTO, MY_VOICE, MY_TEXT, FRIEND_PHOTO, FRIEND_VOICE, FRIEND_TEXT, TYPE_COUNT
    }

    static class ViewHolder {
        private TextView createDateTextView;

        private View divider;
        private View userThumbView;
        private ImageView userThumbImageView;
        private ImageView smsImageView;
        private ImageView langImageView;
        private ImageView photoImageView;
        private ProgressBar photoProgressBar;
        private ImageView voiceImageView;
        private TextView contentTextView;
        private TextView translatedContentTextView;
        private TextView lengthTextView;
        private ProgressBar playProcessBar;
        private TextView timeTextView;
        private TextView sendErrorView;

        private View bubbleLayout;

        private View autoTranslationLayout;

    }

    private static final int DELAY_NEWMSG = 2000;
    private ActionBarActivity mContext;

    public ChatAdapter(Context context, Cursor cursor) {
	    super(context, cursor, false);
	    mContext = context;
	    mInflater = LayoutInflater.from(context);
	    mContentResolver = context.getContentResolver();

        Display display = ((Activity) context).getWindowManager()
                .getDefaultDisplay();
        mWidth = display.getWidth();
        CREATE_DATE_INDEX = cursor
                .getColumnIndexOrThrow(ChatTable.Columns.CREATED_DATE);
    }



    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor) getItem(position);
        Chat chat = ChatTable.parseCursor(cursor);
        return intValue(getChatType(chat));
    }

    public int intValue(ChatType type) {
        return type.ordinal();
    }

    public ChatType getChatType(Chat chat) {
        boolean mine = (chat.getFromJid() == App.readUser().getOF_JabberID());
        ChatType type;
        if (AppPreferences.MESSAGE_TYPE_NAME_PHOTO.equals(chat.getType())) {
            type = mine ? ChatType.MY_PHOTO : ChatType.FRIEND_PHOTO;
        } else if (AppPreferences.MESSAGE_TYPE_NAME_VOICE
                .equals(chat.getType())) {
            type = mine ? ChatType.MY_VOICE : ChatType.FRIEND_VOICE;
        } else {
            type = mine ? ChatType.MY_TEXT : ChatType.FRIEND_TEXT;
        }

        return type;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();
        Chat chat = ChatTable.parseCursor(cursor);
        boolean mine = isMine(chat);

        User user, mFriendUser;
        mFriendUser = App.userDAO.fetchUser(Utils.getTTTalkIDFromOF_JID(chat.getToJid()));
        if (mFriendUser == null ){
            mFriendUser = new User();
            mFriendUser.setLang("CN");
        }
        if (mine) {
            chat.setToLang(mFriendUser.getLang());
            chat.setFromLang(App.readUser().getLang());
            user = App.readUser();
        } else {
            chat.setFromLang(mFriendUser.getLang());
            chat.setToLang(App.readUser().getLang());
            user = mFriendUser;
        }

        if (!mine && chat.getRead() == ChatProvider.DS_NEW) {
            markAsReadDelayed(chat.getId(), DELAY_NEWMSG);
        }

        bindProfileThumb(user, holder.userThumbImageView, holder.smsImageView,
                holder.langImageView);
        bindUserThumbClickEvent(holder.userThumbView, user);
        bindImageClickEvent(chat, holder.photoImageView);
        bindVoiceClickEvent(chat, holder.voiceImageView,
                holder.playProcessBar, holder.bubbleLayout);
        bindLayoutClickEvent(chat, holder.bubbleLayout);
        bindFromView(chat, holder);
        bindToView(chat, holder.translatedContentTextView,
                holder.autoTranslationLayout);
        bindDateTimeView(cursor, holder.createDateTextView);
    }

    private void bindFromView(Chat chat, final ViewHolder holder) {
        if (holder.divider != null)
            holder.divider.setVisibility(View.GONE);
        if (holder.voiceImageView != null)
            holder.voiceImageView.setVisibility(View.GONE);
        if (holder.lengthTextView != null)
            holder.lengthTextView.setVisibility(View.GONE);
        if (holder.photoImageView != null)
            holder.photoImageView.setVisibility(View.GONE);
        if (holder.contentTextView != null)
            holder.contentTextView.setVisibility(View.GONE);
        if (holder.photoProgressBar != null)
            holder.photoProgressBar.setVisibility(View.GONE);

        // time
        String datetimeStr = TimeUtil.getHourAndMin(chat.getCreated_date());
        holder.timeTextView.setText(datetimeStr);


        // divider
        if (!Utils.isEmpty(chat.getTo_content()) && holder.divider != null) {
            holder.divider.setVisibility(View.VISIBLE);
        }

        if (AppPreferences.MESSAGE_TYPE_NAME_PHOTO.equals(chat.getType())) {
            // photo
            bindFromPhotoView(chat, holder.photoImageView,
                    holder.photoProgressBar);

        } else if (AppPreferences.MESSAGE_TYPE_NAME_VOICE
                .equals(chat.getType())) {
            // voice
            bindFromVoiceView(chat, holder.voiceImageView,
                    holder.lengthTextView, holder.playProcessBar);
        } else {// text
            bindFromContentView(chat, holder.contentTextView);
        }

    }

    private void markAsReadDelayed(final int id, int delay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                markAsRead(id);
            }
        }, delay);
    }

    /**
     * 标记为已读消息
     *
     * @param id
     */
    private void markAsRead(int id) {
        Uri rowuri = Uri.parse("content://" + ChatProvider.AUTHORITY + "/"
                + ChatProvider.QUERY_URI + "/" + id);
        Log.d(TAG, "markAsRead: " + rowuri);
        ContentValues values = new ContentValues();
        values.put(ChatTable.Columns.DELIVERY_STATUS, ChatProvider.DS_SENT_OR_READ);
        mContext.getContentResolver().update(rowuri, values, null, null);
    }

    @Override
    public int getViewTypeCount() {
        return intValue(ChatType.TYPE_COUNT);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Chat chat = ChatTable.parseCursor(cursor);

        ViewHolder holder = new ViewHolder();
        View view;
        int resID;
        ChatType type = this.getChatType(chat);
        switch (type) {
            case MY_PHOTO:
                resID = R.layout.item_chatting_msg_right_photo;
                break;
            case MY_VOICE:
                resID = R.layout.item_chatting_msg_right_voice;
                break;
            case MY_TEXT:
                resID = R.layout.item_chatting_msg_right_text;
                break;
            case FRIEND_PHOTO:
                resID = R.layout.item_chatting_msg_left_photo;
                break;
            case FRIEND_VOICE:
                resID = R.layout.item_chatting_msg_left_voice;
                break;
            case FRIEND_TEXT:
                resID = R.layout.item_chatting_msg_left_text;
                break;
            default:
                if (isMine(chat))
                    resID = R.layout.item_chatting_msg_right_text;
                else
                    resID = R.layout.item_chatting_msg_left_text;
        }

        view = mInflater.inflate(resID, parent, false);

        holder.createDateTextView = (TextView) view
                .findViewById(R.id.item_chatting_createdtime_textview);

        holder.bubbleLayout = view.findViewById(R.id.item_chatting_from_layout);
        holder.divider = view.findViewById(R.id.item_chatting_divider);
        holder.userThumbView = view
                .findViewById(R.id.item_chatting_friend_mask);
        holder.userThumbImageView = (ImageView) view
                .findViewById(R.id.item_chatting_user_thumb_imageview);
        holder.smsImageView = (ImageView) view
                .findViewById(R.id.item_chatting_user_sms_imageview);
        holder.langImageView = (ImageView) view
                .findViewById(R.id.item_chatting_user_lang_imageview);

        holder.photoImageView = (ImageView) view
                .findViewById(R.id.item_chatting_photo_imageview);
        holder.photoProgressBar = (ProgressBar) view
                .findViewById(R.id.item_chatting_photo_progress);
        holder.voiceImageView = (ImageView) view
                .findViewById(R.id.item_chatting_voice_imageview);
        holder.contentTextView = (TextView) view
                .findViewById(R.id.item_chatting_content_textview);

        holder.translatedContentTextView = (TextView) view
                .findViewById(R.id.item_chatting_trans_content_textview);

        holder.lengthTextView = (TextView) view
                .findViewById(R.id.item_chatting_length_textview);
        holder.playProcessBar = (ProgressBar) view
                .findViewById(R.id.item_chatting_voice_play_process_bar);
        holder.timeTextView = (TextView) view
                .findViewById(R.id.item_chatting_time_textview);
        holder.sendErrorView = (TextView) view
                .findViewById(R.id.item_chatting_error_textview);

        holder.autoTranslationLayout = view
                .findViewById(R.id.item_chatting_auto_translation_textview);
        view.setTag(holder);
        return view;
    }
	static class MyHandler extends Handler {
		private final ImageView mVoiceImageView;
		private final ProgressBar mPlayProcessBar;

		MyHandler(ImageView voiceImageView, ProgressBar playProcessBar) {
			mVoiceImageView = voiceImageView;
			mPlayProcessBar = playProcessBar;
		}

		@Override
		public void handleMessage(android.os.Message msg) {
			String returnUrl = msg.getData().getString("url");
			if (!Utils.isEmpty(returnUrl)) {
				playVoice(returnUrl, mVoiceImageView, mPlayProcessBar);
			} else {
				Toast.makeText(App.mContext, R.string.file_not_found,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private static void playVoice(String url, final ImageView voiceImageView,
	                              final ProgressBar playProcessBar) {
		if (BuildConfig.DEBUG)
			Log.w(TAG, "url:" + url);
		try {
			if (App.mPlayer != null) {
				App.mPlayer.release();
				App.mPlayer = null;
			}
			App.mPlayer = new MediaPlayer();
			App.mPlayer.setDataSource(url);
			App.mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			App.mPlayer.prepare();
			App.mPlayer.start();

			voiceImageView.setVisibility(View.GONE);
			playProcessBar.setVisibility(View.VISIBLE);

			App.mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mPlayer) {
					voiceImageView.setVisibility(View.VISIBLE);
					playProcessBar.setVisibility(View.GONE);
					App.mPlayer.stop();
					App.mPlayer.release();
				}
			});
		} catch (Exception e) {
			voiceImageView.setVisibility(View.VISIBLE);
			playProcessBar.setVisibility(View.GONE);
			Utils.sendClientException(e, url);
			if (BuildConfig.DEBUG)
				Log.e(TAG, url, e);
		}
	};

	private ArrayList<String> chatPhotoList;

	private GenericTask mRequestVerifyTask;
	public int CREATE_DATE_INDEX;

	public LayoutInflater mInflater;
	private GenericTask mAcceptTranslateTask;

	private GenericTask retrieveMessageTask;

	public int voice_playing_width;

	private Message mMessage;// upload 图片时使用

	public TranslateClient mClient;

	private final TaskListener mRequestVerifyListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				Toast.makeText(getContext(), R.string.send_request_success,
						Toast.LENGTH_SHORT).show();

			} else {
				String msg = task.getMsg();

				if (!Utils.isEmpty(msg)) {
					Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT)
							.show();
				}
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			Toast.makeText(getContext(), R.string.message_verify_processing,
					Toast.LENGTH_SHORT).show();
		}

	};

	private final TaskListener mAcceptTranslateListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			TranslateAcceptTask fsTask = (TranslateAcceptTask) task;
			if (result == TaskResult.OK) {
				boolean existTranslatedMessage = fsTask.getIsNeedRetrieveUser();
				onAcceptTranslateSuccess(existTranslatedMessage);

				Toast.makeText(getContext(), R.string.send_request_success,
						Toast.LENGTH_SHORT).show();

			} else {
				String msg = fsTask.getMsg();
				if (!Utils.isEmpty(msg)) {
					Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT)
							.show();
				}
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			Toast.makeText(getContext(), R.string.message_sending,
					Toast.LENGTH_SHORT).show();
		}

	};
	private Context context;
	public ContentResolver mContentResolver;

	private final TaskListener mRequestTranslateListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			XmppRequestTranslateTask fsTask = (XmppRequestTranslateTask) task;
			if (result == TaskResult.OK) {
				Message message = fsTask.getMessage();
				setMessageID(fsTask.getChat().getPid(), message.getMessageid(), message.getTo_content());
				Log.d(TAG, "Request translate Success");
			} else {
				String msg = fsTask.getMsg();
				Log.d(TAG, "Request translate fail:" + msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {

		}

	};
	private final TaskListener retrieveMessageByIdTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveMessageTask retrieveMessageTask = (RetrieveMessageTask) task;
			if (result == TaskResult.OK) {
				Message message = retrieveMessageTask.getMessage();

				CommonUtilities.messageNotification(App.mContext, message);
			} else {
				String msg = retrieveMessageTask.getMsg();
				if (BuildConfig.DEBUG)
					Log.d(TAG, "retrieveMessageTask:" + msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
		}

	};

	private final TaskListener mUploadTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
//			FileUploadTask photoTask = (FileUploadTask) task;
//			if (result == TaskResult.OK) {
//				Message message = mMessage;
//				message.setFile_path(photoTask.getFileInfo().fileName);
//				AbstractChatActivity.doRequestTranslate(message,
//						mRequestTranslateListener);
//			} else if (result == TaskResult.FAILED) {
//				String msg = photoTask.getMsg();
//				Message message = mMessage;
//				onRequestTranslateFailure(message, msg);
//			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
		}
	};

	public int mWidth;



	protected void bindAutoTranslationClick(final Chat chat,
	                                        final View autoTranslationLayout, final Context mContext) {
		autoTranslationLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Map<String, String> params = new HashMap<>();
				params.put("message_id", String.valueOf(chat.getMessageId()));
				params = HttpConnection.genParams(params);
				String mUrl = App.getHttpServer().genRequestURL(
						"help/auto_translation_help.php", params);

				Intent intent = new Intent(mContext, WebViewActivity.class);
				intent.putExtra(WebViewActivity.EXTRA_WEBVIEW_URL, mUrl);
				intent.putExtra(WebViewActivity.EXTRA_WEBVIEW_TITLE, mContext
						.getResources().getString(R.string.help));
				mContext.startActivity(intent);
			}
		});
	}

	protected void bindDateTimeView(Cursor cursor, final TextView dateTextView) {
		long pubDate = cursor.getLong(CREATE_DATE_INDEX);
		long prevPubDate;
		if (cursor.moveToPrevious()) {
			prevPubDate = cursor.getLong(CREATE_DATE_INDEX);
			cursor.moveToNext();
		} else {
			prevPubDate = 0;
		}

		dateTextView.setVisibility(View.GONE);
		if (prevPubDate > 0 && pubDate > 0) {
			boolean isDiff = (pubDate - prevPubDate) > DateCommonUtils.CHAT_TIME_SPAN_SIZE;
			if (isDiff) {
				dateTextView.setText(DateCommonUtils.formatDateToString(pubDate, true, false));
				dateTextView.setVisibility(View.VISIBLE);
			}
		} else if (prevPubDate == 0 && pubDate > 0) {
			dateTextView.setText(DateCommonUtils.formatDateToString(
					pubDate, true, true));
			dateTextView.setVisibility(View.VISIBLE);
		}
	}

	protected void bindFromClickEvent(final Chat chat, View fromTextView) {
		if (fromTextView == null)
			return;

		fromTextView.setOnLongClickListener(null);
		fromTextView
				.setOnLongClickListener(getFromContentLongClickListener(chat));
	}

	protected void bindFromContentView(final Chat chat,
	                                   final TextView rightFromContentTextView) {
		if (rightFromContentTextView == null)
			return;

		rightFromContentTextView.setVisibility(View.VISIBLE);
		rightFromContentTextView.setMovementMethod(LinkMovementMethod
				.getInstance());
		String unicode = EmojiParser.getInstance(getContext()).parseEmoji(
				chat.getContent());
		SpannableString spannableString = ParseEmojiMsgUtil
				.getExpressionString(getContext(), unicode);
		if (spannableString != null) {
			rightFromContentTextView.setText(spannableString);
		} else {
			rightFromContentTextView.setText(chat.getContent());
		}
	}
	protected void bindFromPhotoView(final Chat chat,
	                                 ImageView photoImageView, ProgressBar progressBar) {
		if (photoImageView == null)
			return;

		photoImageView.setVisibility(View.VISIBLE);
		final String file_path = chat.getFilePath();
		String fileName = file_path.substring(file_path.lastIndexOf('/') + 1);
		if (progressBar != null)
			progressBar.setVisibility(View.GONE);
		if (!Utils.isEmpty(fileName)) {
			ImageManager.imageLoader.displayImage(App
							.readServerAppInfo().getServerMiddle(fileName),
					photoImageView, ImageManager.getOptionsLandscape());
			photoImageView.setTag(fileName);
			setItemSize(photoImageView);
		} else {
			photoImageView.setImageBitmap(ImageManager
					.getDefaultLandscape(context));
			photoImageView.setTag(null);

		}

		photoImageView.setPadding(2, 2, 2, 2);
	}

	protected void bindFromVoiceView(final Chat chat,
	                                 final ImageView rightVoiceImageView, TextView rightLengthTextView,
	                                 final ProgressBar rightPlayProcessBar) {

		if (rightVoiceImageView == null || rightLengthTextView == null
				|| rightPlayProcessBar == null)
			return;

		rightVoiceImageView.setVisibility(View.VISIBLE);
		rightVoiceImageView.setTag(chat.getFilePath());
		rightLengthTextView.setVisibility(View.VISIBLE);
		rightLengthTextView.setText(chat.getFromContentLength() + "'");
		// 下载语音
		File voiceFolder = Utils.getVoiceFolder(context);
		File mVoiceFile = new File(voiceFolder, chat.getFilePath());
		File mDownVoiceFile = new File(voiceFolder, chat.getFilePath()
				+ AppPreferences.VOICE_SURFIX);
		if (!mVoiceFile.exists() && !mDownVoiceFile.exists()) {
			if (!Utils.isEmpty(chat.getFilePath())) {
				Utils.uploadVoiceFile(context, chat.getFilePath(), null);
			}
		}
	}

	protected void bindImageClickEvent(final Chat chat,
	                                   View photoImageView) {

		if (photoImageView == null)
			return;

		photoImageView.setOnClickListener(null);
		if (AppPreferences.MESSAGE_TYPE_NAME_PHOTO.equals(chat.getType())) {
			photoImageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (chat.getStatus() != AppPreferences.MESSAGE_STATUS_SEND_FAILED) {
						gotoImageViewActivity(chat.getFilePath());
					}
				}
			});
		}
	}

	protected void bindLayoutClickEvent(final Chat chat, View layoutView) {
		if (layoutView == null)
			return;

		layoutView.setOnLongClickListener(null);
		if (!isMine(chat)) {
			layoutView
					.setOnLongClickListener(getToContentLongClickListener(chat));
		} else {
			layoutView
					.setOnLongClickListener(getFromContentLongClickListener(chat));
		}
	}

	protected void bindProfileThumb(User user, ImageView userThumb,
	                                ImageView smsImageView, ImageView langImageView) {

		Utils.setUserPicImage(userThumb, user.getPic_url());

		if (smsImageView != null) {
			// sms user
			if (user.active == 1) {
				smsImageView.setVisibility(View.INVISIBLE);
			} else {
				smsImageView.setVisibility(View.VISIBLE);
			}
		}

		if (langImageView != null) {
			langImageView.setImageResource(Utils.getLanguageFlag(user.lang));
		}
	}

	protected void bindSendErrorView(final boolean error,
	                                 final TextView sendErrorView, final Chat chat) {
		if (error) {
			sendErrorView.setVisibility(View.VISIBLE);
		} else {
			sendErrorView.setVisibility(View.GONE);
		}
		sendErrorView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//TODO
//				doReRequestTranslate(message.getId());
			}
		});
	}

	protected void bindToClickEvent(final Chat chat, View toTextView) {
		if (toTextView != null) {
			toTextView.setOnLongClickListener(null);
			toTextView
					.setOnLongClickListener(getToContentLongClickListener(chat));
		}
	}

	protected void bindToView(final Chat chat,
	                          TextView translatedContentTextView, View autoTranslationLayout) {

		if (translatedContentTextView == null || autoTranslationLayout == null)
			return;

		translatedContentTextView.setVisibility(View.GONE);
		autoTranslationLayout.setVisibility(View.GONE);

		final String toContent = chat.getTo_content();
		if (AppPreferences.MESSAGE_TYPE_NAME_PHOTO
				.equals(chat.getType()) || Utils.isEmpty(toContent)) {
			//
		} else {

			translatedContentTextView.setVisibility(View.VISIBLE);
//            if (chat.getAuto_translate() == AppPreferences.AUTO_TRANSLATE_MESSSAGE) {
//                autoTranslationLayout.setVisibility(View.VISIBLE);
//
//                bindAutoTranslationClick(message, autoTranslationLayout,
//                        this.getContext());
//            }
			String unicode = EmojiParser.getInstance(getContext()).parseEmoji(
					chat.getTo_content());
			SpannableString spannableString = ParseEmojiMsgUtil
					.getExpressionString(getContext(), unicode);
			if (spannableString != null) {
				translatedContentTextView.setText(spannableString);
			} else {
				translatedContentTextView.setText(chat.getTo_content());
			}
		}
	}

	protected void bindUserThumbClickEvent(View userThumb, final User user) {

		if (userThumb == null)
			return;

		userThumb.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getContext(),
						FriendProfileActivity.class);
				intent.putExtra(ProfileActivity.EXTRA_USER, user);
				getContext().startActivity(intent);
			}
		});
	}

	protected void bindVoiceClickEvent(final Chat chat,
	                                   final ImageView voiceImageView, final ProgressBar playProcessBar,
	                                   final View bubbleLayout) {

		if (voiceImageView == null || playProcessBar == null)
			return;

		bubbleLayout.setOnClickListener(null);
		if (AppPreferences.MESSAGE_TYPE_NAME_VOICE.equals(chat.getType())) {
			bubbleLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// if (message.getMessage_status() !=
					// AppPreferences.MESSAGE_STATUS_SEND_FAILED) {
					String url = "";
					String file_path = chat.getFilePath();
					File voiceFolder = Utils.getVoiceFolder(context);
					File mVoiceFile = new File(voiceFolder, file_path);
					File mDownVoiceFile = new File(voiceFolder,
							file_path + AppPreferences.VOICE_SURFIX);
					if (mVoiceFile.exists()) {
						url = file_path;
						playVoice(voiceFolder + "/" + url, voiceImageView,
								playProcessBar);
					} else if (mDownVoiceFile.exists()) {
						url = file_path + AppPreferences.VOICE_SURFIX;
						playVoice(voiceFolder + "/" + url, voiceImageView,
								playProcessBar);
					} else {
						if (!Utils.isEmpty(file_path)) {
							final MyHandler myHandler = new MyHandler(
									voiceImageView, playProcessBar);
							Utils.uploadVoiceFile(context, file_path,
									myHandler);
						} else {
							Toast.makeText(context,
									R.string.play_voice_failure,
									Toast.LENGTH_SHORT).show();
						}
					}
				}
				// }
			});
		}
	}

	// 原文文本菜单
	void createFromContentPopMenus(final Chat chat, View v,
	                               final String content) {
		List<String> menuList = new ArrayList<>();
		if (!AppPreferences.MESSAGE_TYPE_NAME_PHOTO.equals(chat
				.getType())
				&& !AppPreferences.MESSAGE_TYPE_NAME_VOICE.equals(chat
				.getType())) {

			menuList.add(getContext().getString(R.string.message_action_copy));
			menuList.add(getContext().getString(R.string.message_action_share));
			menuList.add(getContext().getString(
					R.string.message_action_fullscreen));
			if (Utils.checkTts(chat.getFromLang())
					|| Utils.checkTts(chat.getToLang())) {
				menuList.add(getContext()
						.getString(R.string.message_action_tts));
			}
			// 正在请求中的可以请求自动翻译
//            if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_REQUEST_TRANS)
//            {
//                menuList.add(mContext.getString(
//                        R.string.message_action_auto_translation));
//            }
		}
		menuList.add(getContext().getString(R.string.delete_message));
		// 请求失败可以再请求一次
//        if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_SEND_FAILED) {
//            menuList.add(getContext().getString(
//                    R.string.message_action_request_translate_again));
//        } else if (message.getTo_userid() == App.readUser().getId()
//                && message.getMessage_status() == AppPreferences.MESSAGE_STATUS_NO_TRANSLATE
//                && (!message.getFrom_lang().equals(message.getTo_lang())
//                && !AppPreferences.MESSAGE_TYPE_NAME_PHOTO
//                .equals(message.file_type) && Utils
//                .isEmpty(message.getTo_content()))) {
//            // 只有接收者才能点击接受翻译；
//            // 缺钱的话，发送者也不能点击接受翻译。
//            menuList.add(getContext().getString(
//                    R.string.message_action_accept_translate));
//        }
//        if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_REQUEST_TRANS
//                || message.getMessage_status() == AppPreferences.MESSAGE_STATUS_TRANSLATING
//                || message.getMessage_status() == AppPreferences.MESSAGE_STATUS_ACCEPT_TRANSLATE
//                || message.getMessage_status() == AppPreferences.MESSAGE_STATUS_ACCEPT_TRANSLATING) {
//            menuList.add(getContext().getString(
//                    R.string.message_action_retrieve_message));
//        }
//        // 翻译过的，并且未验证，出钱的人可以点击验证
//        if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_TRANSLATED
//                && message.getVerify_status() < AppPreferences.VERIFY_STATUS_REQUEST
//                && ((App.readUser().getId() == message.getUserid() && message
//                .getFee() > 0) || (App.readUser().getId() == message
//                .getTo_userid() && message.getTo_user_fee() > 0))) {
//            menuList.add(getContext().getString(R.string.message_action_verify));
//        }
		menuOnClickListener(menuList, chat, content);
	}

	// 译文文本菜单
	void createToContentPopMenus(final Chat chat, View v,
	                             final String content) {
		List<String> menuList = new ArrayList<>();
		menuList.add(context.getString(R.string.message_action_copy));
		menuList.add(context.getString(R.string.message_action_share));
		menuList.add(context.getString(R.string.message_action_fullscreen));
		if (Utils.checkTts(chat.getFromLang())
				|| Utils.checkTts(chat.getToLang())) {
			menuList.add(context.getString(R.string.message_action_tts));
		}
		menuList.add(context.getString(
				R.string.message_action_auto_translation));
		menuList.add(context.getString(
				R.string.message_action_accept_translate));
		menuList.add(context.getString(R.string.delete_message));
		// 翻译过的，并且未验证，出钱的人可以点击验证
//        if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_TRANSLATED
//                && message.getVerify_status() < AppPreferences.VERIFY_STATUS_REQUEST
//                && ((App.readUser().getId() == message.getUserid() && message
//                .getFee() > 0) || (App.readUser().getId() == message
//                .getTo_userid() && message.getTo_user_fee() > 0))) {
//            menuList.add(mContext.getString(R.string.message_action_verify));
//        }

		menuOnClickListener(menuList, chat, content);
	}

	private void deleteMessageByMsgId(final Long messageId) {
		App.messageDAO.deleteByMessageId(messageId);
		Toast.makeText(getContext(), R.string.messages_delete_success,
				Toast.LENGTH_SHORT).show();
		getCursor().requery();
		return;
	}

	private void doAcceptTranslate(final Long id) {
		Toast.makeText(
				getContext(),
				getContext()
						.getString(R.string.message_action_accept_translate),
				Toast.LENGTH_SHORT).show();

		if (mAcceptTranslateTask != null
				&& mAcceptTranslateTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		mAcceptTranslateTask = new TranslateAcceptTask(id);
		mAcceptTranslateTask.setListener(mAcceptTranslateListener);
		mAcceptTranslateTask.execute();

	}

	private void doCopy(Chat chat) {
		android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getContext()
				.getSystemService(Context.CLIPBOARD_SERVICE);
		clipboard.setText(chat.getContent());
		Toast.makeText(getContext(),
				getContext().getString(R.string.already_copy_to_clipboard),
				Toast.LENGTH_SHORT).show();
	}

	private void doFullscreen(Chat chat) {
		Intent intent = new Intent(getContext(), FullScreenActivity.class);
		intent.putExtra(FullScreenActivity.EXTRA_MESSAGE, chat.getContent());
		getContext().startActivity(intent);
	}

//	protected void doReRequestTranslate(Long localId) {
//		Toast.makeText(getContext(),
//				getContext().getString(R.string.status_sending),
//				Toast.LENGTH_SHORT).show();
//
//		Message message = App.messageDAO.fetchMessage(localId);
//		message.setMessage_status(AppPreferences.MESSAGE_STATUS_BEFORE_SEND);
//		message.setStatus_text(getContext().getString(R.string.data_sending));
//		App.messageDAO.mergeMessage(message);
//		getCursor().requery();
//
//		if (AppPreferences.MESSAGE_TYPE_NAME_PHOTO.equals(message
//				.getFile_type())) {// js 先上传图片然后发送请求
//			mMessage = message;
//			AbstractChatActivity.doUploadFile(message, mUploadTaskListener);
//		} else {
//			AbstractChatActivity.doRequestTranslate(message,
//					mRequestTranslateListener);
//		}
//	}

	private void doShare(Chat chat) {
		// create the send intent
		Intent shareIntent = new Intent(Intent.ACTION_SEND);

		// set the type
		shareIntent.setType("text/plain");

		// add a subject
		shareIntent.putExtra(Intent.EXTRA_SUBJECT, "");

		// add the message
		shareIntent.putExtra(Intent.EXTRA_TEXT, chat.getContent());

		// start the chooser for sharing
		getContext().startActivity(
				Intent.createChooser(shareIntent,
						getContext().getString(R.string.app_name)));
	}

	private void doTTS(Chat chat) {
		if (!Utils.tts(getContext(), chat.getFromLang(), chat.getToLang(), chat.getContent())) {
			Toast.makeText(getContext(),
					getContext().getString(R.string.tts_no_supported_language),
					Toast.LENGTH_SHORT).show();
		}
	}

	private void doDelete(Chat chat) {
		ContentValues cv = new ContentValues();

		mContentResolver.delete(ChatProvider.CONTENT_URI, TableContent.ChatTable.Columns.ID
				+ " = ?  " , new String[]{String.valueOf(chat.getId())});
	}

	private void doVerify(final Long id) {
		DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				if (mRequestVerifyTask != null
						&& mRequestVerifyTask.getStatus() == GenericTask.Status.RUNNING) {
					return;
				}

				mRequestVerifyTask = new RequestVerifyTask(id);
				mRequestVerifyTask.setListener(mRequestVerifyListener);
				mRequestVerifyTask.execute();
			}
		};
		DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		};
		Utils.AlertDialog(context, positiveListener, negativeListener,
				context.getString(R.string.message_verify),
				context.getString(R.string.message_verify_cost));

	}

	public Context getContext() {
		return context;
	}

	private View.OnLongClickListener getFromContentLongClickListener(
			final Chat chat) {
		View.OnLongClickListener listener = new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				final String content = getMessageMenuFromContent(chat);
//                if (!Utils.isEmpty(content))
				{
					// long click
					createFromContentPopMenus(chat, v, content);
				}
				return true;
			}
		};
		return listener;
	}

	private String getMessageMenuFromContent(final Chat chat) {
		String content;
		if (AppPreferences.MESSAGE_TYPE_NAME_PHOTO.equals(chat
				.getType())) {
			content = getContext().getString(R.string.notification_picture);
		} else if (AppPreferences.MESSAGE_TYPE_NAME_VOICE.equals(chat
				.getType())) {
			content = getContext().getString(R.string.notification_voice);
		} else {
			content = chat.getContent();
		}

		return content;
	}

	private String getMessageMenuToContent(final Chat chat) {
		String content = chat.getTo_content();
		return content;
	}

	String getMessageStatusText(final Message message) {
		if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_GIVEUP) {
			// use server definition
		} else if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_REQUEST_TRANS
				|| message.getMessage_status() == AppPreferences.MESSAGE_STATUS_ACCEPT_TRANSLATE) {
			message.setStatus_text(getContext().getString(
					R.string.message_status_text_requesting));
		} else if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_TRANSLATING
				|| message.getMessage_status() == AppPreferences.MESSAGE_STATUS_ACCEPT_TRANSLATING) {
			message.setStatus_text(getContext().getString(
					R.string.message_status_text_translating));
		} else if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_NO_TRANSLATE
				&& !message.getFrom_lang().equals(message.getTo_lang())
				&& !AppPreferences.MESSAGE_TYPE_NAME_PHOTO.equals(message
				.getFile_type())
				&& message.getTo_userid() == App.readUser().getId()) {
			message.setStatus_text(getContext().getString(
					R.string.message_status_text_accept_translate));
		} else {
			message.setStatus_text("");
		}

		return message.getStatus_text();
	}

	private View.OnLongClickListener getToContentLongClickListener(
			final Chat chat) {
		View.OnLongClickListener listener = new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				final String content = getMessageMenuToContent(chat);
//                if (!Utils.isEmpty(content))
				{
					// long click
					createToContentPopMenus(chat, v, content);
				}
				return true;
			}
		};
		return listener;
	}

	private void gotoImageViewActivity(String file_path) {
		setChatPhotoList(getCursor());
		int position = chatPhotoList.indexOf(App.readServerAppInfo()
				.getServerOriginal(file_path));
		Intent intent = new Intent(getContext(), ImageViewActivity.class);
		intent.putExtra(ImageViewActivity.EXTRA_POSITION, position);
		intent.putExtra(ImageViewActivity.EXTRA_IMAGE_URLS, chatPhotoList);
		getContext().startActivity(intent);
	}

	private void menuOnClickListener(List<String> menuList,
	                                 final Chat chat, final String content) {
		final String[] menus = new String[menuList.size()];
		menuList.toArray(menus);
		CustomDialog alertDialog = new CustomDialog(context).setTitle(
				context.getString(R.string.tips)).setItems(menus,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String selectedItem = menus[which];
						if (context
								.getString(R.string.message_action_copy)
								.equals(selectedItem)) {
							doCopy(chat);
						} else if (context.getString(
								R.string.message_action_share).equals(
								selectedItem)) {
							doShare(chat);
						} else if (context.getString(
								R.string.message_action_fullscreen).equals(
								selectedItem)) {
							doFullscreen(chat);
						} else if (context.getString(
								R.string.message_action_tts).equals(
								selectedItem)) {
							doTTS(chat);// from_lang优先
						} else if (context.getString(
								R.string.delete_message).equals(selectedItem)) {
							doDelete(chat);
						} else if (context.getString(
								R.string.message_action_request_translate_again)
								.equals(selectedItem)) {
//                            doReRequestTranslate(message.getId());
						} else if (context.getString(
								R.string.message_action_auto_translation)
								.equals(selectedItem)) {
//                            doAcceptTranslate(message.messageid);
							doBaiduTranslate(chat);
						} else if (context.getString(
								R.string.message_action_accept_translate)
								.equals(selectedItem)) {
							doTTTalkTranslate(chat);
						} else if (context.getString(
								R.string.message_action_retrieve_message)
								.equals(selectedItem)) {
//                            retrieveMessageById(message.messageid);
						} else if (context.getString(
								R.string.message_action_verify).equals(
								selectedItem)) {
//                            doVerify(message.messageid);
						}
					}
				});
		alertDialog.setTitle(content).show();
	}

	private void onAcceptTranslateSuccess(boolean existTranslatedMessage) {
		getCursor().requery();

		// if (existTranslatedMessage) {
		// doRetrieveUser(App.readUser().getId());//
		// 回到Setting画面，能够立刻看到balance变化。
		// }
	}

	private void onRequestTranslateFailure(Message message, String msg) {
		// save message
		message.setMessage_status(AppPreferences.MESSAGE_STATUS_SEND_FAILED);
		message.setStatus_text(getContext().getString(
				R.string.message_action_click_resend));
		App.messageDAO.mergeMessage(message);

		getCursor().requery();

		if (!Utils.isEmpty(msg)) {
			Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
		}
	}

	private void onRequestTranslateSuccess() {
		getCursor().requery();
		// AbstractChatActivity.doRefresh();
		CommonUtilities.broadcastMessage(context, null);
		if (BuildConfig.DEBUG)
			Log.d(TAG, "send Success");

	}

	private void retrieveMessageById(final Long messageId) {
		if (retrieveMessageTask != null
				&& retrieveMessageTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		retrieveMessageTask = new RetrieveMessageTask(messageId);
		retrieveMessageTask.setListener(retrieveMessageByIdTaskListener);
		retrieveMessageTask.execute();

	}

	private void setChatPhotoList(Cursor chatsCursor) {
		chatPhotoList = new ArrayList<>();
		for (chatsCursor.moveToFirst(); !chatsCursor.isAfterLast(); chatsCursor
				.moveToNext()) {
			Chat chat = TableContent.ChatTable.parseCursor(chatsCursor);
			if (AppPreferences.MESSAGE_TYPE_NAME_PHOTO
					.equals(chat.getType())) {
				final String file_path = chat.getFilePath();
				String fileName = file_path.substring(file_path
						.lastIndexOf('/') + 1);
				chatPhotoList.add(App.readServerAppInfo().getServerOriginal(
						fileName));
			}
		}
	}

	public void setContext(Context context) {
		this.context = context;
	}

	private void setItemSize(View itemView) {
		RelativeLayout.LayoutParams imageParams = (RelativeLayout.LayoutParams) itemView.getLayoutParams();
		imageParams.width = mWidth / 4;
		imageParams.height = mWidth / 4;
		itemView.setLayoutParams(imageParams);
	}

	public void doBaiduTranslate(final Chat chat) {

		if (TextUtils.isEmpty(chat.getContent()))
			return;

		if (mClient != null){
			mClient.translate(chat.getContent(), Utils.convert2BaiduLang(chat.getFromLang()), Utils.convert2BaiduLang(chat.getToLang()), new ITransResultCallback() {

				@Override
				public void onResult(TransResult result) {// 翻译结果回调
					if (result == null) {
						Log.d(TAG, "Trans Result is null");

					} else {
						Log.d(TAG, result.toJSONString());

						String msg;
						if (result.error_code == 0) {// 没错
							msg = result.trans_result;
						} else {
							msg = result.error_msg;
						}
						Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
						setToContent(chat.getId(), result.trans_result);
					}
				}
			});
		}

	}

	private void setToContent(int chatID, String message) {
		ContentValues cv = new ContentValues();
		cv.put(TableContent.ChatTable.Columns.TO_MESSAGE, message);

		mContentResolver.update(ChatProvider.CONTENT_URI, cv, TableContent.ChatTable.Columns.ID
				+ " = ?  " , new String[]{String.valueOf(chatID)});
	}

	private void doTTTalkTranslate(Chat chat) {
		XmppRequestTranslateTask mRequestTranslateTask = new XmppRequestTranslateTask(chat);
		mRequestTranslateTask.setListener(mRequestTranslateListener);
		mRequestTranslateTask.execute();
	}

	public void setMessageID(String packetID, long messageID, String to_content) {
		ContentValues cv = new ContentValues();
		cv.put(TableContent.ChatTable.Columns.MESSAGE_ID, messageID);
		if (to_content == null || to_content.length() == 0)
			cv.put(TableContent.ChatTable.Columns.TO_MESSAGE, context.getString(R.string.message_status_text_translating));
		else
			cv.put(TableContent.ChatTable.Columns.TO_MESSAGE, to_content);

		mContentResolver.update(ChatProvider.CONTENT_URI, cv, TableContent.ChatTable.Columns.PACKET_ID
				+ " = ? AND " + TableContent.ChatTable.Columns.FROM_JID + " = ?", new String[]{packetID, App.readUser().getOF_JabberID()});
	}

	public boolean isMine(Chat chat){
		return (chat.getFromJid() == App.readUser().getOF_JabberID());
	}

}
