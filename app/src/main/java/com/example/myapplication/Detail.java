package com.example.myapplication;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

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
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class Detail extends Fragment {
    public static Detail detail = null;
    //UI
    /*BLE*/
    ConstraintLayout blepage;
    ConstraintLayout settingpage;
    ConstraintLayout detailpage;
    Button next;
    ListView listView;
    /*Setting*/
    Button one_plus;
    Button ten_plus;
    Button hun_plus;
    Button one_minus;
    Button ten_minus;
    Button hun_minus;
    Button confirm;
    Button Default;
    TextView flow_rate_set;
    TextView name_input;
    /*Detail*/
    TextView flow_rate_display;
    TextView name;
    Button flowrate;
    Button linechart;
    LineChart lineChart;
    //BLE
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner scanner;
    private TextView connection_state;
    private static final long SCAN_PERIOD = 9999999;
    private BlueToothDeviceAdapter adapter;
    private BluetoothGatt mBluetoothGatt;
    private List<Float> received_data_list=new ArrayList<>();
    private ArrayList<String> received_data_list_string = new ArrayList<>();
    private ArrayList<String> flow_rate_list = new ArrayList<>();
    public static String flow_rate_value = "0";
    float[] kaiser_window = new float[1024];
    private int tmp = 0;
    private UUID notify_UUID_service = UUID.fromString("A7EA14CF-1000-43BA-AB86-1D6E136A2E9E");
    private UUID notify_UUID_chara = UUID.fromString("A7EA14CF-1100-43BA-AB86-1D6E136A2E9E");
    //setting
    int one_count = 0;
    int ten_count = 0;
    int hun_count = 0;
    private String flow_rate_set_value;
    //detail
    public static boolean warning = false;
    private void initTest(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
                           @Override
                           public void run() {
                               flow_rate_value = String.valueOf(Math.random() * 100);
                               received_data_list.add(Float.valueOf(flow_rate_value));;
                               flow_rate_display.setText(String.valueOf((int)Float.parseFloat(flow_rate_value)));
                               Log.d("flow_rate_value", String.valueOf(flow_rate_value));
                               JudgeWarning(Float.parseFloat(flow_rate_value));
                               if(warning == true){
                                   flow_rate_display.setTextColor(Color.RED);
                               }
                               else{
                                   flow_rate_display.setTextColor(Color.GREEN);
                               }
                               setData(received_data_list);
                               EventBus.getDefault().post(new FlowRateWrap(String.valueOf((int)Float.parseFloat(flow_rate_value))));
                           }
                       }
                ,0,1000);
    }
    public void initView(View view){
        /*Main*/
        blepage = view.findViewById(R.id.blepage);
        settingpage = view.findViewById(R.id.settingpage);
        detailpage = view.findViewById(R.id.detailpage);
        /*BLE*/
        next = view.findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blepage.setVisibility(View.INVISIBLE);
                settingpage.setVisibility(View.VISIBLE);
            }
        });
        connection_state = view.findViewById(R.id.connection_state);
        listView = view.findViewById(R.id.listView);
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
                mBluetoothGatt = device.connectGatt(getActivity().getApplicationContext(), true, mGattCallback, TRANSPORT_LE);
            }
        });
        /*Setting*/
        one_plus = view.findViewById(R.id.one_plus);
        one_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Integer.parseInt(flow_rate_set_value)<100)
                {
                    if(one_count < 9) {
                        one_count++;
                        flow_rate_set_value = String.valueOf(hun_count) + String.valueOf(ten_count) + String.valueOf(one_count);
                        flow_rate_set.setText(flow_rate_set_value);
                    }
                }
            }
        });
        ten_plus = view.findViewById(R.id.ten_plus);
        ten_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ten_count < 9) {
                    ten_count++;
                    flow_rate_set_value = String.valueOf(hun_count) + String.valueOf(ten_count) + String.valueOf(one_count);
                    flow_rate_set.setText(flow_rate_set_value);
                }

            }
        });
        hun_plus = view.findViewById(R.id.hun_plus);
        hun_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hun_count < 9) {
                    hun_count++;
                    flow_rate_set_value = String.valueOf(hun_count) + String.valueOf(ten_count) + String.valueOf(one_count);
                    flow_rate_set.setText(flow_rate_set_value);
                }
            }
        });
        one_minus = view.findViewById(R.id.one_minus);
        one_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(one_count > 0) {
                    one_count--;
                    flow_rate_set_value = String.valueOf(hun_count) + String.valueOf(ten_count) + String.valueOf(one_count);
                    flow_rate_set.setText(flow_rate_set_value);
                }
            }
        });
        ten_minus = view.findViewById(R.id.ten_minus);
        ten_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ten_count > 0) {
                    ten_count--;
                    flow_rate_set_value = String.valueOf(hun_count) + String.valueOf(ten_count) + String.valueOf(one_count);
                    flow_rate_set.setText(flow_rate_set_value);
                }
            }
        });
        hun_minus = view.findViewById(R.id.hun_minus);
        hun_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hun_count > 0) {
                    hun_count--;
                    flow_rate_set_value = String.valueOf(hun_count) + String.valueOf(ten_count) + String.valueOf(one_count);
                    flow_rate_set.setText(flow_rate_set_value);
                }
            }
        });
        confirm = view.findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flow_rate_set_value = String.valueOf(hun_count) + String.valueOf(ten_count) + String.valueOf(one_count);
                flow_rate_set.setText(flow_rate_set_value);
                settingpage.setVisibility(View.INVISIBLE);
                detailpage.setVisibility(View.VISIBLE);
                EventBus.getDefault().post(new NameWrap(name_input.getText().toString()));
            }
        });
        flow_rate_set = view.findViewById(R.id.Flow_rate);
        name_input = view.findViewById(R.id.Name_input);
        Default = view.findViewById(R.id.Default);
        Default.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                one_count = 0;
                ten_count = 5;
                hun_count = 0;
                flow_rate_set_value = String.valueOf(hun_count) + String.valueOf(ten_count) + String.valueOf(one_count);
                flow_rate_set.setText(flow_rate_set_value);
            }
        });
        flow_rate_set_value = String.valueOf(hun_count) + String.valueOf(ten_count) + String.valueOf(one_count);
        flow_rate_set.setText(flow_rate_set_value);
        /*Detail*/
        flowrate = view.findViewById(R.id.flowrate);
        flowrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flow_rate_display.setVisibility(View.VISIBLE);
                lineChart.setVisibility(View.INVISIBLE);
            }
        });
        linechart = view.findViewById(R.id.linechart);
        linechart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flow_rate_display.setVisibility(View.INVISIBLE);
                lineChart.setVisibility(View.VISIBLE);
            }
        });
        flow_rate_display = view.findViewById(R.id.flow_rate);
        lineChart = view.findViewById(R.id.chart);
        name = view.findViewById(R.id.name);
        name.setText(name_input.getText().toString());
    }
    public void initSearch(){
        final BluetoothManager mBluetoothManager = (BluetoothManager) MainActivity.mainActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
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

    private void initKaiserwindow()
    {
        try {
            InputStream inputStream = MainActivity.mainActivity.getResources().openRawResource(R.raw.kaiser_window);
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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connection_state.setText("Connection Success");
                    scanner.stopScan(mScanCallback);
                    if (connection_state.getText().equals(getResources().getString(R.string.connect_success))) {
                        next.setEnabled(true);
                    }
                }
            });
        }
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
            if (received_data_list.size() >= 1024) {
                Fourier(received_data_list);
            }else{flow_rate_list.add("0");}
            received_data_list.add(f3);
            if (received_data_list.size() >= 1024) {
                Fourier(received_data_list);
            }else{flow_rate_list.add("0");}
            received_data_list.add(f4);
            Log.d("MainActivity", "Received data: " + f1 + " " + f2 + " " + f3 + " " + f4);
            Log.d("MainActivity", "received_data_list: " + received_data_list.size());
            if (received_data_list.size() >= 1024) {
                Fourier(received_data_list);
            }else{flow_rate_list.add("0");}
            received_data_list_string = new ArrayList<>();
            for (int i = 0; i < received_data_list.size(); i++) {
                received_data_list_string.add(String.valueOf(received_data_list.get(i)));
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setData(received_data_list);
                }

            });
        }
    };
    //Fourier transform
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
                flow_rate_list.add(String.valueOf(flow_rate));
            } else {
                flow_rate_list.add("0");
            }
            //Log.d("MainActivity","frequency: "+frequency+" flow_rate: "+flow_rate);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (frequency > 0.02) {
                        flow_rate_display.setText(String.valueOf((int)flow_rate));
                    }
                }

            });
        }
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

    //Graph
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
        if(Math.abs(flow_rate_value-Float.parseFloat(flow_rate_set_value))>10)
        {
            warning = true;
        }
        else
        {
            warning = false;
        }
    }

    public static class NameWrap {
        public final String message;
        public static Detail.NameWrap getInstance(String message) {
            return new NameWrap(message);
        }
        private NameWrap(String message) {
            this.message = message;
        }
    }

    public static class FlowRateWrap {
        public final String message;
        public static Detail.FlowRateWrap getInstance(String message) {
            return new FlowRateWrap(message);
        }
        private FlowRateWrap(String message) {
            this.message = message;
        }
    }

    public String getName() {return name_input.getText().toString();}
    public String getFlowRate() {
        return flow_rate_display.getText().toString();
    }
    public String getFlowRateSet() {return flow_rate_set_value;}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new BlueToothDeviceAdapter(MainActivity.mainActivity.getApplicationContext(), R.layout.bluetooth_device_list_item);
        detail = this;
        setHasOptionsMenu(true);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle("");
        }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add("Close").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add("Back").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getTitle().equals("Close"))
        {
            MainActivity.mainActivity.getSupportFragmentManager().beginTransaction().hide(this).commit();
            MainActivity.mainActivity.main.setVisibility(View.VISIBLE);
        }
        if (item.getTitle().equals("Back")) {
            if(blepage.getVisibility() == View.VISIBLE) {
                MainActivity.mainActivity.getSupportFragmentManager().beginTransaction().hide(this).commit();
                MainActivity.mainActivity.main.setVisibility(View.VISIBLE);
            }
            else if(settingpage.getVisibility() == View.VISIBLE) {
                settingpage.setVisibility(View.INVISIBLE);
                blepage.setVisibility(View.VISIBLE);
            }
            else if(detailpage.getVisibility() == View.VISIBLE) {
                detailpage.setVisibility(View.INVISIBLE);
                settingpage.setVisibility(View.VISIBLE);
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail,container,false);
        initView(view);
        initKaiserwindow();
        initSearch();
        initTest();
        return view;
    }
}