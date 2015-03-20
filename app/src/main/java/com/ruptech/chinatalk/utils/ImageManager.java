/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ruptech.chinatalk.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.ruptech.chinatalk.App;
import com.ruptech.dlmu.im.BuildConfig;
import com.ruptech.dlmu.im.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Manages retrieval and storage of icon images. Use the put method to download
 * and store images. Use the get method to retrieve images from the manager.
 */
public class ImageManager {
	private static int calculateInSampleSize(int width, int height,
	                                         int reqWidth, int reqHeight) {
		// Raw height and width of image
		int inSampleSize = 1;
		while ((height / inSampleSize) > reqHeight
				|| (width / inSampleSize) > reqWidth) {
			inSampleSize *= 2;
		}

		return inSampleSize;
	}

	/**
	 * Compress and resize the Image
	 * <p/>
	 * <br />
	 * 因为不论图片大小和尺寸如何, 都会对图片进行一次有损压缩, 所以本地压缩应该 考虑图片将会被二次压缩所造成的图片质量损耗
	 *
	 * @param sourceFile
	 * @param quality              , 0~100, recommend 100
	 * @param wifiAvailible
	 * @param outputStream
	 * @param context
	 * @param abstractChatActivity
	 * @return
	 * @throws IOException
	 */
	public static File compressImage(File sourceFile, int quality,
	                                 Context activity, boolean wifiAvailible) throws IOException {
		File destFile;
		FileOutputStream outputStream;

		String sourcePath = sourceFile.getAbsolutePath();
		String md5 = Utils.getMd5(sourceFile.getPath());
		if (activity != null) {
			destFile = activity.getFileStreamPath(md5);
			outputStream = activity.openFileOutput(md5, Context.MODE_PRIVATE);
		} else {
			destFile = new File(sourceFile.getParentFile(), md5);
			outputStream = new FileOutputStream(destFile);
		}

		// 1. Calculate scale
		int scale = 1;
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(sourcePath, o);

		int max_size = IMAGE_MAX_SIZE_3G;

		if (wifiAvailible) {
			max_size = IMAGE_MAX_SIZE_WIFI;
		}

		if (o.outWidth > max_size || o.outHeight > max_size) {
			int height = o.outHeight;
			int width = o.outWidth;
			scale = calculateInSampleSize(width, height, max_size, max_size);
		}

		// 2. File -> Bitmap (Returning a smaller image)
		o.inJustDecodeBounds = false;
		o.inSampleSize = scale;

		Bitmap bitmap = BitmapFactory.decodeFile(sourcePath, o);
		// 旋转角度
		int angle = Utils.getExifOrientation(sourcePath);
		if (angle != 0) {
			Matrix matrix = new Matrix();
			matrix.postRotate(angle);
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
					bitmap.getHeight(), matrix, true);
		}

		// 3. Bitmap -> File

		writeBitmap(bitmap, quality, outputStream);

		return destFile;
	}

	private static Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return writeToRoundCorner(bitmap, 4);
	}

	public static Bitmap getDefaultLandscape(Context context) {
		if (defaultPortrait == null) {
			defaultPortrait = ImageManager.drawableToBitmap(context
					.getResources().getDrawable(R.drawable.default_landscape));
		}
		return defaultPortrait;
	}

	public static Bitmap getDefaultPortrait(Context context) {
		if (defaultLandscape == null) {
			defaultLandscape = ImageManager.drawableToBitmap(context
					.getResources().getDrawable(R.drawable.default_portrait));
		}
		return defaultLandscape;

	}

	public static DisplayImageOptions getImageOptionsPortrait() {
		if (image_options_portrait == null) {
			image_options_portrait = new DisplayImageOptions.Builder()
					.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
					.bitmapConfig(Bitmap.Config.RGB_565)
					.displayer(new FadeInBitmapDisplayer(300))
					.cacheOnDisk(true).considerExifParams(true).build();
		}
		return image_options_portrait;
	}

	/**
	 * 加载本地图片
	 *
	 * @param url
	 * @return
	 */
	public static Bitmap getLoacalBitmap(String url) {
		try {
			FileInputStream fis = new FileInputStream(url);
			return BitmapFactory.decodeStream(fis);
		} catch (FileNotFoundException e) {
			Utils.sendClientException(e);
			return null;
		}
	}

	public static DisplayImageOptions getOptionsLandscape() {
		if (options_landscape == null) {
			options_landscape = new DisplayImageOptions.Builder()
					.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
					.bitmapConfig(Bitmap.Config.RGB_565)
					.showImageOnLoading(R.drawable.default_landscape)
					.showImageForEmptyUri(R.drawable.default_landscape)
					.cacheOnDisk(true)
					.displayer(new FadeInBitmapDisplayer(300))
					.showImageOnFail(R.drawable.default_landscape)
					.considerExifParams(true).build();
		}
		return options_landscape;
	}

	public static DisplayImageOptions getOptionsPortrait() {
		if (options_portrait == null) {
			options_portrait = new DisplayImageOptions.Builder()
					.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
					.bitmapConfig(Bitmap.Config.RGB_565)
					.showImageOnLoading(R.drawable.default_portrait)
					.showImageForEmptyUri(R.drawable.default_portrait)
					.showImageOnFail(R.drawable.default_portrait)
					.displayer(new FadeInBitmapDisplayer(300))
					.cacheOnDisk(true).considerExifParams(true).build();
		}
		return options_portrait;
	}

	public static DisplayImageOptions getProfileOptionsPortrait() {
		if (profile_options_landscape == null) {
			profile_options_landscape = new DisplayImageOptions.Builder()
					.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
					.bitmapConfig(Bitmap.Config.RGB_565)
					.showImageOnFail(R.drawable.profile_default_bg)
					.displayer(new FadeInBitmapDisplayer(300))
					.cacheOnDisk(true).considerExifParams(true).build();
		}
		return profile_options_landscape;
	}

	public static DisplayImageOptions getProfileThumbOptionsPortrait() {
		if (profile_thumb_options_portrait == null) {
			profile_thumb_options_portrait = new DisplayImageOptions.Builder()
					.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
					.bitmapConfig(Bitmap.Config.RGB_565)
					.showImageOnFail(R.drawable.default_portrait)
					.displayer(new FadeInBitmapDisplayer(300))
					.cacheOnDisk(true).considerExifParams(true).build();
		}
		return profile_thumb_options_portrait;
	}

	public static String getRealPathFromURI(Activity activity, Uri contentUri) {
		String[] proj = {MediaColumns.DATA};
		Cursor cursor = activity.managedQuery(contentUri, proj, null, null,
				null);
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	/**
	 * 将Bitmap写入本地缓存文件.
	 *
	 * @param bitmap
	 * @param quality
	 * @param outputStream
	 * @param file         URL/PATH
	 */
	private static void writeBitmap(Bitmap bitmap, int quality,
	                                OutputStream outputStream) {
		if (bitmap == null) {
			if (BuildConfig.DEBUG)
				Log.w(TAG, "Can't write file. Bitmap is null.");
			return;
		}

		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(outputStream);
			bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos); // PNG
		} finally {
			try {
				if (bos != null) {
					bitmap.recycle();
					bos.flush();
					bos.close();
				}
				// bitmap.recycle();
			} catch (IOException e) {
				if (BuildConfig.DEBUG)
					Log.e(TAG, "Could not close file.");
				Utils.sendClientException(e);
			}
		}
	}

	private static Bitmap writeToRoundCorner(Bitmap bitmap, int pixels) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

	public static ImageLoader imageLoader = ImageLoader.getInstance();

	private static DisplayImageOptions options_portrait;

	private static DisplayImageOptions options_landscape;

	private static DisplayImageOptions image_options_portrait;

	private static DisplayImageOptions profile_options_landscape;

	private static DisplayImageOptions profile_thumb_options_portrait;

	public static final int IMAGE_MAX_SIZE_WIFI = 1920;

	private static final int IMAGE_MAX_SIZE_3G = 1280;

	private final static String TAG = "ImageManager";

	private static Bitmap defaultLandscape;

	private static Bitmap defaultPortrait;

	public ImageManager(Context context) {
		// 图片相关
	}

	/**
	 * 扫描、刷新相册
	 */
	public void scanPhotos(String filePath) {
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		Uri uri = Uri.fromFile(new File(filePath));
		intent.setData(uri);
		App.mContext.sendBroadcast(intent);
	}

	public void setContext(Context context) {
	}
}