/*
package com.example.flowsensor12;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class BlueToothTool {
    //建立成员变量
    //来自布局的信息
    BluetoothAdapter mBluetoothAdapter;//蓝牙适配器
    Context context;
    Handler handlerToMain;

    //构造函数
    private BlueToothTool(){
        super();
    }

    public BlueToothTool(BluetoothAdapter mBluetoothAdapter, Context context, Handler handler){
        this.mBluetoothAdapter = mBluetoothAdapter;
        this.context = context;
        this.handlerToMain = handler;
        //打开蓝牙功能
        BlueToothFuctionStart();
    }

    */
/**
     * 1.尝试打开蓝牙功能
     *//*

    private void BlueToothFuctionStart(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter!=null){
            //检测是否开启了蓝牙功能，注意startActivity会开启子线程，无法在此语句后面直接判断蓝牙功能是否被开启
            if(!mBluetoothAdapter.isEnabled()) {
                //尝试开启蓝牙功能
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                context.startActivity(enableBtIntent);
                //注册对蓝牙状态功能的监听事件
                //实例化IntentFilter对象
                IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);      //可以用构造方法添加Action
                context.registerReceiver(bluetoothStateBroadcastReceive,filter);
            }else{
                //直接执行下一步
                //获取过去配对过的信息
                //2.1查找已配对的设备
            }
        }else{
            Toast.makeText(context,"您的设备不支持蓝牙系统",Toast.LENGTH_SHORT).show();
        }
    }

    */
/**
     * 1.1通过广播对蓝牙状态进行监听
     *//*

    private BluetoothStateBroadcastReceive bluetoothStateBroadcastReceive = new BluetoothStateBroadcastReceive();
    private class BluetoothStateBroadcastReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    if (blueState == BluetoothAdapter.STATE_ON) {
                        try {
                            //注销对象
                            context.unregisterReceiver(bluetoothStateBroadcastReceive);
                        } catch (Exception e) {
                            Log.d("蓝牙状态", "注销监听失败，监听对象不存在");
                        }
                    }
                    //执行下一步
                    //获取过去配对过的信息
                    //2.1查找已配对的设备
            }
        }
    }

}
*/
