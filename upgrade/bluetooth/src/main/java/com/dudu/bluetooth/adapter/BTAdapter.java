package com.dudu.bluetooth.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dudu.bluetooth.R;

import java.util.List;

/**
 * Created by raise.yang on 2016/03/04.
 */
public class BTAdapter extends BaseAdapter {

    private List<BluetoothDevice> mdatas;
    private Context mCtx;

    public BTAdapter(List<BluetoothDevice> mdatas, Context mCtx) {
        this.mdatas = mdatas;
        this.mCtx = mCtx;
    }

    public void setDatas(List<BluetoothDevice> datas) {
        this.mdatas = datas;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mCtx).inflate(R.layout.item_bt, parent, false);
            holder.name_tv = (TextView) convertView.findViewById(R.id.textview_name);
            holder.mac_tv = (TextView) convertView.findViewById(R.id.textview_mac);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        BluetoothDevice item = getItem(position);

        holder.name_tv.setText(item.getName() == null ? "未知设备" : item.getName());
        holder.mac_tv.setText(item.getAddress());

        return convertView;
    }

    static class ViewHolder {
        TextView name_tv;
        TextView mac_tv;
    }


    @Override
    public int getCount() {
        return mdatas.size();
    }

    @Override
    public BluetoothDevice getItem(int position) {
        return mdatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
