package com.ruptech.chinatalk.utils;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.ruptech.chinatalk.App;

public class AlipayResult {

	public static String getNewOrderInfo(
			List<Map<String, String>> sProductsMapList, int position)
			throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("partner=\"");
		sb.append(AppPreferences.ALIPAY_PARTNER);
		sb.append("\"&out_trade_no=\"");
		sb.append(getOutTradeNo());
		sb.append("\"&subject=\"");
		sb.append(sProductsMapList.get(position).get("subject"));
		sb.append("\"&body=\"");
		sb.append(sProductsMapList.get(position).get("body"));
		sb.append("\"&total_fee=\"");
		sb.append(sProductsMapList.get(position).get("price")
				.replace("price:", ""));
		sb.append("\"&notify_url=\"");

		// 网址需要做URL编码
		sb.append(URLEncoder.encode(App.readServerAppInfo().getAppServerUrl()
				+ "recharge/alipay_call_back_return.php", "UTF-8"));
		sb.append("\"&service=\"mobile.securitypay.pay");
		sb.append("\"&_input_charset=\"UTF-8");
		sb.append("\"&return_url=\"");
		sb.append(URLEncoder.encode("http://m.alipay.com", "UTF-8"));
		sb.append("\"&payment_type=\"1");
		sb.append("\"&seller_id=\"");
		sb.append(AppPreferences.ALIPAY_SELLER);

		// 如果show_url值为空，可不传
		// sb.append("\"&show_url=\"");
		sb.append("\"&it_b_pay=\"1m");
		sb.append("\"");

		String info = new String(sb);

		String sign = Rsa.sign(info, AppPreferences.ALIPAY_RSA_PRIVATE);
		sign = URLEncoder.encode(sign, "UTF-8");
		info += "&sign=\"" + sign + "\"&" + AlipayResult.getSignType();

		return info;
	}

	public static String getOutTradeNo() {
		return App.readUser().getId()
				+ "_"
				+ DateCommonUtils.dateFormat(new Date(),
						DateCommonUtils.DF_yyyyMMddHHmmss2);
	}

	public static String getSignType() {
		return "sign_type=\"RSA\"";
	}

	public String resultStatus;
	public String result;
	public String memo;

	public AlipayResult(String rawResult) {
		try {
			String[] resultParams = rawResult.split(";");
			for (String resultParam : resultParams) {
				if (resultParam.startsWith("resultStatus")) {
					resultStatus = getValue(resultParam, "resultStatus");
				}
				if (resultParam.startsWith("result")) {
					result = getValue(resultParam, "result");
				}
				if (resultParam.startsWith("memo")) {
					memo = getValue(resultParam, "memo");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getValue(String content, String key) {
		String prefix = key + "={";
		return content.substring(content.indexOf(prefix) + prefix.length(),
				content.lastIndexOf("}"));
	}

	@Override
	public String toString() {
		return "resultStatus={" + resultStatus + "};memo={" + memo
				+ "};result={" + result + "}";
	}

}
