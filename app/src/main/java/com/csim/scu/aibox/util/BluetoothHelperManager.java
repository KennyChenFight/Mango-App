package com.csim.scu.aibox.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;


import com.csim.scu.aibox.log.Logger;

import java.lang.reflect.Method;
import java.util.Set;

public class BluetoothHelperManager implements BluetoothProfile.ServiceListener {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothHeadset bluetoothHeadset;
    private BluetoothDevice bluetoothDevice;
    private SpeechRecognizerManager speechRecognizerManager;
    private BluetoothReceiver bluetoothReceiver = new BluetoothReceiver();
    private String deviceName;
    private Context context;

    public BluetoothHelperManager(Context context, String deviceName, SpeechRecognizerManager speechRecognizerManager) {
        this.context = context;
        this.deviceName = deviceName;
        this.speechRecognizerManager = speechRecognizerManager;
        doRegisterReceiver();
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean checkSupportBluetooth() {
        if (bluetoothAdapter == null) {
            Logger.d("手機不支援藍芽");
            return false;
        }
        else {
            Logger.d("手機支援藍芽");
            bluetoothAdapter.getProfileProxy(context, this, BluetoothProfile.HEADSET);
            return true;
        }
    }

    public boolean isEnableBluetooth() {
        if (bluetoothAdapter.isEnabled()) {
            Logger.d("藍芽已經開啟");
            return true;
        }
        else {
            Logger.d("藍芽還沒開啟");
            return false;
        }
    }

    public void openBluetooth() {
        bluetoothAdapter.enable();
        Logger.d("開啟藍芽成功");
    }

    public void closeBluetooth() {
        bluetoothAdapter.disable();
    }

    public boolean isAlreadyBindedToDevice() {
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : devices) {
            if (device.getName().equals(deviceName)) {
                bluetoothDevice = device;
                return true;
            }
        }
        return false;
    }

    public void connectToDevice() {
        if (isAlreadyBindedToDevice()) {
            Logger.d("Bonded");
            try {
                Method connect = bluetoothHeadset.getClass().getDeclaredMethod("connect", BluetoothDevice.class);
                connect.setAccessible(true);
                connect.invoke(bluetoothHeadset, bluetoothDevice);
            } catch (Exception e) {
                Logger.e(e.toString());
            }
        }
        else {
            Logger.d("not Bonded");
            bluetoothAdapter.startDiscovery();
            Logger.d("start discovery");
        }
    }

    @Override
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
        if (profile == BluetoothProfile.HEADSET) {
            bluetoothHeadset = (BluetoothHeadset) proxy;
            Logger.d("獲取bluetoothHeadset成功");
        }
    }

    @Override
    public void onServiceDisconnected(int profile) {
        if (profile == BluetoothProfile.HEADSET) {
            bluetoothHeadset = null;
            Logger.d("取消bluetoothHeadset");
        }
    }

    private void bondToDevice(BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice.getName().equals(deviceName)) {
            bluetoothDevice.createBond();
        }
    }

    private void doRegisterReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        context.registerReceiver(bluetoothReceiver, filter);
    }

    public void unRegisterReceiver() {
        context.unregisterReceiver(bluetoothReceiver);
    }

    private class BluetoothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int state = -1;
            Logger.d("onReceive");
            Logger.d("action:" + action);
            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                    bluetoothAdapterReceiver(state);
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    Logger.d("找到藍芽裝置");
                    BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    bondToDevice(bluetoothDevice);
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                    bluetoothDeviceReceiver(state);
                case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED:
                    state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, -1);
                    bluetoothHeadsetReceiver(state);
                    break;
            }
        }
    }

    private void bluetoothAdapterReceiver(int state) {
        switch (state) {
            case BluetoothAdapter.STATE_TURNING_ON:
                Logger.d("藍芽打開中");
                break;
            case BluetoothAdapter.STATE_ON:
                Logger.d("藍芽已打開");
                connectToDevice();
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                Logger.d("藍芽關閉中");
                break;
            case BluetoothAdapter.STATE_OFF:
                Logger.d("藍芽已關閉");
                break;
        }
    }

    private void bluetoothDeviceReceiver(int state) {
        switch (state) {
            case BluetoothDevice.BOND_BONDING:
                Logger.d("藍芽設備綁定中");
                break;
            case BluetoothDevice.BOND_BONDED:
                Logger.d("藍芽設備已綁定");
                break;
            case BluetoothDevice.BOND_NONE:
                Logger.d("無法綁定");
                break;
        }
    }

    private void bluetoothHeadsetReceiver(int state) {
        switch (state) {
            case BluetoothHeadset.STATE_CONNECTED:
                speechRecognizerManager.startListenering();
                Toast.makeText(context, "您可以跟音箱說話了", Toast.LENGTH_LONG).show();
                Logger.d("已連接藍芽耳機");
                break;
            case BluetoothHeadset.STATE_CONNECTING:
                Logger.d("連接藍芽耳機中");
                break;
            case BluetoothHeadset.STATE_DISCONNECTING:
                Logger.d("與藍芽耳機斷開中");
                break;
            case BluetoothHeadset.STATE_DISCONNECTED:
                Logger.d("已斷開藍芽耳機");
                speechRecognizerManager.cancelSpeechRecognizer();
                break;
        }
    }

}
