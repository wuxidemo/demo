package com.dw.merchant.activity;

import android.Manifest;
import android.app.DownloadManager;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;

import com.dw.merchant.App;
import com.dw.merchant.R;
import com.dw.merchant.db.ReceiveRecord;
import com.dw.merchant.db.RecordDao;
import com.dw.merchant.util.CommUtils;
import com.dw.merchant.util.DateTimeUtils;
import com.dw.merchant.util.HttpClient;
import com.dw.merchant.util.NetUtils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends TabActivity {

    public static final String TAB_RECEIVE = "tab_receive";
    public static final String TAB_VERIFICATION = "tab_verification";
    public static final String TAB_RECEIVE_RECORD = "tab_receive_record";
    public static final String TAB_VERIFICATION_RECORD = "tab_verification_record";
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 42;
    private static final String TAG = MainActivity.class.getSimpleName() + "_lyx";
    final int UPDATETIME = 1001;
    private Context context;
    private TabHost tabHost;
    private RadioGroup group;
    private RadioButton r0, r1, r2, r3;
    private String merchant_id = "";
    private String merchant_name = "";
    private String apkName = "金阿福.apk";
    private CompleteReceiver completeReceiver;
    private long apkFileDownloadId = -2;
    private RecordDao recordDao;
    private Timer timer;
    private TimerTask task;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = this;
        recordDao = new RecordDao(context);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATETIME:
                        String sysTimeStr = DateFormat
                                .format("kk:mm:ss", System.currentTimeMillis()).toString();

                        if (sysTimeStr.endsWith("00:00")) {
                            Log.e(TAG, "handleMessage: 当前是整点，时间为＝" + sysTimeStr);

                            Log.e(TAG, "records.size()=" + recordDao.getAllRecords().size());

                            for (ReceiveRecord receiveRecord : recordDao.getAllRecords()) {
                                uploadLocalRecord(receiveRecord);
                            }
                        }
                        break;
                }
            }
        };

        task = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = UPDATETIME;
                message.obj = System.currentTimeMillis();
                handler.sendMessage(message);
            }
        };

        timer = new Timer();
        // 参数：0，延时0秒后执行。1000，每隔1秒执行1次task。
        timer.schedule(task, 0, 1000);

//        recordDao.deleteAll();

        Intent intent = getIntent();
        merchant_id = intent.getStringExtra("merchant_id");
        merchant_name = intent.getStringExtra("merchant_name");

        group = (RadioGroup) findViewById(R.id.main_radio);

        r0 = (RadioButton) findViewById(R.id.radio_button0);
        r1 = (RadioButton) findViewById(R.id.radio_button1);
        r2 = (RadioButton) findViewById(R.id.radio_button2);
        r3 = (RadioButton) findViewById(R.id.radio_button3);

        tabHost = getTabHost();

        // 收款
        Intent intent1 = new Intent(this, TabReceiveActivity.class);
        intent1.putExtra("merchant_name", merchant_name);
        intent1.putExtra("merchant_id", merchant_id);
        // 卡券核销
        Intent intent2 = new Intent(this, TabVerificationActivity.class);
        intent2.putExtra("merchant_name", merchant_name);
        intent2.putExtra("merchant_id", merchant_id);

        // 收款纪录
        Intent intent3 = new Intent(this, TabReceiveRecordsActivity.class);
        intent3.putExtra("merchant_name", merchant_name);
        intent3.putExtra("merchant_id", merchant_id);

        // 核销纪录
        Intent intent4 = new Intent(this, TabVerificationRecordsActivity.class);
        intent4.putExtra("merchant_name", merchant_name);
        intent4.putExtra("merchant_id", merchant_id);

        tabHost.addTab(tabHost.newTabSpec(TAB_RECEIVE)
                .setIndicator(TAB_RECEIVE)
                .setContent(intent1));
        tabHost.addTab(tabHost.newTabSpec(TAB_VERIFICATION)
                .setIndicator(TAB_VERIFICATION)
                .setContent(intent2));
        tabHost.addTab(tabHost.newTabSpec(TAB_RECEIVE_RECORD)
                .setIndicator(TAB_RECEIVE_RECORD)
                .setContent(intent3));
        tabHost.addTab(tabHost.newTabSpec(TAB_VERIFICATION_RECORD)
                .setIndicator(TAB_VERIFICATION_RECORD)
                .setContent(intent4));

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_button0:
                        tabHost.setCurrentTabByTag(TAB_RECEIVE);
                        break;
                    case R.id.radio_button1:
                        tabHost.setCurrentTabByTag(TAB_VERIFICATION);
                        break;
                    case R.id.radio_button2:
                        tabHost.setCurrentTabByTag(TAB_RECEIVE_RECORD);
                        break;
                    case R.id.radio_button3:
                        tabHost.setCurrentTabByTag(TAB_VERIFICATION_RECORD);
                        break;
                    default:
                        break;
                }
            }
        });

        if (NetUtils.hasNetwork(context)) {
            String version = CommUtils.getAppVersion(context);
            HttpClient.get("getAppUrl?version=" + version, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Log.e(TAG, "onSuccess: statusCode==" + statusCode);

                    String apkUrl = new String(responseBody);

                    if (apkUrl != null && !apkUrl.equals("")) {
                        apkFileDownloadId = downloadApkFile(apkUrl);
                        Log.e(TAG, "apkFileDownloadId===" + apkFileDownloadId);
                    }
                    Log.e(TAG, "onSuccess: apkUrl==" + apkUrl);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                      Throwable error) {
                    Log.e(TAG, "onFailure: statusCode==" + statusCode);
                }
            });
        } else {
            Log.e(TAG, "无可用的网络");
        }

        completeReceiver = new CompleteReceiver();

        //register download success broadcast
        registerReceiver(completeReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

// Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "onCreate: AAAAAA");
            // Should we show an explanation?

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);

            // 只提示一次
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.CAMERA)) {
//                Log.e(TAG, "onCreate: BBBBBB");
//                // Show an expanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//
//            } else {
//                Log.e(TAG, "onCreate: CCCCCC");
//                // No explanation needed, we can request the permission.
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.CAMERA},
//                        MY_PERMISSIONS_REQUEST_CAMERA);
//                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                // app-defined int constant. The callback method gets the
//                // result of the request.
//            }
        } else {
            Log.e(TAG, "onCreate: DDDDDD");
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(completeReceiver);
    }

    /**
     * 调用系统自带的下载管理器下载apk文件
     *
     * @param apkUrl apk文件路径
     * @return
     */
    private long downloadApkFile(String apkUrl) {
        if (apkUrl == null || apkUrl.equals("") ||
                !Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return -1;
        }

        initDownloadFolder();

        DownloadManager downloadManager = (DownloadManager) getSystemService(
                Context.DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));

        //设置允许使用的网络类型，这里是移动网络和wifi都可以
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE
                | DownloadManager.Request.NETWORK_WIFI);

        //设置文件类型
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeString = mimeTypeMap.getMimeTypeFromExtension(
                MimeTypeMap.getFileExtensionFromUrl(apkUrl));
        request.setMimeType(mimeString);

        request.setDestinationInExternalPublicDir(App.projectName, apkName);

        //禁止发出通知，即后台下载。需要权限：android.permission.DOWNLOAD_WITHOUT_NOTIFICATION
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);

        request.setVisibleInDownloadsUi(false);

        return downloadManager.enqueue(request);
    }

    /**
     * 为下载APK文件做准备（创建保存apk文件的指定文件夹，删除本地已有的apk文件）
     */
    private void initDownloadFolder() {
        if (!(new File(App.rootPath).exists())) {
            (new File(App.rootPath)).mkdirs();
        }

        File apkFile = new File(App.rootPath, apkName);
        if (apkFile.exists()) {
            apkFile.delete();
        }
    }

    /**
     * 安装apk
     */
    private void installApk() {
        File file = new File(App.rootPath, apkName);
        Log.e(TAG, "file.getPath()===" + file.getPath());
        Log.e(TAG, "file.exists()===" + file.exists());

        if (!file.exists()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 将本地收款记录上传至服务器
     *
     * @param record
     */
    private void uploadLocalRecord(final ReceiveRecord record) {

        String createTime = DateTimeUtils.date2Str(new Date(record.createTime), "yyyyMMddHHmm");
        RequestParams params = new RequestParams();
        params.add("createtime", createTime);
        params.add("merchantid", merchant_id);
        params.add("nickname", record.nickName);
        params.add("ordernum", record.orderNum);
        params.add("paytype", record.payType);
        params.add("total", record.total);
        params.add("translatenum", record.translateNum);

        Log.e(TAG, "onSuccess: record==" + merchant_id);
        Log.e(TAG, "onSuccess: record==" + record.orderNum);
        Log.e(TAG, "onSuccess: createTime==" + createTime);

        HttpClient.post("savealipay", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.e(TAG, "onSuccess: statusCode==" + statusCode);
                Log.e(TAG, "onSuccess: response==" + response);
                if (response.opt("result").equals("1")) {
                    Log.e(TAG, "onSuccess: 本地收款记录上传成功");
                    recordDao.delete(record);
                } else if (response.opt("result").equals("2")) {
                    Log.e(TAG, "onSuccess: 该记录已经存在，无需再次上传");
                    recordDao.delete(record);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                  JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.e(TAG, "onSuccess: statusCode==" + statusCode);
            }
        });
    }

    class CompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // get complete download id
            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            // to do here
            Log.e(TAG, "completeDownloadId===" + completeDownloadId);
            if (completeDownloadId == apkFileDownloadId) {
                Log.e(TAG, "＝＝＝文件下载成功＝＝＝");
                installApk();
            }
        }
    }
}
