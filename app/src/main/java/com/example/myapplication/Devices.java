package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class Devices extends ArrayAdapter<Detail> {
    TextView name;
    TextView flowrate;
    CardView cardView;
    private final LayoutInflater mInflater;
    private int mResource;
    public Devices(Context context, int resource) {
        super(context, resource);
        mInflater = LayoutInflater.from(context);
        mResource = resource;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Detail detail = (Detail) getItem(position);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.bluetooth_device_list, parent, false);
        }
        name = convertView.findViewById(R.id.name);
        flowrate = convertView.findViewById(R.id.flowrate);
        cardView = convertView.findViewById(R.id.Cardview);
        name.setText(detail.getName());
        flowrate.setText(detail.getFlowRate());
        if(Math.abs(Float.parseFloat(detail.getFlowRate())-Float.parseFloat(detail.getFlowRateSet()))>10){
            cardView.setBackgroundColor(Color.RED);

        }else {
            cardView.setBackgroundColor(Color.GREEN);
        }

        return convertView;
    }

}

