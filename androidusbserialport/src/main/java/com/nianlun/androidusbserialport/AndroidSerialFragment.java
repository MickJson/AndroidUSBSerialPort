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

public class AndroidSerialFragment extends Fragment implements View.OnClickListener, SerialController.OnSerialListener {

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
    private SerialPortAdapter mSerialPortAdapter;
    private SerialController mSerialController;
    private String mSerialPort;

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
                mSerialPort = (String) adapter.getItem(position);
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

        btnOpenOrClose.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnSend.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open_or_close:
                if (mSerialController.isOpened()) {
                    mSerialController.closeSerialPort();
                    btnOpenOrClose.setText(R.string.open_port);
                } else {
                    mSerialController.openSerialPort(mSerialPort,
                            Integer.parseInt(spBaudRate.getSelectedItem().toString()),
                            Integer.parseInt(spParity.getSelectedItem().toString()));
                    btnOpenOrClose.setText(R.string.close_port);
                }
                break;
            case R.id.btn_clear:
                tvResult.setText("");
                break;
            case R.id.btn_send:
                mSerialController.sendSerialPort(etWriteData.getText().toString().trim());
                break;
        }
    }

    @Override
    public void onReceivedData(String data) {
        showData(data);
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
}
