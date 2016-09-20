package com.dw.merchant.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dw.merchant.R;

import java.util.List;
import java.util.Map;

/**
 * Created by Acer on 2015/11/24.
 */
public class VerificationRecordsAdapter extends BaseAdapter {

    private List<Map> list_checked_records;

    private Context context;

    public VerificationRecordsAdapter(List<Map> list_checked_records, Context context) {

        this.list_checked_records = list_checked_records;
        this.context = context;

    }


    @Override
    public int getCount() {
        return list_checked_records.size();
    }

    @Override
    public Map getItem(int position) {
        return list_checked_records.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //然后重写getView
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_checked_records, null);
            holder.tv_ownname = (TextView)convertView.findViewById(R.id.tv_ownname);
            holder.tv_bank = (TextView)convertView.findViewById(R.id.tv_bank);
            holder.tv_cardname = (TextView)convertView.findViewById(R.id.tv_cardname);
            holder.tv_usetime = (TextView)convertView.findViewById(R.id.tv_usetime);
            convertView.setTag(holder);
        }else
        {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.tv_ownname.setText(getItem(position).get("ownname").toString());
        holder.tv_cardname.setText(getItem(position).get("cardname").toString());
        holder.tv_usetime.setText(getItem(position).get("usetime").toString());
        holder.tv_bank.setText(getItem(position).get("bank").toString() + "元");
        return convertView;
    }

    //在外面先定义，ViewHolder静态类
    static class ViewHolder {
        public TextView tv_ownname;
        public TextView tv_bank;
        public TextView tv_cardname;
        public TextView tv_usetime;
    }
}
