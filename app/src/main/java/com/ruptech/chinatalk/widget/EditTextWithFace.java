package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.EditText;

import com.ruptech.chinatalk.R;

public class EditTextWithFace extends EditText {
	private Drawable faceImgAble;
	private final Context mContext;

	public EditTextWithFace(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public EditTextWithFace(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public EditTextWithFace(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}

	public void clearImg() {
		setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

	private Bitmap getFaceImage() {
		Bitmap bmpOriginal = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_face);
		int width, height;
		height = bmpOriginal.getHeight();
		width = bmpOriginal.getWidth();

		Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
		Canvas c = new Canvas(bm);
		Paint paint = new Paint();
		paint.setAlpha(128);
		c.drawBitmap(bmpOriginal, 0, 0, paint);
		return bm;
	}

	private void init() {
		faceImgAble = new BitmapDrawable(mContext.getResources(),
				getFaceImage());
		setCompoundDrawablesWithIntrinsicBounds(null, null, faceImgAble, null);
	}
}
