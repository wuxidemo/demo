package com.dw.merchant.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dw.merchant.R;
import com.dw.merchant.db.ReceiveRecord;
import com.dw.merchant.util.DateTimeUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by Acer on 2015/11/24.
 */
public class ReceiveRecordsAdapter extends BaseAdapter {

    private static final String TAG = ReceiveRecordsAdapter.class.getSimpleName() + "_lyx";
    private Context context;
    private List<ReceiveRecord> receiveRecords;


    public ReceiveRecordsAdapter(Context context, List<ReceiveRecord> receiveRecords) {

        this.context = context;
        this.receiveRecords = receiveRecords;

    }

    @Override
    public int getCount() {
        return receiveRecords.size();
    }

    @Override
    public ReceiveRecord getItem(int position) {
        return receiveRecords.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //然后重写getView
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_receive_records, null);
            holder.imgPayType = (ImageView) convertView.findViewById(R.id.img_pay_type);
            holder.imgIsLocal = (ImageView) convertView.findViewById(R.id.img_isLocal);
            holder.txtStatus = (TextView) convertView.findViewById(R.id.tv_status);
            holder.txtTotal = (TextView) convertView.findViewById(R.id.tv_bank);
            holder.txtCreateTime = (TextView) convertView.findViewById(R.id.tv_createtime);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ReceiveRecord receiveRecord = getItem(position);

        if (receiveRecord.payType.equals("1")) {
            holder.imgPayType.setImageResource(R.mipmap.ic_pay_alipay);
        } else {
            holder.imgPayType.setImageResource(R.mipmap.ic_pay_weixin);
        }

        switch (receiveRecord.status) {
            case 0:
                holder.txtStatus.setText(R.string.zf_fail);
                break;
            case 1:
                holder.txtStatus.setText(R.string.zf_sucess);
                break;
            case 2:
                holder.txtStatus.setText(R.string.zf_refund_dealing);
                break;
            case 3:
                holder.txtStatus.setText(R.string.zf_refund_ok);
                break;
            case 4:
                holder.txtStatus.setText(R.string.zf_refund_fail);
                break;
            default:
                holder.txtStatus.setText(R.string.zf_unknow);

        }

        holder.txtCreateTime.setText(DateTimeUtils.date2Str(new Date(receiveRecord.createTime), "HH:mm"));
        holder.txtTotal.setText("￥" + String.format("%.2f", (Double.parseDouble(receiveRecord.total) / 100)));

//        Log.e(TAG, "receiveRecord.isLocal=="+receiveRecord.isLocal);
//        if (receiveRecord.isLocal) {
//            holder.imgIsLocal.setVisibility(View.VISIBLE);
//        } else {
//            holder.imgIsLocal.setVisibility(View.GONE);
//        }

        return convertView;
    }

    //在外面先定义，ViewHolder静态类
    static class ViewHolder {
        public ImageView imgPayType;
        public ImageView imgIsLocal;
        public TextView txtTotal;
        public TextView txtCreateTime;
        public TextView txtStatus;
    }
}
