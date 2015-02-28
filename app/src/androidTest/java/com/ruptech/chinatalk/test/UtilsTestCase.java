package com.ruptech.chinatalk.test;

import static com.ruptech.chinatalk.sqlite.TableContent.ChannelTable;
import static com.ruptech.chinatalk.sqlite.TableContent.FriendTable;
import static com.ruptech.chinatalk.sqlite.TableContent.HotUserPhotoTable;
import static com.ruptech.chinatalk.sqlite.TableContent.MessageTable;
import static com.ruptech.chinatalk.sqlite.TableContent.UserPhotoTable;
import static com.ruptech.chinatalk.sqlite.TableContent.UserPropTable;
import static com.ruptech.chinatalk.sqlite.TableContent.UserTable;

import java.net.URLEncoder;
import java.util.HashMap;

import junit.framework.Assert;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.test.AndroidTestCase;
import android.util.Log;

import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Rsa;
import com.ruptech.chinatalk.utils.Utils;

public class UtilsTestCase extends AndroidTestCase {
	private static final String TAG = "UtilsTestCase";

	private String MD5(String md5) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest
					.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
						.substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
		}
		return null;
	}

	public void testEncryptPassword() {
		String p = "samsung2014092";
		Log.e(TAG, p);
		p = MD5(p).substring(0, 4);
		Log.e(TAG, p);
	}

	//2395fa498a094f29eb052d9fd41ef5274e41ab96
	//76614langKRlang1CNloginid76614parent_id0sourcean-2014122318typemeuserid418152a9304125e25edaa5aff574153eafc95c97672c6
	//2d79af47eaf9093a83fe2792dd89233a5a98fd4a
	//http://app.tttalk.org:4000/photos?lang=KR&lang1=CN&loginid=76614&parent_id=0&sign=2395fa498a094f29eb052d9fd41ef5274e41ab96&source=an-2014122318&type=me&userid=41815

	public void testGenParams() throws Exception {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("lang", "KR");
		params.put("lang1", "CN");
		params.put("parent_id", "0");
		params.put("type", "me");
		params.put("userid", "41815");

		long loginid=76614;
		String appkey="76614";
		params.put("source", "an-2014122318");
		params.put("loginid", String.valueOf(loginid));
		
		String sign = Utils.genSign(params, appkey);
		
		Log.e(TAG, sign);
		Assert.assertEquals("2d79af47eaf9093a83fe2792dd89233a5a98fd4a", sign);
	}
	
	public void testRSASign() throws Exception {
		String sign = Rsa.sign("", AppPreferences.ALIPAY_RSA_PRIVATE);
		sign = URLEncoder.encode(sign, "UTF-8");
		Log.v("UtilsTestCase", sign);
	}

	public void testCreateIndexSQL() {

		Log.e(TAG, UserTable.getCreateSQL());
		Log.e(TAG, UserPhotoTable.getCreateSQL());
		Log.e(TAG, UserPropTable.getCreateSQL());

		Log.e(TAG, FriendTable.getCreateSQL());
		Log.e(TAG, MessageTable.getCreateSQL());

		Log.e(TAG, ChannelTable.getCreateSQL());

		Log.e(TAG, HotUserPhotoTable.getCreateSQL());
	}

	public void testSimCard() throws Exception {
		TelephonyManager mTelephonyMgr = (TelephonyManager) getContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = mTelephonyMgr.getDeviceId();
		String simCountryIso = mTelephonyMgr.getSimCountryIso();
		String simOperator = mTelephonyMgr.getSimOperator();
		String simOperatorName = mTelephonyMgr.getSimOperatorName();
		String simSerialNumber = mTelephonyMgr.getSimSerialNumber();
		int simState = mTelephonyMgr.getSimState();
		String subscriberId = mTelephonyMgr.getSubscriberId();
		Log.v("testSimCard", "testSimCard");

	}

}
