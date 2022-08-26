package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class Devices extends ArrayAdapter<Detail> {
    private final LayoutInflater mInflater;
    private int mResource;
    public Devices(Context context, int resource) {
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
        TextView flowrate = (TextView) convertView.findViewById(R.id.flowrate);
        Detail detail = getItem(position);
        name.setText(detail.name_input.getText().toString());
        flowrate.setText(detail.flow_rate_display.getText().toString());
        return convertView;
    }
}

