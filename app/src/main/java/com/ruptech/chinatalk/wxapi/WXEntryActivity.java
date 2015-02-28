package com.ruptech.chinatalk.wxapi;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.thirdparty.wechat.WeChatUtil;
import com.ruptech.chinatalk.ui.LoginSignupActivity;
import com.ruptech.chinatalk.ui.story.ShareStoryDialogActivity;
import com.ruptech.chinatalk.ui.user.MyWalletActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

	final public static String RECHARGE_FREE = "RECHARGE_FREE";
	final public static String SHARE_STORY = "SHARE_STORY";
	final public static String LOGIN = "LOGIN";

	public static String calledFrom;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		IWXAPI api = WXAPIFactory.createWXAPI(this, WeChatUtil.APP_ID, false);
		api.handleIntent(getIntent(), this);
	}

	@Override
	public void onReq(BaseReq req) {
		switch (req.getType()) {
		case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
			break;
		case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
			break;
		default:
			break;
		}
		finish();
	}

	@Override
	public void onResp(BaseResp resp) {
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			// ERR_OK = 0 分享成功 / 用户同意
			if (RECHARGE_FREE.equals(calledFrom)
					|| SHARE_STORY.equals(calledFrom)) {
				// 分享成功
				Toast.makeText(this, getString(R.string.share_success),
						Toast.LENGTH_SHORT).show();
			}

			if (RECHARGE_FREE.equals(calledFrom)
					&& MyWalletActivity.instance != null) {
				MyWalletActivity.instance
						.freeRecharge(AppPreferences.SHARE_TO_WECHAT_MEMENTS);
			} else if (LOGIN.equals(calledFrom)) {
				// 用户同意登录
				if (LoginSignupActivity.instance != null) {
					LoginSignupActivity.instance.wechat_code = ((SendAuth.Resp) resp).code;
					LoginSignupActivity.instance
							.excuteWeChatAccessTokenTask(LoginSignupActivity.instance.wechat_code);
				}
			}

			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			// ERR_USER_CANCEL = -2 取消分享 / 用户取消
			if (RECHARGE_FREE.equals(calledFrom)
					|| SHARE_STORY.equals(calledFrom)) {
				Toast.makeText(this, getString(R.string.share_canceled),
						Toast.LENGTH_SHORT).show();
			} else if (LOGIN.equals(calledFrom)) {
				// 用户取消登录
				Toast.makeText(this, getString(R.string.you_choosed_cancel),
						Toast.LENGTH_SHORT).show();
			}

			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			// ERR_AUTH_DENIED = -4 认证失败 / 用户拒绝授权
			if (RECHARGE_FREE.equals(calledFrom)
					|| SHARE_STORY.equals(calledFrom)) {
				Toast.makeText(this, getString(R.string.share_failed),
						Toast.LENGTH_SHORT).show();
			} else if (LOGIN.equals(calledFrom)) {
				// 用户拒绝授权
				Toast.makeText(this, getString(R.string.you_choosed_cancel),
						Toast.LENGTH_SHORT).show();
			}

			break;
		default:
			if (RECHARGE_FREE.equals(calledFrom)
					|| SHARE_STORY.equals(calledFrom)) {
				Toast.makeText(this, getString(R.string.share_failed),
						Toast.LENGTH_SHORT).show();
			} else if (LOGIN.equals(calledFrom)) {
				Toast.makeText(this, getString(R.string.you_choosed_cancel),
						Toast.LENGTH_SHORT).show();
			}

			break;
		}

		if (RECHARGE_FREE.equals(calledFrom)
				&& MyWalletActivity.instance != null) {
			MyWalletActivity.instance.currentButton = MyWalletActivity.NONE_BUTTON;
		} else if (SHARE_STORY.equals(calledFrom)
				&& ShareStoryDialogActivity.instance != null) {
			ShareStoryDialogActivity.instance.currentButton = ShareStoryDialogActivity.NONE_BUTTON;
			ShareStoryDialogActivity.close();
		} else if (LOGIN.equals(calledFrom)
				&& LoginSignupActivity.instance != null) {
			// 不处理
		}
		calledFrom = "";
		finish();
	}
}
