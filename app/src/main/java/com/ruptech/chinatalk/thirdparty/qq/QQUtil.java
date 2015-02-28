package com.ruptech.chinatalk.thirdparty.qq;

import android.content.Context;
import android.widget.Toast;

import com.tencent.tauth.Tencent;

public class QQUtil {
	public static final String appid = "1150006347";

	public static boolean qqIsReady(Context context, Tencent mTencent) {

		if (mTencent == null) {
			return false;
		}
		boolean ready = mTencent.isSessionValid()
				&& mTencent.getQQToken().getOpenId() != null;
		if (!ready)
			Toast.makeText(context, "login and get openId first, please!",
					Toast.LENGTH_SHORT).show();
		return ready;
	}
}
