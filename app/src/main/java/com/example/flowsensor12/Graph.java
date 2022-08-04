package com.example.flowsensor12;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class Graph extends Activity
{
    LineChart lineChart;
    private float[] data = new float[100];
    int i = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        lineChart = (LineChart) findViewById(R.id.chart1);
        test();
        //setData(data);
    }

    private void test(){
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                data[i] = new Random().nextFloat()*10;
                i++;
                setData(data);
            }
        };
        timer.schedule(timerTask,1000,500);
    }

    private void setData(float[] voltage) {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setDoubleTapToZoomEnabled(true);
        lineChart.setBackgroundColor(Color.BLACK);
        List<Entry> entries = new ArrayList<>();
        for(int i = 0; i < voltage.length; i++)
        {
            entries.add(new Entry(i, voltage[i]));
        }
        LineDataSet dataSet = new LineDataSet(entries, "Voltage");
        dataSet.setColor(Color.GREEN);
        dataSet.setCircleColor(Color.GREEN);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        dataSet.notifyDataSetChanged();
        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }



}