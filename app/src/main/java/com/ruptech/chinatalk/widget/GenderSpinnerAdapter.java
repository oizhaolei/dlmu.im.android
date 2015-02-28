/**
 *
 */
package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.utils.Utils;

public class GenderSpinnerAdapter extends BaseAdapter {

	static class ViewHolder {
		@InjectView(R.id.item_spinner_gender_text)
		TextView genderText;
		@InjectView(R.id.item_spinner_gender_img)
		ImageView genderImg;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}

	static final String TAG = Utils.CATEGORY + GenderSpinnerAdapter.class.getSimpleName();
	private static final int mResource = R.layout.item_spinner_gender; // xml布局文件
	private final int[] genderArray;
	private ViewHolder holder;
	private LayoutInflater mInflater;

	public GenderSpinnerAdapter(Context context, int[] genderArray) {
		this.genderArray = genderArray;
		if (context != null) {
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
	}

	@Override
	public int getCount() {
		return genderArray.length;
	}

	@Override
	public Object getItem(int position) {
		return genderArray[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view;

		if (convertView == null) {
			view = mInflater.inflate(mResource, null);
			holder = new ViewHolder(view);
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}
		if (position == 0) {
			holder.genderText.setText(R.string.gender_male);
			holder.genderImg.setImageResource(R.drawable.male);
		} else {
			holder.genderText.setText(R.string.gender_female);
			holder.genderImg.setImageResource(R.drawable.female);
		}
		return view;
	}
}