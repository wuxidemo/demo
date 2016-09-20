package com.dw.merchant.db;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;

/**
 * Created by lvyongxu on 2016-01-05 09:23.
 */
public class ReceiveRecord implements Parcelable {

    public static final Creator<ReceiveRecord> CREATOR = new Creator<ReceiveRecord>() {
        public ReceiveRecord createFromParcel(Parcel source) {
            return new ReceiveRecord(source);
        }

        public ReceiveRecord[] newArray(int size) {
            return new ReceiveRecord[size];
        }
    };
    /**
     * 订单id
     */
    @DatabaseField(id = true, index = true)
    public long id;
    /**
     * 订单创建的时间
     */
    @DatabaseField
    public long createTime;
    /**
     * 订单是本地的还是网络的
     */
    @DatabaseField
    public boolean isLocal;
    /**
     * 付款的顾客昵称
     */
    @DatabaseField
    public String nickName;
    /**
     * 订单号（我们自己生成）
     */
    @DatabaseField
    public String orderNum;
    /**
     * 支付方式（1：支付宝；2:微信）
     */
    @DatabaseField
    public String payType;
    /**
     * 订单状态 1未申请退款；2退款中；3退款成功；4退款失败。
     */
    @DatabaseField
    public int status;
    /**
     * 订单金额（单位为分）
     */
    @DatabaseField
    public String total;
    /**
     * 订单号（支付平台内部订单号）
     */
    @DatabaseField
    public String translateNum;

    public ReceiveRecord() {
    }

    protected ReceiveRecord(Parcel in) {
        this.id = in.readLong();
        this.createTime = in.readLong();
        this.nickName = in.readString();
        this.orderNum = in.readString();
        this.payType = in.readString();
        this.status = in.readInt();
        this.total = in.readString();
        this.translateNum = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.createTime);
        dest.writeString(this.nickName);
        dest.writeString(this.orderNum);
        dest.writeString(this.payType);
        dest.writeInt(this.status);
        dest.writeString(this.total);
        dest.writeString(this.translateNum);
    }
}
