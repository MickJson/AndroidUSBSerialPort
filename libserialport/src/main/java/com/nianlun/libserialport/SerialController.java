package com.nianlun.libserialport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;


public class SerialController {

    private ExecutorService mThreadPoolExecutor = Executors.newCachedThreadPool();
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean isOpened = false;
    private OnSerialListener mOnSerialListener;

    /**
     * 获取所有串口路径
     *
     * @return 串口路径集合
     */
    public List<String> getAllSerialPortPath() {
        SerialPortFinder mSerialPortFinder = new SerialPortFinder();
        String[] deviceArr = mSerialPortFinder.getAllDevicesPath();
        return new ArrayList<>(Arrays.asList(deviceArr));
    }

    /**
     * 打开串口
     *
     * @param serialPath 串口地址
     * @param baudRate   波特率
     * @param flags      标志位
     */
    public void openSerialPort(String serialPath, int baudRate, int flags) {
        try {
            SerialPort serialPort = new SerialPort(new File(serialPath), baudRate, flags);
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            isOpened = true;
            if (mOnSerialListener != null) {
                mOnSerialListener.onSerialOpenSuccess();
            }
            mThreadPoolExecutor.execute(new ReceiveDataThread());
        } catch (Exception e) {
            if (mOnSerialListener != null) {
                mOnSerialListener.onSerialOpenException(e);
            }
        }
    }

    /**
     * 关闭串口
     */
    public void closeSerialPort() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            isOpened = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送串口数据
     *
     * @param bytes 发送数据
     */
    public void sendSerialPort(byte[] bytes) {
        if (!isOpened) {
            return;
        }
        try {
            if (outputStream != null) {
                outputStream.write(bytes);
                outputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回串口是否开启
     *
     * @return 是否开启
     */
    public boolean isOpened() {
        return isOpened;
    }

    /**
     * 串口返回数据内容读取
     */
    private class ReceiveDataThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (isOpened) {
                if (inputStream != null) {
                    byte[] readData = new byte[1024];
                    try {
                        int size = inputStream.read(readData);
                        if (size > 0) {
                            if (mOnSerialListener != null) {
                                mOnSerialListener.onReceivedData(readData, size);
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 设置串口监听
     *
     * @param onSerialListener 串口监听
     */
    public void setOnSerialListener(OnSerialListener onSerialListener) {
        this.mOnSerialListener = onSerialListener;
    }

    /**
     * 串口监听
     */
    public interface OnSerialListener {

        /**
         * 串口数据返回
         */
        void onReceivedData(byte[] data, int size);

        /**
         * 串口打开成功
         */
        void onSerialOpenSuccess();

        /**
         * 串口打开异常
         */
        void onSerialOpenException(Exception e);
    }


}