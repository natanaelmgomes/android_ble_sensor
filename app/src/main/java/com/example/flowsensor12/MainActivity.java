package com.example.flowsensor12;


import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.flowsensor12.adapter.BlueToothDeviceAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private BluetoothAdapter mBluetoothAdapter;
    private BlueToothDeviceAdapter adapter;
    private ListView listView;
    private TextView text_state;
    //private TextView text_msg;
    //private static final String NAME = "Sensor";
    //private static final UUID FS_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //private Handler handler;
    private boolean isScaning=false;
    private boolean isConnecting=false;
    private BluetoothGatt mBluetoothGatt;
    private static final long SCAN_PERIOD = 10000;
    //private ScanCallback scanCallback;
    //private BluetoothLeScanner scanner;
    private List<BluetoothDevice> mDatas;

    //服务和特征值
    private UUID read_UUID_service=UUID.fromString("6fa90001-5c4e-48a8-94f4-8030546f36fc");
    private UUID read_UUID_chara=UUID.fromString("6fa90002-5c4e-48a8-94f4-8030546f36fc");
    private UUID notify_UUID_service=UUID.fromString("A7EA14CF-1000-43BA-AB86-1D6E136A2E9E");
    private UUID notify_UUID_chara= UUID.fromString("A7EA14CF-1100-43BA-AB86-1D6E136A2E9E");
    private String hex="7B46363941373237323532443741397D";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }

    public void initView() {
        findViewById(R.id.btn_openBT).setOnClickListener(this);
        findViewById(R.id.btn_search).setOnClickListener(this);
        text_state = (TextView) findViewById(R.id.text_state);
        listView = (ListView) findViewById(R.id.listView);
        adapter = new BlueToothDeviceAdapter(getApplicationContext(), R.layout.bluetooth_device_list_item);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isScaning) {
                    stopScanDevice();
                }
                //BluetoothDevice device = (BluetoothDevice) adapter.getItem(position);
                //连接设备
                //connectDevice(device);
                if (!isConnecting) {
                    isConnecting = true;
                    BluetoothDevice bluetoothDevice = mDatas.get(position);
                    //连接设备
                    text_state.setText(getResources().getString(R.string.connecting));
                    //f (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mBluetoothGatt = bluetoothDevice.connectGatt(MainActivity.this, true, gattCallback, TRANSPORT_LE);
                   // } else {
                      //  mBluetoothGatt = bluetoothDevice.connectGatt(MainActivity.this, true, gattCallback);
                   // }
                }
            }
        });
    }
    private void initData(){
        mDatas=new ArrayList<>();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            //开始按钮
            case R.id.btn_openBT:
                BluetoothStart();
                break;
            //
            case R.id.btn_search:
                    BluetoothSearch();
                break;
        }

    }

    //开启蓝牙
    private void BluetoothStart() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "This device does not support the Bluetooth function", Toast.LENGTH_SHORT).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                startActivityForResult(turnOn, 0);
                Toast.makeText(MainActivity.this, "Please turn on Bluetooth", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(MainActivity.this, "Bluetooth is Already on", Toast.LENGTH_LONG).show();
            }
        }
    }

    //搜索蓝牙设备
    private void BluetoothSearch() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(MainActivity.this, "This device does not support BLE", Toast.LENGTH_SHORT).show();
            finish();
        }
        getBoundedDevices();
        if ((checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                || (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }//GPS开启
        isScaning=true;
        mBluetoothAdapter.startLeScan(scanCallback);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //结束扫描
                mBluetoothAdapter.stopLeScan(scanCallback);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isScaning=false;
                    }
                });
            }
        },10000);
    }
    private void stopScanDevice(){
        isScaning=false;
        mBluetoothAdapter.stopLeScan(scanCallback);
    }
    BluetoothAdapter.LeScanCallback scanCallback=new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            text_state.setText(getResources().getString(R.string.searching));
            if (device.getName() != null){
                if(!mDatas.contains(device)) {
                    adapter.add(device);
                    mDatas.add(device);
                }
            }

        }
    };

    //获取已经配对过的设备
    private void getBoundedDevices() {
        //获取已经配对过的设备
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        //避免重复添加已经绑定过的设备
        adapter.clear();
        //将其添加到设备列表中
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                adapter.add(device);
            }
        }
    }

    //连接蓝牙设备
    private BluetoothGattCallback gattCallback=new BluetoothGattCallback() {
        //断开或连接 状态发生变化时调用
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status==BluetoothGatt.GATT_SUCCESS){
                //连接成功
                if (newState== BluetoothGatt.STATE_CONNECTED){
                    //text_state.setText(getResources().getString(R.string.connect_success));
                    //发现服务
                    gatt.discoverServices();
                }
            }else{
                //连接失败
                text_state.setText(getResources().getString(R.string.connect_over));
                mBluetoothGatt.close();
                isConnecting=false;
            }
        }
        //发现设备（真正建立连接）
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            //直到这里才是真正建立了可通信的连接
            isConnecting=false;
            //订阅通知
            mBluetoothGatt.setCharacteristicNotification(mBluetoothGatt.getService(notify_UUID_service).getCharacteristic(notify_UUID_chara),true);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    text_state.setText(getResources().getString(R.string.connect_success));
                }
            });
        }
        /*//读操作的回调
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }
        //接收到硬件返回的数据
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            final byte[] data=characteristic.getValue();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //addText(tvResponse,bytes2hex(data));
                }
            });
        }*/
    };

}







