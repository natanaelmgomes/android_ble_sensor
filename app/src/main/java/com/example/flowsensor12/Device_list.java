package com.example.flowsensor12;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;
import java.util.UUID;

public class Device_list extends Activity {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner scanner;
    private ListView listView;
    private TextView connection_state;
    private static final long SCAN_PERIOD = 10000;
    private com.example.flowsensor12.adapter.BlueToothDeviceAdapter adapter;
    private BluetoothGatt mBluetoothGatt;
    private UUID notify_UUID_service = UUID.fromString("A7EA14CF-1000-43BA-AB86-1D6E136A2E9E");
    private UUID notify_UUID_chara = UUID.fromString("A7EA14CF-1100-43BA-AB86-1D6E136A2E9E");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
        initSearch();
        initView();
    }
    public void initSearch(){
        scanner.startScan(mScanCallback);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                scanner.stopScan(mScanCallback);
            }
        }, SCAN_PERIOD);
    }
    ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            Log.d("Device_list", "onScanResult: " + device.getName() + " " + device.getAddress());
            if(device.getName() != null){
                adapter.add(device);
            }
        }
    };
    public void initView() {
        connection_state = (TextView) findViewById(R.id.connection_state);
        listView = (ListView) findViewById(R.id.listView);
        adapter = new com.example.flowsensor12.adapter.BlueToothDeviceAdapter(getApplicationContext(), R.layout.bluetooth_device_list_item);
        listView.setAdapter(adapter);
        getBoundedDevices();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                if(mBluetoothGatt != null){
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt.close();
                }
                BluetoothDevice device = (BluetoothDevice) adapter.getItem(position);
                connection_state.setText(getResources().getString(R.string.connecting));
                mBluetoothGatt = device.connectGatt(getApplicationContext(), false, mGattCallback);
            }
        });
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // connection success
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    // find service
                }
            } else {
                // connection failure
                connection_state.setText(getResources().getString(R.string.connect_fail));
                mBluetoothGatt.close();
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            BluetoothGattService service;
            service = mBluetoothGatt.getService(notify_UUID_service);
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(notify_UUID_chara);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
            // Get the Descriptor channel in Notify and then register
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            }
            mBluetoothGatt.setCharacteristicNotification(characteristic, true);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connection_state.setText(getResources().getString(R.string.connect_success));
                }
            });
        }
    };
    private void getBoundedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        adapter.clear();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                adapter.add(device);
            }
        }
    }
}