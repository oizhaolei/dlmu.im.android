/**
 *
 */
package com.ruptech.chinatalk.widget;

import java.io.File;
import java.util.Map;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.Utils;

public class VerifyMessageListArrayAdapter extends ArrayAdapter<Map<String, String>> {

	static class ViewHolder {
		@InjectView(R.id.listitem_message_id)
		TextView listitem_message_id;
		@InjectView(R.id.listitem_message_create_date)
		TextView listitem_message_create_date;

		@InjectView(R.id.listitem_message_from_content)
		TextView listitem_message_from_content;
		@InjectView(R.id.listitem_message_from_content_voice)
		View listitem_message_from_content_voice;

		@InjectView(R.id.listitem_message_to_lang)
		TextView listitem_message_to_lang;

		@InjectView(R.id.listitem_message_to_content)
		TextView listitem_message_to_content;

		@InjectView(R.id.listitem_message_verify_status)
		TextView listitem_message_verify_status;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}

	private static final int mResource = R.layout.item_verify_message; // xml布局文件

	private final String TAG = Utils.CATEGORY
			+ VerifyMessageListArrayAdapter.class.getSimpleName();

	private final LayoutInflater mInflater;

	public VerifyMessageListArrayAdapter(Context context) {
		super(context, mResource);

		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		final Map<String, String> message = getItem(position);
		final String to_content = message.get("to_content");

		View view;
		final ViewHolder holder;
		if (convertView == null) {
			view = mInflater.inflate(mResource, parent, false);

			holder = new ViewHolder(view);

			holder.listitem_message_from_content_voice
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							File mVoiceFile = new File(Utils
									.getVoiceFolder(getContext()), message
									.get("file_path"));
							String url;
							if (mVoiceFile.exists()) {
								url = mVoiceFile.getAbsolutePath();
							} else {
								url = App.readServerAppInfo().getAppServerUrl()
										+ "../" + message.get("file_path");
							}
							playVoice(url);
						}

					});

			final int verify_status = Integer.parseInt(message.get("verify_status"));
			if (verify_status == 0) {
			} else {
				holder.listitem_message_verify_status.setVisibility(View.VISIBLE);

				if (verify_status == 1) {// verify
					holder.listitem_message_verify_status.setText(getContext().getString(
							R.string.message_verify_processing));
				} else if (verify_status == 2) {// mistake
					holder.listitem_message_verify_status.setText(getContext().getString(
							R.string.message_verify_mistake));
				} else if (verify_status == 3) {// right
					holder.listitem_message_verify_status
							.setText(getContext().getString(R.string.message_verify_right));
				} else {
				}
			}

			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();

		}

		// exapand
		// expand
		// holder.listitem_message_id.setVisibility(View.VISIBLE);
		holder.listitem_message_create_date.setVisibility(View.VISIBLE);
		holder.listitem_message_id.setText(message.get("id"));
		String createDateStr = message.get("create_date");
		String messageUtcDatetimeStr = DateCommonUtils.convUtcDateString(
				createDateStr, DateCommonUtils.DF_yyyyMMddHHmmss);
		holder.listitem_message_create_date.setText(messageUtcDatetimeStr);
		if (Utils.isEmpty(message.get("from_content"))) {
			holder.listitem_message_from_content_voice.setTag(message.get("file_path"));

			holder.listitem_message_from_content_voice.setVisibility(View.VISIBLE);
			holder.listitem_message_from_content.setVisibility(View.GONE);
		} else {
			holder.listitem_message_from_content_voice.setTag(null);
			holder.listitem_message_from_content.setText(message.get("from_content"));
			holder.listitem_message_from_content_voice.setVisibility(View.GONE);
			holder.listitem_message_from_content.setVisibility(View.VISIBLE);
		}

		if (Utils.isEmpty(to_content)) {
			holder.listitem_message_to_lang.setVisibility(View.GONE);
			holder.listitem_message_to_content.setVisibility(View.GONE);
			holder.listitem_message_to_content.setText("");
		} else {
			holder.listitem_message_to_lang.setVisibility(View.VISIBLE);
			holder.listitem_message_to_content.setVisibility(View.VISIBLE);
			holder.listitem_message_to_content.setText(to_content);
		}
		return view;
	}

	void playVoice(String url) {
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

			App.mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer arg0) {
					App.mPlayer.release();
				}
			});
		} catch (Exception e) {
			if (BuildConfig.DEBUG)
				Log.e(TAG, url, e);
			Utils.sendClientException(e, url);
		}
	}

}