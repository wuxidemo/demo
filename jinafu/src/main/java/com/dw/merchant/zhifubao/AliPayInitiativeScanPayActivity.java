package com.dw.merchant.zhifubao;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dw.merchant.R;
import com.dw.merchant.activity.BaseActivity;
import com.dw.merchant.activity.ScanPayResultActivity;
import com.dw.merchant.util.DateTimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 支付宝条码支付 商户收银员用扫码终端设备（智能手机或扫码枪等），扫描顾客的支付宝付款码或者二维码并完成支付
 */
public class AliPayInitiativeScanPayActivity extends BaseActivity {


    private static final String TAG = AliPayInitiativeScanPayActivity.class.getSimpleName() + "_lyx";
    TextView textView;
    //
//    private String app_id = "2015110600707517";
    URLTask task = new URLTask();
    //    // 商户私钥，pkcs8格式
//    public static final String RSA_PRIVATE = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAK5367p+RchoyzKBLXtzLAj/CoiQfgnEUUKRjomxvdwvZkL8TEfie6ZUY2WhVObYVELkup7l7PYUtAZRn8UkUfSDkyhuTL4LV/UDlggbOg68B/s6PnTd6Ldk7XnjY+UfyVnhpUUnHdhtXPIYuj83Hmndqj4xHXiyqC/hRYZBsE11AgMBAAECgYBAXXmX+dHg19hvL30KGDlcsErAix4UmFqqRmzhm7NBsjL174Js2r9nY6av7c8WwySC8UNwL782Ifkwg5h/8KBTCtQgvPYapF8PSyD1/cHzF6Tcb32XoZz7/uypnVNVt6kPraTGfXa0jDe+CRCGk09vmnranV/Mev8U/yJ9sIp62QJBANtYuQJYodJuZLC+qld9vZrrsGHD3FYoBKNMOq6gTm7RLX+Tihb9Rithds54AoifHCXtBJfeiupYAOp9Hu9SROMCQQDLn2MbU5koRV3Q8XxmKITIVw5Brq11+rRIMrjilws8JyDQOTOuN3Z29lqw9S4teFkciIxKIHqYpmnPgZpCzQvHAkB82LUFJtmEYp0hFIT0I3emE/xiyQ5CY6iwIZVNC6VY4eqZsKpqh2JHEsSCpEAc7yMgWxXAM0SyOcDbtrfC0/qtAkB50M2yoG2k+PKqOH3qg90EGYiu5LhjN2u5MZcH/8K55tKrnzz6wbV+b91LtjI9A52UA2CiTBHr1srAWFGYGyErAkEAhBw+z7JpGhS8fxLeoTxVrajqpeV31paHywb2fYTotT5eHPoH7qbG8YeGVcCv7YHygxjhY0P7WAcaCtpeIS9nww==";
//    //支付场景,条码支付
    private String scene = "bar_code";
    //接口名称-支付
    private String method = "alipay.trade.pay";
    //接口名称-查询
    private String method_query = "alipay.trade.query";

    //    //    卖家支付宝用户ID,如果该值为空，则默认为商户签约账号对应的支付宝用户ID	2088102146225135
//    private String seller_id = "2088012896752633";
    private String charset = "utf-8";
    //接口版本号
    private String version = "1.0";
    private String out_trade_no;
    private String now_time;
    private LinearLayout layout_progress;


    Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1001:
                    String data = msg.getData().get("data").toString();
                    try {
                        JSONObject json = new JSONObject(data);
                        JSONObject content = new JSONObject(json.get("alipay_trade_pay_response").toString());
                        String code = content.get("code").toString();

//                        10000	业务处理成功
//                        40004	业务处理失败	具体失败原因参见接口返回的错误码
//                        10003	业务处理中	该结果码只有在条码支付请求API时才返回，代表付款还在进行中，需要调用查询接口查询最终的支付结果
//                        20000	业务出现未知错误或者系统异常	业务出现未知错误或者系统异常，如果支付接口返回，需要调用查询接口确认订单状态或者发起撤销
                        if (code.equals("10000")) {
                            textView.setText(getResources().getText(R.string.zf_sucess));

                            String buyer_pay_amount = content.get("total_amount").toString();
                            String trade_no = content.get("trade_no").toString();
                            String out_trade_no = content.get("out_trade_no").toString();
                            String buyer_logon_id = content.get("buyer_logon_id").toString();
                            String send_pay_date = content.get("gmt_payment").toString();

                            Intent intent = new Intent(AliPayInitiativeScanPayActivity.this, ScanPayResultActivity.class);
                            intent.putExtra("result", getString(R.string.zf_sucess));
                            intent.putExtra("type", "zfb");

                            intent.putExtra("buyer_pay_amount", buyer_pay_amount);
                            intent.putExtra("trade_no", trade_no);
                            intent.putExtra("out_trade_no", out_trade_no);
                            intent.putExtra("buyer_logon_id", buyer_logon_id);
                            intent.putExtra("send_pay_date", send_pay_date);

                            intent.putExtra("merchant_id", merchant_id);
                            intent.putExtra("merchant_name", merchant_name);
                            intent.putExtra("type", type);

                            startActivity(intent);
                            finish();
                            layout_progress.setVisibility(View.GONE);


                        } else if (code.equals("10003")) {
                            Timer timer = new Timer();
                            timer.schedule(task, 5000, 3000);
                        } else if (code.equals("40004")) {
                            String sub_code = content.get("sub_code").toString();
                            switch (sub_code) {
                                case "USER_FACE_PAYMENT_SWITCH_OFF":
                                    textView.setText("用户当面付付款开关关闭，请用户在手机上打开当面付付款开关");
                                    Toast.makeText(AliPayInitiativeScanPayActivity.this, "用户当面付付款开关关闭，请用户在手机上打开当面付付款开关", Toast.LENGTH_LONG).show();
                                    break;
                                case "ACQ.NO_PAYMENT_INSTRUMENTS_AVAILABLE":
                                    textView.setText("用户当前没有任何可以用于付款的渠道");
                                    Toast.makeText(AliPayInitiativeScanPayActivity.this, "用户当前没有任何可以用于付款的渠道", Toast.LENGTH_LONG).show();
                                    break;
                                case "ACQ.PAYMENT_REQUEST_HAS_RISK":
                                    textView.setText("当前支付行为，支付宝认为有风险，更换其它付款方式");
                                    Toast.makeText(AliPayInitiativeScanPayActivity.this, "当前支付行为，支付宝认为有风险，更换其它付款方式", Toast.LENGTH_LONG).show();
                                     break;
                                case "ACQ.ERROR_BUYER_CERTIFY_LEVEL_LIMIT":
                                    textView.setText("当前买家（用户）未通过人行认证");
                                    Toast.makeText(AliPayInitiativeScanPayActivity.this, "当前买家（用户）未通过人行认证", Toast.LENGTH_LONG).show();
                                    break;
                                case "ACQ.BEYOND_PER_RECEIPT_RESTRICTION":
                                    textView.setText("商户收款金额超过月限额");
                                    Toast.makeText(AliPayInitiativeScanPayActivity.this, "商户收款金额超过月限额", Toast.LENGTH_LONG).show();
                                    break;
                                case "ACQ.BEYOND_PAY_RESTRICTION":
                                    textView.setText("商户收款额度超限");
                                    Toast.makeText(AliPayInitiativeScanPayActivity.this, "商户收款额度超限", Toast.LENGTH_LONG).show();
                                    break;
                                case "ACQ.PAYMENT_FAIL":
                                    textView.setText("支付失败");
                                    Toast.makeText(AliPayInitiativeScanPayActivity.this, "支付失败", Toast.LENGTH_LONG).show();
                                    break;
                                case "ACQ.MOBILE_PAYMENT_SWITCH_OFF":
                                    textView.setText("用户关闭了无线支付开关，打开无线支付开关后，再重新发起支付");
                                    Toast.makeText(AliPayInitiativeScanPayActivity.this, "用户关闭了无线支付开关，打开无线支付开关后，再重新发起支付", Toast.LENGTH_LONG).show();
                                    break;
                                case "ACQ.PULL_MOBILE_CASHIER_FAIL":
                                    textView.setText("用户刷新条码后，重新扫码发起请求");
                                    Toast.makeText(AliPayInitiativeScanPayActivity.this, "用户刷新条码后，重新扫码发起请求", Toast.LENGTH_LONG).show();
                                    break;
                                case "ACQ.BUYER_SELLER_EQUAL":
                                    textView.setText("交易的买卖家为同一个人，更换买家重新付款");
                                    Toast.makeText(AliPayInitiativeScanPayActivity.this, "交易的买卖家为同一个人，更换买家重新付款", Toast.LENGTH_LONG).show();
                                    break;
                                case "ACQ.ERROR_BALANCE_PAYMENT_DISABLE":
                                    textView.setText("用户打开余额支付开关后，再重新进行支付");
                                    Toast.makeText(AliPayInitiativeScanPayActivity.this, "用户打开余额支付开关后，再重新进行支付", Toast.LENGTH_LONG).show();
                                    break;
                                case "ACQ.BUYER_BANKCARD_BALANCE_NOT_ENOUGH":
                                    textView.setText("用户银行卡余额不足，建议买家更换支付宝进行支付或者更换其它付款方式");
                                    Toast.makeText(AliPayInitiativeScanPayActivity.this, "用户银行卡余额不足，建议买家更换支付宝进行支付或者更换其它付款方式", Toast.LENGTH_LONG).show();
                                    break;
                                case "ACQ.BUYER_BALANCE_NOT_ENOUGH":
                                    textView.setText("买家余额不足，买家绑定新的银行卡或者支付宝余额有钱后再发起支付");
                                    Toast.makeText(AliPayInitiativeScanPayActivity.this, "买家余额不足，买家绑定新的银行卡或者支付宝余额有钱后再发起支付", Toast.LENGTH_LONG).show();
                                    break;
                                case "ACQ.TRADE_HAS_SUCCESS":
                                    textView.setText("该笔交易已存在，并且已经支付成功");
                                    Toast.makeText(AliPayInitiativeScanPayActivity.this, "该笔交易已存在，并且已经支付成功", Toast.LENGTH_LONG).show();
                                    break;
                                case "ACQ.TOTAL_FEE_EXCEED":
                                    textView.setText("订单总金额超过限额");
                                    Toast.makeText(AliPayInitiativeScanPayActivity.this, "订单总金额超过限额", Toast.LENGTH_LONG).show();
                                    break;
                                case "ACQ.SYSTEM_ERROR":
                                    textView.setText("系统超时");
                                    Toast.makeText(AliPayInitiativeScanPayActivity.this, "系统超时", Toast.LENGTH_LONG).show();
                                    break;
                                case "ACQ.INVALID_PARAMETER":
                                    textView.setText("检查请求参数，修改后重新发起请求");
                                    Toast.makeText(AliPayInitiativeScanPayActivity.this, "金额输入有误，请重新输入", Toast.LENGTH_LONG).show();
                                    break;
                                case "ACQ.EXIST_FORBIDDEN_WORD":
                                    textView.setText("订单信息中（标题，商品名称，描述等）包含了违禁词");
                                    Toast.makeText(AliPayInitiativeScanPayActivity.this, "订单信息中（标题，商品名称，描述等）包含了违禁词", Toast.LENGTH_LONG).show();
                                    break;
                                default:
                                    textView.setText(getResources().getText(R.string.zf_fail));
                                    Toast.makeText(AliPayInitiativeScanPayActivity.this, getResources().getText(R.string.zf_fail), Toast.LENGTH_LONG).show();
                            }
                            finish();
                            layout_progress.setVisibility(View.GONE);
                        } else if (code.equals("20000")) {

                            textView.setText("业务出现未知错误或者系统异常");
                            Toast.makeText(AliPayInitiativeScanPayActivity.this, "业务出现未知错误或者系统异常", Toast.LENGTH_LONG).show();
                            finish();
                            layout_progress.setVisibility(View.GONE);

                        } else {
                            textView.setText(getResources().getText(R.string.zf_fail));
                            Toast.makeText(AliPayInitiativeScanPayActivity.this, getResources().getText(R.string.zf_fail), Toast.LENGTH_LONG).show();
                            finish();
                            layout_progress.setVisibility(View.GONE);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.i(TAG, "data===" + new String(data));
                    break;

                case 1002:
                    String data_query = msg.getData().get("data").toString();
                    Log.i(TAG, "data_query===" + data_query);

                    try {
                        JSONObject json = new JSONObject(data_query);
                        JSONObject content = new JSONObject(json.get("alipay_trade_query_response").toString());
                        String code = content.get("code").toString();

//                        10000	业务处理成功
//                        40004	业务处理失败	具体失败原因参见接口返回的错误码
//                        10003	业务处理中	该结果码只有在条码支付请求API时才返回，代表付款还在进行中，需要调用查询接口查询最终的支付结果
//                        20000	业务出现未知错误或者系统异常	业务出现未知错误或者系统异常，如果支付接口返回，需要调用查询接口确认订单状态或者发起撤销
                        if (code.equals("10000")) {

                            String trade_status = content.get("trade_status").toString();

                            switch (trade_status) {
                                case "WAIT_BUYER_PAY":
                                    textView.setText("交易创建，等待买家付款");
                                    break;
                                case "TRADE_CLOSED":
                                    textView.setText("未付款交易超时关闭，或支付完成后全额退款");
                                    task.cancel();
                                    Toast.makeText(AliPayInitiativeScanPayActivity.this, "未付款交易超时关闭，或支付完成后全额退款", Toast.LENGTH_LONG).show();
                                    finish();
                                    layout_progress.setVisibility(View.GONE);
                                    break;
                                case "TRADE_SUCCESS":
                                    textView.setText("交易支付成功");
                                    task.cancel();

                                    String buyer_pay_amount = content.get("total_amount").toString();
                                    String trade_no = content.get("trade_no").toString();
                                    String out_trade_no = content.get("out_trade_no").toString();
                                    String buyer_logon_id = content.get("buyer_logon_id").toString();
                                    String send_pay_date = content.get("gmt_payment").toString();

                                    Intent intent = new Intent(AliPayInitiativeScanPayActivity.this, ScanPayResultActivity.class);
                                    intent.putExtra("result", getString(R.string.zf_sucess));

                                    intent.putExtra("buyer_pay_amount", buyer_pay_amount);
                                    intent.putExtra("trade_no", trade_no);
                                    intent.putExtra("out_trade_no", out_trade_no);
                                    intent.putExtra("buyer_logon_id", buyer_logon_id);
                                    intent.putExtra("send_pay_date", send_pay_date);

                                    intent.putExtra("merchant_id", merchant_id);
                                    intent.putExtra("merchant_name", merchant_name);
                                    intent.putExtra("type", type);

                                    startActivity(intent);
                                    finish();
                                    layout_progress.setVisibility(View.GONE);

                                    break;
                                case "TRADE_FINISHED":
                                    textView.setText("交易结束，不可退款");
                                    task.cancel();
                                    Toast.makeText(AliPayInitiativeScanPayActivity.this, "交易结束，不可退款", Toast.LENGTH_LONG).show();
                                    finish();
                                    layout_progress.setVisibility(View.GONE);
                                    break;
                            }
                        } else {
                            textView.setText(getResources().getText(R.string.zf_fail));
                            task.cancel();
                            Toast.makeText(AliPayInitiativeScanPayActivity.this, getResources().getText(R.string.zf_fail), Toast.LENGTH_LONG).show();
                            finish();
                            layout_progress.setVisibility(View.GONE);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.e(TAG, "data_query===" + data_query);
                    break;
            }
        }
    };

    @Override
    protected int addLayout() {
        return R.layout.activity_alipay_active_scan_pay;
    }

    @Override
    protected void initLayout() {

        textView = (TextView) findViewById(R.id.tv_content);
        layout_progress = (LinearLayout) findViewById(R.id.layout_progress);
        layout_progress.setVisibility(View.VISIBLE);

        nowTime();

        String auth_code = getIntent().getStringExtra("text");
        String total = getIntent().getStringExtra("total");
        merchant_name = getIntent().getStringExtra("merchant_name");
        merchant_id = getIntent().getStringExtra("merchant_id");
        type = getIntent().getStringExtra("type");

        out_trade_no = getOutTradeNo();
        try {
            pay(auth_code, AliPayConstants.SELLER_ID, total, merchant_name, now_time);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void nowTime() {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        now_time = df.format(date);
    }

    /**
     * call alipay sdk pay. 调用SDK支付
     */
    public void pay(String auth_code, String seller_id, String total_amount, String subject, String timestamp) throws Exception {

        // 订单
        String orderInfo = getOrderInfo(auth_code, seller_id, total_amount, subject, timestamp);

        Log.e(TAG, "orderInfo===" + orderInfo);
        // 对订单做RSA 签名
        String sign = sign(orderInfo);
        Log.e(TAG, "sign===" + sign);

        // 完整的符合支付宝参数规范的订单信息
        final String payInfo = orderInfo + "&sign=\"" + sign;
        Log.e(TAG, "url==" + payInfo);

        //请求参数键-值对
        final Map<String, String> params = new HashMap<>();
        params.put("app_id", AliPayConstants.APP_ID);
        params.put("biz_content", getQrCodePayInfo(auth_code, seller_id, total_amount, subject).toString());
        params.put("charset", charset);
        params.put("method", method);
        params.put("sign_type", "RSA");
        params.put("timestamp", timestamp);
        params.put("sign", sign);
        params.put("version", version);

        new Thread() {
            @Override
            public void run() {
                try {
                    String urlPath = "https://openapi.alipay.com/gateway.do";
                    InputStream is = NetTool.getInputStreamByPost(urlPath, params, "UTF-8");

                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putString("data", new String(NetTool.readStream(is)));
                    message.setData(bundle);
                    message.what = 1001;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    /**
     * create the order info. 创建条码支付
     */
    private JSONObject getQrCodePayInfo(String auth_code, String seller_id, String total_amount, String subject) {

        JSONObject json = new JSONObject();
        try {
            json.put("auth_code", auth_code);
            json.put("total_amount", total_amount);   // 订单总金额
            json.put("scene", scene); // 支付场景
            json.put("out_trade_no", out_trade_no);  // 商户订单号
            json.put("seller_id", seller_id);     // 卖家支付宝用户ID
            json.put("subject", subject);
            json.put("timeout_express", "10m");    //        该笔订单允许的最晚付款时间，逾期将关闭交易。
            // 取值范围：1 m～15d。m - 分钟，h - 小时，d - 天，1 c - 当天（无论交易何时创建，都在0点关闭）。
            //        该参数数值不接受小数点，如 1.5 h，可转换为 90 m。该值优先级低于time_expire。

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "json===" + json);
        return json;
    }

    private String getQrCodePayInfoTest(String auth_code, String seller_id, String total_amount, String subject) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time_expire = sdf.format(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
        StringBuilder sb = new StringBuilder();
        sb.append("{\"out_trade_no\":\"" + getOutTradeNo() + "\",");
        sb.append("\"total_amount\":\"" + total_amount + "\",\"discountable_amount\":\"0.00\",");
        sb.append("\"subject\":\"" + subject + "\",\"body\":\"test\",");
        sb.append("\"goods_detail\":[{\"goods_id\":\"apple-01\",\"goods_name\":\"ipad\",\"goods_category\":\"7788230\",\"price\":\"88.00\",\"quantity\":\"1\"},{\"goods_id\":\"apple-02\",\"goods_name\":\"iphone\",\"goods_category\":\"7788231\",\"price\":\"88.00\",\"quantity\":\"1\"}],");
        sb.append("\"operator_id\":\"op001\",\"store_id\":\"pudong001\",\"terminal_id\":\"t_001\",");
        sb.append("\"time_expire\":\"" + time_expire + "\"}");
        System.out.println(sb.toString());

        return sb.toString();
    }


    private JSONObject queryTradeBizJson() {

        JSONObject json = new JSONObject();
        try {
            json.put("out_trade_no", out_trade_no);  // 商户订单号
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "json===" + json);
        return json;
    }

    /**
     * create the order info. 创建订单信息
     */
    private String queryTradeString(String timestamp) {

        // 开发者的AppId
        String orderInfo = "app_id=" + AliPayConstants.APP_ID;
        // 接口名称
        orderInfo += "&biz_content=" + queryTradeBizJson().toString();
        // 参数字符编码
        orderInfo += "&charset=" + charset;
        // 接口名称
        orderInfo += "&method=" + method_query;
        // 签名类型
        orderInfo += "&sign_type=" + "RSA";
        // 时间戳
        orderInfo += "&timestamp=" + timestamp;
        //接口版本号
        orderInfo += "&version=" + version;
        return orderInfo;
    }

    /**
     * call alipay sdk query. 调用SDK查询
     */
    private void query(String timestamp) throws Exception {

        // 订单
        String orderInfo = queryTradeString(timestamp);

        Log.e(TAG, "orderInfo===" + orderInfo);
        // 对订单做RSA 签名
        String sign = sign(orderInfo);
        Log.e(TAG, "sign===" + sign);

        // 完整的符合支付宝参数规范的订单信息
        final String payInfo = orderInfo + "&sign=\"" + sign;
        Log.e(TAG, "url==" + payInfo);

        //请求参数键-值对
        final Map<String, String> params = new HashMap<>();
        params.put("app_id", AliPayConstants.APP_ID);
        params.put("biz_content", queryTradeBizJson().toString());
        params.put("charset", charset);
        params.put("method", method_query);
        params.put("sign_type", "RSA");
        params.put("timestamp", timestamp);
        params.put("sign", sign);
        params.put("version", version);

        String urlPath = "https://openapi.alipay.com/gateway.do";
        InputStream is = NetTool.getInputStreamByPost(urlPath, params, "UTF-8");

        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString("data", new String(NetTool.readStream(is)));
//        Log.e(TAG,"data==="+new String(NetTool.readStream(is)));
        message.setData(bundle);
        message.what = 1002;
        handler.sendMessage(message);

    }


    /**
     * create the order info. 创建订单信息
     */
    private String getOrderInfo(String auth_code, String seller_id, String total_amount, String subject, String timestamp) {

        // 开发者的AppId
        String orderInfo = "app_id=" + AliPayConstants.APP_ID;
        // 接口名称
        orderInfo += "&biz_content=" + getQrCodePayInfo(auth_code, seller_id, total_amount, subject).toString();
        // 参数字符编码
        orderInfo += "&charset=" + charset;
        // 接口名称
        orderInfo += "&method=" + method;
        // 签名类型
        orderInfo += "&sign_type=" + "RSA";
        // 时间戳
        orderInfo += "&timestamp=" + timestamp;
        //接口版本号
        orderInfo += "&version=" + version;
        return orderInfo;
    }

    /**
     * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
     */
    private String getOutTradeNo() {
        String key = DateTimeUtils.date2Str(new Date(), "MMddHHmmssSSS");

        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 7; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        Log.e(TAG, "getOutTradeNo------" + key);
        key = key + sb.toString();
        Log.e(TAG, "getOutTradeNo===" + key);
        return key;
    }

//    public String getOutTradeNo() {
//        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss",
//                Locale.getDefault());
//        Date date = new Date();
//        String key = format.format(date);
//
//        Random r = new Random();
//        key = key + r.nextInt();
//        key = key.substring(0, 15);
//        Log.e(TAG, "getOutTradeNo===" + key);
//        return key;
//    }

    /**
     * sign the order info. 对订单信息进行签名
     *
     * @param content 待签名订单信息
     */
    private String sign(String content) {
        return SignUtils.sign(content, AliPayConstants.RSA_PRIVATE);
    }


    class URLTask extends TimerTask {

        public void run() {
            try {
                nowTime();
                query(now_time);


            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}