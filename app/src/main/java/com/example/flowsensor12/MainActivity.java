package com.example.flowsensor12;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.flowsensor12.adapter.BlueToothDeviceAdapter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter;
    private BlueToothDeviceAdapter adapter;
    private ListView listView;
    private TextView text_state;
    //private TextView text_msg;
    private final int BUFFER_SIZE = 1024;
    private static final String NAME = "Sensor";
    private static final UUID FS_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ConnectThread connectThread;
    //private ListenerThread listenerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        initReceiver();
        //listenerThread = new ListenerThread();
        //listenerThread.start();
    }

    public void initView() {
        findViewById(R.id.btn_openBT).setOnClickListener(this);
        findViewById(R.id.btn_search).setOnClickListener(this);
        //findViewById(R.id.btn_send).setOnClickListener(this);
        text_state = (TextView) findViewById(R.id.text_state);
        //text_msg = (TextView) findViewById(R.id.text_msg);
        listView = (ListView) findViewById(R.id.listView);
        adapter = new BlueToothDeviceAdapter(getApplicationContext(), R.layout.bluetooth_device_list_item);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                BluetoothDevice device = (BluetoothDevice) adapter.getItem(position);
                //连接设备
                connectDevice(device);
            }
        });
    }

    private void initReceiver() {
        //注册广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
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
            //列表
            /*case R.id.btn_send:
                if (connectThread != null) {
                    connectThread.sendMsg("This is the message sent by bluetooth");
                }
                break;*/
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
            //开启被其它蓝牙设备发现的功能
            if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                //设置为一直开启
                i.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                startActivity(i);
            } else {
                Toast.makeText(MainActivity.this, "Bluetooth is Already on", Toast.LENGTH_LONG).show();
            }
        }
    }

    //搜索蓝牙设备
    private void BluetoothSearch() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        getBoundedDevices();
        if ((checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                || (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }//GPS开启
        mBluetoothAdapter.startDiscovery();
    }

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
    private void connectDevice(BluetoothDevice device) {
        text_state.setText(getResources().getString(R.string.connecting));
        try {
            //创建Socket
            BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(FS_UUID);
            connectThread = new ConnectThread(socket, device, true);
            connectThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消搜索
        if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        //注销BroadcastReceiver，防止资源泄露
        unregisterReceiver(mReceiver);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //去除没有名字的设备
                if (device.getName() != null) {
                    adapter.add(device);
                }
                //避免重复添加已经绑定过的设备
                /*if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    adapter.add(device);
                    adapter.notifyDataSetChanged();
                }*/
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Toast.makeText(MainActivity.this, "start searching", Toast.LENGTH_SHORT).show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(MainActivity.this, "searching complete", Toast.LENGTH_SHORT).show();
            }
        }
    };

    //连接线程
    private class ConnectThread extends Thread {
        private BluetoothSocket socket;
        private BluetoothDevice device;
        private boolean activeConnect;
        InputStream inputStream;
        OutputStream outputStream;
        private ConnectThread(BluetoothSocket socket, BluetoothDevice device, boolean connect) {
            this.socket = socket;
            this.activeConnect = connect;
            this.device = device;
        }

        @Override
        public void run() {
            new Thread() {
                public void run() {
                    try {
                        //如果是自动连接 则调用连接方法
                        if (activeConnect) {
                            socket.connect();
                        }
                        text_state.post(new Runnable() {
                            @Override
                            public void run() {
                                text_state.setText(getResources().getString(R.string.connect_success));
                            }
                        });
                        inputStream = socket.getInputStream();
                        outputStream = socket.getOutputStream();

                        byte[] buffer = new byte[BUFFER_SIZE];
                        int bytes;
                        while (true) {
                            //读取数据
                            bytes = inputStream.read(buffer);
                            if (bytes > 0) {
                                final byte[] data = new byte[bytes];
                                System.arraycopy(buffer, 0, data, 0, bytes);
                        /*text_msg.post(new Runnable() {
                            @Override
                            public void run() {
                                text_msg.setText(getResources().getString(R.string.get_msg) + new String(data));
                            }
                        });*/
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        text_state.post(new Runnable() {
                            @Override
                            public void run() {
                                text_state.setText(getResources().getString(R.string.connect_error));
                            }
                        });
                    }
                }
            }.start();
        }
    }
}



/*        public void sendMsg(final String msg) {

            byte[] bytes = msg.getBytes();
            if (outputStream != null) {
                try {
                    //发送数据
                    outputStream.write(bytes);
                    text_msg.post(new Runnable() {
                        @Override
                        public void run() {
                            text_msg.setText(getResources().getString(R.string.send_msgs) + msg);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    text_msg.post(new Runnable() {
                        @Override
                        public void run() {
                            text_msg.setText(getResources().getString(R.string.send_msg_error) + msg);
                        }
                    });
                }
            }
        }*/


   /* //监听线程
    private class ListenerThread extends Thread {

        private BluetoothServerSocket serverSocket;
        private BluetoothSocket socket;

        @Override
        public void run() {
            try {
                serverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, FS_UUID);
                    while (true) {
                        //线程阻塞，等待别的设备连接
                        socket = serverSocket.accept();
                        text_state.post(new Runnable() {
                            @Override
                            public void run() {
                                text_state.setText(getResources().getString(R.string.connecting));
                            }
                        });
                        connectThread = new ConnectThread(socket, false);
                        connectThread.start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

}
*/





