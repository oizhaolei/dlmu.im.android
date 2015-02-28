package com.ruptech.chinatalk.ui.story;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.ui.LoginGateActivity;
import com.ruptech.chinatalk.widget.CustomDialog;

public class PhotoShareActivity extends ActionBarActivity {

	private CustomDialog dialog;

	private void gotoLoginGate() {
		Intent intent = new Intent(this, LoginGateActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();

		if (App.isAvailableShowMain()) {
			Intent forwardIntent = new Intent(this,
					UserStoryImageCropActivity.class);
			forwardIntent.setType(intent.getType());
			forwardIntent.setAction(intent.getAction());
			forwardIntent.putExtras(intent);
			startActivity(forwardIntent);
			finish();
		} else {
			dialog = new CustomDialog(this)
					.setTitle(getString(R.string.tips))
					.setMessage(
							getString(R.string.please_login_to_share_picture))
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									gotoLoginGate();
									finish();
								}
							});// 创建;
			dialog.show();
		}

	}

}
