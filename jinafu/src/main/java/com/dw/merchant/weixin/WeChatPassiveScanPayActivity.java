package com.dw.merchant.weixin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.dw.merchant.App;
import com.dw.merchant.Constants;
import com.dw.merchant.R;
import com.dw.merchant.activity.BaseActivity;
import com.dw.merchant.activity.ScanPayResultActivity;
import com.dw.merchant.util.CommUtils;
import com.dw.merchant.util.MoneyAudioUtil;
import com.dw.merchant.util.NetUtils;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * 微信扫码支付（顾客用微信扫一扫功能，扫描商户根据订单金额生成的二维码方式完成支付）
 */
public class WeChatPassiveScanPayActivity extends BaseActivity {

    private static final String TAG = WeChatPassiveScanPayActivity.class.getSimpleName() + "_lyx";
    private static final int QR_WIDTH = 200;
    private static final int QR_HEIGHT = 200;
    private static final int IMAGE_HALFWIDTH = 30;//宽度值，影响中间图片大小
    private static int ORDER_OK = 1001;
    private static int ORDER_CANCEL = 1002;
    final IWXAPI msgApi = WXAPIFactory.createWXAPI(this, null);
    private Context context;
    private TextView txtOrderStatus;
    private ImageView imgQRCode;
    private String outTradeNo;
    private TextView tv_total;
    private TimerTask task;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            Log.e(TAG, "msg.what======" + msg.what);
            if (msg.what == ORDER_OK) {
                Log.e(TAG, "＝＝＝＝＝＝＝＝＝＝＝＝");
                task.cancel();
            }
            if (msg.what == ORDER_CANCEL) {
                Log.e(TAG, "---------------------");
                task.cancel();
            }
            super.handleMessage(msg);
        }
    };
    //语音播报
    private MoneyAudioUtil mMoneyAudioUtil = new MoneyAudioUtil();
    private String total_fen = "", total = "";//订单金额（需要转为分为单位的）
    private CircularProgressButton btn_tmf;//扫一扫
    private boolean isShowTips = true;// 是否显示订单关闭成功的提示

    @Override
    protected int addLayout() {
        this.context = this;

        return R.layout.activity_weixin_passive_scan_pay;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Message message = new Message();
        message.what = ORDER_CANCEL;
        handler.sendMessage(message);
    }

    @Override
    protected void initLayout() {

        flag = "qrcode";

        total = getIntent().getStringExtra("total");
        merchant_name = getIntent().getStringExtra("merchant_name");
        merchant_id = getIntent().getStringExtra("merchant_id");
        type = getIntent().getStringExtra("type");

        total_fen = String.valueOf((int) (Float.parseFloat(total) * 100));//分为单位,去掉小数点

        txtOrderStatus = (TextView) findViewById(R.id.text_order_status);
//        show = (TextView) findViewById(R.id.editText_prepay_id);
        tv_total = (TextView) findViewById(R.id.tv_total);
        btn_tmf = (CircularProgressButton) findViewById(R.id.btn_tmf);
        btn_tmf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isShowTips = false;
                new CloseOrderTask().execute();
            }
        });

        tv_total.setText("￥" + total);

        imgQRCode = (ImageView) findViewById(R.id.img_qr);

        msgApi.registerApp(Constants.APP_ID);

        if (NetUtils.hasNetwork(context)) {
            new GenPrepayOrderQRCodeTask().execute();
        } else {
            Toast.makeText(context, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
        }

        task = new TimerTask() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                Log.i(TAG, "======task======");
                if (NetUtils.hasNetwork(context)) {
                    new CheckOrderStatusTask().execute();
                } else {
                    Toast.makeText(context, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        showCloseOrder();
        Log.e(TAG, "===onBackPressed===");
        return;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //actionbar自带返回android.R.id.home
        if (id == android.R.id.home) {
            Log.e(TAG, "===onOptionsItemSelected===");
            if (flag.equals("qrcode")) {  //二维码页面
                showCloseOrder();
                Log.e(TAG, "===flag.equals(qrcode)===");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * 是否确认关闭订单提示
     */
    private void showCloseOrder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("确认关闭订单？");
        builder.setMessage(R.string.is_sure_close);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                isShowTips = true;
                new CloseOrderTask().execute();

//                setResult(Activity.RESULT_OK);
//                finish();

            }
        });
        builder.create().show();
    }

    /**
     * 将相关参数拼接成xml格式的字符串（商家填写好订单金额后根据微信支付API要求生成订单url，并将url转换成二维码，
     * 提供给顾客扫码支付）
     *
     * @return
     * @author lvyongxu
     * @date 2015-10-30 上午10:31:55
     */
    public String genProductArgsByMerchant(String body) {
        outTradeNo = WeiXinUtils.genOutTradeNo(10);
        List<NameValuePair> valuePairs = new LinkedList<>();
        valuePairs.add(new BasicNameValuePair("appid", Constants.APP_ID));
        valuePairs.add(new BasicNameValuePair("body", body));
        valuePairs.add(new BasicNameValuePair("mch_id", Constants.MCH_ID));
        valuePairs.add(new BasicNameValuePair("nonce_str", WeiXinUtils.genNonceStr()));
        valuePairs.add(new BasicNameValuePair("notify_url", "http://ts.do-wi.cn/nsh/wxurl/payback"));
        valuePairs.add(new BasicNameValuePair("out_trade_no", outTradeNo));
        valuePairs.add(new BasicNameValuePair("spbill_create_ip", "127.0.0.1"));
        valuePairs.add(new BasicNameValuePair("sub_mch_id", App.userConfig.getWeiXinPayId()));
        valuePairs.add(new BasicNameValuePair("total_fee", total_fen));// 以分为单位
        valuePairs.add(new BasicNameValuePair("trade_type", "NATIVE"));
        valuePairs.add(new BasicNameValuePair("sign", WeiXinUtils.genPackageSign(valuePairs)));
        return WeiXinUtils.listToXml(valuePairs);
    }

    /**
     * 将请求查询订单状态接口所需的参数拼接成xml格式的字符串
     *
     * @return
     * @author lvyongxu
     * @date 2015-10-29 上午11:55:00
     */
    private String genOrderCheckArgs() {
        List<NameValuePair> valuePairs = new LinkedList<>();
        valuePairs.add(new BasicNameValuePair("appid", Constants.APP_ID));
        valuePairs.add(new BasicNameValuePair("mch_id", Constants.MCH_ID));
        valuePairs.add(new BasicNameValuePair("nonce_str", WeiXinUtils.genNonceStr()));
        valuePairs.add(new BasicNameValuePair("out_trade_no", outTradeNo));
        valuePairs.add(new BasicNameValuePair("sub_mch_id", App.userConfig.getWeiXinPayId()));
        valuePairs.add(new BasicNameValuePair("sign", WeiXinUtils.genPackageSign(valuePairs)));
        return WeiXinUtils.listToXml(valuePairs);
    }

    /**
     * 将关闭订单接口所需的参数拼接成xml格式的字符串
     *
     * @return
     * @author lvyongxu
     * @date 2015-10-29 上午11:55:00
     */
    private String genOrderCloseArgs() {
        List<NameValuePair> valuePairs = new LinkedList<>();
        valuePairs.add(new BasicNameValuePair("appid", Constants.APP_ID));
        valuePairs.add(new BasicNameValuePair("mch_id", Constants.MCH_ID));
        valuePairs.add(new BasicNameValuePair("nonce_str", WeiXinUtils.genNonceStr()));
        valuePairs.add(new BasicNameValuePair("out_trade_no", outTradeNo));
        valuePairs.add(new BasicNameValuePair("sub_mch_id", App.userConfig.getWeiXinPayId()));
        valuePairs.add(new BasicNameValuePair("sign", WeiXinUtils.genPackageSign(valuePairs)));
        return WeiXinUtils.listToXml(valuePairs);
    }

    /**
     * 根据微信规则，生成订单链接并转换成二维码
     *
     * @author lvyongxu
     * @date 2015-10-30 下午3:58:06
     */
    class GenPrepayOrderQRCodeTask extends AsyncTask<Void, Void, Map<String, String>> {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(context, getString(R.string.app_tip),
                    getString(R.string.getting_prepayid));
        }

        @Override
        protected void onPostExecute(Map<String, String> result) {
            if (dialog != null) {
                dialog.dismiss();
            }
            String returnCode = result.get("return_code");

            if (returnCode.equalsIgnoreCase("SUCCESS")) {

                if (result.get("result_code").equalsIgnoreCase("SUCCESS")) {
                    String orderUrl = result.get("code_url");
                    Log.e(TAG, "orderUrl======" + orderUrl);
                    if (orderUrl == null || "".equals(orderUrl) || orderUrl.length() < 1) {
                        return;
                    } else {
                        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_pay_weixin);
                        //调用方法createCode生成二维码

                        Bitmap orderQRCodeImg = CommUtils.createImage(orderUrl, logo, IMAGE_HALFWIDTH);

                        if (orderQRCodeImg != null) {
                            imgQRCode.setImageBitmap(orderQRCodeImg);
                            txtOrderStatus.setText("等待顾客支付中");
                            Timer timer = new Timer();
                            timer.schedule(task, 5000, 3000);
                        } else {
                            txtOrderStatus.setText("订单二维码生成失败，请重试");
                        }
                    }
                } else {
                    String errCode = result.get("err_code");
                    Log.e(TAG, "err_code_des===" + result.get("err_code_des"));
                    txtOrderStatus.setText(result.get("err_code_des"));
                }
            } else {
                String returnMsg = result.get("return_msg");
                if (returnMsg.equals("商户号mch_id或sub_mch_id不存在")) {
                    txtOrderStatus.setText("您绑定的微信商户号不存在，请检查后再试");
                } else {
                    txtOrderStatus.setText(returnMsg);
                }
            }
        }

        @Override
        protected Map<String, String> doInBackground(Void... params) {
            String url = String.format("https://api.mch.weixin.qq.com/pay/unifiedorder");
            String entity = genProductArgsByMerchant(merchant_name);
            Log.e(TAG, "entity===" + entity);

            String result = NetUtils.getResponseByPost(url, entity);
            Log.e(TAG, "result=" + result);

            return WeiXinUtils.decodeXmlToMap(result);
        }
    }

    /**
     * 检查订单当前状态
     *
     * @author lvyongxu
     * @date 2015-11-2 下午3:07:39
     */
    class CheckOrderStatusTask extends AsyncTask<Void, Void, Map<String, String>> {
        /**
         * SUCCESS—支付成功 REFUND—转入退款 NOTPAY—未支付 CLOSED—已关闭 REVOKED—已撤销（刷卡支付）
         * USERPAYING--用户支付中 PAYERROR--支付失败(其他原因，如银行返回失败)
         */
        @Override
        protected void onPostExecute(Map<String, String> result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            // String orderUrl = result.get("code_url");
            Log.e(TAG, "result======" + result);

            String returnCode = result.get("return_code");

            if (returnCode.equalsIgnoreCase("SUCCESS")) {

                String tradeState = result.get("trade_state");
                String tradeStateDesc = result.get("trade_state_desc");
                txtOrderStatus.setText(tradeStateDesc);
                Log.e(TAG, "tradeState===" + tradeState);
                Log.e(TAG, "tradeStateDesc===" + tradeStateDesc);
                Message message = new Message();

                if (tradeState.equalsIgnoreCase("SUCCESS")) {
//                txtOrderStatus.setText("支付成功");

                    mMoneyAudioUtil.Audio(context, 11, total);//收款成功

                    String trade_no = result.get("transaction_id").toString();
                    String out_trade_no = result.get("out_trade_no").toString();
                    String buyer_logon_id = result.get("openid").toString();
                    String send_pay_date = result.get("time_end").toString();

                    Intent intent = new Intent(context, ScanPayResultActivity.class);
                    intent.putExtra("result", getString(R.string.zf_sucess));
                    intent.putExtra("buyer_pay_amount", total);
                    intent.putExtra("trade_no", trade_no);
                    intent.putExtra("out_trade_no", out_trade_no);
                    intent.putExtra("buyer_logon_id", buyer_logon_id);
                    intent.putExtra("send_pay_date", send_pay_date);

                    intent.putExtra("merchant_id", merchant_id);
                    intent.putExtra("merchant_name", merchant_name);
                    intent.putExtra("type", type);

                    startActivity(intent);

                    message.what = ORDER_OK; // 停止获取订单状态
                } else if (tradeState.equalsIgnoreCase("CLOSED")) {
                    txtOrderStatus.setText("订单已经关闭");
                    message.what = ORDER_OK; // 停止获取订单状态
                    mMoneyAudioUtil.Audio(context, 10, total);//收款失败
                } else if (tradeState.equalsIgnoreCase("PAYERROR")) {
                    txtOrderStatus.setText("支付失败");
                    message.what = ORDER_OK; // 停止获取订单状态
                    mMoneyAudioUtil.Audio(context, 10, total);//收款失败
                } else if (tradeState.equalsIgnoreCase("REFUND")) {
                    txtOrderStatus.setText("该订单已转入退款状态");
                    message.what = ORDER_OK; // 停止获取订单状态
                    mMoneyAudioUtil.Audio(context, 10, total);//收款失败
                } else if (tradeState.equalsIgnoreCase("REVOKED")) {//（刷卡支付）
                    txtOrderStatus.setText("该订单已撤销");
                    message.what = ORDER_OK; // 停止获取订单状态
                    mMoneyAudioUtil.Audio(context, 10, total);//收款失败
                } else if (tradeState.equalsIgnoreCase("NOTPAY")) {
                    txtOrderStatus.setText("等待顾客支付中");
                } else if (tradeState.equalsIgnoreCase("USERPAYING")) {
                    txtOrderStatus.setText("顾客支付中");
                }
                handler.sendMessage(message);
            } else {
                String returnMsg = result.get("return_msg");
                if (returnMsg.equals("商户号mch_id或sub_mch_id不存在")) {
                    txtOrderStatus.setText("您绑定的微信商户号不存在，请检查后再试");
                }
                txtOrderStatus.setText(returnMsg);
            }
        }

        @Override
        protected Map<String, String> doInBackground(Void... params) {
            // TODO Auto-generated method stub
            String url = String.format("https://api.mch.weixin.qq.com/pay/orderquery");
            String entity = genOrderCheckArgs();
            Log.e(TAG, "entity=" + entity);
            String result = NetUtils.getResponseByPost(url, entity);
            Log.e(TAG, "result=" + result);
            return WeiXinUtils.decodeXmlToMap(result);
        }
    }

    /**
     * 关闭订单
     */
    class CloseOrderTask extends AsyncTask<Void, Void, Map<String, String>> {

        @Override
        protected void onPostExecute(Map<String, String> result) {
            super.onPostExecute(result);

            Log.e(TAG, "CloseOrderTask: result=" + result);

            if (result.get("return_code").equalsIgnoreCase("SUCCESS")) {

                if (result.get("result_code").equalsIgnoreCase("SUCCESS")) {
                    if (isShowTips) {
                        Toast.makeText(context, "订单已经成功关闭", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                    }
                    finish();
                } else {
                    Log.e(TAG, "err_code_des===" + result.get("err_code_des"));
                    txtOrderStatus.setText(result.get("err_code_des"));
                }
            } else {
                String returnMsg = result.get("return_msg");
                if (returnMsg.equals("商户号mch_id或sub_mch_id不存在")) {
                    txtOrderStatus.setText("您绑定的微信商户号不存在，请检查后再试");
                } else {
                    txtOrderStatus.setText(returnMsg);
                }
            }
        }

        @Override
        protected Map<String, String> doInBackground(Void... params) {
            String url = String.format("https://api.mch.weixin.qq.com/pay/closeorder");
            String entity = genOrderCloseArgs();
            Log.e(TAG, "entity=" + entity);
            String result = NetUtils.getResponseByPost(url, entity);
            Log.e(TAG, "result=" + result);
            return WeiXinUtils.decodeXmlToMap(result);
        }
    }

}
