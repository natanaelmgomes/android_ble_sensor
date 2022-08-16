package com.example.myapplication;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BlueToothDevices extends ArrayAdapter<String> {
    private final LayoutInflater mInflater;
    private int mResource;
    public BlueToothDevices(Context context, int resource) {
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
        //TextView rate = (TextView) convertView.findViewById(R.id.rate);
        String name_string = getItem(position);
        name.setText(name_string);
        //rate.setText(BLE_List.ble_list.flow_rate_value);

        return convertView;
    }
}

