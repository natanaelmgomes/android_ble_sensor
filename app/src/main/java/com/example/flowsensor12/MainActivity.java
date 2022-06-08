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
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelUuid;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.flowsensor12.adapter.BlueToothDeviceAdapter;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.util.Log;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private BluetoothAdapter mBluetoothAdapter;
    private BlueToothDeviceAdapter adapter;
    private TextView connection_state;
    private Button Save;
    private TextView text_msg;
    private TextView text_name;
    private boolean isScaning = false;
    private boolean isConnecting = false;
    private BluetoothGatt mBluetoothGatt;
    private static final long SCAN_PERIOD = 999999999;
    private BluetoothLeScanner scanner;
    private List<BluetoothDevice> mDatas;
    private float ble_rx_counter = 0;
    private List<Float> received_data_list;
    private List<String> received_data_list_string;

    //Service and Characteristic
    private UUID notify_UUID_service = UUID.fromString("A7EA14CF-1000-43BA-AB86-1D6E136A2E9E");
    private UUID notify_UUID_chara = UUID.fromString("A7EA14CF-1100-43BA-AB86-1D6E136A2E9E");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPermission();
        initView();
        initData();
        initStart();
    }

    public void initView() {
        connection_state = (TextView) findViewById(R.id.connection_state);
        Save = (Button) findViewById(R.id.savebutton);
        text_msg = (TextView) findViewById(R.id.text_msg);
        text_name = (TextView) findViewById(R.id.text_name);
        adapter = new BlueToothDeviceAdapter(getApplicationContext(), R.layout.bluetooth_device_list_item);
    }

    private void initData() {
        mDatas = new ArrayList<>();
        received_data_list = new ArrayList<Float>();
        received_data_list_string = new ArrayList<String>();
        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    private void initPermission() {
        if ((checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) || (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) || checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MainActivity", "Bluetooth permission is not granted.");
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADMIN}, 200);
        }
        ;//Bluetooth

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(MainActivity.this, "This device does not support BLE", Toast.LENGTH_SHORT).show();
            finish();
        }//BLE

        if ((checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) || (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            Log.d("MainActivity", "Location permission is not granted.");
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        } // GPS

        if ((checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) || (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            Log.d("MainActivity", "Storage permission is not granted.");
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 200);
        } // Storage
    }

    public void onClick(View v) {

    }

    // Turn on BLE
    private void initStart() {
        Log.d("MainActivity", "BluetoothStart.");
        if (mBluetoothAdapter == null) {
            Log.d("MainActivity", "BluetoothAdapter is null.");
            Toast.makeText(this, "This device does not support the Bluetooth function", Toast.LENGTH_SHORT).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Log.d("MainActivity", "BluetoothAdapter is not enabled.");
                Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                startActivityForResult(turnOn, 0);
                Toast.makeText(MainActivity.this, "Please turn on Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
        Log.d("MainActivity", "BluetoothSearch.");
        getBoundedDevices();
        isScaning = true;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //mBluetoothAdapter.startLeScan(scanCallback);
        scanner.startScan(scanCallback);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Search off
                //mBluetoothAdapter.stopLeScan(scanCallback);
                scanner.stopScan(scanCallback);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isScaning = false;
                    }
                });
            }
        }, SCAN_PERIOD);
    }

    private void stopScanDevice() {
        isScaning = false;
        //mBluetoothAdapter.stopLeScan(scanCallback);
        scanner.stopScan(scanCallback);
    }

    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            connection_state.setText(getResources().getString(R.string.searching));
            if (result.getScanRecord().getServiceUuids() != null) {
                if (result.getScanRecord().getServiceUuids().size() > 0) {
                    if (result.getScanRecord().getServiceUuids().get(0).getUuid().toString().equals("a7ea14cf-1000-43ba-ab86-1d6e136a2e9e")) {
                        isConnecting = true;
                        connection_state.setText(getResources().getString(R.string.connecting));
                        text_name.setText(device.getName());
                        mBluetoothGatt = device.connectGatt(MainActivity.this, true, gattCallback, TRANSPORT_LE);
                    }
                }
            }
        }
    };

    // Get bounded devices
    private void getBoundedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        //clear the list
        adapter.clear();
        initData();
        //add to the list
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                adapter.add(device);
            }
        }
    }

    // Connect
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // connection success
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    // find service
                    gatt.discoverServices();
                }
            } else {
                // connection failure
                connection_state.setText(getResources().getString(R.string.connect_fail));
                mBluetoothGatt.close();
                isConnecting = false;
            }
        }

        // discovery devices
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            super.onServicesDiscovered(gatt, status);
            // Only here is the communicable connection really established
            isConnecting = false;
            // Subscribe to notifications
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
                    scanner.stopScan(scanCallback);
                }
            });
        }

        // Receive data returned by hardware
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            final byte[] data = characteristic.getValue();
            ByteBuffer buffer = ByteBuffer.wrap(data);
            float f1 = buffer.order(ByteOrder.LITTLE_ENDIAN).getFloat();
            float f2 = buffer.order(ByteOrder.LITTLE_ENDIAN).getFloat();
            float f3 = buffer.order(ByteOrder.LITTLE_ENDIAN).getFloat();
            String s = String.valueOf("f1:" + f1 + " f2:" + f2 + " f3:" + f3);
            if (f1 - ble_rx_counter > 1.01) {
                // Log.d("MainActivity","Lost packages: " + (f1-ble_rx_counter));
            }
            ble_rx_counter = f1;
            received_data_list.add(f1);
            received_data_list.add(f2);
            received_data_list.add(f3);
            //Log.d("MainActivity","received_data_list: "+received_data_list.size());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    text_msg.setText(s);
                }
            });
        }
    };

    public void save(View view) throws IOException {
        CSVWriter csvWriter = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory() + "/" + "data.csv"));
        received_data_list_string = new ArrayList<>();
        for (int i = 0; i < received_data_list.size(); i++) {
            received_data_list_string.add(String.valueOf(received_data_list.get(i)));
        }
        csvWriter.writeNext(received_data_list_string.toArray(new String[received_data_list_string.size()]));
        csvWriter.close();
        Log.d("MainActivity", "Saved.");
    }
}







