package com.dw.merchant.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.dw.merchant.adapter.VerificationRecordsAdapter;
import com.dw.merchant.util.DateTimeUtils;
import com.dw.merchant.util.HttpClient;
import com.dw.merchant.util.NetUtils;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public final class TabVerificationRecordsActivity extends BaseActivity {

    private static final String TAG = TabVerificationRecordsActivity.class.getSimpleName() + "_lyx";
    private static final String KEY_CONTENT = TabVerificationRecordsActivity.class.getSimpleName();
    String cardid = "";//选择的卡券类型
    private Context context;
    private String merchant_id = "";//getString(R.string.app_name)
    private TextView tv_date;
    private ImageButton imgbtn_left, imgbtn_right, imgbtn_calender;
    private String date = "";//时间作为重要的查询手段
    private int dateTodayNum = 0;
    private String mContent = "???";
    private int j = 0;// j专门表示从昨天开始计算
    private Spinner sp_cards;
    private List<Map> list;
    private List<Map> list_checked_records;//核销记录
    private ListView lst_checked_records;
    private LinearLayout layout_progress;
    private LinearLayout layout_nodata;

    private TextView tv_num, tv_money;

    private double total_money = 0;

    private String merchant_name = "";


    @Override
    protected int addLayout() {
        return R.layout.activity_tab_verification_records;
    }

    @Override
    protected void initLayout() {


        context = TabVerificationRecordsActivity.this;
        Intent intent = getIntent();
        merchant_id = intent.getStringExtra("merchant_id");
        merchant_name = intent.getStringExtra("merchant_name");

        flag = "tab";
        title = merchant_name;


        tv_date = (TextView) findViewById(R.id.tv_date);
        tv_num = (TextView) findViewById(R.id.tv_num);
        tv_money = (TextView) findViewById(R.id.tv_money);

//        Date nowTime = Calendar.getInstance().getTime();
//        SimpleDateFormat f = new SimpleDateFormat("yyyy-MMdd");
//        date = f.format(nowTime);
//        tv_date.setText(date);

        Date today = new Date();
        date = DateTimeUtils.date2Str(today, "yyyy-MM-dd");
        Log.e(TAG, "initLayout: date===" + date);

        dateTodayNum = Integer.parseInt(date.replace("-", ""));

        tv_date.setText(date);

        imgbtn_left = (ImageButton) findViewById(R.id.imgbtn_left);
        imgbtn_right = (ImageButton) findViewById(R.id.imgbtn_right);

        sp_cards = (Spinner) findViewById(R.id.spinner_pay_type);
        sp_cards.setSelection(0, true);
        sp_cards.setDropDownVerticalOffset(50);

        sp_cards.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                // TODO Auto-generated method stub
                String str = parent.getItemAtPosition(position)
                        .toString();

                if (list != null)
                    cardid = list.get(position).get(str).toString();

                Log.e(TAG, "cardid===" + cardid);

                initData(date, merchant_id, cardid);

            }
        });


        imgbtn_left.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                date = DateTimeUtils.getTheDayBefore(date);
                tv_date.setText(date);
                initCards(date, merchant_id);
            }
        });

        imgbtn_right.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                date = DateTimeUtils.getTheDayAfter(date);

                tv_date.setText(date);
                initCards(date, merchant_id);
            }
        });

        imgbtn_calender = (ImageButton) findViewById(R.id.imgbtn_calender);

        imgbtn_calender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, DateChooseActivity.class);
                startActivityForResult(intent, 0);

            }
        });

        lst_checked_records = (ListView) findViewById(R.id.lst_checked_records);

        layout_progress = (LinearLayout) findViewById(R.id.layout_progress);
        layout_nodata = (LinearLayout) findViewById(R.id.layout_nodata);

        initCards(date, merchant_id);

    }

    private void initData(String date, String mid, String cardid) {
//  http://ts.do-wi.cn/nsh/appapi/crlist?time=20151030&merid=47&type=1&cardid=pKEL4smOsqVIvXl4z7bM_tXTzSyk
        total_money = 0;
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

        if (NetUtils.hasNetwork(this)) {
            layout_progress.setVisibility(View.VISIBLE);

            String url;

            if (!cardid.equals(""))
                url = "crlist?time=" + date + "&merid=" + merchant_id + "&type=1" + "&cardid=" + cardid;
            else
                url = "crlist?time=" + date + "&merid=" + merchant_id + "&type=1";

            HttpClient.get(url, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Log.e(TAG, "onSuccess: statusCode==" + statusCode);
                    layout_progress.setVisibility(View.GONE);

                    try {
                        JSONObject data = new JSONObject(new String(responseBody));

                        if (data.get("result").equals("1")) {

                            String list_data = data.get("data").toString();

                            JSONArray array = new JSONArray(list_data);

                            list_checked_records = new ArrayList<>();
                            Map<String, String> map;

                            for (int i = 0; i < array.length(); i++) {

                                map = new HashMap<>();

                                JSONObject object = array.getJSONObject(i);

                                String ownname = object.get("ownname").toString();

                                String bank = object.get("bank").toString();
                                String cardname = object.get("cardname").toString();

                                String usetime = object.get("usetime").toString();

                                //分转元
                                double bankcost = Double.parseDouble(bank) / 100;

                                map.put("bank", String.format("%.2f", bankcost));
                                map.put("cardname", cardname);

                                if (usetime != null && !usetime.isEmpty() && usetime.length() > 11) {
                                    usetime = usetime.substring(11);
                                }
                                map.put("usetime", usetime);
                                map.put("ownname", ownname);

                                total_money += bankcost;

                                list_checked_records.add(map);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String total = String.format("%.2f", total_money);

                    if (list_checked_records.isEmpty()) {

                        layout_nodata.setVisibility(View.VISIBLE);
                        Log.e(TAG, "list_checked_records==" + list_checked_records);
                        VerificationRecordsAdapter adapter = new VerificationRecordsAdapter(list_checked_records, context);
                        lst_checked_records.setAdapter(adapter);
                        tv_num.setText(list_checked_records.size() + "");
                        tv_money.setText(total + "元");

                    } else {
                        Log.e(TAG, "list_checked_records==" + list_checked_records);
                        VerificationRecordsAdapter adapter = new VerificationRecordsAdapter(list_checked_records, context);
                        lst_checked_records.setAdapter(adapter);
                        tv_num.setText(list_checked_records.size() + "");
                        tv_money.setText(total + "元");
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                      Throwable error) {
                    layout_progress.setVisibility(View.GONE);
                    layout_nodata.setVisibility(View.VISIBLE);
                    Log.e(TAG, "onFailure: statusCode==" + statusCode);

                    Toast.makeText(context, R.string.connect_server_error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            layout_progress.setVisibility(View.GONE);
            layout_nodata.setVisibility(View.VISIBLE);
            Toast.makeText(context, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    private void initCards(String date, String mid) {

//  http://ts.do-wi.cn/nsh/appapi/cardnames?time=20151030&merid=47
        date = date.replace("-", "");
        Log.i(TAG, "initCards: date===" + date);

        if (Integer.parseInt(date) >= dateTodayNum) {
            imgbtn_right.setEnabled(false);
            imgbtn_right.setImageResource(R.mipmap.ic_calender_next_disable);
//            imgbtn_right.setVisibility(View.GONE);

        } else {
            imgbtn_right.setEnabled(true);
            imgbtn_right.setImageResource(R.mipmap.ic_calender_next);
//            imgbtn_right.setVisibility(View.VISIBLE);
        }

        if (NetUtils.hasNetwork(this)) {
            HttpClient.get("cardnames?time=" + date + "&merid=" + merchant_id, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Log.e(TAG, "onSuccess: statusCode==" + statusCode);

                    try {
                        JSONArray array = new JSONArray(new String(responseBody));

                        list = new ArrayList<>();
                        String[] mItems = new String[array.length() + 1];
                        Map<String, String> map = new HashMap<>();
                        map.put("卡券", "");
                        list.add(0, map);
                        mItems[0] = "卡券";

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);

                            String name = object.get("name").toString();
                            String cardid = object.get("cardid").toString();

                            mItems[i + 1] = name;
                            map.put(name, cardid);
                            list.add(i + 1, map);

                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                                R.layout.simple_spinner_item, mItems);
                        adapter.setDropDownViewResource(R.layout.
                                simple_spinner_dropdown_item_verification);

                        sp_cards.setAdapter(adapter);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                      Throwable error) {
                    Log.e(TAG, "onFailure: statusCode==" + statusCode);

                    Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
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
            initCards(date, merchant_id);

        }

    }
}
