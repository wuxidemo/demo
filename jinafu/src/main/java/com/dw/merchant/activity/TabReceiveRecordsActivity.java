package com.dw.merchant.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dw.merchant.R;
import com.dw.merchant.adapter.ReceiveRecordsAdapter;
import com.dw.merchant.db.ReceiveRecord;
import com.dw.merchant.db.RecordDao;
import com.dw.merchant.util.DateTimeUtils;
import com.dw.merchant.util.HttpClient;
import com.dw.merchant.util.NetUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public final class TabReceiveRecordsActivity extends BaseActivity {

    private static final String TAG = TabReceiveRecordsActivity.class.getSimpleName() + "_lyx";
    private static final String KEY_CONTENT = TabReceiveRecordsActivity.class.getSimpleName();
    private Context context;
    private String merchant_id = "";//getString(R.string.app_name)
    private TextView tv_date;
    private ImageButton imgbtn_left, imgbtn_right, imgbtn_calender;
    private String date = "";//时间作为重要的查询手段
    private int dateTodayNum = 0;
    private String mContent = "???";
    private Spinner spinnerPayType;
    private ListView listViewOrders;
    private LinearLayout layout_progress;
    private LinearLayout layout_nodata;
    private TextView tv_num, tv_money;

    private double total_money = 0;

    private String merchant_name = "";

    private int width = 0;

    private RecordDao recordDao;

    private List<ReceiveRecord> records = new ArrayList<>();//所有收款记录
    private List<ReceiveRecord> recordsNetwork = new ArrayList<>();//收款记录(网络数据)
    private List<ReceiveRecord> recordsLocal = new ArrayList<>();//收款记录(本地未上传至服务器的数据)

    @Override
    protected int addLayout() {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        width = metric.widthPixels;  // 屏幕宽度（像素）
        Log.e(TAG, "addLayout: width===" + width);
        return R.layout.activity_tab_receive_records;
    }

    @Override
    protected void initLayout() {

        this.context = this;

        recordDao = new RecordDao(context);

        Log.e(TAG, "getAllRecords().size()=" + recordDao.getAllRecords().size());

        Intent intent = getIntent();
        merchant_id = intent.getStringExtra("merchant_id");
        merchant_name = intent.getStringExtra("merchant_name");

        flag = "tab";
        title = merchant_name;

        tv_date = (TextView) findViewById(R.id.tv_date);
        tv_num = (TextView) findViewById(R.id.tv_num);
        tv_money = (TextView) findViewById(R.id.tv_money);

        imgbtn_left = (ImageButton) findViewById(R.id.imgbtn_left);
        imgbtn_calender = (ImageButton) findViewById(R.id.imgbtn_calender);
        imgbtn_right = (ImageButton) findViewById(R.id.imgbtn_right);

        spinnerPayType = (Spinner) findViewById(R.id.spinner_pay_type);

        listViewOrders = (ListView) findViewById(R.id.lst_checked_records);
        layout_progress = (LinearLayout) findViewById(R.id.layout_progress);
        layout_nodata = (LinearLayout) findViewById(R.id.layout_nodata);

//        imgbtn_right.setEnabled(false);

        Date today = new Date();
        date = DateTimeUtils.date2Str(today, "yyyy-MM-dd");
        Log.e(TAG, "addLayout: date===" + date);
        dateTodayNum = Integer.parseInt(date.replace("-", ""));
        tv_date.setText(date);

        spinnerPayType.setSelection(0, true);
        spinnerPayType.setDropDownVerticalOffset(50);
        spinnerPayType.setDropDownWidth(width);
        spinnerPayType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                String str = parent.getItemAtPosition(position).toString();
                String payType = "0";
                switch (str) {
                    case "支付方式":
                        payType = "0";
                        break;
                    case "支付宝":
                        payType = "1";
                        break;
                    case "微信":
                        payType = "2";
                        break;
                }
                initData(date, payType);
            }
        });

        listViewOrders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(context, PayOrderDetailsActivity.class);
                intent.putExtra("result", getString(R.string.zf_sucess));

                intent.putExtra("order", records.get(position));

                intent.putExtra("merchant_id", merchant_id);
                intent.putExtra("merchant_name", merchant_name);
                intent.putExtra("action", "skjl");

                startActivity(intent);

            }
        });

        imgbtn_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                date = DateTimeUtils.getTheDayBefore(date);
                tv_date.setText(date);
                initPayType("0");
            }
        });

        imgbtn_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                date = DateTimeUtils.getTheDayAfter(date);
                tv_date.setText(date);
                initPayType("0");
            }
        });
        imgbtn_calender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DateChooseActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        initPayType("0");


    }

    private void initPayType(String paytype) {
        List<String> mItems = new ArrayList<>();
        if (paytype.equals("0")) {
            mItems.add("支付方式");
            mItems.add("微信");
            mItems.add("支付宝");
        } else if (paytype.equals("1")) {
            mItems.add("支付方式");
            mItems.add("支付宝");
        } else if (paytype.equals("2")) {
            mItems.add("支付方式");
            mItems.add("微信");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                R.layout.simple_spinner_item, mItems);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinnerPayType.setAdapter(adapter);
    }

    private void initData(String date, String payType) {
//        http://ts.do-wi.cn/nsh/appapi/getPayorders?createtime=20151130&merchantid=222&paytype=0
        total_money = 0;
        records.clear();
        layout_nodata.setVisibility(View.GONE);
        date = date.replace("-", "");
        Log.i(TAG, "initData: date===" + date);

        if (Integer.parseInt(date) >= dateTodayNum) {
            imgbtn_right.setEnabled(false);
            imgbtn_right.setImageResource(R.mipmap.ic_calender_next_disable);
//            imgbtn_right.setVisibility(View.GONE);

        } else {
            imgbtn_right.setEnabled(true);
            imgbtn_right.setImageResource(R.mipmap.ic_calender_next);
//            imgbtn_right.setVisibility(View.VISIBLE);
        }

        recordsLocal = recordDao.getRecords(date, payType);

        if (NetUtils.hasNetwork(this)) {
            layout_progress.setVisibility(View.VISIBLE);

            getOrders(date, payType);

        } else {

            initOrdersData();

            layout_progress.setVisibility(View.GONE);
            Toast.makeText(context, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
        }

        for (ReceiveRecord record : recordDao.getAllRecords()) {
            uploadLocalRecord(record);
        }

    }

    /**
     * 从服务器获取所有指定日期指定平台的订单数据
     *
     * @param date    指定日期
     * @param payType 1:支付宝；2:微信
     */
    private void getOrders(String date, String payType) {
        String url = "getPayorders?createtime=" + date + "&merchantid=" + merchant_id + "&paytype=" + payType;

        recordsNetwork.clear();

        HttpClient.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                super.onSuccess(statusCode, headers, response);
                Log.e(TAG, "onSuccess: statusCode==" + statusCode);
                Log.e(TAG, "onSuccess: response==" + response);
                layout_progress.setVisibility(View.GONE);
                try {
                    if (response.get("msg").equals("1")) {
                        JSONArray array = response.optJSONArray("obj");
                        int length = array.length();
//                        double totalNetwork = 0;

                        for (int i = 0; i < length; i++) {
                            JSONObject object = array.getJSONObject(i);
                            ReceiveRecord receiveRecord = new ReceiveRecord();
                            receiveRecord.createTime = DateTimeUtils.str2Date(object.getString("createtime"),
                                    "yyyyMMddHHmm").getTime();
                            receiveRecord.nickName = object.getString("nickname");
                            receiveRecord.orderNum = object.getString("ordernum");
                            receiveRecord.payType = object.getString("paytype");
                            receiveRecord.status = object.getInt("state");
                            receiveRecord.total = object.getString("total");
                            receiveRecord.translateNum = object.getString("translatenum");
                            receiveRecord.isLocal = false;

                            //分转元
//                            double total = Double.parseDouble(receiveRecord.total) / 100;
                            total_money += (Double.parseDouble(receiveRecord.total) / 100);

                            recordsNetwork.add(receiveRecord);
                        }

//                        total_money += totalNetwork;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                initOrdersData();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                layout_progress.setVisibility(View.GONE);
                layout_nodata.setVisibility(View.VISIBLE);
                Log.e(TAG, "onFailure: statusCode==" + statusCode);
                Toast.makeText(context, R.string.connect_server_error, Toast.LENGTH_SHORT).show();
                initOrdersData();
            }
        });
    }

    private void initOrdersData() {
        for (ReceiveRecord receiveRecord : recordsLocal) {
            total_money += (Double.parseDouble(receiveRecord.total) / 100);
        }

        Log.e(TAG, "recordsNetwork.size()= " + recordsNetwork.size());
        Log.e(TAG, "recordsLocal.size()= " + recordsLocal.size());

        records.addAll(recordsNetwork);
        records.addAll(recordsLocal);

        Collections.sort(records, new Comparator<ReceiveRecord>() {
            @Override
            public int compare(ReceiveRecord lhs, ReceiveRecord rhs) {
                return String.valueOf(rhs.createTime).compareTo(String.valueOf(lhs.createTime));
            }
        });

        ReceiveRecordsAdapter adapter = new ReceiveRecordsAdapter(context, records);
        listViewOrders.setAdapter(adapter);
        tv_num.setText(String.valueOf(records.size()));
        tv_money.setText(String.format("%.2f", total_money) + "元");

        if (records.isEmpty()) {
            layout_nodata.setVisibility(View.VISIBLE);
        } else {
            layout_nodata.setVisibility(View.GONE);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e(TAG, "onSaveInstanceState");
        outState.putString(KEY_CONTENT, mContent);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == 100) {

            date = data.getStringExtra("date");
            tv_date.setText(date);
            initPayType("0");

        }
    }

    /**
     * 将本地收款记录上传至服务器
     *
     * @param record
     */
    private void uploadLocalRecord(final ReceiveRecord record) {

        String createTime = DateTimeUtils.date2Str(new Date(record.createTime), "yyyyMMddHHmm");
        RequestParams params = new RequestParams();
        params.add("createtime", createTime);
        params.add("merchantid", merchant_id);
        params.add("nickname", record.nickName);
        params.add("ordernum", record.orderNum);
        params.add("paytype", record.payType);
        params.add("total", record.total);
        params.add("translatenum", record.translateNum);

        Log.e(TAG, "onSuccess: record==" + merchant_id);
        Log.e(TAG, "onSuccess: record==" + record.orderNum);
        Log.e(TAG, "onSuccess: createTime==" + createTime);

        HttpClient.post("savealipay", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.e(TAG, "onSuccess: statusCode==" + statusCode);
                Log.e(TAG, "onSuccess: response==" + response);
                if (response.opt("result").equals("1")) {
                    Log.e(TAG, "onSuccess: 本地收款记录上传成功");
                    recordDao.delete(record);
                } else if (response.opt("result").equals("2")) {
                    Log.e(TAG, "onSuccess: 该记录已经存在，无需再次上传");
                    recordDao.delete(record);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                  JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.e(TAG, "onSuccess: statusCode==" + statusCode);
            }
        });
    }

}
