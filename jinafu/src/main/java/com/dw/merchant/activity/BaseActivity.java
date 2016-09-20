package com.dw.merchant.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dw.merchant.R;

/**
 * Created by Acer on 2015/10/22.
 */
public abstract class BaseActivity extends AppCompatActivity {

    public String title = "";
    public String flag = "";
    public String date = "";//核销记录中选择查询日期
    public String merchant_name = "";
    public String merchant_id = "";
    public String type = "";
    private LinearLayout layout_body;
    private long mExitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_main);


        layout_body = (LinearLayout) findViewById(R.id.layout_body);


        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);


        View view = inflater.inflate(addLayout(), null);

        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


        layout_body.addView(view);


        initLayout();


        if (flag.equals("tab")) {  //
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } else if (flag.equals("zf")) {  //输入金额
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else if (flag.equals("qrcomplete")) {  //ScanPayResultActivity
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } else if (flag.equals("zfdetail")) {  //ScanPayResultActivity
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    protected abstract int addLayout();

    protected abstract void initLayout();

    protected void startToActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        /*若没有getSupportActionBar().setTitle,则getSupportActionBar().getTitle()所取得的
        （lable：manifest）和Activity所设title的比较，目的去(关于)*/
        if (flag.equals("tab")) {
            menu.getItem(0).setTitle(getText(R.string.about));
            menu.getItem(0).setVisible(true);
        } else if (flag.equals("ok")) {//日历选择
            menu.getItem(0).setTitle(getText(R.string.ok));
            menu.getItem(0).setVisible(true);
        } else if (flag.equals("qrcomplete")) {//条码扫描完成
            menu.getItem(0).setTitle(getText(R.string.complete));
            menu.getItem(0).setVisible(true);
        } else if (flag.equals("zfdetail")) {
            menu.getItem(0).setTitle(getText(R.string.complete));
            menu.getItem(0).setVisible(true);
        } else {
            menu.getItem(0).setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //actionbar自带返回android.R.id.home
        if (id == android.R.id.home) {

            if (flag.equals("qrcode")) {  //二维码页面
                Intent intent = new Intent(this, InputOrderAmountActivity.class);
                intent.putExtra("merchant_id", merchant_id);
                intent.putExtra("merchant_name", merchant_name);
                intent.putExtra("type", type);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else
                finish();
        } else if (id == R.id.action_settings) {

            //20151225
            if (flag.equals("ok")) {//核销卡券选择日期的确定
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("date", date);
                setResult(100, intent);
                finish();
            } else if (flag.equals("qrcomplete")) {

                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("merchant_id", merchant_id);
                intent.putExtra("merchant_name", merchant_name);
                startActivity(intent);

            } else if (flag.equals("tab")) {
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
            } else if (flag.equals("zfdetail")) {

                finish();

            }

        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (flag.equals("tab")) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if ((System.currentTimeMillis() - mExitTime) > 2000) {

                    String msg = "再按一次退出程序";
                    Toast.makeText(BaseActivity.this, msg, Toast.LENGTH_SHORT).show();

                    mExitTime = System.currentTimeMillis();
                } else {

                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.cancelAll();
                    finish();
                    BaseActivity.super.onBackPressed();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
                return true;
            }
            // 拦截MENU按钮点击事件，让他无任何操作
            if (keyCode == KeyEvent.KEYCODE_MENU) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
