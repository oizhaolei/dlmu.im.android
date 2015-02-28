package com.ruptech.chinatalk.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.utils.Utils;

public class LoginGateActivity extends Activity {

	class MyPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}

		@Override
		public void onPageSelected(int position) {
			setCurrentDot(position % mViews.size());
			currentPosition = position;
		}
	}

	/**
	 * 监听手势监听器
	 *
	 */
	class MyTouchListener implements OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				isContinue = false;
				mStartTime = System.currentTimeMillis();
				stopTimer();
				break;
			case MotionEvent.ACTION_UP:
				isContinue = true;
				mStartTime = System.currentTimeMillis();
				stopTimer();
				break;
			}
			return false;
		}
	}

	public class ViewPagerAdapter extends PagerAdapter {
		private final List<View> mViews;
		public ViewPagerAdapter(List<View> mViews) {
			this.mViews = mViews;
		}
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			try {
				position = position % mViews.size();
				container.removeView(mViews.get(position));
			} catch (Exception e) {
			}
		}

		@Override
		public int getCount() {
			return Integer.MAX_VALUE;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			try {
				position = position % mViews.size();
				container.addView(mViews.get(position), 0);
			} catch (Exception e) {

			}

			return mViews.get(position);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

	}

	@InjectView(R.id.viewpager)
	ViewPager mViewPager;
	private ViewPagerAdapter mVpAdapter;

	/**
	 * 存放view的列表
	 */
	private List<View> mViews;

	/**
	 * 底部小点图片
	 */
	private ImageView[] dots;

	/**
	 * 记录当前view的位置
	 */
	private int currentIndex;

	/**
	 * 记录当前自动滑动的状态，true就滑动，false停止滑动
	 */
	private boolean isContinue = false;

	/**
	 * 设置viewpager的初始页面
	 */
	private static final int initPositon = 10000;

	/**
	 * viewpager的当前页面
	 */
	private static int currentPosition = initPositon;

	public static LoginGateActivity instance;

	public static void close() {
		if (instance != null) {
			instance.finish();
			instance = null;
		}
	}

	private View firstPageView;
	private View secondPageView;
	private View thirdPageView;
	private View fourthPageView;
	private View fifthPageView;

	private final int pageSize = 5;

	@InjectView(R.id.activity_login_gate_welcome_dot)
	LinearLayout mLinearLayout;

	private Timer mTimer;

	private TimerTask mTimerTask = null;

	private Long mStartTime = null;

	private static int delay = 2000; // 1s
	private static int period = 2000; // 1s

	private final Handler mHandler = new Handler();

	private boolean checkNetwork() {
		boolean network = Utils.isMobileNetworkAvailible(this)
				|| Utils.isWifiAvailible(this);
		if (!network) {
			Toast.makeText(this,
					R.string.wifi_and_3g_networks_are_not_available,
					Toast.LENGTH_LONG).show();
		}
		return network;
	}

	@OnClick(R.id.activity_login_gate_login_button)
	public void doLoginView(View v) {
		if (!checkNetwork()) {
			return;
		}
		Intent intent = new Intent(this, LoginSignupActivity.class);
		intent.putExtra(LoginSignupActivity.EXTRA_TYPE,
				LoginSignupActivity.EXTRA_TYPE_LOGIN);
		startActivity(intent);
	}

	@OnClick(R.id.activity_login_gate_signup_button)
	public void doSignupView(View v) {
		if (!checkNetwork()) {
			return;
		}
		Intent intent = new Intent(this, LoginSignupActivity.class);
		intent.putExtra(LoginSignupActivity.EXTRA_TYPE,
				LoginSignupActivity.EXTRA_TYPE_SIGNUP);
		startActivity(intent);
	}

	/**
	 * 底部圆点初始化
	 */
	private void initDots() {
		dots = new ImageView[5];
		for (int i = 0; i < 5; i++) {
			dots[i] = (ImageView) mLinearLayout.getChildAt(i);
			dots[i].setEnabled(true);
			dots[i].setTag(i);
		}
		currentIndex = 0;
		dots[currentIndex].setEnabled(false);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		setContentView(R.layout.activity_login_gate);
		ButterKnife.inject(this);
		SplashActivity.close();
		setupCompnents();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		isContinue = false;
		stopTimer();
	}

	@Override
	public void onPause() {
		super.onPause();
		isContinue = false;
		stopTimer();
	}

	@Override
	public void onResume() {
		super.onResume();
		startTimer();
	}

	private void setCurrentDot(int position) {
		if (position < 0 || position > pageSize - 1) {
			return;
		}
		dots[position].setEnabled(false);
		dots[currentIndex].setEnabled(true);
		currentIndex = position;
	}
	private void setupCompnents() {
		mViews = new ArrayList<View>();
		LayoutInflater mLi = LayoutInflater.from(this);
		firstPageView = mLi.inflate(R.layout.activity_login_gate_welcome01,
				null);
		secondPageView = mLi.inflate(R.layout.activity_login_gate_welcome02,
				null);
		thirdPageView = mLi.inflate(R.layout.activity_login_gate_welcome03,
				null);
		fourthPageView = mLi.inflate(R.layout.activity_login_gate_welcome04,
				null);
		fifthPageView = mLi.inflate(R.layout.activity_login_gate_welcome05, null);

		mViews.add(firstPageView);
		mViews.add(secondPageView);
		mViews.add(thirdPageView);
		mViews.add(fourthPageView);
		mViews.add(fifthPageView);

		mVpAdapter = new ViewPagerAdapter(mViews);
		mViewPager.setAdapter(mVpAdapter);
		mViewPager.setCurrentItem(initPositon);
		mViewPager.setOnPageChangeListener(new MyPageChangeListener());
		mViewPager.setOnTouchListener(new MyTouchListener());
		initDots();
	}

	private void startTimer() {
		if (mTimer == null) {
			mTimer = new Timer();
		}

		if (mTimerTask == null) {
			mTimerTask = new TimerTask() {
				@Override
				public void run() {
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							currentPosition++;
							mViewPager.setCurrentItem(currentPosition);
						}
					});
				}
			};
		}

		if (mTimer != null && mTimerTask != null)
			mTimer.schedule(mTimerTask, delay, period);
	}

	private void stopTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}

		if (mTimerTask != null) {
			mTimerTask.cancel();
			mTimerTask = null;
		}

		if (isContinue) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					if (isContinue
							&& (System.currentTimeMillis() - mStartTime >= period)) {
						startTimer();
					}
				}
			}, delay);
		}
	}
}
