package com.dw.merchant.activity;

import android.widget.TextView;

import com.dw.merchant.R;
import com.dw.merchant.util.CommUtils;

public class AboutActivity extends BaseActivity {

    private TextView txtAppVersion;

    @Override
    protected int addLayout() {
        return R.layout.activity_about;
    }

    @Override
    protected void initLayout() {

        txtAppVersion = (TextView) findViewById(R.id.tv_version);
        txtAppVersion.setText(CommUtils.getAppVersion(this));

    }
}
