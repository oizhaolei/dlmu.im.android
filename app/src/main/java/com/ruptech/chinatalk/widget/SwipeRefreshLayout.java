package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;

public class SwipeRefreshLayout extends ViewGroup {
    private static final long RETURN_TO_ORIGINAL_POSITION_TIMEOUT = 300;
    private static final float ACCELERATE_INTERPOLATION_FACTOR = 1.5f;
    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;
    private static final float PROGRESS_BAR_HEIGHT = 4;
    private static final float MAX_SWIPE_DISTANCE_FACTOR = .6f;
    private static final int REFRESH_TRIGGER_DISTANCE = 120;
    private static final int[] LAYOUT_ATTRS = new int[]{android.R.attr.enabled};
    private final SwipeProgressBar mProgressBar; // the thing that shows
    private final int mTouchSlop;
    private final int mMediumAnimationDuration;
    private final int mProgressBarHeight;
    private final DecelerateInterpolator mDecelerateInterpolator;
    private final AccelerateInterpolator mAccelerateInterpolator;
    public boolean isProgressTop = true;
    // progress is going
    private View mTarget; // the content that gets pulled down
    private int mOriginalOffsetTop;
    private OnRefreshListener mListener;
    private MotionEvent mDownEvent;
    private int mFrom;
    private boolean mRefreshing = false;
    private float mDistanceToTriggerSync = -1;
    private float mPrevY;
    private float mFromPercentage = 0;
    private final Animation mShrinkTrigger = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            float percent = mFromPercentage
                    + ((0 - mFromPercentage) * interpolatedTime);
            mProgressBar.setTriggerPercentage(percent);
        }
    };
    private float mCurrPercentage = 0;
    private final AnimationListener mShrinkAnimationListener = new BaseAnimationListener() {
        @Override
        public void onAnimationEnd(Animation animation) {
            mCurrPercentage = 0;
        }
    };
    private int mCurrentTargetOffsetTop;
    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop = 0;
            if (mFrom != mOriginalOffsetTop) {
                targetTop = (mFrom + (int) ((mOriginalOffsetTop - mFrom) * interpolatedTime));
            }
            int offset = targetTop - mTarget.getTop();
            // final int currentTop = mTarget.getTop();

            // if (offset + currentTop < 0) {
            // offset = 0 - currentTop;
            // }

            setTargetOffsetTopAndBottom(offset);
        }
    };
    private final AnimationListener mReturnToStartPositionListener = new BaseAnimationListener() {
        @Override
        public void onAnimationEnd(Animation animation) {
            // Once the target content has returned to its start position, reset
            // the target offset to 0
            mCurrentTargetOffsetTop = 0;
        }
    };
    // Target is returning to its start offset because it was cancelled or a
    // refresh was triggered.
    private boolean mReturningToStart;
    private final Runnable mReturnToStartPosition = new Runnable() {

        @Override
        public void run() {
            mReturningToStart = true;
            animateOffsetToStartPosition(mCurrentTargetOffsetTop
                    + getPaddingTop(), mReturnToStartPositionListener);
        }

    };
    // Cancel the refresh gesture and animate everything back to its original
    // state.
    private final Runnable mCancel = new Runnable() {

        @Override
        public void run() {
            mReturningToStart = true;
            // Timeout fired since the user last moved their finger; animate the
            // trigger to 0 and put the target back at its original position
            if (mProgressBar != null) {
                mFromPercentage = mCurrPercentage;
                mShrinkTrigger.setDuration(mMediumAnimationDuration);
                mShrinkTrigger.setAnimationListener(mShrinkAnimationListener);
                mShrinkTrigger.reset();
                mShrinkTrigger.setInterpolator(mDecelerateInterpolator);
                startAnimation(mShrinkTrigger);
            }
            animateOffsetToStartPosition(mCurrentTargetOffsetTop
                    + getPaddingTop(), mReturnToStartPositionListener);
        }

    };

    /**
     * Simple constructor to use when creating a SwipeRefreshLayout from code.
     *
     * @param context
     */
    public SwipeRefreshLayout(Context context) {
        this(context, null);
    }

    /**
     * Constructor that is called when inflating SwipeRefreshLayout from XML.
     *
     * @param context
     * @param attrs
     */
    public SwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mMediumAnimationDuration = getResources().getInteger(
                android.R.integer.config_mediumAnimTime);

        setWillNotDraw(false);
        mProgressBar = new SwipeProgressBar(this);
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mProgressBarHeight = (int) (metrics.density * PROGRESS_BAR_HEIGHT);
        mDecelerateInterpolator = new DecelerateInterpolator(
                DECELERATE_INTERPOLATION_FACTOR);
        mAccelerateInterpolator = new AccelerateInterpolator(
                ACCELERATE_INTERPOLATION_FACTOR);

        final TypedArray a = context
                .obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        setEnabled(a.getBoolean(0, true));
        a.recycle();
    }

    private void animateOffsetToStartPosition(int from,
                                              AnimationListener listener) {
        mFrom = from;
        mAnimateToStartPosition.reset();
        mAnimateToStartPosition.setDuration(mMediumAnimationDuration);
        mAnimateToStartPosition.setAnimationListener(listener);
        mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
        mTarget.startAnimation(mAnimateToStartPosition);
    }

    /**
     * 检查是否可以上拉,对于版本14以下的暂不支持
     *
     * @return
     */
    public boolean canChildScrollDown() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView
                        .getChildAt(0).getTop() < absListView
                        .getPaddingTop());
            } else {
                return mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, 1);
        }
    }

    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
     */
    public boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView
                        .getChildAt(0).getTop() < absListView
                        .getPaddingTop());
            } else {
                return mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        mProgressBar.draw(canvas);
    }

    private void ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid
        // out yet.
        if (mTarget == null) {
            if (getChildCount() > 1 && !isInEditMode()) {
                throw new IllegalStateException(
                        "SwipeRefreshLayout can host only one direct child");
            }
            mTarget = getChildAt(0);
            mOriginalOffsetTop = mTarget.getTop() + getPaddingTop();
        }
        if (mDistanceToTriggerSync == -1) {
            if (getParent() != null && ((View) getParent()).getHeight() > 0) {
                final DisplayMetrics metrics = getResources()
                        .getDisplayMetrics();
                mDistanceToTriggerSync = (int) Math.min(
                        ((View) getParent()).getHeight()
                                * MAX_SWIPE_DISTANCE_FACTOR,
                        REFRESH_TRIGGER_DISTANCE * metrics.density);
            }
        }
    }

    /**
     * @return Whether the SwipeRefreshWidget is actively showing refresh
     * progress.
     */
    public boolean isRefreshing() {
        return mRefreshing;
    }

    /**
     * Notify the widget that refresh state has changed. Do not call this when
     * refresh is triggered by a swipe gesture.
     *
     * @param refreshing Whether or not the view should show refresh progress.
     */
    public void setRefreshing(boolean refreshing) {
        if (mRefreshing != refreshing) {
            ensureTarget();
            mCurrPercentage = 0;
            mRefreshing = refreshing;
            if (mRefreshing) {
                mProgressBar.start();
            } else {
                mProgressBar.stop();
            }
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        removeCallbacks(mCancel);
        removeCallbacks(mReturnToStartPosition);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mReturnToStartPosition);
        removeCallbacks(mCancel);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();
        boolean handled = false;
        if (mReturningToStart && ev.getAction() == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }
        if (isEnabled() && !mReturningToStart
                && (!canChildScrollUp() || !canChildScrollDown())) {
            handled = onTouchEvent(ev);
        }
        return !handled ? super.onInterceptTouchEvent(ev) : handled;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (this.isProgressTop)
            mProgressBar.setBounds(0, 0, width, mProgressBarHeight);
        else
            mProgressBar.setBounds(0, height - mProgressBarHeight, width,
                    height);

        if (getChildCount() == 0) {
            return;
        }
        final View child = getChildAt(0);
        final int childLeft = getPaddingLeft();
        final int childTop = mCurrentTargetOffsetTop + getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        child.layout(childLeft, childTop, childLeft + childWidth, childTop
                + childHeight);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getChildCount() > 1 && !isInEditMode()) {
            throw new IllegalStateException(
                    "SwipeRefreshLayout can host only one direct child");
        }
        if (getChildCount() > 0) {
            getChildAt(0).measure(
                    MeasureSpec.makeMeasureSpec(getMeasuredWidth()
                                    - getPaddingLeft() - getPaddingRight(),
                            MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getMeasuredHeight()
                                    - getPaddingTop() - getPaddingBottom(),
                            MeasureSpec.EXACTLY));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        boolean handled = false;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mCurrPercentage = 0;
                mDownEvent = MotionEvent.obtain(event);
                mPrevY = mDownEvent.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mDownEvent != null && !mReturningToStart) {
                    final float eventY = event.getY();
                    float yDiff = eventY - mDownEvent.getY();

                    if (canChildScrollUp()) {
                        yDiff = -eventY + mDownEvent.getY();
                    }
                    if (yDiff > mTouchSlop) {
                        // User velocity passed min velocity; trigger a refresh
                        if (yDiff > mDistanceToTriggerSync) {
                            // User movement passed distance; trigger a refresh
                            startRefresh();
                            handled = true;
                            break;
                        } else {
                            // Just track the user's movement
                            setTriggerPercentage(mAccelerateInterpolator
                                    .getInterpolation(yDiff
                                            / mDistanceToTriggerSync));
                            float offsetTop = yDiff;
                            if (mPrevY > eventY) {
                                offsetTop = yDiff - mTouchSlop;
                            }

                            updateContentOffsetTop((int) (offsetTop));
                            // if (mPrevY > eventY && (mTarget.getTop() <
                            // mTouchSlop)) {
                            // // If the user puts the view back at the top, we
                            // // don't need to. This shouldn't be considered
                            // // cancelling the gesture as the user can restart
                            // // from the top.
                            // removeCallbacks(mCancel);
                            // } else {
                            // updatePositionTimeout();
                            // }
                            updatePositionTimeout();
                            mPrevY = event.getY();
                            handled = true;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mDownEvent != null) {
                    mDownEvent.recycle();
                    mDownEvent = null;
                }
                break;
        }
        return handled;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        // Nope.
    }

    /**
     * Set the four colors used in the progress animation. The first color will
     * also be the color of the bar that grows in response to a user swipe
     * gesture.
     *
     * @param colorRes1 Color resource.
     * @param colorRes2 Color resource.
     * @param colorRes3 Color resource.
     * @param colorRes4 Color resource.
     */
    public void setColorScheme(int colorRes1, int colorRes2, int colorRes3,
                               int colorRes4) {
        ensureTarget();
        final Resources res = getResources();
        final int color1 = res.getColor(colorRes1);
        final int color2 = res.getColor(colorRes2);
        final int color3 = res.getColor(colorRes3);
        final int color4 = res.getColor(colorRes4);
        mProgressBar.setColorScheme(color1, color2, color3, color4);
    }

    /**
     * Set the listener to be notified when a refresh is triggered via the swipe
     * gesture.
     */
    public void setOnRefreshListener(OnRefreshListener listener) {
        mListener = listener;
    }

    public void setProgressTop(boolean isTop) {
        this.isProgressTop = isTop;
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (this.isProgressTop)
            mProgressBar.setBounds(0, 0, width, mProgressBarHeight);
        else
            mProgressBar.setBounds(0, height - mProgressBarHeight, width,
                    height);
    }

    private void setTargetOffsetTopAndBottom(int offset) {
        mTarget.offsetTopAndBottom(offset);
        mCurrentTargetOffsetTop = mTarget.getTop();
    }

    private void setTriggerPercentage(float percent) {
        if (percent == 0f) {
            // No-op. A null trigger means it's uninitialized, and setting it to
            // zero-percent
            // means we're trying to reset state, so there's nothing to reset in
            // this case.
            mCurrPercentage = 0;
            return;
        }
        mCurrPercentage = percent;
        mProgressBar.setTriggerPercentage(percent);
    }

    private void startRefresh() {

        removeCallbacks(mCancel);
        mReturnToStartPosition.run();
        setRefreshing(true);
        if (canChildScrollUp()) {
            setProgressTop(false);
            mListener.onRefresh(false);
        } else {
            setProgressTop(true);
            mListener.onRefresh(true);
        }
    }

    private void updateContentOffsetTop(int targetTop) {
        final int currentTop = mTarget.getTop();
        if (targetTop > mDistanceToTriggerSync) {
            targetTop = (int) mDistanceToTriggerSync;
        } else if (targetTop < 0) {
            targetTop = 0;
        }
        if (canChildScrollUp()) {
            this.setProgressTop(false);
            targetTop = -targetTop;
        } else {
            this.setProgressTop(true);
        }
        setTargetOffsetTopAndBottom(targetTop - currentTop);
    }

    private void updatePositionTimeout() {
        removeCallbacks(mCancel);
        postDelayed(mCancel, RETURN_TO_ORIGINAL_POSITION_TIMEOUT);
    }

    /**
     * Classes that wish to be notified when the swipe gesture correctly
     * triggers a refresh should implement this interface.
     */
    public interface OnRefreshListener {
        public void onRefresh(boolean isUp);
    }

    /**
     * Simple AnimationListener to avoid having to implement unneeded methods in
     * AnimationListeners.
     */
    private class BaseAnimationListener implements AnimationListener {
        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }
    }
}
