package com.ruptech.chinatalk.widget;

import static butterknife.ButterKnife.findById;

import java.util.List;
import java.util.Map;
import java.util.Random;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.ui.setting.SettingQaActivity;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
public class GuideViewManager {

	private static final float yOffsetMax = 50;
	private static final int IDLE_MILLIS = 5000;

	private final Activity activity;
	private View guideView;
	private Thread autoGotoThread;

	private long lastTouchMillis;
	private float totalHeightDiff = 0;
	private boolean isAutoUp = false;
	private boolean isStopThread = false;

	private final AnimatorListener animationListener = new AnimatorListener() {

		@Override
		public void onAnimationCancel(Animator animation) {

		}

		@Override
		public void onAnimationEnd(Animator animation) {
			removeGuide();
		}

		@Override
		public void onAnimationRepeat(Animator animation) {

		}

		@Override
		public void onAnimationStart(Animator animation) {
			isStopThread = true;
		}

	};

	private final OnTouchListener listener = new View.OnTouchListener() {
		/* Starting Y point (where touch started). */
		float yStart = 0;

		/*
		 * The last y touch that occurred. This is used to determine if the view
		 * should snap up or down on release. Used in conjunction with
		 * directionDown boolean.
		 */
		float lastY = 0;
		boolean directionDown = false;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			lastTouchMillis = System.currentTimeMillis();
			switch (event.getAction()) {

			/* User tapped down on screen. */
			case MotionEvent.ACTION_DOWN:
				// User has tapped the screen
				yStart = event.getRawY();
				lastY = event.getRawY();
				break;

			/* User is dragging finger. */
			case MotionEvent.ACTION_MOVE:

				// Calculate the total height change thus far.
				float diff = event.getRawY() - yStart;
				if (diff < 0) {
					totalHeightDiff = diff;
					guideView.setTranslationY(totalHeightDiff);
				}

				// Check and set which direction drag is moving.
				if (event.getRawY() > lastY) {
					directionDown = true;
				} else {
					directionDown = false;
				}

				// Set the lastY for comparison in the next ACTION_MOVE
				// event.
				lastY = event.getRawY();
				break;

			/* User lifted up finger. */
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				startAnimation(directionDown);
				break;

			}
			return true;
		}

	};

	final Runnable gotoRunnable = new Runnable() {

		@Override
		public void run() {
			long interval = System.currentTimeMillis() - lastTouchMillis;
			while (interval < IDLE_MILLIS) {
				if (isStopThread)
					return;

				interval = System.currentTimeMillis() - lastTouchMillis;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Utils.sendClientException(e);
				}
			}
			if (isStopThread)
				return;
			guideView.setOnTouchListener(null);
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					isAutoUp = true;
					startAnimation(false);
				}
			});

		}
	};

	public GuideViewManager(Activity activity) {
		this.activity = activity;
	}

	private void removeGuide() {
		if (guideView != null && guideView.getParent() != null)
			((ViewGroup) guideView.getParent()).removeView(guideView);
	}

	public void showGuideView() {

		List<Map<String, String>> qaList = PrefUtils.readOpenedQA();
		if (qaList == null || qaList.size() == 0)
			return;

		lastTouchMillis = System.currentTimeMillis();
		LayoutInflater viewInflater = LayoutInflater.from(activity);
		View rootView = activity.getWindow().getDecorView().getRootView();
		viewInflater.inflate(R.layout.activity_guide, (ViewGroup) rootView);
		guideView = findById(rootView, R.id.activity_splash_layout);

		guideView.setOnTouchListener(listener);

		autoGotoThread = new Thread(gotoRunnable);
		autoGotoThread.start();

		Random rd = new Random();

		int size = qaList.size();
		Map<String, String> qa = qaList.get(rd.nextInt(size));
		String question = qa.get("question");
		question = String.format("<u>%s</u>", Utils.htmlSpecialChars(question));
		final String question_id = qa.get("id");
		TextView mQuestionTextView = (TextView) findById(guideView,
				R.id.guide_question_textview);
		mQuestionTextView.setText(Html.fromHtml(question));
		mQuestionTextView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, SettingQaActivity.class);
				intent.putExtra(SettingQaActivity.EXTRA_QA_ID, question_id);
				activity.startActivity(intent);
				isStopThread = true;
				removeGuide();
			}

		});
	}

	private void startAnimation(boolean directionDown) {
		ObjectAnimator slide;
		if (!directionDown && (totalHeightDiff < -yOffsetMax || isAutoUp)) {
			slide = ObjectAnimator.ofFloat(guideView, "y", totalHeightDiff,
					-guideView.getHeight()).setDuration(300);
			slide.addListener(animationListener);
			slide.setInterpolator(new OvershootInterpolator(1));
		} else {

			slide = ObjectAnimator.ofFloat(guideView, "y", totalHeightDiff, 0)
					.setDuration(1000);
			slide.setInterpolator(new BounceInterpolator());

		}

		slide.start();
	}
}
