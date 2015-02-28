package com.ruptech.chinatalk.widget.processbutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import com.ruptech.chinatalk.R;

public abstract class ProcessButton extends FlatButton {

    /**
     * A {@link android.os.Parcelable} representing the {@link com.dd.processbutton.ProcessButton}'s
     * state.
     */
    public static class SavedState extends BaseSavedState {

        private int mProgress;

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        private SavedState(Parcel in) {
            super(in);
            mProgress = in.readInt();
        }

        public SavedState(Parcelable parcel) {
            super(parcel);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mProgress);
        }
    }
    private int mProgress;
    private int mMaxProgress;

    private int mMinProgress;
    private GradientDrawable mProgressDrawable;
    private GradientDrawable mCompleteDrawable;

    private GradientDrawable mErrorDrawable;
    private CharSequence mLoadingText;
    private CharSequence mCompleteText;

    private CharSequence mErrorText;

    public ProcessButton(Context context) {
        super(context);
        init(context, null);
    }

    public ProcessButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ProcessButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public abstract void drawProgress(Canvas canvas);

    public GradientDrawable getCompleteDrawable() {
        return mCompleteDrawable;
    }

    public CharSequence getCompleteText() {
        return mCompleteText;
    }

    public GradientDrawable getErrorDrawable() {
        return mErrorDrawable;
    }

    public CharSequence getErrorText() {
        return mErrorText;
    }

    public CharSequence getLoadingText() {
        return mLoadingText;
    }

    public int getMaxProgress() {
        return mMaxProgress;
    }

    public int getMinProgress() {
        return mMinProgress;
    }

    public int getProgress() {
        return mProgress;
    }

    public GradientDrawable getProgressDrawable() {
        return mProgressDrawable;
    }

    private void init(Context context, AttributeSet attrs) {
        mMinProgress = 0;
        mMaxProgress = 100;

		mProgressDrawable = (GradientDrawable) getDrawable(R.drawable.rect_btn)
				.mutate();
        mProgressDrawable.setCornerRadius(getCornerRadius());

		mCompleteDrawable = (GradientDrawable) getDrawable(R.drawable.rect_btn)
				.mutate();
        mCompleteDrawable.setCornerRadius(getCornerRadius());

		mErrorDrawable = (GradientDrawable) getDrawable(R.drawable.rect_btn)
				.mutate();
        mErrorDrawable.setCornerRadius(getCornerRadius());

        if (attrs != null) {
            initAttributes(context, attrs);
        }
    }

    private void initAttributes(Context context, AttributeSet attributeSet) {
        TypedArray attr = getTypedArray(context, attributeSet, R.styleable.ProcessButton);

        if (attr == null) {
            return;
        }

        try {
            mLoadingText = attr.getString(R.styleable.ProcessButton_pb_textProgress);
            mCompleteText = attr.getString(R.styleable.ProcessButton_pb_textComplete);
            mErrorText = attr.getString(R.styleable.ProcessButton_pb_textError);

			int purple = getColor(R.color.white_text);
            int colorProgress = attr.getColor(R.styleable.ProcessButton_pb_colorProgress, purple);
            mProgressDrawable.setColor(colorProgress);

			int green = getColor(R.color.add_friend_ok);
            int colorComplete = attr.getColor(R.styleable.ProcessButton_pb_colorComplete, green);
            mCompleteDrawable.setColor(colorComplete);

			int red = getColor(R.color.red);
            int colorError = attr.getColor(R.styleable.ProcessButton_pb_colorError, red);
            mErrorDrawable.setColor(colorError);

        } finally {
            attr.recycle();
        }
    }

    protected void onCompleteState() {
        if(getCompleteText() != null) {
            setText(getCompleteText());
        }
        setBackgroundCompat(getCompleteDrawable());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // progress
        if(mProgress > mMinProgress && mProgress < mMaxProgress) {
            drawProgress(canvas);
        }

        super.onDraw(canvas);
    }

    protected void onErrorState() {
        if(getErrorText() != null) {
            setText(getErrorText());
        }
        setBackgroundCompat(getErrorDrawable());
    }

    protected void onNormalState() {
        if(getNormalText() != null) {
            setText(getNormalText());
        }
        setBackgroundCompat(getNormalDrawable());
    }

    protected void onProgress() {
        if(getLoadingText() != null) {
            setText(getLoadingText());
        }
        setBackgroundCompat(getNormalDrawable());
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            mProgress = savedState.mProgress;
            super.onRestoreInstanceState(savedState.getSuperState());
            setProgress(mProgress);
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.mProgress = mProgress;

        return savedState;
    }

    public void setCompleteDrawable(GradientDrawable completeDrawable) {
        mCompleteDrawable = completeDrawable;
    }

    public void setCompleteText(CharSequence completeText) {
        mCompleteText = completeText;
    }

    public void setErrorDrawable(GradientDrawable errorDrawable) {
        mErrorDrawable = errorDrawable;
    }

    public void setErrorText(CharSequence errorText) {
        mErrorText = errorText;
    }

    public void setLoadingText(CharSequence loadingText) {
        mLoadingText = loadingText;
    }

    public void setProgress(int progress) {
        mProgress = progress;

        if (mProgress == mMinProgress) {
            onNormalState();
        } else if (mProgress == mMaxProgress) {
            onCompleteState();
        } else if (mProgress < mMinProgress){
            onErrorState();
        } else {
            onProgress();
        }

        invalidate();
    }

    public void setProgressDrawable(GradientDrawable progressDrawable) {
        mProgressDrawable = progressDrawable;
    }
}
