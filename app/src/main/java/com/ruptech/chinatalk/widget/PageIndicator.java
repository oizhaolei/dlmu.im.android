package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

import com.ruptech.dlmu.im.R;


public class PageIndicator extends View {

	private float mRadius = 6.0f;
	private float mSpacing = 10.0f;
	private final float spacing;

	private int activeColor = 0xFFFF0000;
	private int inactiveColor = 0xFF7F7F7F;
	private int count = 4;
	private int index = 3;
	private final Paint mPaintInactive = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mPaintActive = new Paint(Paint.ANTI_ALIAS_FLAG);

	public PageIndicator(Context context) {
		super(context);
		spacing = mSpacing + 2 * mRadius;
		initColors();
	}

	public PageIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.PageIndicator);
		activeColor = a.getColor(R.styleable.PageIndicator_active_color,
				activeColor);
		inactiveColor = a.getColor(R.styleable.PageIndicator_inactive_color,
				inactiveColor);
		count = a.getColor(R.styleable.PageIndicator_count, count);
		index = a.getColor(R.styleable.PageIndicator_index, index);
		mRadius = a.getDimension(R.styleable.PageIndicator_radius, mRadius);
		mSpacing = a.getDimension(R.styleable.PageIndicator_spacing, mSpacing);
		spacing = mSpacing + 2 * mRadius;
		a.recycle();

		initColors();
	}

	private void initColors() {

		mPaintInactive.setStyle(Style.FILL);
		mPaintInactive.setColor(inactiveColor);
		mPaintActive.setStyle(Style.FILL);
		mPaintActive.setColor(activeColor);
	}


	private int measureHeight(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		// We were told how big to be
		if (specMode == MeasureSpec.EXACTLY) {
			result = specSize;
		}
		// Measure the height
		else {
			result = (int) (2 * mRadius + getPaddingTop() + getPaddingBottom() + 1);
			// Respect AT_MOST value if that was what is called for by
			// measureSpec
			if (specMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize);
			}
		}
		return result;
	}


	private int measureWidth(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		// We were told how big to be
		if (specMode == MeasureSpec.EXACTLY) {
			result = specSize;
		}
		// Calculate the width according the views count
		else {
			// Remember that spacing is centre-to-centre
			result = (int) (getPaddingLeft() + getPaddingRight()
					+ (2 * mRadius) + (count - 1) * spacing);
			// Respect AT_MOST value if that was what is called for by
			// measureSpec
			if (specMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize);
			}
		}
		return result;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		float centeringOffset = 0;

		int leftPadding = getPaddingLeft();

		// Draw stroked circles
		for (int i = 0; i < count; i++) {
			if (i == index) {
				canvas.drawCircle(leftPadding + mRadius + (i * spacing)
								+ centeringOffset, getPaddingTop() + mRadius, mRadius,
						mPaintActive);
			} else {
				canvas.drawCircle(leftPadding + mRadius + (i * spacing)
								+ centeringOffset, getPaddingTop() + mRadius, mRadius,
						mPaintInactive);
			}
		}

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(measureWidth(widthMeasureSpec),
				measureHeight(heightMeasureSpec));
	}
}
