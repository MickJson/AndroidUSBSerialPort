package com.nianlun.androidusbserialport.adapter;

import android.graphics.Color;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.nianlun.androidusbserialport.R;
import com.nianlun.libserialport.usbdriver.UsbSerialPort;

import java.util.List;

public class UsbSerialPortAdapter extends BaseQuickAdapter<UsbSerialPort, BaseViewHolder> {

    private int mSelectedPosition = -1;

    public UsbSerialPortAdapter(List<UsbSerialPort> data) {
        super(R.layout.recyclerview_usb_serial_port_item, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, UsbSerialPort item) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            helper.setText(R.id.tv_manufacturer_usb_serial_port_item, item.getDriver().getDevice().getManufacturerName());
        }
        helper.setText(R.id.tv_vendor_usb_serial_port_item, "vendorId:" + item.getDriver().getDevice().getVendorId());
        helper.setText(R.id.tv_product_usb_serial_port_item, "productId:" + item.getDriver().getDevice().getProductId() + "");
        helper.setText(R.id.tv_name_usb_serial_port_item, "name:" + item.getDriver().getDevice().getDeviceName());

        if (mSelectedPosition == helper.getAdapterPosition()) {
            helper.itemView.setBackgroundColor(Color.GREEN);
        } else {
            helper.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    public void setSelectedPosition(int position) {
        this.mSelectedPosition = position;
        notifyDataSetChanged();
    }
}
