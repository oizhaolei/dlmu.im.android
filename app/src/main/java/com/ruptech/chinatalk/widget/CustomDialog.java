package com.ruptech.chinatalk.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ruptech.dlmu.im.R;

public class CustomDialog extends AlertDialog {

    private final LayoutInflater mInflater;

    public CustomDialog(Context context) {
        super(context);
        mInflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    public static CustomDialog Builder(Context context) {
        return new CustomDialog(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Resources res = getContext().getResources();
        final int yellow = res.getColor(R.color.orange_highlight);

        // Title
        final int titleId = res.getIdentifier("alertTitle", "id", "android");
        final View title = findViewById(titleId);
        if (title != null) {
            ((TextView) title).setTextColor(yellow);
        }

        // Title divider
        final int titleDividerId = res.getIdentifier("titleDivider", "id",
                "android");
        final View titleDivider = findViewById(titleDividerId);
        if (titleDivider != null) {
            titleDivider.setBackgroundColor(yellow);
        }

        Button button = getButton(DialogInterface.BUTTON_NEGATIVE);
        button.setBackgroundResource(R.drawable.list_selector);
        button = getButton(DialogInterface.BUTTON_NEUTRAL);
        button.setBackgroundResource(R.drawable.list_selector);
        button = getButton(DialogInterface.BUTTON_POSITIVE);
        button.setBackgroundResource(R.drawable.list_selector);
    }

    public CustomDialog setItems(CharSequence[] items,
                                 final DialogInterface.OnClickListener listener) {
        View modeList = mInflater.inflate(R.layout.dialog_menu, null, false);
        ListView menuView = (ListView) modeList.findViewById(android.R.id.list);
        modeList.setBackgroundResource(R.drawable.list_selector);
        final MenuListArrayAdapter adapter = new MenuListArrayAdapter(
                this.getContext());

        for (int i = 0; i < items.length; i++) {
            adapter.add(items[i]);
        }

        menuView.setAdapter(adapter);

        menuView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                listener.onClick(CustomDialog.this, arg2);
                CustomDialog.this.cancel();
            }

        });

        setView(modeList);
        return this;
    }

    public CustomDialog setMessage(String message) {
        super.setMessage(message);
        return this;
    }

    public CustomDialog setNegativeButton(int resID, OnClickListener mListener) {

        setButton(android.app.AlertDialog.BUTTON_NEGATIVE, getContext()
                .getString(resID), mListener);
        return this;
    }

    public CustomDialog setNegativeButton(String label, OnClickListener mListener) {

        setButton(android.app.AlertDialog.BUTTON_NEGATIVE, label, mListener);
        return this;
    }

    public CustomDialog setNeutralButton(String label, OnClickListener mListener) {

        setButton(android.app.AlertDialog.BUTTON_NEUTRAL, label, mListener);
        return this;
    }

    public CustomDialog setPositiveButton(int resID, OnClickListener mListener) {

        setButton(android.app.AlertDialog.BUTTON_POSITIVE, getContext()
                .getString(resID), mListener);
        return this;
    }

    public CustomDialog setPositiveButton(String label,
                                          OnClickListener mListener) {

        setButton(android.app.AlertDialog.BUTTON_POSITIVE, label, mListener);
        return this;
    }

    public CustomDialog setTitle(String title) {
        super.setTitle(title);
        return this;
    }


}
