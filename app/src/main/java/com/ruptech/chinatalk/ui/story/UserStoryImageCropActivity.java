package com.ruptech.chinatalk.ui.story;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouch.OnImageViewTouchDoubleTapListener;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.Utils;

@SuppressLint("DefaultLocale")
public class UserStoryImageCropActivity extends ActionBarActivity implements
		OnImageViewTouchDoubleTapListener {

	private final String TAG = Utils.CATEGORY
			+ UserStoryImageCropActivity.class.getSimpleName();
	@InjectView(R.id.activity_crop_image)
	ImageViewTouch mImage;
	private MenuItem saveMenu;
	private File cropedFile;
	private Bitmap imageBitmap;

	private int backgroundColor = Color.WHITE;

	@OnClick({ R.id.activity_crop_image_top, R.id.activity_crop_image_bottom })
	public void changeDisplayType(View view) {
		if (mImage.getScale() == 1) {
			mImage.zoomTo(getCropScale(), 50);
		} else {
			mImage.zoomTo(1, 50);
		}
		((View) mImage.getParent()).invalidate();
	}

	private float getCropScale() {
		float w = imageBitmap.getWidth();
		float h = imageBitmap.getHeight();
		float scale = Math.max(h / w, w / h);
		return scale;
	}

	protected Bitmap decode(BitmapFactory.Options options, String uriStr) {
		Bitmap bitmap = null;
		if (uriStr.startsWith("file://")) {
			File imageFile = new File(uriStr.substring(7));
			try {
			bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(),
					options);
			} catch (Exception e) {

			}
		} else {
			Uri uri = Uri.parse(uriStr);
			try {
				InputStream inputStream;
				if (uri.getScheme().startsWith("http")
						|| uri.getScheme().startsWith("https")) {
					inputStream = new URL(uri.toString()).openStream();
				} else {
					inputStream = getContentResolver().openInputStream(uri);
				}
				bitmap = BitmapFactory.decodeStream(inputStream, null, options);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return bitmap;
	}

	protected int getImageOrientation(String uriStr) {
		if (uriStr.startsWith("file://")) {
			try {
				File imageFile = new File(uriStr.substring(7));
				ExifInterface exif = new ExifInterface(
						imageFile.getAbsolutePath());
				int orientation = exif.getAttributeInt(
						ExifInterface.TAG_ORIENTATION, 1);
				switch (orientation) {
				case ExifInterface.ORIENTATION_NORMAL:
					return 0;
				case ExifInterface.ORIENTATION_ROTATE_90:
					return 90;
				case ExifInterface.ORIENTATION_ROTATE_180:
					return 180;
				case ExifInterface.ORIENTATION_ROTATE_270:
					return 270;
				default:
					return 0;
				}
			} catch (Exception e) {
				return 0;
			}
		} else {
			Uri uri = Uri.parse(uriStr);
			Cursor cursor = getContentResolver()
					.query(uri,
							new String[] { MediaStore.Images.ImageColumns.ORIENTATION },
							null, null, null);

			if (cursor == null || cursor.getCount() != 1) {
				return 0;
			}

			cursor.moveToFirst();
			return cursor.getInt(0);
		}

	}

	private void gotoStoryFilterActivity() {
		removeOldFile();
		cropedFile = saveCropImage();
		if (cropedFile != null) {
			Intent intent = new Intent(this, UserStoryImageFilterActivity.class);
			intent.putExtra(PhotoAlbumActivity.EXTRA_FILE,
					cropedFile.getAbsolutePath());
			if (getIntent().getExtras() != null) {
				intent.putExtra(UserStoryTagActivity.EXTRA_TAG, getIntent()
						.getExtras().getString(UserStoryTagActivity.EXTRA_TAG));
			}
			startActivityForResult(intent,
					PhotoAlbumActivity.EXTRA_ACTIVITY_RESULT_SEND_STORY);
		} else {
			Toast.makeText(this, "Can not crop image!", Toast.LENGTH_SHORT)
					.show();
		}

		saveMenu.setEnabled(true);
	}

	private String handleSendImage(Intent intent) {
		Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		if (imageUri != null) {
			Log.i(TAG, "handleSendImage=" + imageUri.toString());
			if (imageUri.toString().startsWith("content://media/external")
					|| imageUri.toString().startsWith("file://")) {
				return imageUri.toString();
			} else {
				return parseURL(imageUri);
			}
		} else {
			return null;
		}
	}

	private Bitmap loadResizedImage(String uri) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		decode(options, uri);
		int scale = 1;
		while (options.outWidth / scale > ImageManager.IMAGE_MAX_SIZE_WIFI
				|| options.outHeight / scale > ImageManager.IMAGE_MAX_SIZE_WIFI) {
			scale++;
		}

		if (scale < 1) {
			scale = 1;
		}
		options = new BitmapFactory.Options();
		options.inSampleSize = scale;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inPurgeable = true;
		options.inTempStorage = new byte[32 * 1024];
		Bitmap bitmap = decode(options, uri);
		if (bitmap == null) {
			return null;
		}

		bitmap = rotateImage(bitmap, getImageOrientation(uri));

		return bitmap;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			removeOldFile();
			setResult(Activity.RESULT_OK);
			this.finish();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.edit_picture);

		setContentView(R.layout.activity_story_image_crop);
		ButterKnife.inject(this);

		setupComponents();
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		String uri = extras.getString(PhotoAlbumActivity.EXTRA_FILE);

		if (uri != null)
			uri = "file://" + uri;

		String type = intent.getType();
		String action = intent.getAction();
		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if (type.startsWith("image/")) {
				uri = handleSendImage(intent);
			}
		}

		if (uri != null) {
			setImage(uri);
		} else {
			showError();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		int order = 0;

		saveMenu = menu.add(Menu.NONE, Menu.FIRST + order, order++,
				Utils.getPostStep(3, 1, R.string.next));

		saveMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onDestroy() {
		if (imageBitmap != null) {
			imageBitmap.recycle();
			imageBitmap = null;
		}
		super.onDestroy();
	}

	@Override
	public void onDoubleTap() {
		setImageBackground();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		} else if (item.getItemId() == saveMenu.getItemId()) {
			saveMenu.setEnabled(false);
			gotoStoryFilterActivity();
		}
		return true;
	}

	private String parseURL(Uri uri) {

		String host = uri.getHost();
		String path = uri.getPath();
		int port = uri.getPort();
		String mPhotoUrl;
		String specailUrl = praseSpecialUrl(path);
		if (specailUrl != null) {
			mPhotoUrl = specailUrl;
		} else {
			if (port != -1)
				mPhotoUrl = String.format("http://%s%d%s", host, port, path);
			else
				mPhotoUrl = String.format("http://%s%s", host, path);

		}
		Log.i(TAG, "photoUrl=" + mPhotoUrl);
		return mPhotoUrl;

	}

	private String praseSpecialUrl(String url) {
		String result = null;
		String URL_PATTERN[] = { "http://", "https://" };
		for (int i = 0; i < URL_PATTERN.length; i++) {
			String temp = URL_PATTERN[i];
			int index = url.toLowerCase().indexOf(temp.toLowerCase());
			if (index >= 0) {
				result = url.substring(index);
				break;
			}
		}
		return result;
	}

	private void removeOldFile() {
		if (cropedFile != null && cropedFile.exists())
			cropedFile.delete();
	}

	private Bitmap rotateImage(Bitmap bitmap, int orientation) {
		if (bitmap == null) {
			return null;
		}
		Bitmap rotatedBitmap = bitmap;

		if (orientation != 0) {
			Matrix matrix = new Matrix();
			matrix.postRotate(orientation);
			rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
					bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			bitmap.recycle();
			bitmap = null;
		}

		return rotatedBitmap;
	}

	private File saveCropImage() {

		Bitmap bitmap = Bitmap.createBitmap(mImage.getWidth(),
				mImage.getHeight(), Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		mImage.draw(canvas);

		String fileName = String.valueOf(System.currentTimeMillis());

		File saveFile = new File(Utils.getFilterFolder(this), fileName);

		try {
			FileOutputStream out = new FileOutputStream(saveFile);
			bitmap.compress(CompressFormat.JPEG, 90, out);
			out.close();

		} catch (Exception e) {
			Log.i("test", e.getMessage());
			saveFile = null;
		}
		bitmap.recycle();
		bitmap = null;

		return saveFile;

	}

	private void setImage(String uri) {
		imageBitmap = loadResizedImage(uri);
		if (imageBitmap == null) {
			this.finish();
		}
		mImage.setImageBitmap(imageBitmap, getCropMatrix(), -1, 8f);

	}

	private int getScreenWidth() {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int height = displaymetrics.heightPixels;
		int width = displaymetrics.widthPixels;
		return width;
	}

	private Matrix getCropMatrix() {
		Matrix matrix = new Matrix();
		float scale = getCropScale();
		matrix.postScale(scale, scale, getScreenWidth() / 2,
				getScreenWidth() / 2);
		return matrix;
	}

	private void setImageBackground() {
		if (backgroundColor == Color.WHITE)
			backgroundColor = Color.BLACK;
		else
			backgroundColor = Color.WHITE;

		mImage.setBackgroundColor(backgroundColor);
	}

	private void setupComponents() {
		mImage.setDisplayType(DisplayType.FIT_TO_SCREEN);

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);

		int width = size.x;
		if (width > size.y)
			width = size.y;

		android.view.ViewGroup.LayoutParams lp = mImage.getLayoutParams();
		lp.width = width;
		lp.height = width;
		mImage.setLayoutParams(lp);
		mImage.setDoubleTapEnabled(false);
		mImage.setDoubleTapListener(this);
		setImageBackground();
	}

	private void showError() {
		Toast.makeText(this, "Can not load image!", Toast.LENGTH_SHORT).show();
		this.finish();
	}
}
