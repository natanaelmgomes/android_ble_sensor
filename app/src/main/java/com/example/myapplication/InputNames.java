package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class InputNames extends ArrayAdapter<Detail> {
    private final LayoutInflater mInflater;
    private int mResource;
    public InputNames(Context context, int resource) {
        super(context, resource);
        mInflater = LayoutInflater.from(context);
        mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mInflater.inflate(mResource, parent, false);
        }
        TextView name = (TextView) convertView.findViewById(R.id.name);
        Detail detail = getItem(position);
        name.setText((CharSequence) detail.name);
        return convertView;
    }
}

