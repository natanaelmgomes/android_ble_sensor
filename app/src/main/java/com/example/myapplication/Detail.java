package com.example.myapplication;
import static com.example.myapplication.BLE_List.ble_list;
import static com.example.myapplication.Setting_page.setting;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Detail extends AppCompatActivity {
    public static Detail detail = null;
    TextView flow_rate;
    TextView name;
    Button options;
    Button flowrate;
    Button linechart;
    LineChart lineChart;
    private ArrayList<Float> data = new ArrayList<Float>();
    public static List<Float> flow_rate_list;
    float flow_rate_value = 0;
    public static boolean warning = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        detail = this;
        initView();
    }
    @Override
    protected void onStart() {
        super.onStart();
        //注册监听 已注册监听 不能继续注册
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消监听
        EventBus.getDefault().unregister(this);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetEventBus(BLE_List.FlowRateWrap wrap){
        flow_rate_value = Float.parseFloat(wrap.message);
        flow_rate.setText(String.valueOf((int)flow_rate_value));
        JudgeWarning(flow_rate_value);
        if(warning == true){
            flow_rate.setTextColor(Color.RED);
        }
        else{
            flow_rate.setTextColor(Color.GREEN);
        }
        flow_rate_list.add(flow_rate_value);
        //Log.d("onGetEventBus", String.valueOf(flow_rate_list.size()));
        //setData(flow_rate_list);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetEventBus(BLE_List.VoltageWrap wrap){
        setData(wrap.message);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_optionmenu, menu);
        return true;
    }
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu1:
                BLE_List.ble_list.mBluetoothGatt.disconnect();
                finish();
                setting.finish();
                ble_list.finish();
                break;
            case R.id.menu2:
                Intent intent = new Intent(Detail.this, save_option.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    private void initView() {
        options = findViewById(R.id.options);
        flowrate = findViewById(R.id.flowrate);
        linechart = findViewById(R.id.linechart);
        flow_rate = findViewById(R.id.flow_rate);
        lineChart = findViewById(R.id.chart);
        name = findViewById(R.id.name);
        name.setText(getIntent().getStringExtra("name_input"));
    }

    public void setOptions(View v) {
        //Intent intent = new Intent();
        // intent.setClass(Detail.this, MainActivity.class);
       // intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //startActivity(intent);
        finish();
        setting.finish();
        ble_list.finish();
    }


    public void setFlowrate(View v) {
        flow_rate.setVisibility(View.VISIBLE);
        lineChart.setVisibility(View.INVISIBLE);
    }

    public void setLinechart(View v) {
        flow_rate.setVisibility(View.INVISIBLE);
        lineChart.setVisibility(View.VISIBLE);
    }

    private void setData(List<Float> data) {
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
            entries.add(new Entry(i,data.get(i)));
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

    private void JudgeWarning(float flow_rate_value) {
        if(Math.abs(flow_rate_value-Float.parseFloat(setting.flow_rate_value))>10)
        {
            warning = true;
        }
        else
        {
            warning = false;
        }
    }


}