/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ruptech.chinatalk.ui.story;

import static butterknife.ButterKnife.findById;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImage.OnPictureSavedListener;
import jp.co.cyberagent.android.gpuimage.GPUImage3x3ConvolutionFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageColorInvertFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageDirectionalSobelEdgeDetectionFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageEmbossFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageExposureFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageGammaFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageHighlightShadowFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageHueFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageMonochromeFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageOpacityFilter;
import jp.co.cyberagent.android.gpuimage.GPUImagePixelationFilter;
import jp.co.cyberagent.android.gpuimage.GPUImagePosterizeFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageRGBFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSaturationFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSharpenFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSobelEdgeDetection;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.GPUImageVignetteFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageWhiteBalanceFilter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.utils.Utils;

public class UserStoryImageFilterActivity extends ActionBarActivity implements
		OnPictureSavedListener {
	public static class FilterList {
		public List<String> names = new LinkedList<String>();
		public List<FilterType> filters = new LinkedList<FilterType>();

		public void addFilter(final String name, final FilterType filter) {
			names.add(name);
			filters.add(filter);
		}
	}

	public enum FilterType {
		NOFILTER, CONTRAST, AUTUMN, RED, PINK, GRAYSCALE, SHARPEN, SEPIA, SOBEL_EDGE_DETECTION, THREE_X_THREE_CONVOLUTION, FILTER_GROUP, EMBOSS, POSTERIZE, GAMMA, BRIGHTNESS, INVERT, HUE, PIXELATION, SATURATION, EXPOSURE, HIGHLIGHT_SHADOW, MONOCHROME, OPACITY, RGB, WHITE_BALANCE, VIGNETTE
	}

	public static GPUImageFilter createFilterForType(final Context context,
			final FilterType type) {

		switch (type) {
		case CONTRAST:
			return new GPUImageContrastFilter(1.1f);// [0,2];
		case GAMMA:
			return new GPUImageGammaFilter(2.0f);// [0,3]
		case INVERT:
			return new GPUImageColorInvertFilter();
		case PIXELATION:
			GPUImagePixelationFilter filter = new GPUImagePixelationFilter();
			filter.setPixel(10); // [1,100]
			return filter;
		case HUE:
			return new GPUImageHueFilter(90.0f);// [0, 360]
		case BRIGHTNESS:
			return new GPUImageBrightnessFilter(0.5f);// [-1.0f, 1.0f]
		case GRAYSCALE:
			return new GPUImageGrayscaleFilter();
		case SEPIA:
			return new GPUImageSepiaFilter();// [0.0f, 2.0f]
		case SHARPEN:
			GPUImageSharpenFilter sharpness = new GPUImageSharpenFilter();
			sharpness.setSharpness(2.0f);// [-4.0, 4.0]
			return sharpness;
		case SOBEL_EDGE_DETECTION:
			GPUImageSobelEdgeDetection detection = new GPUImageSobelEdgeDetection();
			detection.setLineSize(1.0f);// [0.0f, 5.0f]
			return detection;
		case THREE_X_THREE_CONVOLUTION:
			GPUImage3x3ConvolutionFilter convolution = new GPUImage3x3ConvolutionFilter();
			convolution.setConvolutionKernel(new float[] { -1.0f, 0.0f, 1.0f,
					-2.0f, 0.0f, 2.0f, -1.0f, 0.0f, 1.0f });
			return convolution;
		case EMBOSS:
			GPUImageEmbossFilter embossFilter = new GPUImageEmbossFilter();
			embossFilter.setIntensity(1.0f);// [0.0f, 4.0f]
			return embossFilter;
		case POSTERIZE:
			GPUImagePosterizeFilter posterize = new GPUImagePosterizeFilter();
			posterize.setColorLevels(10);// [1, 50]
			return posterize;
		case SATURATION:
			return new GPUImageSaturationFilter(2.0f);// [0, 2.0f]
		case EXPOSURE:
			return new GPUImageExposureFilter(0.5f);// [-10.0f, 10.0f]
		case HIGHLIGHT_SHADOW:
			GPUImageHighlightShadowFilter shadow = new GPUImageHighlightShadowFilter(
					0.0f, 1.0f);
			shadow.setShadows(0.5f);// [0.0f, 1.0f]
			shadow.setHighlights(0.5f);// [0.0f, 1.0f]
			return shadow;
		case MONOCHROME:
			return new GPUImageMonochromeFilter(1.0f, new float[] { 0.6f,
					0.45f, 0.3f, 1.0f });
		case OPACITY:
			return new GPUImageOpacityFilter(0.5f);// [0.0f, 1.0f];
		case AUTUMN:
			GPUImageRGBFilter autumnFilter = new GPUImageRGBFilter(1.0f, 1.0f,
					1.0f);
			autumnFilter.setRed(1.0f);// [0.0f, 1.0f];
			autumnFilter.setGreen(0.5f);// [0.0f, 1.0f];
			autumnFilter.setBlue(0.5f);// [0.0f, 1.0f];
			return autumnFilter;
		case RED:
			GPUImageRGBFilter redFilter = new GPUImageRGBFilter(1.0f, 1.0f,
					1.0f);
			redFilter.setRed(1.2f);// [0.0f, 1.0f];
			redFilter.setGreen(1.0f);// [0.0f, 1.0f];
			redFilter.setBlue(1.0f);// [0.0f, 1.0f];
			return redFilter;
		case PINK:
			GPUImageRGBFilter pinkFilter = new GPUImageRGBFilter(1.0f, 1.0f,
					1.0f);
			pinkFilter.setRed(1.0f);// [0.0f, 1.0f];
			pinkFilter.setGreen(0.8f);// [0.0f, 1.0f];
			pinkFilter.setBlue(1.0f);// [0.0f, 1.0f];
			return pinkFilter;
		case WHITE_BALANCE:
			GPUImageWhiteBalanceFilter balance = new GPUImageWhiteBalanceFilter(
					5000.0f, 0.0f);
			balance.setTemperature(5000);// [2000.0f, 8000.0f]
			balance.setTint(10);// [-100.0f, 100.0f]
			return balance;
		case VIGNETTE:
			GPUImageVignetteFilter vignette = new GPUImageVignetteFilter();
			PointF centerPoint = new PointF();
			centerPoint.x = 0.5f;
			centerPoint.y = 0.5f;
			vignette.setVignetteCenter(centerPoint);
			vignette.setVignetteColor(new float[] { 0.1f, 0.1f, 0.1f });
			vignette.setVignetteStart(0.2f);
			vignette.setVignetteEnd(0.7f);
			return vignette;
		case FILTER_GROUP:
			List<GPUImageFilter> filters = new LinkedList<GPUImageFilter>();
			filters.add(new GPUImageContrastFilter());
			filters.add(new GPUImageDirectionalSobelEdgeDetectionFilter());
			filters.add(new GPUImageGrayscaleFilter());
			return new GPUImageFilterGroup(filters);
		case NOFILTER:
			return new GPUImageOpacityFilter(1.0f);// [0.0f, 1.0f];
		default:
			throw new IllegalStateException("No filter of that type!");

		}
	}

	public static FilterList getFilterList() {
		FilterList filters = new FilterList();
		filters.addFilter("original", FilterType.NOFILTER);
		filters.addFilter("white", FilterType.EXPOSURE);
		filters.addFilter("red", FilterType.RED);
		filters.addFilter("pink", FilterType.PINK);
		filters.addFilter("autumn", FilterType.VIGNETTE);
		filters.addFilter("contrast", FilterType.CONTRAST);
		filters.addFilter("vibrance", FilterType.SATURATION);

		return filters;
	}

	public static int getFilterName(String filter) {
		filter = "filter_" + filter;
		String packageName = App.mContext.getPackageName();
		int identifier = App.mContext.getResources().getIdentifier(filter,
				"string", packageName);

		return identifier;
	}

	public static int getFilterSample(String filter) {
		filter = "filter_" + filter;
		String packageName = App.mContext.getPackageName();
		int identifier = App.mContext.getResources().getIdentifier(filter,
				"drawable", packageName);
		return identifier;
	}

	private final String TAG = Utils.CATEGORY
			+ UserStoryImageFilterActivity.class.getSimpleName();
	private MenuItem saveMenu;

	private GPUImageFilter mFilter;

	@InjectView(R.id.gpuimage)
	GPUImageView mGPUImageView;
	private View previousFilterView;

	Bitmap bitmap;

	@InjectView(R.id.filer_scrollview)
	LinearLayout scrollView;

	private void gotoStorySaveActivity(String imageFile) {

		Intent intent = new Intent(this, UserStorySaveActivity.class);
		if (UserStorySaveActivity.ACTION_COMMENT_UPLOAD.equals(this.getIntent()
				.getAction())) {
			intent.setAction(UserStorySaveActivity.ACTION_COMMENT_UPLOAD);
			intent.putExtras(this.getIntent());
		}

		if (getIntent().getExtras() != null) {
			intent.putExtra(UserStoryTagActivity.EXTRA_TAG, getIntent()
					.getExtras().getString(UserStoryTagActivity.EXTRA_TAG));
		}
		intent.putExtra(PhotoAlbumActivity.EXTRA_FILE, imageFile);
		startActivityForResult(intent,
				PhotoAlbumActivity.EXTRA_ACTIVITY_RESULT_SEND_STORY);
	}

	private void handleUri(String uriStr) {
		if (uriStr != null) {
			// bitmap = ImageManager.imageLoader.loadImageSync(uriStr);
			// mGPUImageView.setImage(bitmap);
			Uri uri = Uri.parse(uriStr);
			if ("file".equals(uri.getScheme())) {
				String path = uriStr.substring(7);
				mGPUImageView.setImage(new File(path));
			} else {
				mGPUImageView.setImage(uri);
			}

		} else {
			Toast.makeText(this, "Can not load image!", Toast.LENGTH_SHORT)
					.show();
			this.finish();
		}
	}

	private void initFilterList() {
		final FilterList filters = getFilterList();
		LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View cell;
		for (int i = 0; i < filters.names.size(); i++) {
			String filterName = filters.names.get(i);
			final FilterType type = filters.filters.get(i);

			cell = mInflater.inflate(R.layout.item_story_image_filter,
					scrollView, false);
			ImageView image = (ImageView) findById(cell, R.id.filter_imageview);
			image.setImageResource(getFilterSample(filterName));

			TextView title = (TextView) findById(cell, R.id.filter_textview);
			title.setText(getFilterName(filterName));
			cell.setTag(type);
			scrollView.addView(cell);
			cell.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (previousFilterView != null)
						setChecked(previousFilterView, false);

					setChecked(v, true);

					GPUImageFilter filter = createFilterForType(
							UserStoryImageFilterActivity.this, type);
					switchFilterTo(filter);
					mGPUImageView.requestRender();
				}

			});

			if (i == 0) {
				setChecked(cell, true);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			setResult(Activity.RESULT_OK);
			this.finish();
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_story_image_filter);
		ButterKnife.inject(this);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.edit_picture);
		mGPUImageView.setScaleType(GPUImage.ScaleType.CENTER_INSIDE);
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		String uri = extras.getString(PhotoAlbumActivity.EXTRA_FILE);

		if (uri != null)
			uri = "file://" + uri;
		handleUri(uri);
		initFilterList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		int order = 0;

		saveMenu = menu.add(Menu.NONE, Menu.FIRST + order, order++,
				Utils.getPostStep(3, 2, R.string.next));

		saveMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onDestroy() {
		bitmap = null;
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		} else if (item.getItemId() == saveMenu.getItemId()) {
			if (this.mGPUImageView.isValid()) {
				saveMenu.setEnabled(false);
				saveImage();
			}
		}
		return true;
	}

	@Override
	public void onPictureSaved(String path) {
		saveMenu.setEnabled(true);
		gotoStorySaveActivity(path);
	}

	private void saveImage() {
		String fileName = String.valueOf(System.currentTimeMillis());
		mGPUImageView.saveToPictures(Utils.getFilterFolder(this)
				.getAbsolutePath(), fileName, this);
	}

	private void setChecked(View v, boolean isChecked) {
		View checkView = findById(v, R.id.filter_checked);
		if (isChecked) {
			checkView.setVisibility(View.VISIBLE);
			previousFilterView = v;
		} else {
			checkView.setVisibility(View.INVISIBLE);
		}

	}

	private void switchFilterTo(final GPUImageFilter filter) {
		if (mFilter == null || filter != null) {
			mFilter = filter;
			mGPUImageView.setFilter(mFilter);
		}
	}
}
