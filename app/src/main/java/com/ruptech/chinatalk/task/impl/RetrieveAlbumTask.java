package com.ruptech.chinatalk.task.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore.Images.Media;

import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.ui.story.PhotoAlbumActivity;

public class RetrieveAlbumTask extends GenericTask {

	private List<Map<String, String>> mPhotoList;
	private final ContentResolver cr;
	private String lastImageId;

	public RetrieveAlbumTask(ContentResolver cr, String lastImageId) {
		super();
		this.cr = cr;
		this.lastImageId = lastImageId;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		mPhotoList = new ArrayList<Map<String, String>>();

		String[] projection = { Media.DATA, Media._ID };
		String selection = null;
		String selectionArgs[] = null;
		String sortOrder = String.format("%s DESC LIMIT %d", Media._ID,
				PhotoAlbumActivity.IMAGE_COUNT_PER_PAGE);
		if (lastImageId != null) {
			selection = Media._ID + "<?";
			selectionArgs = new String[] { lastImageId };
		}
		Cursor imageCursor = cr.query(Media.EXTERNAL_CONTENT_URI, projection,
				selection, selectionArgs, sortOrder);

		if (imageCursor != null && imageCursor.moveToFirst()) {
			String image_path;
			int dataColumn = imageCursor.getColumnIndex(Media.DATA);
			int idColumn = imageCursor.getColumnIndex(Media._ID);
			do {
				image_path = imageCursor.getString(dataColumn);
				lastImageId = imageCursor.getString(idColumn);

				final Map<String, String> photoData = new HashMap<String, String>();
				photoData.put("path", image_path);

				mPhotoList.add(photoData);

			} while (imageCursor.moveToNext());
			imageCursor.close();
		}
		return TaskResult.OK;
	}

	public String getLastImageId(){
		return this.lastImageId;
	}

	public List<Map<String, String>> getPhotoList() {
		return mPhotoList;
	}
}