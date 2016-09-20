package com.dw.merchant.activity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.dw.merchant.R;
import com.dw.merchant.util.CommUtils;
import com.dw.merchant.util.CountDownButtonView;
import com.dw.merchant.util.HttpClient;
import com.dw.merchant.util.NetUtils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class ForgetPwdFirstActivity extends BaseActivity {

    private static final String TAG = ForgetPwdFirstActivity.class.getSimpleName() + "_lyx";

    private Context context;

    private MaterialEditText edittext_verification, edittext_phone;

    private CircularProgressButton btn_next;
    private CountDownButtonView btn_get_code;

    private String verification = "", phone_before_pressed = "", phone_after_pressed = "";


    @Override
    protected int addLayout() {
        return R.layout.activity_forget_pwd_first;
    }

    @Override
    protected void initLayout() {

        context = this;

        edittext_verification = (MaterialEditText) findViewById(R.id.edittext_verification);
        edittext_phone = (MaterialEditText) findViewById(R.id.edittext_phone);

        btn_get_code = (CountDownButtonView) findViewById(R.id.btn_get_code);

        btn_next = (CircularProgressButton) findViewById(R.id.btn_next);

        btn_get_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                getVerification();

            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                verificatNext();
            }
        });

    }

    private void verificatNext() {
        phone_after_pressed = edittext_phone.getText().toString().trim();
        verification = edittext_verification.getText().toString().trim();

        if (phone_after_pressed.isEmpty()) {
            edittext_phone.setError(context.getString(R.string.error_phone_null));
            CommUtils.showKeyboard(edittext_phone);
            return;
        } else if (!CommUtils.isMobileNum(phone_after_pressed)) {
            edittext_phone.setError(context.getString(R.string.error_phone));
            CommUtils.showKeyboard(edittext_phone);
        } else if (!phone_after_pressed.equals(phone_before_pressed)) {
            edittext_phone.setError(context.getString(R.string.error_phone_modify));
            CommUtils.showKeyboard(edittext_phone);
        } else if (verification.isEmpty()) {
            edittext_verification.setError(context.getString(R.string.error_verification_null));
            CommUtils.showKeyboard(edittext_verification);
            return;
        } else {

            btn_next.setProgress(50);
//              http://ts.do-wi.cn/nsh/appapi/checkcode?phone=XXXX&code=XXXX
            if (NetUtils.hasNetwork(context)) {

                HttpClient.get("checkcode?phone=" + phone_after_pressed + "&code=" + verification, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.e(TAG, "onSuccess: statusCode==" + statusCode);

                        try {
                            JSONObject object = new JSONObject(new String(responseBody));
                            Log.e(TAG, "onSuccess: json==" + object.toString());

                            if (object.getInt("result") == 1) {

                                String msg = object.optString("msg");

                                Intent intent = new Intent(ForgetPwdFirstActivity.this, ForgetPwdSecondActivity.class);
                                intent.putExtra("phone", phone_after_pressed);
                                startActivity(intent);

                            }
                            if (object.getInt("result") == 0) {

                                btn_next.setProgress(0);
                                String msg = object.optString("msg");

                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();

                                Log.e(TAG, "onSuccess: msg==" + msg);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        btn_next.setProgress(0);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                          Throwable error) {
                        btn_next.setProgress(0);
                        Log.e(TAG, "onFailure: statusCode==" + statusCode);
                        Toast.makeText(context, R.string.connect_server_error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                btn_next.setProgress(0);
                Toast.makeText(context, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
            }
        }

    }


    private void getVerification() {

//        http://ts.do-wi.cn/nsh/wxpage/getcode?phone=XXXXX

        phone_before_pressed = edittext_phone.getText().toString().trim();

        if (phone_before_pressed.isEmpty()) {
            edittext_phone.setError(context.getString(R.string.error_phone_null));
            CommUtils.showKeyboard(edittext_phone);
            return;
        } else if (!CommUtils.isMobileNum(phone_before_pressed)) {
            edittext_phone.setError(context.getString(R.string.error_phone));
            CommUtils.showKeyboard(edittext_phone);
        } else {

            if (NetUtils.hasNetwork(context)) {

                //获取验证码
                if (!btn_get_code.isCountDown) {
                    btn_get_code.startCountDown();
                } else {
                    btn_get_code.stopCountDown();
                }

                HttpClient.get("getcode?phone=" + phone_before_pressed, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.e(TAG, "onSuccess: statusCode==" + statusCode);
                        try {
                            JSONObject object = new JSONObject(new String(responseBody));
                            Log.e(TAG, "onSuccess: json==" + object.toString());

                            if (object.getInt("result") == 1) {

                                String msg = object.optString("msg");
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();

                                Log.e(TAG, "onSuccess: msg==" + msg);
                            }
                            if (object.getInt("result") == 0) {

                                if (btn_get_code.isCountDown) {
                                    btn_get_code.stopCountDown();
                                }

                                String msg = object.optString("msg");
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "onSuccess: msg==" + msg);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                          Throwable error) {
                        Log.e(TAG, "onFailure: statusCode==" + statusCode);
                        Toast.makeText(context, R.string.connect_server_error, Toast.LENGTH_SHORT).show();
                        if (btn_get_code.isCountDown) {
                            btn_get_code.stopCountDown();
                        }
                    }
                });
            } else {
                Toast.makeText(context, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
