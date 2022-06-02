package com.example.flowsensor12;


import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.flowsensor12.adapter.BlueToothDeviceAdapter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private BluetoothAdapter mBluetoothAdapter;
    private BlueToothDeviceAdapter adapter;
    private ListView listView;
    private TextView text_state;
    private TextView text_msg;
    private TextView text_name;
    private boolean isScaning=false;
    private boolean isConnecting=false;
    private BluetoothGatt mBluetoothGatt;
    private static final long SCAN_PERIOD = 10000;
    //private ScanCallback scanCallback;
    //private BluetoothLeScanner scanner;
    private List<BluetoothDevice> mDatas;

    //服务和特征值
    private UUID notify_UUID_service=UUID.fromString("A7EA14CF-1000-43BA-AB86-1D6E136A2E9E");
    private UUID notify_UUID_chara= UUID.fromString("A7EA14CF-1100-43BA-AB86-1D6E136A2E9E");
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
        findViewById(R.id.Start).setOnClickListener(this);
        text_state = (TextView) findViewById(R.id.text_state);
        text_msg = (TextView) findViewById(R.id.text_msg);
        text_name = (TextView) findViewById(R.id.text_name);
        listView = (ListView) findViewById(R.id.listView);
        adapter = new BlueToothDeviceAdapter(getApplicationContext(), R.layout.bluetooth_device_list_item);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isScaning) {
                    stopScanDevice();
                    text_state.setText(getResources().getString(R.string.search_over));
                }
                //连接设备
                if (!isConnecting) {
                    isConnecting = true;
                    BluetoothDevice bluetoothDevice = (BluetoothDevice) adapter.getItem(position);
                    //连接设备
                    text_state.setText(getResources().getString(R.string.connecting));
                    text_name.setText(bluetoothDevice.getName());
                    mBluetoothGatt = bluetoothDevice.connectGatt(MainActivity.this, true, gattCallback, TRANSPORT_LE);

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
            case R.id.Start:
                BluetoothStart();
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
            BluetoothGattService service;
            service = mBluetoothGatt.getService(notify_UUID_service);
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(notify_UUID_chara);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
            //获取到Notify当中的Descriptor通道 然后再进行注册
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            }
            mBluetoothGatt.setCharacteristicNotification(characteristic, true);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    text_state.setText(getResources().getString(R.string.connect_success));
                }
            });
        }

        //接收到硬件返回的数据
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            final byte[] data=characteristic.getValue();
            //float f = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            float f = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT,0);
            //String s = characteristic.getStringValue(0);
            String s= String.valueOf(f);
            //int b = Math.round(f);
            //String ss = String.valueOf(b);
            /*float[] flow_voltage;
            try{
                if(data.length==4){
                    flow_voltage = ByteArrayToFloatArray(data);
                }
                if(data.length==8){
                    flow_voltage = ByteArrayToFloatArray(data);
                }
                if(data.length==12){
                    flow_voltage = ByteArrayToFloatArray(data);
                }
                if(data.length==16){
                    flow_voltage = ByteArrayToFloatArray(data);
                }
            }catch (Exception e){}*/
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    text_msg.setText(s);
                }
            });
        }
    };

    public static float[] ByteArrayToFloatArray(byte[] data)
    {
        float[] result = new float[data.length / 4];
        int temp = 0;
        for (int i = 0; i < data.length; i += 4)
        {
            temp = temp | (data[i] & 0xff) << 0;
            temp = temp | (data[i+1] & 0xff) << 8;
            temp = temp | (data[i+2] & 0xff) << 16;
            temp = temp | (data[i+3] & 0xff) << 24;
            result[i / 4] = Float.intBitsToFloat(temp);
        }
        return result;
    }
}







