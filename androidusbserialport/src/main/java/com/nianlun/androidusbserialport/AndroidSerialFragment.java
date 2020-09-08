package com.nianlun.androidusbserialport;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.nianlun.androidusbserialport.adapter.SerialPortAdapter;
import com.nianlun.libserialport.SerialController;
import com.nianlun.libserialport.usbdriver.HexDump;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AndroidSerialFragment extends Fragment implements View.OnClickListener, SerialController.OnSerialListener {

    private ExecutorService execute = Executors.newCachedThreadPool();
    private SerialPortAdapter mSerialPortAdapter;
    private SerialController mSerialController;
    private String mSerialPortPath;

    private Spinner spBaudRate;
    private Spinner spData;
    private Spinner spStop;
    private LinearLayout usbTitle;
    private TextView tvParity;
    private Spinner spParity;
    private Button btnOpenOrClose;
    private RecyclerView rvListUsbSerialPort;
    private TextView tvSendDataCount;
    private TextView tvReceiveDataCount;
    private TextView tvLoseDataCount;
    private TextView tvAbnormalDataCount;
    private TextView tvResult;
    private ScrollView svResult;
    private EditText etWriteData;
    private CheckBox cbHex;
    private CheckBox cbHexRev;
    private TextView tvReceiveNumber;
    private Button btnClear;
    private Button btnSend;
    private Button btnStartTest;
    private Button btnStopTest;
    private SendingThread mTestSendingThread;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_android_serial_port, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initListener();
    }

    private void initListener() {
        mSerialController.setOnSerialListener(this);
        mSerialPortAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mSerialPortPath = (String) adapter.getItem(position);
                mSerialPortAdapter.setSelectedPosition(position);
            }
        });
    }

    private void initData() {
        mSerialController = new SerialController();
        mSerialPortAdapter = new SerialPortAdapter(mSerialController.getAllSerialPortPath());
        rvListUsbSerialPort.setAdapter(mSerialPortAdapter);
    }

    private void initView(View view) {
        spBaudRate = (Spinner) view.findViewById(R.id.sp_baud_rate);
        spData = (Spinner) view.findViewById(R.id.sp_data);
        spStop = (Spinner) view.findViewById(R.id.sp_stop);
        usbTitle = (LinearLayout) view.findViewById(R.id.usb_title);
        usbTitle.setVisibility(View.GONE);
        tvParity = (TextView) view.findViewById(R.id.tv_parity);
        spParity = (Spinner) view.findViewById(R.id.sp_parity);
        btnOpenOrClose = (Button) view.findViewById(R.id.btn_open_or_close);
        rvListUsbSerialPort = (RecyclerView) view.findViewById(R.id.rv_list_usb_serial_port);
        rvListUsbSerialPort.setLayoutManager(new LinearLayoutManager(getContext()));
        tvSendDataCount = (TextView) view.findViewById(R.id.tv_sendDataCount);
        tvReceiveDataCount = (TextView) view.findViewById(R.id.tv_receiveDataCount);
        tvLoseDataCount = (TextView) view.findViewById(R.id.tv_loseDataCount);
        tvAbnormalDataCount = (TextView) view.findViewById(R.id.tv_abnormalDataCount);
        tvResult = (TextView) view.findViewById(R.id.tv_result);
        svResult = (ScrollView) view.findViewById(R.id.sv_result);
        etWriteData = (EditText) view.findViewById(R.id.et_write_data);
        cbHex = (CheckBox) view.findViewById(R.id.cb_hex);
        cbHexRev = (CheckBox) view.findViewById(R.id.cb_hex_rev);
        tvReceiveNumber = (TextView) view.findViewById(R.id.tv_receive_number);
        btnClear = (Button) view.findViewById(R.id.btn_clear);
        btnSend = (Button) view.findViewById(R.id.btn_send);
        btnStartTest = (Button) view.findViewById(R.id.btn_start_test);
        btnStopTest = (Button) view.findViewById(R.id.btn_stop_test);

        btnOpenOrClose.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        btnStartTest.setOnClickListener(this);
        btnStopTest.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open_or_close:
                if (mSerialController.isOpened()) {
                    mSerialController.closeSerialPort();
                    btnOpenOrClose.setText(R.string.open_port);
                } else {
                    mSerialController.openSerialPort(mSerialPortPath,
                            Integer.parseInt(spBaudRate.getSelectedItem().toString()),
                            Integer.parseInt(spParity.getSelectedItem().toString()));
                    btnOpenOrClose.setText(R.string.close_port);
                }
                break;
            case R.id.btn_clear:
                tvResult.setText("");
                break;
            case R.id.btn_send:
                String data = etWriteData.getText().toString().trim();
                byte[] bytes;
                if (cbHex.isChecked()) {
                    try {
                        bytes = HexDump.hexStringToByteArray(data);
                    } catch (Exception e) {
                        showData(getResources().getString(R.string.is_not_hex,data));
                        return;
                    }
                } else {
                    bytes = data.getBytes();
                }
                mSerialController.sendSerialPort(bytes);
                break;
            case R.id.btn_start_test:
                mIsTesting = true;
                resetTest();
                if (mSerialController.isOpened()) {
                    mTestSendingThread = new SendingThread();
                    if (!execute.isShutdown()) {
                        execute.execute(mTestSendingThread);
                    }
                }
                break;
            case R.id.btn_stop_test:
                mIsTesting = false;
                mTestSendingThread.interrupt();
                break;
            default:
                break;
        }
    }

    @Override
    public void onReceivedData(byte[] data, int size) {
        if (mIsTesting) {
            synchronized (mByteReceivedBackSemaphore) {
                int i;
                for (i = 0; i < size; i++) {
                    if (data[i] == mValueToSend && (!mByteReceivedBack)) {
                        mValueToSend++;
                        mByteReceivedBack = true;
                        mByteReceivedBackSemaphore.notify();
                    } else {
                        mCorrupted++;
                    }
                }
            }
        } else {
            String receivedData = "";
            if (cbHexRev.isChecked()) {
                try {
                    receivedData = HexDump.dumpHexString(data, 0, size);
                } catch (Exception e) {
                    showData(getResources().getString(R.string.is_not_hex));
                }
            } else {
                receivedData = new String(data, 0, size);
            }
            showData(receivedData);
        }
    }

    @Override
    public void onSerialOpenSuccess() {
        showData(getResources().getString(R.string.open_port_success));
    }

    @Override
    public void onSerialOpenException(Exception e) {
        showData(getResources().getString(R.string.open_port_fail) + ":" + e.getMessage());
    }

    private void showData(String msg) {
        tvResult.append(msg + "\n");
    }

    private boolean mIsTesting;
    private byte mValueToSend;
    private boolean mByteReceivedBack;
    private final Object mByteReceivedBackSemaphore = new Object();
    private int mIncoming = 0;
    private int mOutgoing = 0;
    private int mLost = 0;
    private int mCorrupted = 0;

    private class SendingThread extends Thread {
        @Override
        public void run() {
            while (mIsTesting) {
                synchronized (mByteReceivedBackSemaphore) {
                    mByteReceivedBack = false;
                    mSerialController.sendSerialPort(new byte[]{mValueToSend});
                    mOutgoing++;
                    try {
                        mByteReceivedBackSemaphore.wait(100);
                        if (mByteReceivedBack) {
                            mIncoming++;
                        } else {
                            mLost++;
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setTestData(mOutgoing, mLost, mIncoming, mCorrupted);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void setTestData(int mOutgoing, int mLost, int mIncoming, int mCorrupted) {
        tvSendDataCount.setText(String.valueOf(mOutgoing));
        tvLoseDataCount.setText(String.valueOf(mLost));
        tvReceiveDataCount.setText(String.valueOf(mIncoming));
        tvAbnormalDataCount.setText(String.valueOf(mCorrupted));
    }

    private void resetTest() {
        mIncoming = 0;
        mOutgoing = 0;
        mLost = 0;
        mCorrupted = 0;
        setTestData(0, 0, 0, 0);
    }
}
