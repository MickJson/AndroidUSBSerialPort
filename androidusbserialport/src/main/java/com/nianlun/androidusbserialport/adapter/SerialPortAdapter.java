package com.nianlun.androidusbserialport.adapter;

import android.graphics.Color;
import android.os.Build;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.nianlun.androidusbserialport.R;
import com.nianlun.libserialport.usbdriver.UsbSerialPort;

import java.util.List;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

public class SerialPortAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    private int mSelectedPosition = -1;

    public SerialPortAdapter(List<String> data) {
        super(R.layout.recyclerview_serial_port_item, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, String item) {
        helper.setText(R.id.tv_name_serial_port_item, item);
        if (mSelectedPosition == helper.getAdapterPosition()) {
            helper.itemView.setBackgroundColor(Color.GREEN);
        }else{
            helper.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    public void setSelectedPosition(int position) {
        this.mSelectedPosition = position;
        notifyDataSetChanged();
    }
}
