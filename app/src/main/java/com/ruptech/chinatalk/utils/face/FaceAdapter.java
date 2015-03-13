package com.ruptech.chinatalk.utils.face;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.ruptech.chinatalk.R;

import java.util.List;

public class FaceAdapter extends BaseAdapter {

	class ViewHolder {

		public ImageView imageView_face;
	}

	private final List<MsgEmojiModle> data;

	private final LayoutInflater inflater;

	private int size = 0;

	public FaceAdapter(Context context, List<MsgEmojiModle> list) {
		this.inflater = LayoutInflater.from(context);
		this.data = list;
		this.size = list.size();
	}

	@Override
	public int getCount() {
		return this.size;
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MsgEmojiModle emoji = data.get(position);
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = inflater.inflate(R.layout.item_face_img, null);
			viewHolder.imageView_face = (ImageView) convertView
					.findViewById(R.id.face_image_view);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		if (emoji.getId() == R.drawable.icon_emotion_del) {
			viewHolder.imageView_face.setImageResource(emoji.getId());
		} else if (TextUtils.isEmpty(emoji.getCharacter())) {
			viewHolder.imageView_face.setImageDrawable(null);
		} else {
			viewHolder.imageView_face.setTag(emoji);
			viewHolder.imageView_face.setImageResource(emoji.getId());
		}

		return convertView;
	}
}