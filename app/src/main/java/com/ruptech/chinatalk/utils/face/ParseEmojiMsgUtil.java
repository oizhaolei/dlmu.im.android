package com.ruptech.chinatalk.utils.face;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.ui.story.UserStoryCommentActivity;
import com.ruptech.chinatalk.utils.Utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseEmojiMsgUtil {
	private static final String TAG = ParseEmojiMsgUtil.class.getSimpleName();
	private static final String REGEX_STR = "\\[e\\](.*?)\\[/e\\]";

	/**
	 * 是否都为表情字符串
	 */
	public static boolean checkMsgFace(String msg) {
		List<String> tempList = Arrays.asList(MsgFaceUtils.faceImgNames);
		if (tempList.contains(msg)) {
			return true;
		}
		return false;
	}

	/**
	 * 表情解析,转成unicode字符
	 */
	public static String convertToMsg(CharSequence cs, Context mContext) {
		SpannableStringBuilder ssb = new SpannableStringBuilder(cs);
		ImageSpan[] spans = ssb.getSpans(0, cs.length(), ImageSpan.class);
		for (int i = 0; i < spans.length; i++) {
			ImageSpan span = spans[i];
			String c = span.getSource();
			int a = ssb.getSpanStart(span);
			int b = ssb.getSpanEnd(span);
			if (!Utils.isEmpty(c) && c.contains("[")) {
				ssb.replace(a, b, convertUnicode(c));
			}
		}
		ssb.clearSpans();
		return ssb.toString();
	}

	public static String convertUnicode(String emo) {
		emo = emo.substring(1, emo.length() - 1);
		if (emo.length() < 6) {
			return new String(Character.toChars(Integer.parseInt(emo, 16)));
		}
		String[] emos = emo.split("_");
		char[] char0 = Character.toChars(Integer.parseInt(emos[0], 16));
		char[] char1 = Character.toChars(Integer.parseInt(emos[1], 16));
		char[] emoji = new char[char0.length + char1.length];
		for (int i = 0; i < char0.length; i++) {
			emoji[i] = char0[i];
		}
		for (int i = char0.length; i < emoji.length; i++) {
			emoji[i] = char1[i - char0.length];
		}
		return new String(emoji);
	}

	/**
	 * 对spanableString进行正则判断，如果符合要求，则以表情图片代替
	 */
	public static void dealExpression(Context context,
			SpannableString spannableString, Pattern patten, int start)
			throws Exception {
		Matcher matcher = patten.matcher(spannableString);
		while (matcher.find()) {
			String key = matcher.group();
			Log.d("Key", key);
			if (matcher.start() < start) {
				continue;
			}
			Field field = R.drawable.class
					.getDeclaredField("emoji_"
							+ key.substring(key.indexOf("]") + 1,
									key.lastIndexOf("[")));
			int resId = Integer.parseInt(field.get(null).toString());
			if (resId != 0) {
				Bitmap bitmap = BitmapFactory.decodeResource(
						context.getResources(), resId);
				ImageSpan imageSpan = new ImageSpan(context, bitmap);
				int end = matcher.start() + key.length();
				spannableString.setSpan(imageSpan, matcher.start(), end,
						Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
				if (end < spannableString.length()) {
					dealExpression(context, spannableString, patten, end);
				}
				break;
			}
		}
	}

	/**
	 * 解析字符串中的表情字符串替换成表情图片
	 */
	public static SpannableString getExpressionString(Context context,
			String str) {
		SpannableString spannableString = new SpannableString(str);
		Pattern sinaPatten = Pattern.compile(REGEX_STR,
				Pattern.CASE_INSENSITIVE);
		try {
			dealExpression(context, spannableString, sinaPatten, 0);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		return spannableString;
	}

	/**
	 * 贴图内容， 不需要翻译： 表情、@XXX,表情、@XXX, 表情。parseEmoji之后 [e]XXX[/e]
	 */
	public static boolean isNoNeedTranslate(String content, Context context) {
		String result = content;
		int start = content.indexOf(UserStoryCommentActivity.TAG_PATTERN);
		int end = content.indexOf(UserStoryCommentActivity.SPLIT_PATTERN);
		if (start != -1 && end != -1 && end > start) {
			content.substring(0, start);
			content.substring(start, end);
			String suffix = content.substring(end);
			result = suffix.trim();
		}

		boolean isNoNeedTranslate = true;
		String tempStr = result.replace("[", "");
		tempStr = tempStr.replace("/", "");
		tempStr = tempStr.replace("e", "");
		tempStr = tempStr.replace("]", "");
		tempStr = tempStr.replace(",", "");
		tempStr = tempStr.replace(" ", "");
		tempStr = tempStr.replace(",", "");
		if (Utils.isEmpty(tempStr)) {
			return true;
		}
		String unicode = EmojiParser.getInstance(context).parseEmoji(tempStr);
		String[] msgArray = unicode.split("\\[/e\\]");
		for (String msg : msgArray) {
			String str = msg + "[/e]";
			if (!str.startsWith("[e]")) {
				isNoNeedTranslate = false;// need translate
				break;
			}
		}
		return isNoNeedTranslate;
	}

}