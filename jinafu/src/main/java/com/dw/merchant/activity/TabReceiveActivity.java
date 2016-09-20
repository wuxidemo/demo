package com.dw.merchant.activity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dw.merchant.App;
import com.dw.merchant.R;
import com.dw.merchant.db.ReceiveRecord;
import com.dw.merchant.db.RecordDao;
import com.dw.merchant.util.DateTimeUtils;
import com.dw.merchant.weixin.WeiXinUtils;

public final class TabReceiveActivity extends BaseActivity {

    private static final String TAG = TabReceiveActivity.class.getSimpleName() + "_lyx";
    private Context context;

//    private LinearLayout btn_zhifubao, btn_weixin;

    private Button btnWeiXinPay, btnAliPay;

    private String merchant_name = "";
    private String merchant_id = "";

    private EditText editInput;

    @Override
    protected int addLayout() {
        this.context = this;
        return R.layout.activity_tab_receive;
    }

    @Override
    protected void initLayout() {

        Intent intent = getIntent();
        merchant_id = intent.getStringExtra("merchant_id");
        merchant_name = intent.getStringExtra("merchant_name");

        flag = "tab";
        title = merchant_name;

//        btn_zhifubao = (LinearLayout) findViewById(R.id.btn_zfb);
//        btn_weixin = (LinearLayout) findViewById(R.id.btn_wx);


        btnWeiXinPay = (Button) findViewById(R.id.btn_weixin_pay);
        btnAliPay = (Button) findViewById(R.id.btn_alipay);


//        editInput = (EditText) findViewById(R.id.edit_input);

//        editInput.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                Log.e(TAG, "beforeTextChanged: s===" + s.toString());
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//                Log.e(TAG, "onTextChanged: s===" + s.toString());
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                Log.e(TAG, "afterTextChanged: s===" + s.toString());
//            }
//        });


//        if (App.userConfig.getAliPayId().equals("")) {
//            btnAliPay.setEnabled(false);
//        } else {
//            btnAliPay.setEnabled(true);
//        }
//
//        if (App.userConfig.getWeiXinPayId().equals("")) {
//            btnWeiXinPay.setEnabled(false);
//        } else {
//            btnWeiXinPay.setEnabled(true);
//        }

        btnAliPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "App.userConfig.getAliPayId()==" + App.userConfig.getAliPayId());

                if (!App.userConfig.getAliPayId().equals("")) {
                    Intent intent = new Intent(context, InputOrderAmountActivity.class);
                    intent.putExtra("merchant_name", merchant_name);
                    intent.putExtra("merchant_id", merchant_id);
                    intent.putExtra("type", "zfb");
                    startActivity(intent);
                } else {
                    Toast.makeText(context, "请绑定支付宝用户ID", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnWeiXinPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e(TAG, "App.userConfig.getWeiXinPayId()==" + App.userConfig.getWeiXinPayId());

                if (!App.userConfig.getWeiXinPayId().equals("")) {
                    Intent intent = new Intent(context, InputOrderAmountActivity.class);
                    intent.putExtra("merchant_name", merchant_name);
                    intent.putExtra("merchant_id", merchant_id);
                    intent.putExtra("type", "wx");
                    startActivity(intent);
                } else {
                    Toast.makeText(context, "请绑定微信商户号", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnWeiXinPay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (addTestRecord()) {
                    Toast.makeText(context, "本地数据添加成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "本地数据添加失败，请重试", Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });
    }

    /**
     * 添加本地测试数据（ReceiveRecord）
     */
    private boolean addTestRecord() {
        ReceiveRecord record = new ReceiveRecord();
        record.id = DateTimeUtils.getSecond();
        record.createTime = DateTimeUtils.getMilliSecond();
        record.isLocal = true;
        record.nickName = "oKEL4sulppmswKYg4fWhNPAachwE";
        record.orderNum = WeiXinUtils.genOutTradeNo(10);
        record.payType = "2";
        record.status = 1;
        record.translateNum = "10" + DateTimeUtils.getMilliSecond() + DateTimeUtils.getMilliSecond();
        record.total = "1";

        return new RecordDao(context).add(record);
    }

}
