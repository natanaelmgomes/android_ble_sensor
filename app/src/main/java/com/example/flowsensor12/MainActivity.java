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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import android.util.Log;
import com.opencsv.CSVReader;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private BluetoothAdapter mBluetoothAdapter;
    private TextView connection_state;
    private TextView text_msg;
    private TextView text_name;
    private BluetoothGatt mBluetoothGatt;
    private static final long SCAN_PERIOD = 999999999;
    private BluetoothLeScanner scanner;
    private float ble_rx_counter = 0;
    private List<Float> received_data_list;
    private ArrayList<String> received_data_list_string;
    float[] kaiser_window = new float[1024];

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
        text_msg = (TextView) findViewById(R.id.text_msg);
        text_name = (TextView) findViewById(R.id.text_name);
    }

    private void initData() {
        received_data_list = new ArrayList<Float>();
        received_data_list_string = new ArrayList<String>();
        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    private void initKaiserwindow()
    {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.kaiser_window);
            CSVReader KAISERWINDOW = new CSVReader(new InputStreamReader(inputStream));
            List<String[]> Kaiser_window = KAISERWINDOW.readAll();
            for (int row = 0; row < Kaiser_window.size(); row++) {
                String[] thisRowStrings = Kaiser_window.get(row);
                float[] thisRowFloats = new float[thisRowStrings.length];
                for (int c = 0; c < thisRowStrings.length; c++) {
                    thisRowFloats[c] = Float.parseFloat(thisRowStrings[c]);
                }
                kaiser_window[row] = thisRowFloats[0];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        Intent intent = new Intent();
        intent.setAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivity(intent);
    }

    public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, save_option.class);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("received_data_list_string", received_data_list_string);
        intent.putExtras(bundle);
        startActivity(intent);
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
                startActivity(turnOn);
                Toast.makeText(MainActivity.this, "Please turn on Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
        Log.d("MainActivity", "BluetoothSearch.");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        scanner.startScan(scanCallback);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                //Search off
                scanner.stopScan(scanCallback);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        }, SCAN_PERIOD);
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
                        connection_state.setText(getResources().getString(R.string.connecting));
                        text_name.setText(device.getName());
                        mBluetoothGatt = device.connectGatt(MainActivity.this, true, gattCallback, TRANSPORT_LE);
                    }
                }
            }
        }
    };

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
            }
        }

        // discovery devices
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            super.onServicesDiscovered(gatt, status);
            // Only here is the communicable connection really established
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
            //String s = String.valueOf("f1:" + f1 + " f2:" + f2 + " f3:" + f3);
            received_data_list.add(f1);
            received_data_list.add(f2);
            received_data_list.add(f3);
            if (received_data_list.size() > 1024) {
                Fourier(received_data_list);
            }
            received_data_list_string = new ArrayList<>();
            for (int i = 0; i < received_data_list.size(); i++) {
                received_data_list_string.add(String.valueOf(received_data_list.get(i)));
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //text_msg.setText(s);
                }

            });
        }
    };

    public void Fourier(List<Float> data) {
        double[] data_sum = new double[0];
        for (int i = 0; i < received_data_list.size(); i++) {
            data_sum = Arrays.copyOf(data_sum, data_sum.length + 1);
            data_sum[data_sum.length - 1] = received_data_list.get(i);
        }
        initKaiserwindow();
        double[] data_fft = new double[1024];
        for (int i = 0; i < data_sum.length-1024; i=i+10) {
            for (int j = 0; j < 1024; j++) {
                data_fft[j] = data_sum[i + j];
            }
            double temp_average = getAverage(data_fft);
            for (int k = 0; k < 1024; k++) {
                data_fft[k] = data_fft[k] - temp_average;
                data_fft[k] = data_fft[k] * kaiser_window[k];
            }
            double[] data_fft_temp = new double[16*1024];
            for (int k = 0; k < 16*1024; k++) {
                if(k<1024) {
                    data_fft_temp[k] = data_fft[k];
                } else {
                    data_fft_temp[k] = 0;
                }
            }
            FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
            Complex[] fft_result = fft.transform(data_fft_temp, TransformType.FORWARD);
            double[] data_abs = new double[8*1024];
            for (int k = 0; k < 8*1024; k++) {
                data_abs[k] = fft_result[k].abs();
            }
            //double max = getMax(data_abs);
            int max_index = getMaxIndex(data_abs);
            //Log.d("MainActivity","max: "+max+" max_index: "+max_index);
            double frequency = 0.0006103515625 * max_index;
            double flow_rate = frequency / 0.001251233545;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    text_msg.setText(String.valueOf(flow_rate));
                }

            });
        }
        }
        public double getMax(double[] data) {
            double max = data[0];
            for (int i = 0; i < data.length; i++) {
                if (data[i] > max) {
                    max = data[i];
                }
            }
            return max;
        }
    public int getMaxIndex(double[] data) {
        double max = data[0];
        int max_index = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] > max) {
                max = data[i];
                max_index = i;
            }
        }
        return max_index;
    }
        public double getAverage(double[] data) {
            float sum = 0;
            for (int i = 0; i < data.length; i++) {
                sum += data[i];
            }
            return sum/data.length;
        }
}







