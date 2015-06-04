package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.utils.AppVersion;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;

import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class OrgListArrayAdapter extends ArrayAdapter<Map<String, Object>> {
    private static final int mResource = R.layout.item_org; // xml布局文件
    protected LayoutInflater mInflater;

    public OrgListArrayAdapter(Context context) {
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

        Map<String, Object> org = getItem(position);
        String jid = (String) org.get("jid");
        String name = (String) org.get("name");
        String is_parent = (String) org.get("is_parent");

        holder.orgJid.setText(jid);
        holder.orgName.setText(name);
        if ("false".equals(is_parent)) {
            String portrait = AppVersion.getPortraitUrl(User.getUsernameFromJid(jid));
            Utils.setUserPicImage(holder.orgThumb, portrait);
            holder.orgThumb.setVisibility(View.VISIBLE);
            holder.orgSubmenu.setVisibility(View.GONE);
        } else {
            holder.orgThumb.setVisibility(View.GONE);
            holder.orgSubmenu.setVisibility(View.VISIBLE);
        }

        return view;
    }

    static class ViewHolder {
        @InjectView(R.id.item_org_icon)
        ImageView orgThumb;
        @InjectView(R.id.item_org_jid)
        TextView orgJid;
        @InjectView(R.id.item_org_name)
        TextView orgName;
        @InjectView(R.id.item_org_submenu_imageview)
        ImageView orgSubmenu;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

}