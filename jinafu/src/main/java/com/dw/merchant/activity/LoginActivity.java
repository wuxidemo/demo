package com.dw.merchant.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
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

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName() + "_lyx";

    private Context context;

    private MaterialEditText editAccount;
    private MaterialEditText editPassword;

    private CircularProgressButton btnLogin;

    private String account;
    private String password;

    private Button btn_forget;

    private CheckBox checkBox_remember;

    private boolean checked = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_login);

        context = this;

        editAccount = (MaterialEditText) findViewById(R.id.edittext_account);
        editPassword = (MaterialEditText) findViewById(R.id.edittext_pwd);


        btn_forget = (Button) findViewById(R.id.btn_forget);

        checkBox_remember = (CheckBox) findViewById(R.id.checkBox_remember);

        btnLogin = (CircularProgressButton) findViewById(R.id.btn_login);
        btnLogin.setIndeterminateProgressMode(true);//当该属性为true时，progress效果为未确定


        editAccount.setText(App.userConfig.getAccount());
        if (App.userConfig.isRemembered()) {
            editPassword.setText(App.userConfig.getPassword());
            checkBox_remember.setChecked(true);
            checked = true;
        }
        editAccount.setSelection(App.userConfig.getAccount().length());

        btn_forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgetPwdFirstActivity.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUser();
            }
        });


        checkBox_remember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                checked = isChecked;

            }
        });


        editAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals(App.userConfig.getAccount())) {
                    editPassword.setText(App.userConfig.getPassword());
                } else {
                    editPassword.setText("");
                }
            }
        });

        editAccount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.e(TAG, "onEditorAction: actionId==" + actionId);
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    editPassword.setSelection(editPassword.getText().toString().length());
                }
                return false;
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 用户登录
     */
    private void saveUser() {

        account = editAccount.getText().toString().trim();
        password = editPassword.getText().toString().trim();

        if (account.isEmpty()) {
            editAccount.setError(context.getString(R.string.error_account));
            CommUtils.showKeyboard(editAccount);
            return;
        }

        if (password.isEmpty()) {
            editPassword.setError(context.getString(R.string.error_pwd));
            CommUtils.showKeyboard(editPassword);
            return;
        } else {
            btnLogin.setProgress(50);

            App.userConfig.setAccount(account);
            App.userConfig.setPassword(password);

            if (NetUtils.hasNetwork(context)) {

                HttpClient.get("login?name=" + account + "&password=" + password, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.e(TAG, "onSuccess: statusCode==" + statusCode);
                        try {
                            JSONObject object = new JSONObject(new String(responseBody));
                            Log.e(TAG, "onSuccess: json==" + object.toString());

                            if (object.getInt("result") == 1) {

                                JSONObject merchantObject = object.getJSONObject("mer");
                                String merchant_id = merchantObject.optString("id");
                                String merchant_name = merchantObject.optString("name");
                                String aliPayId = merchantObject.optString("alipaynum");
                                String weiXinPayId = merchantObject.optString("wxpaynum");

                                if (aliPayId.equals("null")) {
                                    aliPayId = "";
                                }
                                if (weiXinPayId.equals("null")) {
                                    weiXinPayId = "";
                                }
                                Log.e(TAG, "onSuccess: merchantID==" + merchant_id);
                                Log.e(TAG, "onSuccess: merchantName==" + merchant_name);
                                Log.e(TAG, "onSuccess: aliPayId==" + aliPayId);
                                Log.e(TAG, "onSuccess: weiXinPayId==" + weiXinPayId);

                                App.userConfig.setID(merchant_id);
                                App.userConfig.setRemember(checked);
                                App.userConfig.setAccount(account);
                                App.userConfig.setPassword(password);
                                App.userConfig.setAliPayId(aliPayId);
                                App.userConfig.setWeiXinPayId(weiXinPayId);

//                                Intent resultIntent = new Intent(LoginActivity.this, TabsActivity.class);
                                Intent resultIntent = new Intent(LoginActivity.this, MainActivity.class);
                                resultIntent.putExtra("merchant_id", merchant_id);
                                resultIntent.putExtra("merchant_name", merchant_name);
                                startActivity(resultIntent);

                                LoginActivity.this.finish();

                            }
                            if (object.getInt("result") == 0) {
                                btnLogin.setProgress(0);

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
                        btnLogin.setProgress(0);
                        Toast.makeText(context, R.string.connect_server_error, Toast.LENGTH_SHORT).show();
                    }

                });
            } else {
                btnLogin.setProgress(0);
                Toast.makeText(context, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
            }
        }
    }
}