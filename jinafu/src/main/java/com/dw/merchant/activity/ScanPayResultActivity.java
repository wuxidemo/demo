package com.dw.merchant.activity;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.dw.merchant.R;
import com.dw.merchant.db.ReceiveRecord;
import com.dw.merchant.db.RecordDao;
import com.dw.merchant.util.DateTimeUtils;
import com.dw.merchant.util.HttpClient;
import com.dw.merchant.util.NetUtils;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class ScanPayResultActivity extends BaseActivity {

    private static final String TAG = ScanPayResultActivity.class.getSimpleName() + "_lyx";
    private Context context;

    private TextView tv_status, tv_money, tv_out_trade_no, tv_send_pay_date, tv_pay_type;

    private CircularProgressButton btnSure;
    private String paytype = "";

    private ReceiveRecord receiveRecord;

    private RecordDao recordDao;

    @Override
    protected int addLayout() {
        return R.layout.activity_scan_prepay_result;
    }

    @Override
    protected void initLayout() {

        flag = "qrcomplete";

        this.context = this;

        Intent intent = getIntent();
        String result = intent.getStringExtra("result");
        String buyer_pay_amount = intent.getStringExtra("buyer_pay_amount");
        String trade_no = intent.getStringExtra("trade_no");
        String out_trade_no = intent.getStringExtra("out_trade_no");
        String nickName = intent.getStringExtra("buyer_logon_id");
        String send_pay_date = intent.getStringExtra("send_pay_date");
        String state = intent.getStringExtra("state");
        String action = intent.getStringExtra("action");

        merchant_id = intent.getStringExtra("merchant_id");
        merchant_name = intent.getStringExtra("merchant_name");
        type = intent.getStringExtra("type");

        Log.e(TAG, "merchant_id==" + merchant_id);
        Log.e(TAG, "merchant_name==" + merchant_name);

        tv_status = (TextView) findViewById(R.id.tv_status);
        tv_money = (TextView) findViewById(R.id.tv_money);
        tv_out_trade_no = (TextView) findViewById(R.id.tv_out_trade_no);
        tv_send_pay_date = (TextView) findViewById(R.id.tv_send_pay_date);
        tv_pay_type = (TextView) findViewById(R.id.tv_pay_type);
//        layout_progress = (LinearLayout) findViewById(R.id.layout_progress);
//        layout_pay_type = (LinearLayout) findViewById(R.id.layout_pay_type);
//        layout_out_trade_no = (LinearLayout) findViewById(R.id.layout_out_trade_no);

        btnSure = (CircularProgressButton) findViewById(R.id.btn_sure);

        tv_status.setText(result);
        tv_money.setText(buyer_pay_amount + "元");
        tv_out_trade_no.setText(out_trade_no);
        tv_send_pay_date.setText(send_pay_date);


//        img_pay_type = (ImageView) findViewById(R.id.img_pay_type);


        if (type.equals("wx")) {
            tv_pay_type.setText(getText(R.string.weixin));
            paytype = "2";
        } else if (type.equals("zfb")) {
            paytype = "1";
            tv_pay_type.setText(getText(R.string.zhifubao));
        }

        if (action != null && action.equals("skjl")) {
//            layout_out_trade_no.setVisibility(View.VISIBLE);
//            layout_pay_type.setVisibility(View.VISIBLE);
            flag = "zfdetail";
        } else {
            if (NetUtils.hasNetwork(context)) {
                initData(buyer_pay_amount, out_trade_no, nickName, trade_no);
            } else {
                Toast.makeText(context, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
            }
        }

        btnSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag = "zfdetail";

                setSimulateClick(btnSure, 160, 100);

//                finish();
            }
        });
    }


    private void setSimulateClick(View view, float x, float y) {
        long downTime = SystemClock.uptimeMillis();
        final MotionEvent downEvent = MotionEvent.obtain(downTime, downTime,
                MotionEvent.ACTION_DOWN, x, y, 0);
        downTime += 1000;
        final MotionEvent upEvent = MotionEvent.obtain(downTime, downTime,
                MotionEvent.ACTION_UP, x, y, 0);
        view.onTouchEvent(downEvent);
        view.onTouchEvent(upEvent);
        downEvent.recycle();
        upEvent.recycle();
    }


    private void initData(String buyer_pay_amount, final String out_trade_no, final String buyer_logon_id,
                          final String trade_no) {

//  http://ts.do-wi.cn/nsh/appapi/savealipay?total=&merchantid=&ordernum=&nickname=&translatenum=&paytype=


        final String total_fen = String.valueOf((int) (Float.parseFloat(buyer_pay_amount) * 100));//分为单位,去掉小数点

        final String createTime = DateTimeUtils.date2Str(new Date(), "yyyyMMddHHmm");

        String url = "savealipay?total=" + total_fen + "&merchantid=" + merchant_id +
                "&ordernum=" + out_trade_no + "&nickname=" + buyer_logon_id + "&translatenum="
                + trade_no + "&paytype=" + paytype + "&createtime=" + createTime;

        Log.e(TAG, "initData: url===" + url);

        HttpClient.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.e(TAG, "onSuccess: statusCode==" + statusCode);
//                    layout_progress.setVisibility(View.GONE);

                try {
                    JSONObject data = new JSONObject(new String(responseBody));

                    if (data.get("result").equals("1")) {

                        Toast.makeText(context, "订单已提交", Toast.LENGTH_SHORT).show();

                    } else {

                        saveOrderToLocal(buyer_logon_id, createTime, out_trade_no, total_fen, trade_no);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                  Throwable error) {
//                    layout_progress.setVisibility(View.GONE);

                Log.e(TAG, "onFailure: statusCode==" + statusCode);

                saveOrderToLocal(buyer_logon_id, createTime, out_trade_no, total_fen, trade_no);

                Toast.makeText(context, "服务器连接异常，订单上传失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 将未上传的订单信息保存至本地数据库
     *
     * @param nickName
     * @param out_trade_no
     * @param total_fen
     * @param trade_no
     */
    private void saveOrderToLocal(String nickName, String time, String out_trade_no, String total_fen, String trade_no) {
        recordDao = new RecordDao(context);
        receiveRecord = new ReceiveRecord();
        receiveRecord.id = DateTimeUtils.getSecond();
        receiveRecord.nickName = nickName;
        receiveRecord.createTime = DateTimeUtils.str2Date(time, "yyyyMMddHHmm").getTime();
        receiveRecord.orderNum = out_trade_no;
        receiveRecord.payType = paytype;
//        receiveRecord.total = String.valueOf(Double.parseDouble(total_fen) / 100);
        receiveRecord.total = total_fen;
        receiveRecord.status = 1;//订单状态
        receiveRecord.translateNum = trade_no;
        receiveRecord.isLocal = true;
        recordDao.add(receiveRecord);
    }

}
