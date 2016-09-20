package com.dw.merchant.activity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.dw.merchant.App;
import com.dw.merchant.R;
import com.dw.merchant.util.CommUtils;
import com.dw.merchant.util.HttpClient;
import com.dw.merchant.util.NetUtils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;

public class ForgetPwdSecondActivity extends BaseActivity {


    private static final String TAG = ForgetPwdSecondActivity.class.getSimpleName() + "_lyx";

    private Context context;

    private MaterialEditText edittext_psw, edittext_psw_again;

    private CircularProgressButton btn_save;

    private String psw = "", psw_again = "";
    private String phone = "";


    @Override
    protected int addLayout() {
        return R.layout.activity_forget_pwd_second;
    }

    @Override
    protected void initLayout() {

        context = this;

        Intent intent = getIntent();
        phone = intent.getStringExtra("phone");


        edittext_psw = (MaterialEditText) findViewById(R.id.edittext_psw);
        edittext_psw_again = (MaterialEditText) findViewById(R.id.edittext_psw_again);


        btn_save = (CircularProgressButton) findViewById(R.id.btn_save);


        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                savePwd(phone);

            }
        });


    }

    private void savePwd(String phone) {
        psw = edittext_psw.getText().toString().trim();
        psw_again = edittext_psw_again.getText().toString().trim();

        String regex = "[A-Za-z0-9]{6,18}";
        String str = psw;
        Pattern pat = Pattern.compile(regex);
        Matcher matcher = pat.matcher(str);
        boolean pwd_format = matcher.matches();

        if (psw.isEmpty()) {
            edittext_psw.setError(context.getString(R.string.error_pwd));
            CommUtils.showKeyboard(edittext_psw);
        } else if (psw_again.isEmpty()) {
            edittext_psw_again.setError(context.getString(R.string.error_psw_again));
            CommUtils.showKeyboard(edittext_psw_again);
        } else if (!pwd_format) {
            edittext_psw.setError(context.getString(R.string.error_psw_format));
            CommUtils.showKeyboard(edittext_psw);
        } else if (!psw.equals(psw_again)) {
            edittext_psw_again.setError(context.getString(R.string.error_psw_modify));
            CommUtils.showKeyboard(edittext_psw_again);
        } else {

            btn_save.setProgress(50);
//              http://ts.do-wi.cn/nsh/wxpage/setnewpass?phone=XXXXX&newpass=XXXX
            if (NetUtils.hasNetwork(context)) {

                HttpClient.get("setnewpass?phone=" + phone + "&newpass=" + psw, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.e(TAG, "onSuccess: statusCode==" + statusCode);


                        try {
                            JSONObject object = new JSONObject(new String(responseBody));
                            Log.e(TAG, "onSuccess: json==" + object.toString());

                            if (object.getInt("result") == 1) {

                                String msg = object.optString("msg");

                                Intent intent = new Intent(ForgetPwdSecondActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();

                                App.userConfig.setPassword("");
                                App.userConfig.setRemember(false);

                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();

                            }
                            if (object.getInt("result") == 0) {


                                btn_save.setProgress(0);

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
                        btn_save.setProgress(0);
                        Log.e(TAG, "onFailure: statusCode==" + statusCode);
                        Toast.makeText(context, R.string.connect_server_error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                btn_save.setProgress(0);
                Toast.makeText(context, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
