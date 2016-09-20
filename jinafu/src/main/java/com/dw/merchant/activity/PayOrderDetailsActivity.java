package com.dw.merchant.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.dw.merchant.R;
import com.dw.merchant.db.ReceiveRecord;
import com.dw.merchant.util.DateTimeUtils;
import com.dw.merchant.util.HttpClient;
import com.dw.merchant.util.NetUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.Date;

import cz.msebera.android.httpclient.Header;

/**
 * 订单详细信息，申请退款
 */
public class PayOrderDetailsActivity extends BaseActivity {

    private static final String TAG = PayOrderDetailsActivity.class.getSimpleName() + "_lyx";
    private Context context;

    private ReceiveRecord receiveRecord;

    private ImageView imgPayType;

    private TextView txtTotal, txtCreateTime, txtPayType, txtOrderNum, txtTranslateNum;

    private CircularProgressButton btnApplyRefund;

    @Override
    protected int addLayout() {
        return R.layout.activity_pay_order_details;
    }

    @Override
    protected void initLayout() {

        this.context = this;
        flag = "";

        imgPayType = (ImageView) findViewById(R.id.img_pay_type);
        txtTotal = (TextView) findViewById(R.id.tv_money);

        txtTranslateNum = (TextView) findViewById(R.id.tv_trade_no);
        txtOrderNum = (TextView) findViewById(R.id.tv_out_trade_no);
        txtCreateTime = (TextView) findViewById(R.id.tv_send_pay_date);
        txtPayType = (TextView) findViewById(R.id.tv_pay_type);

        btnApplyRefund = (CircularProgressButton) findViewById(R.id.btn_apply_refund);

        bindOrderData();

        btnApplyRefund.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetUtils.hasNetwork(context)) {
                    if (receiveRecord.payType.equals("2")) {
                        showWarning();

//                        applyRefund();


                    } else {
                        Toast.makeText(context, "请在客户端自己调用支付宝的退款接口", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /**
     * 显示订单相关数据
     */
    private void bindOrderData() {
        Intent intent = getIntent();

        String action = intent.getStringExtra("action");
        if (action != null && action.equals("skjl")) {
            flag = "zfdetail";
        }

        merchant_id = intent.getStringExtra("merchant_id");
        merchant_name = intent.getStringExtra("merchant_name");
        Log.e(TAG, "merchant_name==" + merchant_name);

        receiveRecord = intent.getParcelableExtra("order");

        if (receiveRecord == null) {
            return;
        }

        txtTotal.setText(String.format("%.2f", Double.parseDouble(receiveRecord.total) / 100) + "元");
        txtCreateTime.setText(DateTimeUtils.date2Str(new Date(receiveRecord.createTime),
                "yyyy-MM-dd HH:mm"));
        txtOrderNum.setText(receiveRecord.orderNum);
        txtTranslateNum.setText(receiveRecord.translateNum);

        if (receiveRecord.payType.equals("1")) {
            imgPayType.setImageResource(R.mipmap.ic_pay_alipay);
            txtPayType.setText(getText(R.string.zhifubao));
        } else if (receiveRecord.payType.equals("2")) {
            imgPayType.setImageResource(R.mipmap.ic_pay_weixin);
            txtPayType.setText(getText(R.string.weixin));
        }

        switch (receiveRecord.status) {
            case 0:
                btnApplyRefund.setText(R.string.zf_fail);
                btnApplyRefund.setEnabled(false);
                break;
            case 1:
                btnApplyRefund.setText(R.string.zf_refund_apply);
                btnApplyRefund.setEnabled(true);
                break;
            case 2:
                btnApplyRefund.setText(R.string.zf_refund_dealing);
                btnApplyRefund.setEnabled(false);
                break;
            case 3:
                btnApplyRefund.setText(R.string.zf_refund_ok);
                btnApplyRefund.setEnabled(false);
                break;
            case 4:
                btnApplyRefund.setText(R.string.zf_refund_fail);
                btnApplyRefund.setEnabled(false);
                break;
            default:
                btnApplyRefund.setText(R.string.zf_unknow);
                btnApplyRefund.setEnabled(false);
        }
    }

    /**
     * 显示退款操作提示
     */
    private void showWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("⚠警告，确定申请退款吗？");
        builder.setMessage("若点击确定，该订单的款项会按照原路径退回到顾客手中，该操作不可逆，请谨慎操作。");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                applyRefund();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * 申请退款
     */
    private void applyRefund() {

        btnApplyRefund.setProgress(50);
        RequestParams params = new RequestParams();
        params.add("merchantid", merchant_id);
        params.add("ordernum", receiveRecord.orderNum);
        params.add("type", receiveRecord.payType);
        HttpClient.post("payrefund", params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                btnApplyRefund.setProgress(0);
                Log.e(TAG, "onSuccess: response=" + response);

                if (response.optInt("result") == 1) {
                    Toast.makeText(context, "退款申请成功", Toast.LENGTH_SHORT).show();
                    btnApplyRefund.setText(R.string.zf_refund_dealing);
                    btnApplyRefund.setEnabled(false);
                } else {
                    Toast.makeText(context, "申请退款失败，请联系相应的技术人员，或者稍后再试",
                            Toast.LENGTH_LONG).show();
                    btnApplyRefund.setText(R.string.zf_refund_apply);
                    btnApplyRefund.setEnabled(true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                  JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                btnApplyRefund.setProgress(0);
                Log.e(TAG, "onFailure: statusCode==" + statusCode);
                Toast.makeText(context, R.string.connect_server_error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
