package com.ruptech.chinatalk.utils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import android.content.Context;
import android.os.Environment;

import com.ruptech.chinatalk.App;

/**
 * 对SD卡文件的管理
 *
 * @author ch.linghu
 *
 */
public class FileHelper {
	private static final String BASE_PATH = "tttalk";

	public static File apkToSave() throws IOException {
		File file = new File(FileHelper.getPublicPath(App.mContext),
				"android_tttalk.apk");
		return file;
	}

	/** 清除本应用所有的数据 */
	public static void cleanApplicationData(Context context) {
		// 清理应用文件下文件
		File basePath = new File(Environment.getExternalStorageDirectory(),
				BASE_PATH);
		deleteFilesByDirectory(basePath);
		// 清除本应用内部缓存
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			deleteFilesByDirectory(context.getExternalCacheDir());
		}
	}

	/** 删除某个文件夹下的所有文件 */
	private static void deleteFilesByDirectory(File directory) {
		if (directory != null && directory.exists() && directory.isDirectory()) {
			for (File item : directory.listFiles()) {
				if (item.isDirectory()) {
					deleteFilesByDirectory(item);
				} else {
					item.delete();
				}
			}
		}
	}

	/** 获取缓存大小 */
	public static String getApplicationDataSize(Context context) {
		long size = 0;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File basePath = context.getExternalCacheDir();
			try {
				size += getFileSize(basePath);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		File basePath = new File(Environment.getExternalStorageDirectory(),
				BASE_PATH);
		try {
			size += getFileSize(basePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (size > 0) {
			DecimalFormat df = new DecimalFormat("#.00");
			String fileSizeString = "";
			if (size < 1024) {
				fileSizeString = df.format((double) size) + "B";
			} else if (size < 1048576) {
				fileSizeString = df.format((double) size / 1024) + "K";
			} else if (size < 1073741824) {
				fileSizeString = df.format((double) size / 1048576) + "M";
			} else {
				fileSizeString = df.format((double) size / 1073741824) + "G";
			}
			return fileSizeString;
		}
		return "0K";
	}

	private static long getFileSize(File f) throws Exception// 取得文件夹大小
	{
		long size = 0;
		File flist[] = f.listFiles();
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getFileSize(flist[i]);
			} else {
				size = size + flist[i].length();
			}
		}
		return size;
	}

	public static File getPublicPath(Context context) {
		File basePath = new File(Environment.getExternalStorageDirectory(),
				BASE_PATH);

		basePath.mkdirs();

		if (!basePath.exists() || !basePath.isDirectory()) {
			basePath = new File(context.getExternalFilesDir(null), BASE_PATH);
			basePath.mkdirs();
		}

		if (!basePath.exists() || !basePath.isDirectory()) {
			basePath = context.getDir(BASE_PATH, Context.MODE_PRIVATE);
			basePath.mkdirs();
		}
		if (!basePath.exists() || !basePath.isDirectory()) {
			throw new RuntimeException(String.format("%s is not a directory!",
					basePath.toString()));
		}

		return basePath;
	}
}
