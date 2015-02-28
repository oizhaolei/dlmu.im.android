package com.ruptech.chinatalk.widget;

import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.ui.AbstractChatActivity;
import com.ruptech.chinatalk.ui.user.LanguageActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;

public class EditTextWithLang extends EditText {
	private final static String TAG = "EditTextWithLang";
	private Drawable langImgAble;
	private final Context mContext;

	private String lang;

	private int hintContentStringId;

	private boolean isShowHintContent = true;

	private String otherLangCode;

	private String mTextLang;
	public static final int EDITTEXT_TYPE_1 = 1;
	public static final int EDITTEXT_TYPE_2 = 2;
	public static final int EDITTEXT_TYPE_3 = 3;
	private int type = EDITTEXT_TYPE_1;

	public EditTextWithLang(Context context) {
		super(context);
		mContext = context;
		lang = PrefUtils.getPrefPerferLang();
		init();
	}

	public EditTextWithLang(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		lang = PrefUtils.getPrefPerferLang();
		init();
	}

	public EditTextWithLang(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		lang = PrefUtils.getPrefPerferLang();
		init();
	}

	public void changeLang(String langStr){
		lang = langStr;
		PrefUtils.savePrefPreferLang(lang);
		if (!Utils.isEmpty(otherLangCode)
				&& otherLangCode.equals(lang)) {
			if (AbstractChatActivity.instance != null) {// 刷新聊天画面footer
				AbstractChatActivity.refreshFooterBySelectLang();
			}
			isShowHintContent = false;
		} else {
			isShowHintContent = true;
		}
		displayLang();
	}

	private void displayLang() {
		String userLangName = Utils.getLangDisplayName(lang);
		if (type == EDITTEXT_TYPE_3) {// 翻译画面edittext
			if (mTextLang.equals(lang)) {
				setHint(mContext
						.getString(R.string.please_select_translate_language));
				String inputText = getText().toString().trim();
				if (!Utils.isEmpty(inputText)) {
					Toast.makeText(mContext,
							R.string.please_select_translate_language,
							Toast.LENGTH_SHORT).show();
				}
			} else {
				setHint(mContext.getString(hintContentStringId, userLangName));
			}
		} else {
			if (isShowHintContent) {
				if (Utils.isEmpty(otherLangCode)) {
					setHint(mContext.getString(hintContentStringId,
							userLangName));
				} else {// 聊天画面
					if (AbstractChatActivity.instance != null) {
						AbstractChatActivity.refreshFooterBySelectLang();
					}
					setHint(mContext.getString(hintContentStringId,
							userLangName,
							Utils.getLangDisplayName(otherLangCode)));
				}
			} else {
				setHint("");
			}
		}

		// setText("");
		langImgAble = new BitmapDrawable(mContext.getResources(),
				getLangImage());
		setCompoundDrawablesWithIntrinsicBounds(null, null, langImgAble, null);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

	private Bitmap getLangImage() {
		Bitmap bmpOriginal = BitmapFactory.decodeResource(getResources(),
				Utils.getLanguageFlag(lang));
		int width, height;
		height = bmpOriginal.getHeight();
		width = bmpOriginal.getWidth();

		Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
		Canvas c = new Canvas(bm);
		Paint paint = new Paint();
		paint.setAlpha(128);
		c.drawBitmap(bmpOriginal, 0, 0, paint);
		return bm;
	}

	protected void gotoChangeLangActivity() {
		User user = App.readUser();
		if (user == null)
			return;
		Intent intent = new Intent(mContext, LanguageActivity.class);
		intent.putExtra(ProfileActivity.EXTRA_USER, user);
		mContext.startActivity(intent);
	}

	private void init() {
		setText("");
		// langImgAble = mContext.getResources().getDrawable(
		// Utils.getLanguageFlag(lang));
		// setCompoundDrawablesWithIntrinsicBounds(null, null, langImgAble,
		// null);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (langImgAble != null && event.getAction() == MotionEvent.ACTION_UP) {
			int eventX = (int) event.getRawX();
			int eventY = (int) event.getRawY();
			Rect rect = new Rect();
			getGlobalVisibleRect(rect);
			rect.left = rect.right - 100;
			if (rect.contains(eventX, eventY)) {
				final List<String> allLangs = App.readUser().getAllLangs();
				allLangs.add(mContext.getString(R.string.other_language));
				final String[] menus = new String[allLangs.size()];
				final String[] langCode = new String[allLangs.size()];
				for (int i = 0; i < allLangs.size(); i++) {
					if (i < allLangs.size() - 1) {
						menus[i] = Utils.getLangDisplayName(allLangs.get(i));
						langCode[i] = allLangs.get(i);
					} else {
						menus[i] = allLangs.get(i);
					}
				}

				CustomDialog alertDialog = new CustomDialog(mContext).setTitle(
						mContext.getString(R.string.language)).setItems(menus,
								new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (which == menus.length - 1) {
									gotoChangeLangActivity();
								} else {
									changeLang(langCode[which]);
								}
							}
						});
				alertDialog.show();
			}
		}
		return super.onTouchEvent(event);
	}

	public void setHintContent(int id) {// 评论
		type = EDITTEXT_TYPE_1;
		hintContentStringId = id;
		displayLang();
	}

	// 聊天画面, 对方lang code
	public void setHintContent(int id, boolean isShow, String otherLang) {
		type = EDITTEXT_TYPE_2;
		hintContentStringId = id;
		isShowHintContent = isShow;
		otherLangCode = otherLang;
		displayLang();
	}

	public void setHintContent(int id, String textLang) {// 翻译
		type = EDITTEXT_TYPE_3;
		hintContentStringId = id;
		mTextLang = textLang;
		displayLang();
	}
}
