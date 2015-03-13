/**
 *
 */
package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.utils.Utils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MenuListArrayAdapter extends ArrayAdapter<CharSequence> {
	static class ViewHolder {
		@InjectView(R.id.item_menu_textview)
		TextView friendFullName;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}

	private static final int mResource = R.layout.item_menu; // xml布局文件

	private final LayoutInflater mInflater;


	public MenuListArrayAdapter(Context context) {
		super(context, mResource);

		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		View view;
		final ViewHolder holder;
		if (convertView == null) {
			view = mInflater.inflate(mResource, parent, false);
			holder = new ViewHolder(view);
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}

		String menuItem = (String) getItem(position);

		if (Utils.isEmpty(menuItem)) {
			holder.friendFullName.setText("");
		} else {
			holder.friendFullName.setText(Utils.abbrString(menuItem, 20));
		}

		return view;
	}

}