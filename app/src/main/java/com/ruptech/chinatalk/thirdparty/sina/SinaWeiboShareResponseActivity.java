package com.ruptech.chinatalk.thirdparty.sina;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.ui.story.ShareStoryDialogActivity;
import com.ruptech.chinatalk.ui.user.MyWalletActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.constant.WBConstants;

public class SinaWeiboShareResponseActivity extends Activity implements
		IWeiboHandler.Response {

	final public static String RECHARGE_FREE = "RECHARGE_FREE";
	final public static String SHARE_STORY = "SHARE_STORY";

	public static String calledFrom;

	// 新浪微博分享API
	IWeiboShareAPI mWeiboShareAPI;

	protected final String TAG = Utils.CATEGORY
			+ SinaWeiboShareResponseActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 创建微博 SDK 接口实例
		mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, SinaUtil.APP_KEY);

		// 从当前应用唤起微博并进行分享后，返回到当前应用时，需要在此处调用该函数
		// 来接收微博客户端返回的数据；执行成功，返回 true，并调用
		// {@link IWeiboHandler.Response#onResponse}；失败返回 false，不调用上述回调
		mWeiboShareAPI.handleWeiboResponse(this.getIntent(), this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		// 从当前应用唤起微博并进行分享后，返回到当前应用时，需要在此处调用该函数
		// 来接收微博客户端返回的数据；执行成功，返回 true，并调用
		// {@link IWeiboHandler.Response#onResponse}；失败返回 false，不调用上述回调
		mWeiboShareAPI.handleWeiboResponse(intent, this);
	}

	/**
	 * 接收微客户端博请求的数据。 当微博客户端唤起当前应用并进行分享时，该方法被调用。
	 *
	 * @param baseRequest
	 *            微博请求数据对象
	 * @see {@link IWeiboShareAPI#handleWeiboRequest}
	 */
	@Override
	public void onResponse(BaseResponse baseResp) {
		switch (baseResp.errCode) {
		case WBConstants.ErrorCode.ERR_OK:
			// 新浪微博分享成功
			Toast.makeText(this, R.string.send_ok, Toast.LENGTH_SHORT).show();
			if (RECHARGE_FREE.equals(calledFrom)
					&& MyWalletActivity.instance != null) {
				MyWalletActivity.instance
						.freeRecharge(AppPreferences.SHARE_TO_SINA_WEIBO);
			}
			break;
		case WBConstants.ErrorCode.ERR_CANCEL:
			Toast.makeText(this, R.string.share_canceled, Toast.LENGTH_SHORT)
					.show();
			break;
		case WBConstants.ErrorCode.ERR_FAIL:
			Toast.makeText(
					this,
					getString(R.string.share_failed) + " Error Message: "
							+ baseResp.errMsg, Toast.LENGTH_SHORT).show();
			break;
		}

		if (RECHARGE_FREE.equals(calledFrom)
				&& MyWalletActivity.instance != null) {
			MyWalletActivity.instance.currentButton = MyWalletActivity.NONE_BUTTON;
		} else if (SHARE_STORY.equals(calledFrom)
				&& ShareStoryDialogActivity.instance != null) {
			ShareStoryDialogActivity.instance.currentButton = ShareStoryDialogActivity.NONE_BUTTON;
			ShareStoryDialogActivity.close();
		}
		calledFrom = "";
		finish();
	}

}