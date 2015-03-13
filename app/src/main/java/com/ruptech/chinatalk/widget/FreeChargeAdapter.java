package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.ui.user.MyWalletActivity.RechargeItem;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class FreeChargeAdapter extends ArrayAdapter<RechargeItem> {
	class ViewHolder {
		@InjectView(R.id.item_recharge_image)
		ImageView imageView;
		@InjectView(R.id.item_recharge_text)
		TextView txtTitle;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}

	private final LayoutInflater viewInflater;


	public FreeChargeAdapter(Context context, int resource) {
		super(context, resource);
		viewInflater = LayoutInflater.from(getContext());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = viewInflater.inflate(R.layout.item_recharge,
					parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		RechargeItem item = this.getItem(position);
		holder.imageView.setImageResource(item.imageId);
		holder.txtTitle.setText(item.titleId);
		convertView.setId(item.imageId);

		return convertView;
	}


}
