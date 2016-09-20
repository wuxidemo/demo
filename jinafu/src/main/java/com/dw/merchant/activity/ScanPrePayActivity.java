package com.dw.merchant.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.dw.merchant.R;
import com.dw.merchant.util.CommUtils;
import com.dw.merchant.weixin.WeChatInitiativeScanPayActivity;
import com.dw.merchant.weixin.WeChatPassiveScanPayActivity;
import com.dw.merchant.zhifubao.AliPayInitiativeScanPayActivity;
import com.dw.merchant.zhifubao.AliPayPassiveScanPayActivity;

public final class ScanPrePayActivity extends BaseActivity implements QRCodeReaderView.OnQRCodeReadListener {


    private static final String TAG = ScanPrePayActivity.class.getSimpleName() + "_lyx";
    private static final int TO_SCAN_RESULT_WX = 2001;
    private static final int TO_SCAN_RESULT_ALIPAY = 2002;
    private static final int TO_CREATE_QRCODE_WX = 2003;
    private static final int TO_CREATE_QRCODE_ALIPAY = 2004;
    private Context context;
    private TextView tv_total;
    private QRCodeReaderView mydecoderview;
    private ImageView line_image;
    private boolean token = true;//防止扫描两次返回两次扫描结果
    private CircularProgressButton btn_smf;//扫码付
    private String type = "";
    private String total = "";


    @Override
    protected int addLayout() {
        this.context = this;
        return R.layout.activity_scan_prepay;
    }

    @Override
    protected void initLayout() {

        Intent intent = getIntent();
        merchant_id = intent.getStringExtra("merchant_id");
        merchant_name = intent.getStringExtra("merchant_name");
        type = getIntent().getStringExtra("type");
        total = intent.getStringExtra("total");

        if (type.equals("zfb"))
            title = "支付宝支付";
        else if (type.equals("wx"))
            title = "微信支付";

        flag = "zf";//支付页面ZfbGateActivity

        mydecoderview = (QRCodeReaderView) findViewById(R.id.qrcode_reader_view);
        mydecoderview.setOnQRCodeReadListener(this);

        line_image = (ImageView) findViewById(R.id.qrcode_reader_green_line);

        TranslateAnimation mAnimation = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.1f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.9f);
        mAnimation.setDuration(1000);
        mAnimation.setRepeatCount(-1);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mAnimation.setInterpolator(new LinearInterpolator());
        line_image.setAnimation(mAnimation);


        btn_smf = (CircularProgressButton) findViewById(R.id.btn_smf);
        tv_total = (TextView) findViewById(R.id.tv_total);


        tv_total.setText("￥" + total);
        btn_smf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (type.equals("zfb")) {
                    Intent intent = new Intent();
                    intent.putExtra("total", total);
                    intent.putExtra("merchant_id", merchant_id);
                    intent.putExtra("merchant_name", merchant_name);
                    intent.putExtra("type", type);
                    intent.setClass(ScanPrePayActivity.this, AliPayPassiveScanPayActivity.class);
                    startActivityForResult(intent, TO_CREATE_QRCODE_ALIPAY);

//                    startActivity(intent);
                } else if (type.equals("wx")) {
                    Intent intent = new Intent();
                    intent.putExtra("total", total);
                    intent.putExtra("merchant_id", merchant_id);
                    intent.putExtra("merchant_name", merchant_name);
                    intent.putExtra("type", type);
                    intent.setClass(ScanPrePayActivity.this, WeChatPassiveScanPayActivity.class);
                    startActivityForResult(intent, TO_CREATE_QRCODE_WX);
//                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onQRCodeRead(String text, PointF[] points) {

//        Log.e(TAG,"isInteger(text)==="+isInteger(text));

        if (!text.equals("") && text.length() == 18 && CommUtils.isInteger(text)) {
            if (token) {
                token = false;

                if (type.equals("zfb")) {
                    Intent intent = new Intent(this, AliPayInitiativeScanPayActivity.class);
                    intent.putExtra("text", text);//text为扫描的条码
                    intent.putExtra("total", total);//total为总金额
                    intent.putExtra("type", type);
                    intent.putExtra("merchant_name", merchant_name);
                    intent.putExtra("merchant_id", merchant_id);
                    startActivityForResult(intent, TO_SCAN_RESULT_ALIPAY);

                } else if (type.equals("wx")) {
                    Intent intent = new Intent(this, WeChatInitiativeScanPayActivity.class);
                    intent.putExtra("text", text);//text为扫描的条码
                    intent.putExtra("total", total);//total为总金额
                    intent.putExtra("type", type);
                    intent.putExtra("merchant_name", merchant_name);
                    intent.putExtra("merchant_id", merchant_id);
                    startActivityForResult(intent, TO_SCAN_RESULT_WX);
                }
            }
        } else {
            Toast.makeText(this, "付款码无效，请扫描顾客的付款码", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void cameraNotFound() {
    }

    @Override
    public void QRCodeNotFoundOnCamImage() {
    }

    @Override
    public void onPause() {
        super.onPause();
        mydecoderview.getCameraManager().stopPreview();
    }

    @Override
    public void onResume() {
        super.onResume();
        token = true;
        mydecoderview.getCameraManager().startPreview();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e(TAG, "onActivityResult: requestCode=" + requestCode);
        Log.e(TAG, "onActivityResult: resultCode=" + resultCode);

        if (requestCode == TO_CREATE_QRCODE_WX) {
            if (resultCode == Activity.RESULT_OK) {
                setResult(Activity.RESULT_OK);
                finish();
            }
        }
    }
}
