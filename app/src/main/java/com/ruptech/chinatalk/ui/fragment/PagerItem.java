package com.ruptech.chinatalk.ui.fragment;

import android.support.v4.app.Fragment;

public abstract class PagerItem {
    private final CharSequence mTitle;

    PagerItem(CharSequence title) {
        mTitle = title;
    }

    abstract Fragment createFragment();

    CharSequence getTitle() {
        return mTitle;
    }
}
