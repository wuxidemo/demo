package com.dw.merchant.activity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dd.CircularProgressButton;
import com.dw.merchant.R;
import com.dw.merchant.util.HttpClient;
import com.dw.merchant.util.MoneyAudioUtil;
import com.dw.merchant.util.NetUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class VerificationResultActivity extends BaseActivity {

    private static final String TAG = VerificationResultActivity.class.getSimpleName() + "_lyx";
    private TextView tv_tip;
    private com.dd.CircularProgressButton btn_continue;

    private LinearLayout layout_progress;
    private Context context;
    //语音播报
    private MoneyAudioUtil mMoneyAudioUtil = new MoneyAudioUtil();

    @Override
    protected int addLayout() {
        this.context = this;
        return R.layout.activity_verification_result;
    }

    @Override
    protected void initLayout() {

        Intent intent = getIntent();
        String merchant_id = intent.getStringExtra("merchant_id");
        String cardCode = intent.getStringExtra("text");

//        img_tip = (ImageView) findViewById(R.id.img_tip);
        tv_tip = (TextView) findViewById(R.id.tv_tip);

        btn_continue = (CircularProgressButton) findViewById(R.id.btn_continue);

        layout_progress = (LinearLayout) findViewById(R.id.layout_progress);
        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });

        if (NetUtils.hasNetwork(context)) {
            layout_progress.setVisibility(View.VISIBLE);
            verficationCard(cardCode, merchant_id);
        } else {
            layout_progress.setVisibility(View.GONE);
            tv_tip.setText(R.string.network_unavailable);
            tv_tip.setBackgroundResource(R.mipmap.bg_card_varification_noresult);
        }

    }

    /**
     * 核销卡券
     */
    private void verficationCard(String cardCode, String merchantId) {

//        appapi/usecard?code=142522365412&merid=123
        HttpClient.get("usecard?code=" + cardCode + "&merid=" + merchantId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                layout_progress.setVisibility(View.GONE);
                Log.e(TAG, "onSuccess: statusCode==" + statusCode);
                Log.e(TAG, "onSuccess: response==" + response);
                if (response.optInt("result") == 1) {
                    tv_tip.setBackgroundResource(R.mipmap.bg_card_varification_success);
                    mMoneyAudioUtil.Audio(context, 21, null);//核销成功
                }
                if (response.optInt("result") == 0) {
                    tv_tip.setBackgroundResource(R.mipmap.bg_card_varification_fail);
                    mMoneyAudioUtil.Audio(context, 20, null);//核销失败
                }
                tv_tip.setText(response.optString("msg"));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                  JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.e(TAG, "onFailure: statusCode==" + statusCode);
                layout_progress.setVisibility(View.GONE);
                tv_tip.setText(R.string.connect_server_error);
                tv_tip.setBackgroundResource(R.mipmap.bg_card_varification_noresult);

                mMoneyAudioUtil.Audio(context, 20, null);//核销失败
            }
        });
    }
}
