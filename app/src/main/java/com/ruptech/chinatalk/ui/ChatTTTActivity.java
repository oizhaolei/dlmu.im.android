package com.ruptech.chinatalk.ui;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.text.InputFilter;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.adapter.TTTChatAdapter;
import com.ruptech.chinatalk.db.ChatProvider;
import com.ruptech.chinatalk.sqlite.TableContent;
import com.ruptech.chinatalk.ui.story.TextShareActivity;
import com.ruptech.chinatalk.ui.user.MyWalletActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.EditTextWithFace;
import com.ruptech.chinatalk.widget.LangSpinnerAdapter;
import com.ruptech.chinatalk.widget.RecordButton;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 *
 * @author
 */
public class ChatTTTActivity extends AbstractChatActivity {

	static final String TAG = Utils.CATEGORY
			+ ChatTTTActivity.class.getSimpleName();

	/**
	 * TTT根据新的lang列表，重新定位之前使用过的lang位置
	 *
	 * @param lang
	 * @param langArray
	 * @return
	 */
	public static int getLangArrayPosition(String lang, String[] langArray) {
		int pos = 0;
		for (int i = 0; i < langArray.length; i++) {
			if (lang.equalsIgnoreCase(langArray[i])) {
				pos = i;
				break;
			}
		}
		return pos;
	}

	@InjectView(R.id.activity_ttt_chat_voice_button)
	RecordButton mVoiceRecordButton;
	@InjectView(R.id.activity_ttt_chat_message_edittext)
	EditTextWithFace mMessageEditText;
	@InjectView(R.id.activity_ttt_chat_message_listview)
	ListView mMessageListView;

	@InjectView(R.id.activity_ttt_chat_btn_send)
	Button mSendButton;

	@InjectView(R.id.activity_ttt_chat_message_type_button)
	ImageView mMessageTypeButton;

	@InjectView(R.id.activity_ttt_chat_message_type_keyboard_button)
	View keyboardTypeButton;

	private Cursor chatsCursor;

	private String fromLang;
	@InjectView(R.id.btn_swap)
	ImageView mBtnSwap;

	private String[] mLang1Array;
	@InjectView(R.id.spinner_lang1)
	Spinner mLang1Spinner;

	private String[] mLang2Array;

	@InjectView(R.id.spinner_lang2)
	Spinner mLang2Spinner;

	@InjectView(R.id.activity_ttt_chat_select_message_type)
	View mSelectMessageTypeView;

	private String toLang;

	@InjectView(R.id.activity_ttt_chat_message_type_voice_button)
	View voiceTypeButton;

	@InjectView(R.id.item_chatting_balance_remind_textview)
	TextView remindItemFooterTextView;

	@InjectView(R.id.text_layout)
	LinearLayout remindItemFooterLayout;

	@InjectView(R.id.expand_btn)
	ImageView expandBtn;

	@InjectView(R.id.remind_panel)
	View remindItemFooterView;

	private boolean isSelectedLang = false;

	@Override
	void displayFriend() {

	}

	@Override
	public void doRefleshFooterBySelectLang() {
	}

	@Override
	String getFriendLang() {
		return toLang;
	}

	@Override
	public long getFriendUserId() {
		return AppPreferences.TTT_REQUEST_TO_USERID;// 翻译小秘书
	}

	@Override
	View getKeyboardButton() {
		return keyboardTypeButton;
	}

	@Override
	EditText getMessageEditText() {
		return mMessageEditText;
	}

	@Override
	ListView getMessageListView() {
		return mMessageListView;
	}

	@Override
	View getMessageTypeButton() {
		return mMessageTypeButton;
	}

	@Override
	String getMyLang() {
		return fromLang;
	}

	@Override
	View getSendButton() {
		return mSendButton;
	}

	@Override
	View getVoiceButton() {
		return voiceTypeButton;
	}

	@Override
	TextView getVoiceRecordButton() {
		return mVoiceRecordButton;
	}

	void handleSendText(Intent intent) {
		String sharedText = intent.getStringExtra(TextShareActivity.SEND_TEXT);
		if (sharedText != null) {
			switchTextInputMode();
			mMessageEditText.setText(sharedText);
		}
	}

	private void initInputPanel() {
		boolean enableVoice = !googleTranslate;
		voiceTypeButton.setVisibility(enableVoice ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ttt_chat);
		ButterKnife.inject(this);

		getSupportActionBar().setTitle(R.string.translation_secretary);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mWithJabberID = AppPreferences.TTT_OF_USERNAME;
		setupComponents();
		switchTextInputMode();
		handleSendText(getIntent());
		initInputPanel();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			Utils.onBackPressed(this);
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		Utils.onBackPressed(this);
	}

	@OnClick(R.id.activity_ttt_chat_message_type_keyboard_button)
	public void onSelectTextMessageType(View v) {
		this.mSelectMessageTypeView.setVisibility(View.GONE);
		switchTextInputMode();
	}

	@OnClick(R.id.activity_ttt_chat_message_type_voice_button)
	public void onSelectVoiceMessageType(View v) {
		this.mSelectMessageTypeView.setVisibility(View.GONE);
		if (googleTranslate
				|| App.readUser().getBalance() < AppPreferences.MINI_BALANCE) {
			Toast.makeText(this,
					getString(R.string.no_money_cannot_translate_voice),
					Toast.LENGTH_SHORT).show();
		} else {
			switchVoiceMode();
		}
	}

	@OnClick(R.id.activity_ttt_chat_select_message_type_voice_recognize_button)
	public void onSelectVoiceRecognizeTextMessageType(View v) {
		this.mSelectMessageTypeView.setVisibility(View.GONE);
		switchTextInputMode();
		voiceRecognition(fromLang);
	}

	@Override
	protected void remindFooter() {
		if (!isSelectedLang) {
			remindItemFooterView.setVisibility(View.GONE);
			return;
		}
		remindItemFooterView.setVisibility(View.VISIBLE);
		if (googleTranslate) {
			remindItemFooterTextView.setText(R.string.translate_is_free);
		} else if (App.readUser().getBalance() < AppPreferences.MINI_BALANCE) {
			// 1.自己钱不够，显示
			remindItemFooterTextView.setText(Html
					.fromHtml(getString(R.string.your_balance_is_not_enough)));
		} else if (translatorCount == 0) {
			remindItemFooterTextView.setText(R.string.no_translator_online);// 没有翻译者的时候显示不能进行翻译
		} else {
			remindItemFooterTextView.setText(R.string.normal_translation);// 没有翻译者的时候显示不能进行翻译
		}

		remindItemFooterView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (App.readUser().getBalance() < AppPreferences.MINI_BALANCE) {
					Intent intent = new Intent(ChatTTTActivity.this,
							MyWalletActivity.class);
					ChatTTTActivity.this.startActivity(intent);
				}
			}
		});
	}

	@OnClick(R.id.activity_ttt_chat_message_type_button)
	public void selectMessageType(View v) {
		if (mSelectMessageTypeView.getVisibility() == View.GONE) {
			this.mSelectMessageTypeView.setVisibility(View.VISIBLE);

			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(
					mSelectMessageTypeView.getWindowToken(), 0);
		} else if (mSelectMessageTypeView.getVisibility() == View.VISIBLE)
			this.mSelectMessageTypeView.setVisibility(View.GONE);

		if (mSelectMessageTypeView.getVisibility() == View.VISIBLE) {
			initInputPanel();
		}
	}

	@OnClick(R.id.activity_ttt_chat_btn_send)
	public void send_ttt_text(View v) {
		send_text();
	}

	@Override
	public void setupComponents() {
		super.setupComponents();
		int lang1SpinnerSelectPos = 0;

		mSelectMessageTypeView.setVisibility(View.GONE);

		mLang1Array = Utils.getServerTransLang();
		LangSpinnerAdapter lang1Adapter = new LangSpinnerAdapter(this,
				mLang1Array);
		mLang1Spinner.setAdapter(lang1Adapter);
		String lastSelectedLang1 = PrefUtils.getPrefTTTLastSelectedLang(1);
		if (!Utils.isEmpty(lastSelectedLang1)) {
			lang1SpinnerSelectPos = getLangArrayPosition(lastSelectedLang1,
					mLang1Array);
		} else {
			String userLanguage = Utils.getUserLanguage();
			for (int i = 0; i < mLang1Array.length; i++) {
				if (mLang1Array[i].equalsIgnoreCase(userLanguage)) {
					lang1SpinnerSelectPos = i;
					break;
				}
			}
		}
		mLang1Spinner.setSelection(lang1SpinnerSelectPos);
		mLang1Spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				mLang2Array = Utils
						.getAvailableLang2ByLang1(mLang1Array[position]);
				LangSpinnerAdapter lang2Adapter = new LangSpinnerAdapter(
						ChatTTTActivity.this, mLang2Array);
				mLang2Spinner.setAdapter(lang2Adapter);
				String lastSelectedLang2 = PrefUtils
						.getPrefTTTLastSelectedLang(2);
				if (lastSelectedLang2.equals(mLang1Array[position])) {
					lastSelectedLang2 = PrefUtils.getPrefTTTLastSelectedLang(1);
				}
				if (!Utils.isEmpty(lastSelectedLang2)) {
					mLang2Spinner.setSelection(getLangArrayPosition(
							lastSelectedLang2, mLang2Array));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}

		});

		mLang2Spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long id) {
				fromLang = mLang1Array[mLang1Spinner.getSelectedItemPosition()];
				toLang = mLang2Array[position];
				PrefUtils.savePrefTTTLastSelectedLang(1, fromLang);
				PrefUtils.savePrefTTTLastSelectedLang(2, toLang);



				googleTranslate = Utils.isGoogleTranslate(fromLang, toLang);

				translatorCount = Utils.getStoreTranslatorCount(fromLang,
						toLang);
				isSelectedLang = true;// refresh remindFooter
				remindFooter();
				if (googleTranslate) {
					switchTextInputMode();
				}
				initInputPanel();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}

		});

        setChatWindowAdapter();

		mMessageListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}

			@Override
			public void onScrollStateChanged(AbsListView arg0, int arg1) {
				mInputMethodManager.hideSoftInputFromWindow(
						mMessageEditText.getWindowToken(), 0);
				mSelectMessageTypeView.setVisibility(View.GONE);
			}

		});

		mVoiceRecordButton.setOnFinishedRecordListener(voiceRecordListener);

		mMessageEditText.clearImg();
		mMessageEditText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mMessageListView.setSelection(mMessageListView.getCount() - 1);
				mSelectMessageTypeView.setVisibility(View.GONE);
			}

		});

		InputFilter[] filters = { new LengthFilter() };
		mMessageEditText.setFilters(filters);

		mMessageEditText.addTextChangedListener(mTextWatcher);

		mBtnSwap.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String lastSelectedLang2 = PrefUtils
						.getPrefTTTLastSelectedLang(2);
				int position = getLangArrayPosition(lastSelectedLang2,
						mLang1Array);
				mLang1Spinner.setSelection(position);
			}
		});
	}

    /**
     * 设置聊天的Adapter
     */
    private void setChatWindowAdapter() {
        String selection = TableContent.ChatTable.Columns.JID + "='" + mWithJabberID + "'";
        // 异步查询数据库
        new AsyncQueryHandler(getContentResolver()) {

            @Override
            protected void onQueryComplete(int token, Object cookie,
                                           Cursor cursor) {
                // ListAdapter adapter = new ChatWindowAdapter(cursor,
                // PROJECTION_FROM, PROJECTION_TO, mWithJabberID);
                ListAdapter adapter = new TTTChatAdapter(ChatTTTActivity.this,
                        cursor, PROJECTION_FROM, client, null);
                getMessageListView().setAdapter(adapter);
                getMessageListView().setSelection(adapter.getCount() - 1);
            }

        }.startQuery(0, null, ChatProvider.CONTENT_URI, PROJECTION_FROM,
                selection, null, null);
    }
}