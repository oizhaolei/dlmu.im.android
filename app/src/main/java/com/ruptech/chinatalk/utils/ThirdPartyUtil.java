package com.ruptech.chinatalk.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.thirdparty.model.Share;
import com.ruptech.chinatalk.widget.CustomDialog;

public class ThirdPartyUtil {

	private static final String TAG = "LOGIN.Util";

	private static Dialog mProgressDialog;
	private static Toast mToast;

	/*
	 * 16进制数字字符集
	 */
	private static String hexString = "0123456789ABCDEF";
	private static final int MAX_DECODE_PICTURE_SIZE = 1920 * 1440;

	// public static byte[] bmpToByteArray(final Bitmap bmp,
	// final boolean needRecycle) {
	// int i;
	// int j;
	// if (bmp.getHeight() > bmp.getWidth()) {
	// i = bmp.getWidth();
	// j = bmp.getWidth();
	// } else {
	// i = bmp.getHeight();
	// j = bmp.getHeight();
	// }
	//
	// Bitmap localBitmap = Bitmap.createBitmap(i, j, Bitmap.Config.RGB_565);
	// Canvas localCanvas = new Canvas(localBitmap);
	//
	// while (true) {
	// localCanvas.drawBitmap(bmp, new Rect(0, 0, i, j), new Rect(0, 0, i,
	// j), null);
	// if (needRecycle)
	// bmp.recycle();
	// ByteArrayOutputStream localByteArrayOutputStream = new
	// ByteArrayOutputStream();
	// localBitmap.compress(Bitmap.CompressFormat.JPEG, 100,
	// localByteArrayOutputStream);
	// localBitmap.recycle();
	// byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
	// try {
	// localByteArrayOutputStream.close();
	// return arrayOfByte;
	// } catch (Exception e) {
	// // F.out(e);
	// }
	// i = bmp.getHeight();
	// j = bmp.getHeight();
	// }
	// }

	public static byte[] bmpToByteArray(final Bitmap bmp,
			final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);
		if (needRecycle) {
			bmp.recycle();
		}

		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			Utils.sendClientException(e);
		}

		return result;
	}

	/**
	 * 从byte[]得到File
	 * 
	 * @param b
	 * @param outputFile
	 * @return
	 */
	public static File byteArrayToFile(byte[] b, String outputFile) {
		BufferedOutputStream stream = null;
		File file = null;
		try {
			file = new File(outputFile);
			FileOutputStream fstream = new FileOutputStream(file);
			stream = new BufferedOutputStream(fstream);
			stream.write(b);
		} catch (Exception e) {
			Utils.sendClientException(e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e1) {
					Utils.sendClientException(e1);
				}
			}
		}
		return file;
	}

	/*
	 * Convert byte[] to hex
	 * string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
	 * 
	 * @param src byte[] data
	 * 
	 * @return hex string
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	/**
	 * Convert char to byte
	 * 
	 * @param c
	 *            char
	 * @return byte
	 */
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,

	int minSideLength, int maxNumOfPixels) {

		double w = options.outWidth;

		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 :

		(int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));

		int upperBound = (minSideLength == -1) ? 128 :

		(int) Math.min(Math.floor(w / minSideLength),

		Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {

			// return the larger one when there is no overlapping zone.

			return lowerBound;

		}

		if ((maxNumOfPixels == -1) &&

		(minSideLength == -1)) {

			return 1;

		} else if (minSideLength == -1) {

			return lowerBound;

		} else {

			return upperBound;

		}
	}

	public static int computeSampleSize(BitmapFactory.Options options,

	int minSideLength, int maxNumOfPixels) {

		int initialSize = computeInitialSampleSize(options, minSideLength,

		maxNumOfPixels);

		int roundedSize;

		if (initialSize <= 8) {

			roundedSize = 1;

			while (roundedSize < initialSize) {

				roundedSize <<= 1;

			}

		} else {

			roundedSize = (initialSize + 7) / 8 * 8;

		}

		return roundedSize;
	}

	public static final void dismissDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	public static Bitmap extractThumbNail(final String path, final int height,
			final int width, final boolean crop) {
		Assert.assertTrue(path != null && !path.equals("") && height > 0
				&& width > 0);

		BitmapFactory.Options options = new BitmapFactory.Options();

		try {
			options.inJustDecodeBounds = true;
			Bitmap tmp = BitmapFactory.decodeFile(path, options);
			if (tmp != null) {
				tmp.recycle();
				tmp = null;
			}

			Log.d(TAG, "extractThumbNail: round=" + width + "x" + height
					+ ", crop=" + crop);
			final double beY = options.outHeight * 1.0 / height;
			final double beX = options.outWidth * 1.0 / width;
			Log.d(TAG, "extractThumbNail: extract beX = " + beX + ", beY = "
					+ beY);
			options.inSampleSize = (int) (crop ? (beY > beX ? beX : beY)
					: (beY < beX ? beX : beY));
			if (options.inSampleSize <= 1) {
				options.inSampleSize = 1;
			}

			// NOTE: out of memory error
			while (options.outHeight * options.outWidth / options.inSampleSize > MAX_DECODE_PICTURE_SIZE) {
				options.inSampleSize++;
			}

			int newHeight = height;
			int newWidth = width;
			if (crop) {
				if (beY > beX) {
					newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
				} else {
					newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
				}
			} else {
				if (beY < beX) {
					newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
				} else {
					newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
				}
			}

			options.inJustDecodeBounds = false;

			Log.i(TAG, "bitmap required size=" + newWidth + "x" + newHeight
					+ ", orig=" + options.outWidth + "x" + options.outHeight
					+ ", sample=" + options.inSampleSize);
			Bitmap bm = BitmapFactory.decodeFile(path, options);
			if (bm == null) {
				Log.e(TAG, "bitmap decode failed");
				return null;
			}

			Log.i(TAG,
					"bitmap decoded size=" + bm.getWidth() + "x"
							+ bm.getHeight());
			final Bitmap scale = Bitmap.createScaledBitmap(bm, newWidth,
					newHeight, true);
			if (scale != null) {
				bm.recycle();
				bm = scale;
			}

			if (crop) {
				final Bitmap cropped = Bitmap.createBitmap(bm,
						(bm.getWidth() - width) >> 1,
						(bm.getHeight() - height) >> 1, width, height);
				if (cropped == null) {
					return bm;
				}

				bm.recycle();
				bm = cropped;
				Log.i(TAG,
						"bitmap croped size=" + bm.getWidth() + "x"
								+ bm.getHeight());
			}
			return bm;

		} catch (final OutOfMemoryError e) {
			Utils.sendClientException(e);
			Log.e(TAG, "decode bitmap failed: " + e.getMessage());
			options = null;
		}

		return null;
	}

	public static Bitmap getBitmapByUrl(String imgUrl) throws IOException {
		ByteArrayOutputStream outstream;

		try {
			InputStream is = new URL(imgUrl).openStream();
			outstream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024]; // 用数据装
			int len = -1;
			while ((len = is.read(buffer)) != -1) {
				outstream.write(buffer, 0, len);
			}
			outstream.close();
			byte[] data = outstream.toByteArray();
			return BitmapFactory.decodeByteArray(data, 0, data.length);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * 根据一个网络连接(String)获取bitmap图像
	 * 
	 * @param imageUri
	 * @return
	 * @throws MalformedURLException
	 */
	public static Bitmap getBitmapFromCacheByUrl(String imageUrl) {
		File cachedImage = ImageLoader.getInstance().getDiscCache()
				.get(imageUrl);
		if (cachedImage.exists()) { /* 产生Bitmap对象，并放入mImageView中 */
			String filePath = cachedImage.getAbsolutePath();
			return BitmapFactory.decodeFile(filePath);
		} else {
			return null;
		}
	}

	public static byte[] getBytesByUrl(String imgUrl) throws IOException {
		ByteArrayOutputStream outstream;

		try {
			InputStream is = new URL(imgUrl).openStream();
			outstream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024]; // 用数据装
			int len = -1;
			while ((len = is.read(buffer)) != -1) {
				outstream.write(buffer, 0, len);
			}
			outstream.close();
			return outstream.toByteArray();
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public static byte[] getBytesFromCacheByUrl(String imageUrl) {
		File f = ImageLoader.getInstance().getDiscCache().get(imageUrl);
		if (f == null) {
			return null;
		}
		try {
			FileInputStream stream = new FileInputStream(f);
			ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1000];
			int n;
			while ((n = stream.read(b)) != -1)
				out.write(b, 0, n);
			stream.close();
			out.close();
			return out.toByteArray();
		} catch (IOException e) {
		}
		return null;
	}

	/**
	 * 在Chache中得到已经下载的图片
	 * 
	 * @param imageUrl
	 *            图片网址
	 * @return
	 */
	public static File getCacheFileByUrl(String imageUrl) {
		File cachedImage = ImageLoader.getInstance().getDiscCache()
				.get(imageUrl);
		if (cachedImage.exists()) { /* 产生Bitmap对象，并放入mImageView中 */
			return cachedImage;
		} else {
			return null;
		}
	}

	/**
	 * 在Chache中得到已经下载的图片
	 * 
	 * @param imageUrl
	 *            图片网址
	 * @return
	 */
	public static String getCachePathByUrl(String imageUrl) {
		File cachedImage = ImageLoader.getInstance().getDiscCache()
				.get(imageUrl);
		if (cachedImage.exists()) { /* 产生Bitmap对象，并放入mImageView中 */
			return cachedImage.getAbsolutePath();
		} else {
			return null;
		}
	}

	/**
	 * 压缩图片
	 * 
	 * @param bitmap
	 * @param maxSize
	 * @return
	 */
	public static Bitmap getCompressedBitmap(Bitmap bitmap, double maxSize) {
		// 图片允许最大空间 单位：KB
		// 将bitmap放至数组中，意在bitmap的大小（与实际读取的原文件要大）
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] b = baos.toByteArray();
		// 将字节换成KB
		double mid = b.length / 1024;
		// 判断bitmap占用空间是否大于允许最大空间 如果大于则压缩 小于则不压缩
		if (mid > maxSize) {
			// 获取bitmap大小 是允许最大大小的多少倍
			double i = mid / maxSize;
			// 开始压缩 此处用到平方根 将宽带和高度压缩掉对应的平方根倍
			// （1.保持刻度和高度和原bitmap比率一致，压缩后也达到了最大大小占用空间的大小）
			bitmap = zoomImage(bitmap, bitmap.getWidth() / Math.sqrt(i),
					bitmap.getHeight() / Math.sqrt(i));
			return getCompressedBitmap(bitmap, maxSize);
		} else {
			return bitmap;
		}

	}

	public static File getFileFromCacheByUrl(String imageUrl) {
		File cachedImage = ImageLoader.getInstance().getDiscCache()
				.get(imageUrl);
		if (cachedImage.exists()) { /* 产生Bitmap对象，并放入mImageView中 */
			return cachedImage;
		} else {
			return null;
		}
	}

	public static byte[] getHtmlByteArray(final String url) {
		URL htmlUrl = null;
		InputStream inStream = null;
		try {
			htmlUrl = new URL(url);
			URLConnection connection = htmlUrl.openConnection();
			HttpURLConnection httpConnection = (HttpURLConnection) connection;
			int responseCode = httpConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				inStream = httpConnection.getInputStream();
			}
		} catch (MalformedURLException e) {
			Utils.sendClientException(e);
		} catch (IOException e) {
			Utils.sendClientException(e);
		}
		byte[] data = inputStreamToByte(inStream);

		return data;
	}

	/**
	 * 取得 第三方分享信息
	 * 
	 * @param language
	 *            语种
	 * @return
	 */
	public static Share getThirdPartyShare(String language) {
		List<String> langFull = Utils.getFullLanguageList(language);

		Share shareInfo = null;
		List<Share> thirdPartyShareArray = PrefUtils.readThirdParyShare();
		if (thirdPartyShareArray != null) {
			int size = thirdPartyShareArray.size();
			for (int i = 0; i < size; i++) {
				shareInfo = thirdPartyShareArray.get(i);
				if (langFull.contains(shareInfo.getLang().toUpperCase())) {
					return shareInfo;
				}
			}
		}

		return shareInfo;
	}

	/**
	 * Convert hex string to byte[]
	 * 
	 * @param hexString
	 *            the hex string
	 * @return byte[]
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	// 转换十六进制编码为字符串
	public static String hexToString(String s) {
		if ("0x".equals(s.substring(0, 2))) {
			s = s.substring(2);
		}
		byte[] baKeyword = new byte[s.length() / 2];
		for (int i = 0; i < baKeyword.length; i++) {
			try {
				baKeyword[i] = (byte) (0xff & Integer.parseInt(
						s.substring(i * 2, i * 2 + 2), 16));
			} catch (Exception e) {
				Utils.sendClientException(e);
			}
		}

		try {
			s = new String(baKeyword, "utf-8");// UTF-16le:Not
		} catch (Exception e1) {
			Utils.sendClientException(e1);
		}
		return s;
	}

	public static byte[] inputStreamToByte(InputStream is) {
		try {
			ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
			int ch;
			while ((ch = is.read()) != -1) {
				bytestream.write(ch);
			}
			byte imgdata[] = bytestream.toByteArray();
			bytestream.close();
			return imgdata;
		} catch (Exception e) {
			Utils.sendClientException(e);
		}

		return null;
	}

	/**
	 * 检测移动端是否安装了指定的客户端
	 * 
	 * @param third_party_type
	 * @return
	 */
	public static boolean isAvailableClient(String third_party_type) {
		String pkg = "";
		if (AppPreferences.THIRD_PARTY_TYPE_TENCENT_QQ.equals(third_party_type)) {
			pkg = "com.tencent.mobileqq";
		} else if (AppPreferences.THIRD_PARTY_TYPE_TENCENT_QQ_INTERNATIONAL
				.equals(third_party_type)) {
			pkg = "com.tencent.mobileqqi";
		} else if (AppPreferences.THIRD_PARTY_TYPE_TENCENT_QQ_LITE
				.equals(third_party_type)) {
			pkg = "com.tencent.qqlite";
		} else if (AppPreferences.THIRD_PARTY_TYPE_SINA_WEIBO
				.equals(third_party_type)) {
			pkg = "com.sina.weibo";
		} else if (AppPreferences.THIRD_PARTY_TYPE_FACEBOOK
				.equals(third_party_type)) {
			pkg = "com.facebook.katana";
		} else if (AppPreferences.THIRD_PARTY_TYPE_GOOGLE_PLUS
				.equals(third_party_type)) {
			pkg = "com.google.android.apps.plus";
		} else if (AppPreferences.THIRD_PARTY_TYPE_TENCENT_WECHAT
				.equals(third_party_type)) {
			pkg = "com.tencent.mm";
		} else if (AppPreferences.THIRD_PARTY_TYPE_GOOGLE
				.equals(third_party_type)) {
			pkg = "com.android.vending";
		} else if (AppPreferences.THIRD_PARTY_TYPE_KAKAO
				.equals(third_party_type)) {
			pkg = "com.kakao.talk";
		} else if (AppPreferences.THIRD_PARTY_TYPE_LINE
				.equals(third_party_type)) {
			pkg = "jp.naver.line.android";
		}
		PackageInfo packageInfo = null;
		try {
			packageInfo = App.mContext.getPackageManager().getPackageInfo(pkg,
					0);
		} catch (NameNotFoundException e) {
			packageInfo = null;
		}

		if (packageInfo == null) {
			return false;
		} else {
			return true;
		}

	}

	/**
	 * 以最省内存的方式读取图片
	 */
	public static Bitmap readBitmap(final String path) {
		try {
			FileInputStream stream = new FileInputStream(new File(path
					+ "test.jpg"));
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize = 8;
			opts.inPurgeable = true;
			opts.inInputShareable = true;
			Bitmap bitmap = BitmapFactory.decodeStream(stream, null, opts);
			return bitmap;
		} catch (Exception e) {
			return null;
		}
	}

	public static byte[] readFromFile(String fileName, int offset, int len) {
		if (fileName == null) {
			return null;
		}

		File file = new File(fileName);
		if (!file.exists()) {
			Log.i(TAG, "readFromFile: file not found");
			return null;
		}

		if (len == -1) {
			len = (int) file.length();
		}

		Log.d(TAG, "readFromFile : offset = " + offset + " len = " + len
				+ " offset + len = " + (offset + len));

		if (offset < 0) {
			Log.e(TAG, "readFromFile invalid offset:" + offset);
			return null;
		}
		if (len <= 0) {
			Log.e(TAG, "readFromFile invalid len:" + len);
			return null;
		}
		if (offset + len > (int) file.length()) {
			Log.e(TAG, "readFromFile invalid file len:" + file.length());
			return null;
		}

		byte[] b = null;
		try {
			RandomAccessFile in = new RandomAccessFile(fileName, "r");
			b = new byte[len];
			in.seek(offset);
			in.readFully(b);
			in.close();

		} catch (Exception e) {
			Log.e(TAG, "readFromFile : errMsg = " + e.getMessage());
			Utils.sendClientException(e);
		}
		return b;
	}

	public static void share(Context context, String pkg, String cls,
			String text, Bitmap thumb, String url) {
		Intent intent = new Intent();
		ComponentName comp = null;
		try {
			context.getPackageManager().getPackageInfo(pkg, 0);
			comp = new ComponentName(pkg, cls);
			intent.setComponent(comp);
		} catch (NameNotFoundException e) {
		}

		intent.setAction(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_TEXT, text);

		if (thumb == null) {

			intent.setType("text/plain");

		} else {
			try {
				// intent.setType("image/*");
				intent.setType("image/*;text/plain");
				String path = Environment.getExternalStorageDirectory()
						.getPath() + File.separator + "MyStory.jpg";
				byte[] bytes = ThirdPartyUtil.bmpToByteArray(thumb, false);
				File imageFile = ThirdPartyUtil.byteArrayToFile(bytes, path);
				if (imageFile.exists()) {
					intent.putExtra(Intent.EXTRA_STREAM,
							Uri.fromFile(imageFile));
				}
			} catch (Exception e) {
				if (e.getMessage().contains("No space left on device")) {
					Toast.makeText(
							App.mContext.getApplicationContext(),
							App.mContext
.getString(R.string.no_space_left_on_device),
							Toast.LENGTH_SHORT).show();
				}
			}
		}

		intent.putExtra(Intent.EXTRA_TEXT, url);
		intent.putExtra(Intent.EXTRA_SUBJECT, text);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		context.startActivity(Intent.createChooser(intent,
				context.getString(R.string.message_action_share)));
	}

	public static void share(Context context, String pkg, String cls,
			String text, byte[] bytes, String url) {
		Intent intent = new Intent();
		ComponentName comp = null;
		try {
			context.getPackageManager().getPackageInfo(pkg, 0);
			comp = new ComponentName(pkg, cls);
			intent.setComponent(comp);
		} catch (NameNotFoundException e) {
		}

		intent.setAction(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_TEXT, text);

		if (bytes == null || bytes.length == 0) {

			intent.setType("text/plain");

		} else {

			// intent.setType("image/*");
			intent.setType("image/*;text/plain");
			String path = Environment.getExternalStorageDirectory().getPath()
					+ File.separator + "MyStory.jpg";
			File imageFile = ThirdPartyUtil.byteArrayToFile(bytes, path);
			intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));
		}

		intent.putExtra(Intent.EXTRA_TEXT, url);
		intent.putExtra(Intent.EXTRA_SUBJECT, text);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		context.startActivity(Intent.createChooser(intent,
				context.getString(R.string.message_action_share)));
	}

	public static void share(Context context, String pkg, String cls,
			String text, String imgUrl, String url) {
		Intent intent = new Intent();
		ComponentName comp = null;
		try {
			context.getPackageManager().getPackageInfo(pkg, 0);
			comp = new ComponentName(pkg, cls);
			intent.setComponent(comp);
		} catch (NameNotFoundException e) {
		}

		intent.setType("image/*;text/plain");
		intent.setAction(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(imgUrl));

		intent.putExtra(Intent.EXTRA_TEXT, url);
		intent.putExtra(Intent.EXTRA_SUBJECT, text);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		context.startActivity(Intent.createChooser(intent,
				context.getString(R.string.message_action_share)));
	}

	public static void shareImage(Context context, String pkg, String cls) {
		Share share = getThirdPartyShare(App.readUser().getLang());

		Intent intent = new Intent();
		ComponentName comp = null;
		try {
			context.getPackageManager().getPackageInfo(pkg, 0);
			comp = new ComponentName(pkg, cls);
			intent.setComponent(comp);
		} catch (NameNotFoundException e) {
		}

		intent.setAction(Intent.ACTION_SEND);
		intent.setType("image/*");
		intent.putExtra(Intent.EXTRA_TEXT, share.getThirdparty_share_text());
		intent.putExtra(Intent.EXTRA_TEXT,
				share.getThirdparty_share_targeturl());
		String path = Environment.getExternalStorageDirectory().getPath()
				+ File.separator + "chinatalk.png";
		Bitmap thumb = ((BitmapDrawable) context.getResources().getDrawable(
				R.drawable.ic_launcher)).getBitmap();
		byte[] bytes = ThirdPartyUtil.bmpToByteArray(thumb, false);
		File imageFile = ThirdPartyUtil.byteArrayToFile(bytes, path);
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));
		context.startActivity(intent);
	}

	public static void shareText(Context context, String pkg, String cls) {
		Share share = getThirdPartyShare(App.readUser().getLang());

		Intent intent = new Intent();
		ComponentName comp = null;
		try {
			context.getPackageManager().getPackageInfo(pkg, 0);
			comp = new ComponentName(pkg, cls);
			intent.setComponent(comp);
		} catch (NameNotFoundException e) {
		}

		intent.setAction(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, share.getThirdparty_share_text());
		intent.putExtra(Intent.EXTRA_TEXT,
				share.getThirdparty_share_targeturl());
		context.startActivity(intent);
	}

	public static final void showProgressDialog(Activity context, String title,
			String message) {
		dismissDialog();
		if (TextUtils.isEmpty(title)) {
			title = context.getString(R.string.please_waiting);
		}
		if (TextUtils.isEmpty(message)) {
			message = "LOAD...";
		}
		if (context.hasWindowFocus()) {
			mProgressDialog = ProgressDialog.show(context, title, message);
		}
	}

	public static final void showResultDialog(Context context, String msg,
			String title) {
		if (msg == null)
			return;
		String rmsg = msg.replace(",", "\n");
		Log.d("Util", rmsg);
		new CustomDialog(context).setTitle(title).setMessage(rmsg)
				.setNegativeButton(context.getString(R.string.got_it), null)
				.show();
	}

	/**
	 * 打印消息并且用Toast显示消息
	 * 
	 * @param activity
	 * @param message
	 * @param logLevel
	 *            填d, w, e分别代表debug, warn, error; 默认是debug
	 */
	public static final void toastMessage(final Activity activity,
			final String message) {
		toastMessage(activity, message, null);
	}

	/**
	 * 打印消息并且用Toast显示消息
	 * 
	 * @param activity
	 * @param message
	 * @param logLevel
	 *            填d, w, e分别代表debug, warn, error; 默认是debug
	 */
	public static final void toastMessage(final Activity activity,
			final String message, String logLevel) {
		if ("w".equals(logLevel)) {
			Log.w("sdkDemo", message);
		} else if ("e".equals(logLevel)) {
			Log.e("sdkDemo", message);
		} else {
			Log.d("sdkDemo", message);
		}
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mToast != null) {
					mToast.cancel();
					mToast = null;
				}
				mToast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
				mToast.show();
			}
		});
	}

	/*
	 * 将字符串编码成16进制数字,适用于所有字符（包括中文）
	 */
	public static String toHexString(String str) {
		// 根据默认编码获取字节数组
		byte[] bytes = str.getBytes();
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		// 将字节数组中每个字节拆解成2位16进制整数
		for (int i = 0; i < bytes.length; i++) {
			sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
			sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
		}
		return sb.toString();
	}

	/**
     * 将json对象转换成Map
     *
     * @param jsonObject json对象
     * @return Map对象
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> toMap(JSONObject jsonObject)
    {
        Map<String, String> result = new HashMap<>();
        Iterator<String> iterator = jsonObject.keys();
        String key = null;
        String value = null;
        while (iterator.hasNext())
        {
            key = iterator.next();
			try {
				value = jsonObject.getString(key);
			} catch (JSONException e) {
				continue;
			}
            result.put(key, value);
        }
        return result;
    }

	/**
	 * 图片的缩放方法
	 * 
	 * @param bgimage
	 *            ：源图片资源
	 * @param newWidth
	 *            ：缩放后宽度
	 * @param newHeight
	 *            ：缩放后高度
	 * @return
	 */
	public static Bitmap zoomImage(Bitmap bgimage, double newWidth,
			double newHeight) {
		// 获取这个图片的宽和高
		float width = bgimage.getWidth();
		float height = bgimage.getHeight();
		// 创建操作图片用的matrix对象
		Matrix matrix = new Matrix();
		// 计算宽高缩放率
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 缩放图片动作
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
				(int) height, matrix, true);
		return bitmap;
	}

}
