package com.ruptech.chinatalk.thirdparty.wechat;

import java.security.MessageDigest;

public class WeChatUtil {
	public static class ShowMsgActivity {
		public static final String STitle = "showmsg_title";
		public static final String SMessage = "showmsg_message";
		public static final String BAThumbData = "showmsg_thumb_data";
	}

	public static final String APP_ID = "wxa3786a870068a473";
	public static final String APP_SECRET = "8d4d979234eed31c165d1a5e50cc0012";
	public static final String APP_KEY = "iD27pX4eo3UiRX3XRhQLkA7T6VwEu9oU1ronTH3ZQ5STKE6RogjjHeLv3qYcHIFMzl39BxxugCm3zffMfDAGNoLDwvzZojYG6wDidphRoubhwJYYQKax7HAFubmN2g1M";
	public static final String PARTNER_ID = "1220125601";
	public static final String PARTNER_KEY = "3049c068c3e00f9e02c967077df9962c";

	// public static final String APP_ID = "wxf8950e128dcd405b";
	// public static final String APP_SECRET =
	// "d069724c43080b4787d2dab74e58ca26";
	// public static final String APP_KEY =
	// "L305sphkE70sFti5JrAL41PUZkKcn1ioqFpmhk41DlJx8GFPsqt9GlYJENxbJuXtKecYSdj27Tgw7LGFYEvYvfJfY3qEAOwg9SiRLm2GYOI9DaVWqhd3eIK1IV8VcB0r";
	// public static final String PARTNER_ID = "1220322901";
	// public static final String PARTNER_KEY =
	// "d9d39f9f3e0aa4cb5dff42e872ca2340";

	public static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";

	public static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;

	public static String sha1(String str) {
		if (str == null || str.length() == 0) {
			return null;
		}

		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };

		try {
			MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
			mdTemp.update(str.getBytes());

			byte[] md = mdTemp.digest();
			int j = md.length;
			char buf[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
				buf[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(buf);
		} catch (Exception e) {
			return null;
		}
	}

}
