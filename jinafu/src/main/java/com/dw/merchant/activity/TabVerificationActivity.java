package com.dw.merchant.activity;

import android.content.Intent;
import android.graphics.PointF;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.dw.merchant.R;

public final class TabVerificationActivity extends BaseActivity implements QRCodeReaderView.OnQRCodeReadListener {

    private static final String TAG = TabVerificationActivity.class.getSimpleName() + "_lyx";
    private TextView myTextView;
    private QRCodeReaderView mydecoderview;
    private ImageView line_image;
    private boolean token = true;//防止扫描两次返回两次扫描结果
    private String merchant_id = "";
    private String merchant_name = "";


    @Override
    protected int addLayout() {
        return R.layout.activity_tab_verification;
    }

    @Override
    protected void initLayout() {

        Intent intent = getIntent();
        merchant_id = intent.getStringExtra("merchant_id");
        merchant_name = intent.getStringExtra("merchant_name");

        flag = "tab";
        title = merchant_name;

        mydecoderview = (QRCodeReaderView) findViewById(R.id.qrcode_reader_view);
        mydecoderview.setOnQRCodeReadListener(this);

        myTextView = (TextView) findViewById(R.id.exampleTextView);

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
    }


    @Override
    public void onQRCodeRead(String text, PointF[] points) {
//        myTextView.setText(text);

        if (!text.equals("") && token) {
            Log.e(TAG, "text===" + text);
            token = false;
            Intent intent = new Intent(TabVerificationActivity.this, VerificationResultActivity.class);
            intent.putExtra("text", text);
            intent.putExtra("merchant_id", merchant_id);
            startActivity(intent);
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
}
