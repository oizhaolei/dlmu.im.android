package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class ImageProgressBar extends ProgressBar {
	private String text;
	private Paint mPaint;
	private int paintColor = Color.GRAY;

	public ImageProgressBar(Context context) {
		super(context);
		initText(paintColor);
	}

	public ImageProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initText(paintColor);
	}

	public ImageProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initText(paintColor);
	}

	// 初始化，画笔
	public void initText(int color) {
		this.mPaint = new Paint();
		this.mPaint.setColor(color);

	}

	Rect rect = new Rect();
	@Override
	protected synchronized void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// this.setText();
		this.mPaint.getTextBounds(this.text, 0, this.text.length(), rect);
		int x = (getWidth() / 2) - rect.centerX();
		int y = (getHeight() / 2) - rect.centerY();
		canvas.drawText(this.text, x, y, this.mPaint);
	}

	public void setPaintColor(int color) {
		paintColor = color;
	}

	@Override
	public synchronized void setProgress(int progress) {
		setText(progress);
		super.setProgress(progress);

	}

	// 设置文字内容
	private void setText(int progress) {
		int i = (progress * 100) / this.getMax();
		this.text = String.valueOf(i) + "%";
	}

}
