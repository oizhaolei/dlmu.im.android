package com.ruptech.chinatalk.test;

import android.content.res.AssetManager;
import android.os.Environment;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.impl.FileUploadTask.FileUploadInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class Http2ServerTestCase extends InstrumentationTestCase {

	private AssetManager assetManager;

	private final boolean isTestDeletedUser = true;

	private File copyFileToSDCard(String assetFileName){
		String filePath = Environment.getExternalStorageDirectory().toString()
				+ File.separator + assetFileName;

		File file = new File(filePath);
		if (file.exists())
			return file;

		FileOutputStream fos;
		try {
			InputStream ins = assetManager.open(assetFileName);
			byte[] buffer = new byte[ins.available()];
			ins.read(buffer);
			ins.close();

			fos = new FileOutputStream(filePath);
			fos.write(buffer);
			fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		file = new File(filePath);
		return file;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Thread.sleep(1000);
		assetManager = this.getInstrumentation().getContext().getAssets();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testUploadPhoto() throws Exception {
		HttpStoryServerTestCase.setTestUserType(isTestDeletedUser);

		File file = copyFileToSDCard("sample_photo.jpg");
		try {
			FileUploadInfo info = App.getHttpStoryServer().uploadFile(file, null);

			Log.i("test", info.fileName);
			HttpStoryServerTestCase
			.assertDeletedUserResponse(isTestDeletedUser);
		} catch (Exception e) {
			HttpStoryServerTestCase.assertDeletedUserException(
					isTestDeletedUser, e);
		}
	}


	public void testUploadSound() throws Exception {
		HttpStoryServerTestCase.setTestUserType(isTestDeletedUser);

		File file = copyFileToSDCard("sample_sound.amr");
		try {
			FileUploadInfo info = App.getHttpStoryServer().uploadSound(file, null);

			Log.i("test", info.fileName);
			HttpStoryServerTestCase
			.assertDeletedUserResponse(isTestDeletedUser);
		} catch (Exception e) {
			HttpStoryServerTestCase.assertDeletedUserException(
					isTestDeletedUser, e);
		}
	}
}
