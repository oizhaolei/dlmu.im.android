package com.ruptech.chinatalk.widget;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;

import com.ruptech.dlmu.im.R;
import com.ruptech.chinatalk.utils.ImageManager;

import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static butterknife.ButterKnife.findById;

public class AlbumListAdapter extends ArrayAdapter<Map<String, String>> {
	class ViewHolder {
		@InjectView(R.id.item_album_grid_image)
		ImageView imageView;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}

	private final Context mContext;
	private final LayoutInflater viewInflater;
	private final int mWidth;

	private Camera mCamera;
	private SurfaceView mPreview;
	private View cameraView;

	private final SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
		                           int height) {

			if (mCamera != null) {
				try {
					Camera.Parameters params = mCamera.getParameters();
					List<Camera.Size> sizes = params.getSupportedPreviewSizes();
					for (int i = 0; i < sizes.size(); i++) {
						Size temp = sizes.get(i);
						if (temp.width == temp.height) {
							params.setPreviewSize(temp.width, temp.height);
							break;
						}
					}
					mCamera.setParameters(params);
				} catch (Exception e) {
					Log.i("TAG", "Set parameter" + e.getMessage());
				}

				mCamera.startPreview();
			}
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				mCamera = Camera.open();
				mCamera.setDisplayOrientation(90);
				mCamera.setPreviewDisplay(mPreview.getHolder());
			} catch (Exception e) {
				// Utils.sendClientException(e);
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {

			releaseCamera();
		}
	};

	public AlbumListAdapter(Context context, int resource) {
		super(context, resource);
		mContext = context;
		Display display = ((Activity) mContext).getWindowManager()
				.getDefaultDisplay();
		mWidth = display.getWidth();
		viewInflater = LayoutInflater.from(getContext());
		this.initCamera();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		return position == 0 ? 0 : 1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			if (position == 0) {
				return cameraView;

			} else {
				convertView = viewInflater.inflate(
						R.layout.item_photo_album_image, parent, false);
				holder = new ViewHolder(convertView);
				convertView.setTag(holder);
				setItemSize(holder.imageView);
				setItemSize(findById(convertView, R.id.item_mask));
			}

		} else {
			holder = (ViewHolder) convertView.getTag();
			if (position == 0) {
				return cameraView;
			}
		}

		if (position != 0) {
			Map<String, String> photoData = getItem(position);
			String path = "file://" + photoData.get("path");

			if (!path.equals(holder.imageView.getTag())) {
				ImageManager.imageLoader.displayImage(path, holder.imageView,
						ImageManager.getOptionsLandscape(), null);
				holder.imageView.setTag(path);
			}

		}

		return convertView;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	private View initCamera() {

		cameraView = viewInflater.inflate(R.layout.item_photo_camera_image,
				null, false);
		mPreview = (SurfaceView) findById(cameraView, R.id.surfaceView1);

		setItemSize(mPreview);
		setItemSize(findById(cameraView, R.id.item_mask));
		mPreview.getHolder().addCallback(callback);
		mPreview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mPreview.getHolder().setKeepScreenOn(true);

		return cameraView;
	}

	public void releaseCamera() {
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}

	}

	public void removeCallback() {
		mPreview.getHolder().removeCallback(callback);
	}

	private void setItemSize(View itemView) {

		LayoutParams imageParams = (LayoutParams) itemView.getLayoutParams();
		imageParams.width = mWidth / 3;
		imageParams.height = mWidth / 3;
		itemView.setLayoutParams(imageParams);
	}
}
