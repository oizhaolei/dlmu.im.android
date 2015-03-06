package com.ruptech.chinatalk.test;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.ruptech.chinatalk.ui.story.UserStoryImageFilterActivity;
import com.ruptech.chinatalk.ui.story.UserStoryImageFilterActivity.FilterList;
import com.ruptech.chinatalk.ui.story.UserStoryImageFilterActivity.FilterType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

public class ImageFilterTestCase extends InstrumentationTestCase {

	private static final String TAG = "ImageFilterTestCase";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testOnPictureSaved2() {
		GPUImage mGPUImage = new GPUImage(this.getInstrumentation()
				.getContext());
		AssetManager assetManager = this.getInstrumentation().getContext()
				.getAssets();
		InputStream istr;
		Bitmap bitmap = null;
		try {
			istr = assetManager.open("sample_photo.jpg");
			bitmap = BitmapFactory.decodeStream(istr);

			FilterList filters = UserStoryImageFilterActivity.getFilterList();
			for (int index = 0; index < filters.names.size(); index++) {
				String name = filters.names.get(index);
				FilterType type = filters.filters.get(index);

				GPUImageFilter filter = UserStoryImageFilterActivity
						.createFilterForType(this.getInstrumentation()
								.getContext(), type);
				mGPUImage.setFilter(filter);
				mGPUImage.requestRender();

				final String fileName = name + ".jpg";

				Bitmap filterdBitmap = mGPUImage
						.getBitmapWithFilterApplied(bitmap);
				File myDir = new File(
						Environment.getExternalStorageDirectory(), "filters");
				if (!myDir.exists())
					myDir.mkdirs();

				File file = new File(myDir, fileName);
				Log.i(TAG, file.getAbsolutePath());
				FileOutputStream out = new FileOutputStream(file);
				filterdBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
				out.flush();
				out.close();

				if (filterdBitmap != null) {
					filterdBitmap.recycle();
					filterdBitmap = null;
				}

			}
			if (bitmap != null) {
				bitmap.recycle();
				bitmap = null;
			}


		} catch (IOException e) {
			fail("Eexception = " + e.getMessage());
		}

	}
}
