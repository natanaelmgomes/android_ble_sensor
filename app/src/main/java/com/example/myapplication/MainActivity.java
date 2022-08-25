package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.opencsv.CSVReader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //UI
    ConstraintLayout main;
    Button add;
    ListView namelist;
    ListView flowratelist;
    //Fragment
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    public static Detail[] fragment = new Detail[100];
    public static int count = 0;
    //Data
    float[] kaiser_window = new float[1024];
    public static InputNames inputnames = null;
    public static FlowRateDisplay flowratedisplay = null;
    public static MainActivity mainActivity = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
        initView();
        initPermission();
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
    public void onGetEventBus(NextActivity.MessageWrap wrap){
        flowratedisplay.clear();
        flowratedisplay.add(wrap.message);
    }

    public void initView() {
        main = findViewById(R.id.Main);
        namelist = findViewById(R.id.Namelist);
        flowratelist = findViewById(R.id.Flowratelist);
        add = findViewById(R.id.add);
        inputnames = new InputNames(getApplicationContext(), R.layout.bluetooth_device_list);
        flowratedisplay = new FlowRateDisplay(getApplicationContext(), R.layout.bluetooth_device_list);
        namelist.setAdapter(inputnames);
        flowratelist.setAdapter(flowratedisplay);
        namelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Detail detail = (Detail) inputnames.getItem(position);
                FragmentTransaction transaction1 = fragmentManager.beginTransaction();
                transaction1.show(detail).commit();
                main.setVisibility(View.INVISIBLE);
            }

        });
    }

    private void initFragment() {
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        fragment[count] = new Detail();
        transaction.add(R.id.content, fragment[count]);
        count++;
        transaction.commit();
    }


    private void initPermission() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(MainActivity.this, "This device does not support BLE", Toast.LENGTH_SHORT).show();
            finish();
        }//BLE
        if ((checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                || (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
                || (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                || (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            Log.d("MainActivity", "Bluetooth permission is not granted.");
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,}, 200);
        }//Bluetooth&Location
    }

    public void onClick(View v){
        initFragment();
        main.setVisibility(View.INVISIBLE);
    }
}