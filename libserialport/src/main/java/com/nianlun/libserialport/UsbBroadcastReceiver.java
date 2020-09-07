package com.nianlun.libserialport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

public class UsbBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_USB_PERMISSION = "android.intent.USB_PERMISSION";

    private OnUsbDeviceStateChangeListener mOnUsbDeviceStateChangeListener;

    public void registerReceiver(Context context, OnUsbDeviceStateChangeListener onUsbDeviceStateChangeListener) {
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        context.registerReceiver(this, filter);
        this.mOnUsbDeviceStateChangeListener = onUsbDeviceStateChangeListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        UsbDevice mUsbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        int usbState = -1;
        //Usb串口权限
        if (ACTION_USB_PERMISSION.equals(action)) {
            usbState = 0;
        }
        // Usb设备连接
        else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            usbState = 1;
        }
        // Usb设备断开
        else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            usbState = 2;
        }
        if (mOnUsbDeviceStateChangeListener != null) {
            mOnUsbDeviceStateChangeListener.onUsbDeviceStateChange(usbState, mUsbDevice);
        }
    }

    public interface OnUsbDeviceStateChangeListener {
        /**
         * Usb权限获取
         */
        int ACTION_PERMISSION_GAINED = 0;
        /**
         * Usb设备连接
         */
        int ACTION_DEVICE_ATTACHED = 1;
        /**
         * Usb设备断开
         */
        int ACTION_DEVICE_DETACHED = 2;

        void onUsbDeviceStateChange(int usbDeviceState, UsbDevice usbDevice);
    }
}