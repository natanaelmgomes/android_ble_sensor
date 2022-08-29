package com.example.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class Devices extends ArrayAdapter<Detail> {
    String name_string="";
    String flowrate_string="";
    TextView name;
    TextView flowrate;
    CardView cardView;
    private final LayoutInflater mInflater;
    private int mResource;
    public Devices(Context context, int resource) {
        super(context, resource);
        mInflater = LayoutInflater.from(context);
        mResource = resource;
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mInflater.inflate(mResource, parent, false);
        }
        name = (TextView) convertView.findViewById(R.id.name);
        flowrate = (TextView) convertView.findViewById(R.id.flowrate);
        cardView = (CardView) convertView.findViewById(R.id.Cardview);
        return convertView;
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetEventBus(Detail.NameWrap wrap){
        name_string = wrap.message;
        name.setText(name_string);
        name.setTextColor(Color.WHITE);
        name.setTextSize(40);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetEventBus(Detail.FlowRateWrap wrap){
        flowrate_string = wrap.message;
        flowrate.setText(flowrate_string);
        flowrate.setTextColor(Color.WHITE);
        flowrate.setTextSize(40);
        if(Detail.warning == true){
            //name.setTextColor(Color.RED);
            //flowrate.setTextColor(Color.RED);
            cardView.setCardBackgroundColor(Color.RED);
        }
        else{
            //name.setTextColor(Color.GREEN);
            //flowrate.setTextColor(Color.GREEN);
            cardView.setCardBackgroundColor(Color.GREEN);
        }
    }
}

