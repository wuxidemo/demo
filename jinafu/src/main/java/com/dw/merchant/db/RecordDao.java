package com.dw.merchant.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.dw.merchant.util.DateTimeUtils;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lvyongxu on 2016-01-05 10:03.
 */
public class RecordDao {

    private String TAG = RecordDao.class.getSimpleName();

    private RuntimeExceptionDao<ReceiveRecord, Integer> dao = null;
    private DataHelper dbHelper = null;

    public RecordDao(Context context) {
        // TODO Auto-generated constructor stub
        this.dbHelper = OpenHelperManager.getHelper(context, DataHelper.class);
        dao = dbHelper.getRuntimeExceptionDao(ReceiveRecord.class);
    }

    /**
     * 添加订单
     *
     * @param receiveRecord
     * @return
     */
    public boolean add(ReceiveRecord receiveRecord) {
        return dao.createIfNotExists(receiveRecord) != null;
    }

    /**
     * 删除订单
     *
     * @param receiveRecord
     * @return
     */
    public boolean delete(ReceiveRecord receiveRecord) {
        return dao.delete(receiveRecord) > 0;
    }

    public boolean deleteAll() {
        return dao.delete(dao.queryForAll()) > 0;
    }

    /**
     * 获取所有本地的订单
     *
     * @return
     */
    public List<ReceiveRecord> getAllRecords() {
        return dao.queryForAll();
    }

    /**
     * 获取所有本地的订单(sql语句查询)
     *
     * @return
     */
    public List<ReceiveRecord> getAllRecordsBySQL() {
        SQLiteDatabase database = this.dbHelper.getReadableDatabase();
        String sql = "select id, createTime, nickName, orderNum, payType, total, translateNum, status, isLocal from ReceiveRecord";
        Cursor cursor = database.rawQuery(sql, new String[]{});
        List<ReceiveRecord> receiveRecords = processCursor(cursor);
        Log.e(TAG, "getAllRecords: " + receiveRecords.size());
        return receiveRecords;
    }

    /**
     * 根据所给的日期(日期的毫秒数)和支付平台，获取所有那天的订单
     *
     * @param date
     * @param payType
     * @return
     */
    public List<ReceiveRecord> getRecords(String date, String payType) {
        SQLiteDatabase database = this.dbHelper.getReadableDatabase();
        long timeStamp = DateTimeUtils.str2Date(date, "yyyyMMdd").getTime();

        String sql;
        Cursor cursor;
        if (payType.equals("0")) {
            sql = "select id, createTime, nickName, orderNum, payType, total, translateNum, status, isLocal from ReceiveRecord where createTime > ? and createTime < ?";
            cursor = database.rawQuery(sql, new String[]{(timeStamp + ""), (timeStamp + 86399999) + ""});
        } else {
            sql = "select id, createTime, nickName, orderNum, payType, total, translateNum, status, isLocal from ReceiveRecord where createTime = ? and payType = ?";
            cursor = database.rawQuery(sql, new String[]{date, payType});
        }
        List<ReceiveRecord> receiveRecords = processCursor(cursor);
        Log.e(TAG, "getRecords: " + receiveRecords.size());
        return receiveRecords;
    }

    /**
     * 把cursor对象转换成需要的集合对象
     *
     * @param cursor 需要处理的对象
     * @return 符合条件的对象集合
     */
    private List<ReceiveRecord> processCursor(Cursor cursor) {
        List<ReceiveRecord> lstResult = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                ReceiveRecord receiveRecord = new ReceiveRecord();
                receiveRecord.id = cursor.getLong(0);
                receiveRecord.createTime = cursor.getLong(1);
                receiveRecord.nickName = cursor.getString(2);
                receiveRecord.orderNum = cursor.getString(3);
                receiveRecord.payType = cursor.getString(4);
                receiveRecord.total = cursor.getString(5);
                receiveRecord.translateNum = cursor.getString(6);
                receiveRecord.status = cursor.getInt(7);
                receiveRecord.isLocal = cursor.getInt(8) == 1 ? true : false;
                lstResult.add(receiveRecord);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return lstResult;
    }
}
