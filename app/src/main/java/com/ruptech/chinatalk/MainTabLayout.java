package com.ruptech.chinatalk;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;

import java.util.ArrayList;
import java.util.List;

import static butterknife.ButterKnife.findById;

public class MainTabLayout extends LinearLayout implements OnClickListener {

    static final String LOG_TAG = "MainTabLayout";
    private final List<TabItem> mTabs = new ArrayList<TabItem>();
    private OnTabClickListener tabClickListener;
    private View selectedView;
    private long previousTabClickTime;
    public MainTabLayout(Context context) {
        super(context);
    }

    public MainTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainTabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void clickTab(int index) {
        View view = getChildAt(index);
        onClick(view);
    }

    private View createTabView(int textID, int iconID) {

        View tab = LayoutInflater.from(getContext()).inflate(
                R.layout.item_main_tab, null);
        TextView textView = (TextView) findById(tab, R.id.tab_textview);
        ImageView icon = (ImageView) findById(tab, R.id.tab_icon);
        icon.setVisibility(View.VISIBLE);

        if (iconID != R.drawable.tab_icon_post) {
            Utils.setImageResource(icon, iconID);
            textView.setText(textID);
            Utils.setTextColor(textView);
        } else {
            icon.setImageResource(iconID);
            textView.setVisibility(View.GONE);
        }
        return tab;
    }

    public void init() {

        mTabs.add(new TabItem(R.string.main_tab_chat, R.drawable.tab_icon_chat));
        mTabs.add(new TabItem(R.string.main_tab_service,
                R.drawable.tab_icon_service));
        mTabs.add(new TabItem(R.string.main_tab_myself,
                R.drawable.tab_icon_me));

        populateTab();
    }

    @Override
    public void onClick(View v) {
        if (selectedView != v && tabClickListener != null)
            tabClickListener.onTabClick(v.getId());

        if (selectedView == v
                && previousTabClickTime + 2000 > System.currentTimeMillis()
                && tabClickListener != null) {
            tabClickListener.onTabDoubleClick(v.getId());
        }

        previousTabClickTime = System.currentTimeMillis();
    }

    private void populateTab() {
        TabItem tab;
        for (int i = 0; i < mTabs.size(); i++) {
            tab = mTabs.get(i);
            View tabView = createTabView(tab.getTitleResId(),
                    tab.getIconResId());
            tabView.setId(tab.getTitleResId());
            tabView.setOnClickListener(this);
            tabView.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LayoutParams.MATCH_PARENT, 1f));
            addView(tabView);
            setTabIndex(tab.getTitleResId(), i);
        }
    }

    public void setNewCountForTab(int count, int tabIndex) {
        if (tabIndex < 0 || tabIndex >= mTabs.size())
            return;

        View tabView = getChildAt(tabIndex);

        if (tabView != null) {
            TextView countTextView = (TextView) tabView
                    .findViewById(R.id.tab_number_icon);
            ImageView smallNewMark = (ImageView) tabView
                    .findViewById(R.id.new_mark);

            if (countTextView == null || smallNewMark == null)
                return;

            if (tabIndex == MainActivity.TAB_INDEX_MYSELF && count > 0)
                count += 10;

            if (count == 0) {
                countTextView.setVisibility(View.INVISIBLE);
                smallNewMark.setVisibility(View.INVISIBLE);
            } else if (count < 10) {
                countTextView.setText(String.valueOf(count));
                countTextView.setVisibility(View.VISIBLE);
                smallNewMark.setVisibility(View.INVISIBLE);
            } else {
                countTextView.setVisibility(View.INVISIBLE);
                smallNewMark.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setOnTabClickListener(OnTabClickListener listener) {
        tabClickListener = listener;
    }

    private void setTabIndex(int resId, int index) {
        switch (resId) {
            case R.string.main_tab_service:
                MainActivity.TAB_INDEX_DISCOVER = index;
                break;
            case R.string.main_tab_chat:
                MainActivity.TAB_INDEX_CHAT = index;
                break;
            case R.string.main_tab_myself:
                MainActivity.TAB_INDEX_MYSELF = index;
                break;

        }
    }

    public interface OnTabClickListener {
        void onTabClick(int viewId);

        void onTabDoubleClick(int viewId);
    }

    class TabItem {
        private final int mTitleResId;
        private final int mIconResId;

        TabItem(int titleResId, int iconResId) {
            mTitleResId = titleResId;
            mIconResId = iconResId;
        }

        public int getIconResId() {
            return mIconResId;
        }

        public int getTitleResId() {
            return mTitleResId;
        }
    }

}