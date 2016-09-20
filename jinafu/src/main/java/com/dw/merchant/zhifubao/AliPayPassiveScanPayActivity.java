package com.dw.merchant.zhifubao;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dd.CircularProgressButton;
import com.dw.merchant.R;
import com.dw.merchant.activity.BaseActivity;
import com.dw.merchant.activity.ScanPayResultActivity;
import com.dw.merchant.util.DateTimeUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 支付宝扫码支付（顾客用支付宝扫一扫功能，扫描商户根据订单金额生成的二维码方式完成支付）
 */
public class AliPayPassiveScanPayActivity extends BaseActivity {


    private static final String TAG = AliPayPassiveScanPayActivity.class.getSimpleName() + "_lyx";
    private static final int QR_WIDTH = 200;
    private static final int QR_HEIGHT = 200;

    private static final int ORDER_OK = 2001;
    private static final int ORDER_CANCEL = 2002;
    private static final int IMAGE_HALFWIDTH = 30;//宽度值，影响中间图片大小


    //    // 商户私钥，pkcs8格式
//    public static final String RSA_PRIVATE = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAK5367p+RchoyzKBLXtzLAj/CoiQfgnEUUKRjomxvdwvZkL8TEfie6ZUY2WhVObYVELkup7l7PYUtAZRn8UkUfSDkyhuTL4LV/UDlggbOg68B/s6PnTd6Ldk7XnjY+UfyVnhpUUnHdhtXPIYuj83Hmndqj4xHXiyqC/hRYZBsE11AgMBAAECgYBAXXmX+dHg19hvL30KGDlcsErAix4UmFqqRmzhm7NBsjL174Js2r9nY6av7c8WwySC8UNwL782Ifkwg5h/8KBTCtQgvPYapF8PSyD1/cHzF6Tcb32XoZz7/uypnVNVt6kPraTGfXa0jDe+CRCGk09vmnranV/Mev8U/yJ9sIp62QJBANtYuQJYodJuZLC+qld9vZrrsGHD3FYoBKNMOq6gTm7RLX+Tihb9Rithds54AoifHCXtBJfeiupYAOp9Hu9SROMCQQDLn2MbU5koRV3Q8XxmKITIVw5Brq11+rRIMrjilws8JyDQOTOuN3Z29lqw9S4teFkciIxKIHqYpmnPgZpCzQvHAkB82LUFJtmEYp0hFIT0I3emE/xiyQ5CY6iwIZVNC6VY4eqZsKpqh2JHEsSCpEAc7yMgWxXAM0SyOcDbtrfC0/qtAkB50M2yoG2k+PKqOH3qg90EGYiu5LhjN2u5MZcH/8K55tKrnzz6wbV+b91LtjI9A52UA2CiTBHr1srAWFGYGyErAkEAhBw+z7JpGhS8fxLeoTxVrajqpeV31paHywb2fYTotT5eHPoH7qbG8YeGVcCv7YHygxjhY0P7WAcaCtpeIS9nww==";
//    //支付场景,条码支付
//    private String scene = "bar_code";
//
//    private String app_id = "2015110600707517";
    URLGetNotifyTask notify_task = new URLGetNotifyTask();//duwei-notify回调
    URLTask task = new URLTask();//alipay-query回调
    private ImageView img;
    //接口名称
    private String method = "alipay.trade.precreate";

    //    //    卖家支付宝用户ID,如果该值为空，则默认为商户签约账号对应的支付宝用户ID	2088102146225135
//    private String seller_id = "2088012896752633";
    //接口名称-查询
    private String method_query = "alipay.trade.query";
    private String charset = "utf-8";
    //接口版本号
    private String version = "1.0";

    //    private String notify_url = "http://ts.do-wi.cn/nsh/appapi/alipaycallback/";
    private String out_trade_no;
    private String now_time;
    private String total;
    private TextView tv_status, tv_total;
    Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i(TAG, "msg.what===" + msg.what);

            switch (msg.what) {
                case 1001:
                    String data = msg.getData().get("data").toString();
                    try {
                        JSONObject json = new JSONObject(data);
                        JSONObject content = new JSONObject(json.get("alipay_trade_precreate_response").toString());
                        String code = content.get("code").toString();

                        if (code.equals("10000")) {
                            String qr_code = content.get("qr_code").toString();
                            qr_code = qr_code.replace("\\", "");
//                            createImage(qr_code);

                            Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_pay_alipay);
                            try {
                                //调用方法createCode生成二维码
                                createImage(qr_code, logo, BarcodeFormat.QR_CODE);
                            } catch (WriterException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                            Timer timer = new Timer();
                            timer.schedule(task, 5000, 3000);
                            tv_status.setText("等待顾客支付中");

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, new String(data));
                    break;

                case 1003://duwei-notify 接口回调
                    if (msg.obj != null) {
                        String result = msg.obj.toString();
                        Log.e(TAG, "result==" + result);
                        try {
                            JSONObject json = new JSONObject(result);
                            if (json.has(out_trade_no)) {
                                String info = json.get(out_trade_no).toString();
                                String[] arry_info = info.split("&");
                                for (String s : arry_info) {
                                    if (s.contains("trade_status=")) {
                                        s = s.substring(13);
                                        Log.e(TAG, "s==" + s);

                                        if (s.equals("TRADE_SUCCESS")) {
                                            tv_status.setText(getResources().getText(R.string.zf_sucess));
                                            Intent intent = new Intent();
                                            intent.putExtra("result", getResources().getText(R.string.zf_sucess));
                                            intent.setClass(AliPayPassiveScanPayActivity.this, ScanPayResultActivity.class);
                                            startActivity(intent);


                                        } else {
                                            tv_status.setText(getResources().getText(R.string.zf_fail));
                                            Intent intent = new Intent();
                                            intent.putExtra("result", getResources().getText(R.string.zf_fail));
                                            intent.setClass(AliPayPassiveScanPayActivity.this, ScanPayResultActivity.class);
                                            startActivity(intent);

                                        }
                                        task.cancel();
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 1002://alipay-query接口回调
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
                                    tv_status.setText("等待顾客支付中");
                                    break;
                                case "TRADE_CLOSED":
                                    tv_status.setText("顾客未付款交易超时关闭，或支付完成后全额退款");
                                    task.cancel();
                                    break;
                                case "TRADE_SUCCESS":
//                                    tv_status.setText("交易支付成功");

                                    String buyer_pay_amount = content.get("buyer_pay_amount").toString();
                                    String trade_no = content.get("trade_no").toString();
                                    String out_trade_no = content.get("out_trade_no").toString();
                                    String buyer_logon_id = content.get("buyer_logon_id").toString();
                                    String send_pay_date = content.get("send_pay_date").toString();

                                    Intent intent = new Intent(AliPayPassiveScanPayActivity.this, ScanPayResultActivity.class);
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

                                    task.cancel();
                                    break;
                                case "TRADE_FINISHED":
                                    tv_status.setText("交易结束，不可退款");
                                    task.cancel();
                                    break;
                            }
                        } else if (code.equals("10003")) {

                        } else if (code.equals("40004")) {
                            String sub_msg = content.get("sub_msg").toString();
                            tv_status.setText(sub_msg);

                            String sub_code = content.get("sub_code").toString();
                            switch (sub_code) {
                                case "ACQ.TRADE_NOT_EXIST":
                                    tv_status.setText("等待顾客支付中");
                                    break;
                                case "ACQ.SYSTEM_ERROR":
                                    tv_status.setText("系统超时，请重新生成订单，或者使用条码支付方式收款");
                                    break;
                                case "ACQ.INVALID_PARAMETER":
                                    tv_status.setText("数据异常，请重新生成订单，或者使用条码支付方式收款");
                                    break;

                                default:
                                    tv_status.setText(getResources().getText(R.string.zf_fail));
                            }
                        } else if (code.equals("20000")) {

                            tv_status.setText("业务出现未知错误或者系统异常,请重试");

                        } else {
                            tv_status.setText(getResources().getText(R.string.zf_fail));
                            task.cancel();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;

            }
        }
    };
    private CircularProgressButton btn_tmf;//扫一扫

    @Override
    protected int addLayout() {
        return R.layout.activity_alipay_passive_scan_pay;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (task != null) {
            task.cancel();
        }
    }

    @Override
    protected void initLayout() {

        flag = "qrcode";

        img = (ImageView) findViewById(R.id.img);
        tv_status = (TextView) findViewById(R.id.tv_status);
        tv_total = (TextView) findViewById(R.id.tv_total);
        btn_tmf = (CircularProgressButton) findViewById(R.id.btn_tmf);

        Intent intent = getIntent();
        total = intent.getStringExtra("total");
        merchant_name = getIntent().getStringExtra("merchant_name");
        merchant_id = getIntent().getStringExtra("merchant_id");
        type = getIntent().getStringExtra("type");

        tv_total.setText("￥" + total);

        btn_tmf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        nowTime();


        out_trade_no = getOutTradeNo();
        Log.e(TAG, "out_trade_no==" + out_trade_no);
        pay(AliPayConstants.SELLER_ID, total, merchant_name, now_time);

    }

    private void nowTime() {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        now_time = df.format(date);
    }

    /**
     * call alipay sdk pay. 调用SDK支付
     */
    private void pay(String seller_id, String total_amount, String subject, String timestamp) {

        // 订单
        String orderInfo = getOrderInfo(seller_id, total_amount, subject, timestamp);

        Log.e(TAG, "orderInfo===" + orderInfo);
        // 对订单做RSA 签名
        String sign = sign(orderInfo);
        Log.e(TAG, "sign===" + sign);

        // 完整的符合支付宝参数规范的订单信息
        String payInfo = orderInfo + "&sign=\"" + sign;
        Log.e(TAG, "url==" + payInfo);

        String qrOrder = getQrCodePayInfo(seller_id, total_amount, subject).toString();

        final Map<String, String> params = new HashMap();
        params.put("app_id", AliPayConstants.APP_ID);
        params.put("biz_content", qrOrder);
        params.put("charset", charset);
        params.put("method", method);
        params.put("sign_type", "RSA");
        params.put("timestamp", timestamp);
        params.put("sign", sign);
        params.put("version", version);
        params.put("notify_url", AliPayConstants.NOTIFY_URL + out_trade_no);

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
    private JSONObject getQrCodePayInfo(String seller_id, String total_amount, String subject) {

        JSONObject json = new JSONObject();
        try {
            json.put("total_amount", total_amount);   // 订单总金额
            json.put("out_trade_no", out_trade_no);  // 商户订单号
            json.put("seller_id", seller_id);     // 卖家支付宝用户ID

            json.put("subject", subject);// 订单标题


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

    /**
     * create the order info. 创建订单信息
     */
    private String getOrderInfo(String seller_id, String total_amount, String subject, String timestamp) {

        // 开发者的AppId
        String orderInfo = "app_id=" + AliPayConstants.APP_ID;
        // 接口名称
        orderInfo += "&biz_content=" + getQrCodePayInfo(seller_id, total_amount, subject).toString();
        // 参数字符编码
        orderInfo += "&charset=" + charset;
        // 接口名称
        orderInfo += "&method=" + method;
        //接口异步通知url
        orderInfo += "&notify_url=" + AliPayConstants.NOTIFY_URL + out_trade_no;
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

    /**
     * sign the order info. 对订单信息进行签名
     *
     * @param content 待签名订单信息
     */
    private String sign(String content) {
        return SignUtils.sign(content, AliPayConstants.RSA_PRIVATE);
    }


    /**
     * 根据字符串生成相应的二维码图片
     *
     * @param string 要生成二维码的字符串
     */
    private void createImage(String string) {
        Log.e(TAG, "string======" + string);
        if (string == null || "".equals(string) || string.length() < 1) {
            return;
        }
        Hashtable<EncodeHintType, String> hints = new Hashtable();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = new QRCodeWriter().encode(string,
                    BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
        for (int y = 0; y < QR_HEIGHT; y++) {
            for (int x = 0; x < QR_WIDTH; x++) {
                if (bitMatrix.get(x, y)) {
                    pixels[y * QR_WIDTH + x] = 0xff000000;
                } else {
                    pixels[y * QR_WIDTH + x] = 0xffffffff;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
        img.setImageBitmap(bitmap);
    }


    /**
     * 生成二维码
     *
     * @param string  二维码中包含的文本信息
     * @param mBitmap logo图片
     * @param format  编码格式
     * @return Bitmap 位图
     * @throws WriterException
     */
    private void createImage(String string, Bitmap mBitmap, BarcodeFormat format)
            throws WriterException {
        Matrix m = new Matrix();
        float sx = (float) 2 * IMAGE_HALFWIDTH / mBitmap.getWidth();
        float sy = (float) 2 * IMAGE_HALFWIDTH
                / mBitmap.getHeight();
        m.setScale(sx, sy);//设置缩放信息
        //将logo图片按martix设置的信息缩放
        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
                mBitmap.getWidth(), mBitmap.getHeight(), m, false);
        MultiFormatWriter writer = new MultiFormatWriter();
        Hashtable<EncodeHintType, String> hst = new Hashtable<EncodeHintType, String>();
        hst.put(EncodeHintType.CHARACTER_SET, "UTF-8");//设置字符编码
        BitMatrix matrix = writer.encode(string, format, 400, 400, hst);//生成二维码矩阵信息
        int width = matrix.getWidth();//矩阵高度
        int height = matrix.getHeight();//矩阵宽度
        int halfW = width / 2;
        int halfH = height / 2;
        int[] pixels = new int[width * height];//定义数组长度为矩阵高度*矩阵宽度，用于记录矩阵中像素信息
        for (int y = 0; y < height; y++) {//从行开始迭代矩阵
            for (int x = 0; x < width; x++) {//迭代列
                if (x > halfW - IMAGE_HALFWIDTH && x < halfW + IMAGE_HALFWIDTH
                        && y > halfH - IMAGE_HALFWIDTH
                        && y < halfH + IMAGE_HALFWIDTH) {//该位置用于存放图片信息
                    //记录图片每个像素信息
                    pixels[y * width + x] = mBitmap.getPixel(x - halfW
                            + IMAGE_HALFWIDTH, y - halfH + IMAGE_HALFWIDTH);
                } else {
                    if (matrix.get(x, y)) {//如果有黑块点，记录信息
                        pixels[y * width + x] = 0xff000000;//记录黑块信息
                    }
                }

            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        // 通过像素数组生成bitmap
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        img.setImageBitmap(bitmap);
    }

    private JSONObject queryTradeBizJson() {

        JSONObject json = new JSONObject();
        try {
            json.put("out_trade_no", out_trade_no);  // 商户订单号
        } catch (JSONException e) {
            e.printStackTrace();
        }
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


    class URLGetNotifyTask extends TimerTask {

        public void run() {
            try {
                String msg = NetTool.readContentFromGet();
                Message message = Message.obtain();
                message.obj = msg;
                message.what = 1003;
                handler.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
