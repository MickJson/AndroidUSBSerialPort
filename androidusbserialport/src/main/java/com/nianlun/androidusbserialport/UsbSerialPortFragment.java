package com.nianlun.androidusbserialport;

import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.nianlun.androidusbserialport.adapter.UsbSerialPortAdapter;
import com.nianlun.libserialport.UsbSerialPortController;
import com.nianlun.libserialport.UsbSerialPortParameters;
import com.nianlun.libserialport.usbdriver.HexDump;
import com.nianlun.libserialport.usbdriver.UsbSerialPort;

import java.util.List;
import java.util.Locale;

public class UsbSerialPortFragment extends Fragment implements UsbSerialPortController.OnUsbSerialListener, View.OnClickListener {

    private UsbSerialPort mUsbSerialPort;

    private UsbSerialPortAdapter mUsbSerialPortAdapter;
    private UsbSerialPortController mUsbSerialPortController;

    private RecyclerView rvListUsbSerialPort;
    private Spinner spBaudRate;
    private Spinner spData;
    private Spinner spStop;
    private Spinner spParity;
    private Button btnOpenOrClose;
    private TextView tvResult;
    private EditText etWriteData;
    private CheckBox cbHex;
    private CheckBox cbHexRev;
    private TextView tvReceiveNumber;
    private Button btnClear;
    private Button btnSend;
    private List<UsbSerialPort> mUsbSerialPortList;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_usb_serial_port, container, false);
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
        mUsbSerialPortAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mUsbSerialPortAdapter.setSelectedPosition(position);
                mUsbSerialPort = (UsbSerialPort) adapter.getItem(position);
            }
        });
    }

    private void initData() {
        mUsbSerialPortController = new UsbSerialPortController(getContext());
        mUsbSerialPortController.setUsbSerialConnectListener(this);
        mUsbSerialPortList = mUsbSerialPortController.getAllSerialPort();
        mUsbSerialPortAdapter = new UsbSerialPortAdapter(mUsbSerialPortList);
        rvListUsbSerialPort.setAdapter(mUsbSerialPortAdapter);
    }

    private void initView(View view) {
        rvListUsbSerialPort = (RecyclerView) view.findViewById(R.id.rv_list_usb_serial_port);
        rvListUsbSerialPort.setLayoutManager(new LinearLayoutManager(getContext()));
        spBaudRate = (Spinner) view.findViewById(R.id.sp_baud_rate);
        spData = (Spinner) view.findViewById(R.id.sp_data);
        spStop = (Spinner) view.findViewById(R.id.sp_stop);
        spParity = (Spinner) view.findViewById(R.id.sp_parity);
        btnOpenOrClose = (Button) view.findViewById(R.id.btn_open_or_close);
        btnOpenOrClose.setOnClickListener(this);
        tvResult = (TextView) view.findViewById(R.id.tv_result);
        etWriteData = (EditText) view.findViewById(R.id.et_write_data);
        cbHex = (CheckBox) view.findViewById(R.id.cb_hex);
        cbHexRev = (CheckBox) view.findViewById(R.id.cb_hex_rev);
        tvReceiveNumber = (TextView) view.findViewById(R.id.tv_receive_number);
        btnClear = (Button) view.findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(this);
        btnSend = (Button) view.findViewById(R.id.btn_send);
        btnSend.setOnClickListener(this);
    }

    @Override
    public void onUsbDeviceStateChange(int usbDeviceState, UsbDevice usbDevice) {
        mUsbSerialPortList = mUsbSerialPortController.getAllSerialPort();
        mUsbSerialPortAdapter.setNewData(mUsbSerialPortList);
        mUsbSerialPortAdapter.setSelectedPosition(-1);
    }

    @Override
    public void onSerialOpenSuccess() {
        showData(getResources().getString(R.string.open_port_success));
    }

    @Override
    public void onSerialOpenException(Exception e) {
        showData(getResources().getString(R.string.open_port_fail));
    }

    @Override
    public void onReceivedData(byte[] data) {
        String receivedData;
        if (cbHexRev.isChecked()) {
            receivedData = HexDump.toHexString(data);
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : data) {
                stringBuilder.append((int) b).append(" ");
            }
            receivedData = stringBuilder.toString();
        }
        showData(receivedData);
        tvReceiveNumber.setText(String.format(Locale.US, "%d", data.length));
    }

    @Override
    public void onSendException(Exception e) {
        showData(getResources().getString(R.string.serial_write_fail));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open_or_close:
                if (mUsbSerialPortController.isOpened()) {
                    mUsbSerialPortController.closeSerialPort();
                    btnOpenOrClose.setText(R.string.open_port);
                } else {
                    mUsbSerialPortController.openSerialPort(mUsbSerialPort, new UsbSerialPortParameters(
                            Integer.parseInt(spBaudRate.getSelectedItem().toString()),
                            Integer.parseInt(spData.getSelectedItem().toString()),
                            Integer.parseInt(spStop.getSelectedItem().toString()),
                            Integer.parseInt(spParity.getSelectedItem().toString())));
                    btnOpenOrClose.setText(R.string.close_port);
                }
                break;
            case R.id.btn_clear:
                tvResult.setText("");
                break;
            case R.id.btn_send:
                String command = etWriteData.getText().toString().trim();
                boolean isWrite;
                if (cbHex.isChecked()) {
                    isWrite = sendHexData(command);
                } else {
                    isWrite = mUsbSerialPortController.sendSerialPort(command.getBytes());
                }
                if (isWrite) {
                    showData(getResources().getString(R.string.serial_write_success));
                } else {
                    showData(getResources().getString(R.string.serial_write_fail));
                }
                break;
        }
    }

    private boolean sendHexData(String command) {
//        String[] hexs = command.split(" ");
//        byte[] bytes = new byte[hexs.length];
//        boolean okflag = true;
//        int i = 0;
//        for (String hex : hexs) {
//            try {
//                int d = Integer.parseInt(hex, 16);
//                if (d > 255) {
//                    showData(String.format(getResources().getString(R.string.greater_than_ff), hex));
//                    okflag = false;
//                } else {
//                    bytes[i] = (byte) d;
//                }
//            } catch (NumberFormatException e) {
//                showData(String.format(getResources().getString(R.string.is_not_hex), hex));
//                e.printStackTrace();
//                okflag = false;
//            }
//            i++;
//        }
        command = command.replace(" ", "");
        byte[] bytes;
        try {
            bytes = HexDump.hexStringToByteArray(command);
        } catch (Exception e) {
            showData(getResources().getString(R.string.is_not_hex, command));
            return false;
        }
        if (bytes.length > 0) {
            return mUsbSerialPortController.sendSerialPort(bytes);
        }
        return false;
    }

    private void showData(final String str) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvResult.append(str + "\n");
            }
        });

    }

}
