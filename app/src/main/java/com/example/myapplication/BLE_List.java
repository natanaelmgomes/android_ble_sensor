package com.example.myapplication;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

import androidx.appcompat.app.AppCompatActivity;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.opencsv.CSVReader;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BLE_List extends AppCompatActivity {

    public static BLE_List ble_list = null;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner scanner;
    private ListView listView;
    private TextView connection_state;
    private static final long SCAN_PERIOD = 9999999;
    private BlueToothDeviceAdapter adapter;
    public BluetoothGatt mBluetoothGatt;
    private List<Float> received_data_list=new ArrayList<>();
    public static ArrayList<String> received_data_list_string = new ArrayList<>();
    public ArrayList<Float> flow_rate_list;
    public String flow_rate_value;
    float[] kaiser_window = new float[1024];
    private int tmp = 0;
    private UUID notify_UUID_service = UUID.fromString("A7EA14CF-1000-43BA-AB86-1D6E136A2E9E");
    private UUID notify_UUID_chara = UUID.fromString("A7EA14CF-1100-43BA-AB86-1D6E136A2E9E");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_list);
        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
        ble_list = this;
        initSearch();
        initView();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
                           @Override
                           public void run() {
                               flow_rate_value = String.valueOf(Math.random() * 100);
                               EventBus.getDefault().post(new FlowRateWrap(flow_rate_value));
                               received_data_list.add(Float.valueOf(flow_rate_value));
                               EventBus.getDefault().post(new VoltageWrap(received_data_list));
                           }
                       }
        ,0,1000);
    }
    public void onClick(View view) {
        Intent intent = new Intent(this, Setting_page.class);
        startActivity(intent);
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
            if(device.getName() != null){
                for (int i = 0; i < adapter.getCount(); i++) {
                    if (adapter.getItem(i).getAddress().equals(device.getAddress())) {
                        return;
                    }
                }
                adapter.add(device);
            }
        }
    };

    public void initView() {
        connection_state = (TextView) findViewById(R.id.connection_state);
        listView = (ListView) findViewById(R.id.listView);
        adapter = new BlueToothDeviceAdapter(getApplicationContext(), R.layout.bluetooth_device_list_item);
        //getBoundedDevices();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                if(mBluetoothGatt != null){
                    mBluetoothGatt.disconnect();
                }
                BluetoothDevice device = (BluetoothDevice) adapter.getItem(position);
                connection_state.setText(getResources().getString(R.string.connecting));
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.close();
                }
                mBluetoothGatt = device.connectGatt(getApplicationContext(), true, mGattCallback, TRANSPORT_LE);
            }
        });
    }

    public static class FlowRateWrap {
        public final String message;
        public static FlowRateWrap getInstance(String message) {return new FlowRateWrap(message);}
        private FlowRateWrap(String message) {
            this.message = message;
        }
    }
    public static class VoltageWrap {
        public final List<Float> message;
        public static VoltageWrap getInstance(List<Float> message) {return new VoltageWrap(message);}
        private VoltageWrap(List<Float> message) {
            this.message = message;
        }
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

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
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
                    connection_state.setText("Connection Success");
                    scanner.stopScan(mScanCallback);
                    if (connection_state.getText().equals(getResources().getString(R.string.connect_success))) {
                        Intent intent = new Intent(BLE_List.this, Setting_page.class);
                        startActivity(intent);
                    }
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
            float f4 = buffer.order(ByteOrder.LITTLE_ENDIAN).getFloat();
            received_data_list.add(f2);
            EventBus.getDefault().post(new VoltageWrap(received_data_list));
            if (received_data_list.size() >= 1024) {
                Fourier(received_data_list);
            }else{flow_rate_list.add(Float.valueOf("0"));}
            received_data_list.add(f3);
            EventBus.getDefault().post(new VoltageWrap(received_data_list));
            if (received_data_list.size() >= 1024) {
                Fourier(received_data_list);
            }else{flow_rate_list.add(Float.valueOf("0"));}
            received_data_list.add(f4);
            EventBus.getDefault().post(new VoltageWrap(received_data_list));
            Log.d("MainActivity", "Received data: " + f1 + " " + f2 + " " + f3 + " " + f4);
            flow_rate_list = new ArrayList<>();
            Log.d("MainActivity", "received_data_list: " + received_data_list.size());
            if (received_data_list.size() >= 1024) {
                Fourier(received_data_list);
            }else{flow_rate_list.add(Float.valueOf("0"));}
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
        //for (int i = 0; i < data_sum.length-1024; i=i+10) {
        if(tmp <= data_sum.length-1024) {
            for (int j = 0; j < 1024; j++) {
                //data_fft[j] = data_sum[i + j];
                data_fft[j] = data_sum[tmp + j];
            }
            tmp = tmp + 10;
            double temp_average = getAverage(data_fft);
            for (int k = 0; k < 1024; k++) {
                data_fft[k] = data_fft[k] - temp_average;
                data_fft[k] = data_fft[k] * kaiser_window[k];
            }
            double[] data_fft_temp = new double[16 * 1024];
            for (int k = 0; k < 16 * 1024; k++) {
                if (k < 1024) {
                    data_fft_temp[k] = data_fft[k];
                } else {
                    data_fft_temp[k] = 0;
                }
            }
            FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
            Complex[] fft_result = fft.transform(data_fft_temp, TransformType.FORWARD);
            double[] data_abs = new double[8 * 1024];
            for (int k = 0; k < 8 * 1024; k++) {
                data_abs[k] = fft_result[k].abs();
            }
            //double max = getMax(data_abs);
            int max_index = getMaxIndex(data_abs);
            //Log.d("MainActivity","max: "+max+" max_index: "+max_index);
            double frequency = 0.0006103515625 * max_index;
            double flow_rate = frequency / 0.001251233545;
            Log.d("MainActivity",   " flow_rate: " + flow_rate);
            if (frequency > 0.02) {
                flow_rate_list.add(Float.valueOf((float) flow_rate));
            } else {
                flow_rate_list.add(Float.valueOf("0"));
            }
            //Log.d("MainActivity","frequency: "+frequency+" flow_rate: "+flow_rate);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (frequency > 0.02) {
                        flow_rate_value = String.valueOf(flow_rate);
                        EventBus.getDefault().post(new FlowRateWrap(flow_rate_value));
                    }
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