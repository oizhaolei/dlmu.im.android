package com.ruptech.chinatalk.ui.gift;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Gift;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.GiftDonateTask;
import com.ruptech.chinatalk.ui.user.MyWalletActivity;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.Utils;

import java.text.NumberFormat;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class GiftDonateActivity extends ActionBarActivity {

	public static final String EXTRA_TO_USER_ID = "EXTRA_TO_USER_ID";
	public static final String EXTRA_TO_USER_PHOTO_ID = "EXTRA_TO_USER_PHOTO_ID";
	public static final String EXTRA_GIFT = "EXTRA_GIFT";

	private final String TAG = Utils.CATEGORY
			+ GiftDonateActivity.class.getSimpleName();

	@InjectView(R.id.activity_gift_donate_thumb_imageview)
	ImageView giftThumbImageView;
	@InjectView(R.id.activity_gift_donate_cost_textview)
	TextView giftCostTextView;
	@InjectView(R.id.activity_gift_donate_title_textview)
	TextView giftTitleTextView;
	@InjectView(R.id.activity_gift_donate_charm_textview)
	TextView giftCharmTextView;
	@InjectView(R.id.activity_gift_donate_quantity_edittext)
	EditText giftQuantity;
	@InjectView(R.id.activity_gift_donate_quantity_plus)
	ImageView giftPlusButton;
	@InjectView(R.id.activity_gift_donate_quantity_minus)
	ImageView giftMinusButton;

	@InjectView(R.id.activity_gift_donate_balance_textview)
	TextView gifttBalanceTextView;

	private long mToUserId;
	private long mToPhotoId;
	private Gift mGift;
	private static GiftDonateActivity instance = null;

	private Gift getGiftFromExtras() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			Gift gift = (Gift) extras.get(EXTRA_GIFT);
			return gift;
		}
		return null;
	}

	private long getToPhototIdFromExtras() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			long userId = extras.getLong(EXTRA_TO_USER_PHOTO_ID);
			return userId;
		}
		return 0;
	}

	private long getToUserIdFromExtras() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			long userId = extras.getLong(EXTRA_TO_USER_ID);
			return userId;
		}
		return 0;
	}

	public void giftDonateButton(View v) {
		final int mQuantity = Integer
				.valueOf(giftQuantity.getText().toString());
		if (mGift.getCost_point() * mQuantity > App.readUser().getBalance()) {
			Toast.makeText(this,
					getString(R.string.no_money_cannot_gift_donate),
					Toast.LENGTH_LONG).show();
			return;
		}

		GiftDonateTask giftDonateTask = new GiftDonateTask(mToUserId,
				mGift.getId(), mToPhotoId, mQuantity);
		giftDonateTask.setListener(new TaskAdapter() {
			@Override
			public void onPostExecute(GenericTask task, TaskResult result) {
				if (result == TaskResult.OK) {
					Toast.makeText(GiftDonateActivity.this,
							getString(R.string.gift_donate_ok),
							Toast.LENGTH_LONG).show();
					App.readUser().setBalance(
							App.readUser().getBalance() - mGift.getCost_point()
									* mQuantity);
					if (mToPhotoId > 0) {
						CommonUtilities
								.broadcastStoryGift(GiftDonateActivity.this);
					}
					GiftDonateActivity.this.finish();
				} else {
					Toast.makeText(GiftDonateActivity.this,
							getString(R.string.gift_donate_error),
							Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onPreExecute(GenericTask task) {
			}

		});
		giftDonateTask.execute();

	}

	public void myWalletButton(View v) {
		Intent intent = new Intent(this, MyWalletActivity.class);
		startActivity(intent);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gift_donate);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.present);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		instance = this;
		mToUserId = getToUserIdFromExtras();
		mToPhotoId = getToPhototIdFromExtras();
		mGift = getGiftFromExtras();
		if (mGift == null || mToUserId <= 0) {
			this.finish();
		}
		// 加载画面
		setupComponents();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private void setupComponents() {

		Utils.setGiftPicImage(giftThumbImageView, mGift.getPic_url());

		giftCostTextView.setText(mGift.getCost_point()
				+ getString(R.string.point));
		giftTitleTextView.setText(mGift.getTitle());
		giftCharmTextView.setText(getString(R.string.gift_charm,
				mGift.getCharm_point()));

		giftQuantity.setFocusable(false);
		giftQuantity.setEnabled(false);

		gifttBalanceTextView.setText(Html.fromHtml(getString(
				R.string.curr_user_balance, NumberFormat.getNumberInstance()
						.format(App.readUser().getBalance())))
				+ getString(R.string.point));

		giftPlusButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int number = 0;
				String numString = giftQuantity.getText().toString();
				if (numString == null || numString.equals("")) {
					number = 1;
					giftQuantity.setText("1");
				} else {
					number = Integer.valueOf(numString) + 1;
				}
				giftQuantity.setText(String.valueOf(number));
				giftCostTextView.setText(mGift.getCost_point() * number
						+ getString(R.string.point));
				giftCharmTextView.setText(getString(R.string.gift_charm,
						mGift.getCharm_point() * number));
			}
		});
		giftMinusButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int number = 0;
				String numString = giftQuantity.getText().toString();
				if (numString == null || numString.equals("")) {
					number = 1;
					giftQuantity.setText("1");
				} else {
					if (Integer.valueOf(numString) > 1) {
						number = Integer.valueOf(numString) - 1;
					} else {
						number = 1;
					}
				}
				giftQuantity.setText(String.valueOf(number));
				giftCostTextView.setText(mGift.getCost_point() * number
						+ getString(R.string.point));
				giftCharmTextView.setText(getString(R.string.gift_charm,
						mGift.getCharm_point() * number));
			}
		});
	}
}