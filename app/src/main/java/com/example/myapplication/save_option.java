package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class save_option extends Activity {
    private TextView tester_name;
    private TextView sensor_id_1;
    private TextView sensor_id_2;
    private TextView flow_rate_1;
    private TextView flow_rate_2;
    private TextView backpressure_1;
    private TextView backpressure_2;
    private Button done;
    //private ArrayList<String> received_data_list_string;
    //private ArrayList<Float> flow_rate_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_option);
        //Bundle bundle = getIntent().getExtras();
        //if(bundle != null){
           // received_data_list_string = BLE_List.ble_list.received_data_list_string;
           // flow_rate_list = BLE_List.ble_list.flow_rate_list;
        //}
        initView();
        initStoragePermission();
    }

    public void initView() {
        tester_name = findViewById(R.id.tester_name);
        sensor_id_1 = findViewById(R.id.sensor_id_1);
        sensor_id_2 = findViewById(R.id.sensor_id_2);
        flow_rate_1 = findViewById(R.id.flow_rate_1);
        flow_rate_2 = findViewById(R.id.flow_rate_2);
        backpressure_1 = findViewById(R.id.backpressure_1);
        backpressure_2 = findViewById(R.id.backpressure_2);
        done = findViewById(R.id.done);
    }

    public void onClick(View v) {
        saveData();
        this.finish();
    }
    private void initStoragePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 先判断有没有权限
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(this, "Android VERSION  R OR ABOVE，NO MANAGE_EXTERNAL_STORAGE GRANTED!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                startActivity(intent);
            }
        }
    }
    public void saveData()  {
        //String TestInfo = "tester_name:" + tester_name.getText().toString() + "   sensor_id_1:" + sensor_id_1.getText().toString() +
        //        "   sensor_id_2:" + sensor_id_2.getText().toString() + "   flow_rate_1:" + flow_rate_1.getText().toString() +
         //       "   flow_rate_2:" + flow_rate_2.getText().toString() + "   backpressure_1:" + backpressure_1.getText().toString() +
         //       "   backpressure_2:" + backpressure_2.getText().toString() ;
        final String[][] data = new String[BLE_List.ble_list.received_data_list_string.size()][];
        String[] head = {"Info", "Voltage", "Flow rate"};
        //data[0][0] = "info";
        //data[0][1] = "voltage";
        //data[0][2] = "flow rate";
        String[] info = {"Tester name", "Sensor ID 1", "Sensor ID 2", "Flow rate 1", "Flow rate 2", "Backpressure 1", "Backpressure 2"};
        for(int i = 1; i < data.length; i++){
            data[i][0] = info[i-1];
            data[i][1] = BLE_List.ble_list.received_data_list_string.get(i-1);
            data[i][2] = String.valueOf(BLE_List.ble_list.flow_rate_list.get(i-1));
        }
        try {
            CSVWriter csvWriter = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory() + "/" +
                    "data.csv", true));
            //String[] data = {TestInfo};
            //csvWriter.writeNext(data);
            csvWriter.close();
            Log.d("MainActivity", "Saved.");
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}

