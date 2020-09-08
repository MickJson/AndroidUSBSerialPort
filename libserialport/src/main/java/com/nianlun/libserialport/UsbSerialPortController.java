package com.nianlun.libserialport;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.nianlun.libserialport.usbdriver.SerialInputOutputManager;
import com.nianlun.libserialport.usbdriver.UsbSerialDriver;
import com.nianlun.libserialport.usbdriver.UsbSerialPort;
import com.nianlun.libserialport.usbdriver.UsbSerialProber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author 几圈年轮
 * @email jed.zhu.@qq.com
 * description 串口操作类
 */
public class UsbSerialPortController implements SerialInputOutputManager.Listener, UsbBroadcastReceiver.OnUsbDeviceStateChangeListener {

    private static final int PORT_WRITE_TIME_OUT_MILLIS = 1000;

    private UsbManager mUsbManager;
    private PendingIntent mPermissionIntent;
    private SerialInputOutputManager mSerialInputOutputManager;
    private OnUsbSerialListener mOnUsbSerialListener;
    private boolean isOpened = false;
    private UsbSerialPort mUsbSerialPort;
    private UsbSerialPortParameters mUsbSerialPortParameters;

    public UsbSerialPortController(Context context) {
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(UsbBroadcastReceiver.ACTION_USB_PERMISSION), 0);
        UsbBroadcastReceiver mBroadcastReceiver = new UsbBroadcastReceiver();
        mBroadcastReceiver.registerReceiver(context, this);
    }

    /**
     * 获取所有USB串口设备
     */
    public List<UsbSerialPort> getAllSerialPort() {
        List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        List<UsbSerialPort> result = new ArrayList<>();
        for (UsbSerialDriver driver : drivers) {
            List<UsbSerialPort> ports = driver.getPorts();
            result.addAll(ports);
        }
        return result;
    }

    /**
     * 建立串口通信
     */
    public void openSerialPort(UsbSerialPort usbSerialPort, UsbSerialPortParameters usbSerialPortParameters) {
        if (usbSerialPort == null || usbSerialPortParameters == null) {
            return;
        }
        UsbDeviceConnection connection = mUsbManager.openDevice(usbSerialPort.getDriver().getDevice());
        this.mUsbSerialPort = usbSerialPort;
        this.mUsbSerialPortParameters = usbSerialPortParameters;
        if (connection != null) {
            try {
                usbSerialPort.open(connection);
                usbSerialPort.setParameters(usbSerialPortParameters.getBaudRate(), usbSerialPortParameters.getDataBits(), usbSerialPortParameters.getStopBits(), usbSerialPortParameters.getParity());
                mSerialInputOutputManager = new SerialInputOutputManager(usbSerialPort, this);
                ExecutorService mExecutor = Executors.newSingleThreadExecutor();
                mExecutor.submit(mSerialInputOutputManager);
            } catch (IOException e) {
                if (mOnUsbSerialListener != null) {
                    mOnUsbSerialListener.onSerialOpenException(e);
                }
            }
            isOpened = true;
            if (mOnUsbSerialListener != null) {
                mOnUsbSerialListener.onSerialOpenSuccess();
            }
        } else {
            mUsbManager.requestPermission(usbSerialPort.getDriver().getDevice(), mPermissionIntent);
        }
    }

    /**
     * 发送命令到控制卡
     */
    public boolean sendSerialPort(byte[] commandBytes) {
        if (mUsbSerialPort != null && isOpened && commandBytes.length > 0) {
            try {
                mUsbSerialPort.write(commandBytes, PORT_WRITE_TIME_OUT_MILLIS);
            } catch (IOException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean isOpened() {
        return isOpened;
    }

    /**
     * 关闭串口
     */
    public void closeSerialPort() {
        try {
            if (mUsbSerialPort != null) {
                mUsbSerialPort.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        isOpened = false;
        if (mSerialInputOutputManager != null) {
            mSerialInputOutputManager.stop();
            mSerialInputOutputManager = null;
        }
    }

    @Override
    public void onNewData(byte[] data) {
        if (mOnUsbSerialListener != null) {
            mOnUsbSerialListener.onReceivedData(data);
        }
    }

    @Override
    public void onRunError(Exception e) {
        if (mOnUsbSerialListener != null) {
            mOnUsbSerialListener.onSendException(e);
        }
    }

    public void setUsbSerialConnectListener(OnUsbSerialListener onUsbSerialListener) {
        this.mOnUsbSerialListener = onUsbSerialListener;
    }

    @Override
    public void onUsbDeviceStateChange(int state, UsbDevice usbDevice) {
        if (mUsbSerialPort != null && usbDevice.getDeviceId() == mUsbSerialPort.getDriver().getDevice().getDeviceId()) {
            if (state == ACTION_PERMISSION_GAINED || state == ACTION_DEVICE_ATTACHED) {
                openSerialPort(mUsbSerialPort, mUsbSerialPortParameters);
            } else if (state == ACTION_DEVICE_DETACHED) {
                closeSerialPort();
            }
        }
        if (mOnUsbSerialListener != null) {
            mOnUsbSerialListener.onUsbDeviceStateChange(state, usbDevice);
        }
    }

    public interface OnUsbSerialListener {

        /**
         * 设备连接状态改变
         *
         * @param usbDeviceState 连接状态
         * @param usbDevice      USB设备
         */
        void onUsbDeviceStateChange(int usbDeviceState, UsbDevice usbDevice);

        /**
         * 连接成功
         */
        void onSerialOpenSuccess();

        /**
         * 连接异常
         *
         * @param e 异常
         */
        void onSerialOpenException(Exception e);

        /**
         * 消息发送返回内容
         *
         * @param data 返回信息
         */
        void onReceivedData(byte[] data);

        /**
         * 消息发送异常
         *
         * @param e 异常信息
         */
        void onSendException(Exception e);
    }

}
