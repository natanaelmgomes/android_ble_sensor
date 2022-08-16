package com.example.myapplication;

import static com.example.myapplication.BLE_List.ble_list;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class Setting_page extends AppCompatActivity {
    Button one_plus;
    Button ten_plus;
    Button hun_plus;
    Button one_minus;
    Button ten_minus;
    Button hun_minus;
    Button confirm;
    Button Default;
    TextView flow_rate;
    TextView name_input;
    int one_count = 0;
    int ten_count = 0;
    int hun_count = 0;
    public static String flow_rate_value;
    public static Setting_page setting = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_page);
        setting = this;
        initView();
        flow_rate_value = String.valueOf(hun_count) + String.valueOf(ten_count) + String.valueOf(one_count);
        flow_rate.setText(flow_rate_value);
    }
    private void initView() {
        one_plus = findViewById(R.id.one_plus);
        ten_plus = findViewById(R.id.ten_plus);
        hun_plus = findViewById(R.id.hun_plus);
        one_minus = findViewById(R.id.one_plus);
        ten_minus = findViewById(R.id.ten_minus);
        hun_minus = findViewById(R.id.one_plus);
        confirm = findViewById(R.id.confirm);
        flow_rate = findViewById(R.id.Flow_rate);
        name_input = findViewById(R.id.Name_input);
        Default = findViewById(R.id.Default);
    }
    public void setOne_plus(View v) {
        if(one_count < 9) {
            one_count++;
            flow_rate_value = String.valueOf(hun_count) + String.valueOf(ten_count) + String.valueOf(one_count);
            flow_rate.setText(flow_rate_value);
        }
    }
    public void setTen_plus(View v) {
        if(ten_count < 9) {
            ten_count++;
            flow_rate_value = String.valueOf(hun_count) + String.valueOf(ten_count) + String.valueOf(one_count);
            flow_rate.setText(flow_rate_value);
        }
    }
    public void setHun_plus(View v) {
        if(hun_count < 9) {
            hun_count++;
            flow_rate_value = String.valueOf(hun_count) + String.valueOf(ten_count) + String.valueOf(one_count);
            flow_rate.setText(flow_rate_value);
        }
    }
    public void setOne_minus(View v) {
        if (one_count > 0) {
            one_count--;
            flow_rate_value = String.valueOf(hun_count) + String.valueOf(ten_count) + String.valueOf(one_count);
            flow_rate.setText(flow_rate_value);
        }
    }
    public void setTen_minus(View v) {
        if (ten_count > 0) {
            ten_count--;
            flow_rate_value = String.valueOf(hun_count) + String.valueOf(ten_count) + String.valueOf(one_count);
            flow_rate.setText(flow_rate_value);
        }
    }
    public void setHun_minus(View v) {
        if (hun_count > 0) {
            hun_count--;
            flow_rate_value = String.valueOf(hun_count) + String.valueOf(ten_count) + String.valueOf(one_count);
            flow_rate.setText(flow_rate_value);
        }
    }
    public void setConfirm(View v) {
        Intent intent = new Intent(Setting_page.this, Detail.class);
        intent.putExtra("flow_rate", flow_rate_value);
        intent.putExtra("name_input", name_input.getText().toString());
        MainActivity.main.blueToothDevices.add(name_input.getText().toString());
        startActivity(intent);
    }
    public void Default(View v) {
        one_count = 0;
        ten_count = 5;
        hun_count = 0;
        flow_rate_value = String.valueOf(hun_count) + String.valueOf(ten_count) + String.valueOf(one_count);
        flow_rate.setText(flow_rate_value);
    }
}