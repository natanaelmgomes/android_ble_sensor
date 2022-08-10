package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class Detail extends AppCompatActivity {
    TextView flow_rate;
    TextView name;
    Button options;
    Button close;
    Button flowrate;
    Button linechart;
    LineChart lineChart;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothLeScanner scanner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        initView();
    }
    private void initView() {
        options = findViewById(R.id.options);
        close = findViewById(R.id.close);
        flowrate = findViewById(R.id.flowrate);
        linechart = findViewById(R.id.linechart);
        flow_rate = findViewById(R.id.flow_rate);
        lineChart = findViewById(R.id.chart);
        name = findViewById(R.id.name);
        name.setText(getIntent().getStringExtra("name_input"));
    }

    public void setOptions(View v) {

    }

    public void setClose(View v) {
        if(mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
    }

    public void setFlowrate(View v) {
        flow_rate.setVisibility(View.VISIBLE);
        lineChart.setVisibility(View.INVISIBLE);
    }

    public void setLinechart(View v) {
        flow_rate.setVisibility(View.INVISIBLE);
        lineChart.setVisibility(View.VISIBLE);
    }

    private void setData(ArrayList<Float> data) {
        List<Entry> entries = new ArrayList<>();
        LineDataSet dataSet = new LineDataSet(entries, "Voltage");
        LineData lineData = new LineData(dataSet);
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setDoubleTapToZoomEnabled(true);
        // lineChart.setBackgroundColor(Color.BLACK);
        for(int i = 0; i < data.size(); i++)
        {
            entries.add(new Entry(i, data.get(i)));
        }
        dataSet.setColor(Color.GREEN);
        dataSet.setCircleColor(Color.GREEN);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.setData(lineData);
        dataSet.notifyDataSetChanged();
        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }
}