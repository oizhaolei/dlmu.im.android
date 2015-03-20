package com.ruptech.chinatalk.sqlite;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.ruptech.chinatalk.App;

public class UserProvider extends ContentProvider {

	private static final String AUTHORITY = "com.ruptech.dlmu.im.userprovider";

	private static final String BASE_PATH = "users";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
	                    String[] selectionArgs, String sortOrder) {

		if (selectionArgs.length >= 2) {
			long id = Long.parseLong(selectionArgs[0]);
			return App.userDAO.fetchChats(id, selectionArgs[1]);
		} else {
			return null;
		}

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
	                  String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
