package com.dw.merchant.weixin;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dw.merchant.App;
import com.dw.merchant.Constants;
import com.dw.merchant.R;
import com.dw.merchant.activity.BaseActivity;
import com.dw.merchant.activity.ScanPayResultActivity;
import com.dw.merchant.util.DateTimeUtils;
import com.dw.merchant.util.MoneyAudioUtil;
import com.dw.merchant.util.NetUtils;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * 微信刷卡支付 商户收银员用扫码终端设备（智能手机或扫码枪等），扫描顾客的微信付款二维码并完成支付
 */
public class WeChatInitiativeScanPayActivity extends BaseActivity {

    private static final String TAG = WeChatInitiativeScanPayActivity.class.getSimpleName() + "_lyx";
    private static int ORDER_OK = 1002;
    final IWXAPI msgApi = WXAPIFactory.createWXAPI(this, null);
    private Context context;
    private TextView txtOrderStatus;
    private String userCode;
    private String total_fen = "", total = "";//订单金额（需要转为分为单位的）
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
            super.handleMessage(msg);
        }
    };
    private String outTradeNo;
    private LinearLayout layout_progress;

    private MoneyAudioUtil mMoneyAudioUtil = new MoneyAudioUtil(); //语音播报
    @Override
    protected int addLayout() {
        this.context = this;
        return R.layout.activity_weixin_active_scan_pay;
    }

    @Override
    protected void initLayout() {

        merchant_name = getIntent().getStringExtra("merchant_name");
        merchant_id = getIntent().getStringExtra("merchant_id");
        type = getIntent().getStringExtra("type");
        total = getIntent().getStringExtra("total");
        userCode = getIntent().getStringExtra("text");

        total_fen = String.valueOf((int) (Float.parseFloat(total) * 100));//分为单位,去掉小数点

        txtOrderStatus = (TextView) findViewById(R.id.text_order_status);
        layout_progress = (LinearLayout) findViewById(R.id.layout_progress);
        layout_progress.setVisibility(View.VISIBLE);

        msgApi.registerApp(Constants.APP_ID);

        if (NetUtils.hasNetwork(context)) {
            new InitiativeScanTask().execute();
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

    /**
     * 将商家扫描顾客支付二维码生成订单接口所需的参数拼接成xml格式的字符串
     *
     * @param userCode 顾客的支付二维码
     * @param body     商品或支付单简要描述
     * @return
     */
    private String genProductArgs(String userCode, String body) {

        outTradeNo = WeiXinUtils.genOutTradeNo(10);
        List<NameValuePair> valuePairs = new LinkedList<>();
        valuePairs.add(new BasicNameValuePair("appid", Constants.APP_ID));
        valuePairs.add(new BasicNameValuePair("auth_code", userCode));
//            valuePairs.add(new BasicNameValuePair("body", URLEncoder.encode(body, "UTF-8")));
        valuePairs.add(new BasicNameValuePair("body", body));
//			valuePairs.add(new BasicNameValuePair("detail", URLEncoder.encode("测试微信支付","UTF-8")));// 此参数可不传
        valuePairs.add(new BasicNameValuePair("mch_id", Constants.MCH_ID));
        valuePairs.add(new BasicNameValuePair("nonce_str", WeiXinUtils.genNonceStr()));
        valuePairs.add(new BasicNameValuePair("out_trade_no", outTradeNo));
        valuePairs.add(new BasicNameValuePair("spbill_create_ip", "127.0.0.1"));
        valuePairs.add(new BasicNameValuePair("sub_mch_id", App.userConfig.getWeiXinPayId()));
        valuePairs.add(new BasicNameValuePair("total_fee", total_fen));// 以分为单位
        // valuePairs.add(new BasicNameValuePair("trade_type", "NATIVE"));

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
     * 刷卡支付（商户收银员用扫码终端设备扫描顾客的微信付款码），生成订单
     *
     * @author lvyongxu
     * @date 2015-10-30 上午10:28:22
     */
    class InitiativeScanTask extends AsyncTask<Void, Void, Map<String, String>> {

        @Override
        protected void onPostExecute(Map<String, String> result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            layout_progress.setVisibility(View.GONE);

            String returnCode = result.get("return_code");

            if (returnCode.equalsIgnoreCase("SUCCESS")) {

                if (result.get("result_code").equalsIgnoreCase("SUCCESS")) {
//                    txtOrderStatus.setText("支付成功");

                    mMoneyAudioUtil.Audio(context, 11, total);//收款成功

                    String trade_no = result.get("transaction_id").toString();
                    String out_trade_no = result.get("out_trade_no").toString();
                    String buyer_logon_id = result.get("openid").toString();

                    Date orderDate = DateTimeUtils.str2Date(result.get("time_end").toString(),
                            DateTimeUtils.EnumDateFmt.yyyyMMddHHmmss);

                    String send_pay_date = DateTimeUtils.date2Str(orderDate,
                            DateTimeUtils.EnumDateFmt._yyyyMMddHHmm);

                    Log.e(TAG, "InitiativeScanTask: send_pay_date===" + send_pay_date);

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
//                    finish();

                } else {
                    String errCode = result.get("err_code");
                    Log.e(TAG, "err_code_des===" + result.get("err_code_des"));
                    Timer timer = new Timer();

                    if (errCode.equalsIgnoreCase("AUTHCODEEXPIRE")) {
                        txtOrderStatus.setText("付款码无效，请扫描顾客的微信付款码");
                        mMoneyAudioUtil.Audio(context, 10, total);//收款失败
                    } else if (errCode.equalsIgnoreCase("ORDERCLOSED")) {
                        txtOrderStatus.setText("订单已关闭，请重新生成订单并扫描顾客的支付码收款");
                        mMoneyAudioUtil.Audio(context, 10, total);//收款失败
                    } else if (errCode.equalsIgnoreCase("ORDERREVERSED")) {
                        txtOrderStatus.setText("当前订单状态为“订单已撤销”，请提示顾客重新支付");
                        mMoneyAudioUtil.Audio(context, 10, total);//收款失败
                    } else if (errCode.equalsIgnoreCase("NOTENOUGH")) {
                        txtOrderStatus.setText("余额不足.请提示顾客更换支付的银行卡后重新收款");
                        mMoneyAudioUtil.Audio(context, 10, total);//收款失败
                    } else if (errCode.equalsIgnoreCase("AUTH_CODE_ERROR")) {
                        txtOrderStatus.setText("每个二维码仅限使用一次，请顾客刷新付款码后重新收款");
                        mMoneyAudioUtil.Audio(context, 10, total);//收款失败
                    } else if (errCode.equalsIgnoreCase("NOTSUPORTCARD")) {
                        txtOrderStatus.setText("该卡不支持当前支付，请提示顾客换卡支付或绑定新卡后再支付");
                        mMoneyAudioUtil.Audio(context, 10, total);//收款失败
                    } else if (errCode.equalsIgnoreCase("AUTH_CODE_INVALID")) {
                        txtOrderStatus.setText("付款码无效，请扫描顾客的微信付款码");
                        mMoneyAudioUtil.Audio(context, 10, total);//收款失败
                    } else if (errCode.equalsIgnoreCase("SYSTEMERROR")) {
                        //TODO 记得调用订单查询API，然后根据返回的结果再做相应的处理
                        //请立即调用被扫订单结果查询API，查询当前订单状态，并根据订单的状态决定下一步的操作
                        txtOrderStatus.setText("系统超时");
                        timer.schedule(task, 100, 3000);
                    } else if (errCode.equalsIgnoreCase("BANKERROR")) {
                        //TODO 记得调用订单查询API，然后根据返回的结果再做相应的处理
                        //请立即调用被扫订单结果查询API，查询当前订单的不同状态，决定下一步的操作
                        txtOrderStatus.setText("银行端超时");
                        timer.schedule(task, 100, 3000);
                    } else if (errCode.equalsIgnoreCase("USERPAYING")) {
                        //TODO 记得调用订单查询API，然后根据返回的结果再做相应的处理
                        //等待5秒，然后调用被扫订单结果查询API接口，并根据订单的状态决定下一步的操作。
                        txtOrderStatus.setText("等待顾客输入支付密码");
                        timer.schedule(task, 5000, 3000);
                    }
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
            // TODO Auto-generated method stub
            String url = String.format("https://api.mch.weixin.qq.com/pay/micropay");
            String entity = genProductArgs(userCode, merchant_name);
            Log.e(TAG, "entity=" + entity);

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
            // Log.e(TAG, "orderUrl======" + orderUrl);

            String returnCode = result.get("return_code");

            if (returnCode.equalsIgnoreCase("SUCCESS")) {
                String tradeState = result.get("trade_state");
                String tradeStateDesc = result.get("trade_state_desc");

                txtOrderStatus.setText(tradeStateDesc);
                Log.e(TAG, "tradeState===" + tradeState);

                Message message = new Message();
                if (tradeState.equalsIgnoreCase("SUCCESS")) {
                    txtOrderStatus.setText("支付成功");
                    message.what = ORDER_OK; // 停止获取订单状态

                    mMoneyAudioUtil.Audio(context, 11, total);//收款成功

                    String trade_no = result.get("transaction_id").toString();
                    String out_trade_no = result.get("out_trade_no").toString();
                    String buyer_logon_id = result.get("openid").toString();

                    Date orderDate = DateTimeUtils.str2Date(result.get("time_end").toString(),
                            DateTimeUtils.EnumDateFmt.yyyyMMddHHmmss);

                    String send_pay_date = DateTimeUtils.date2Str(orderDate, DateTimeUtils.EnumDateFmt._yyyyMMddHHmm);

                    Log.e(TAG, "CheckOrderStatusTask: send_pay_date===" + send_pay_date);

                    Intent intent = new Intent(context, ScanPayResultActivity.class);
                    intent.putExtra("result", getString(R.string.zf_sucess));
                    intent.putExtra("type", "wx");

                    intent.putExtra("buyer_pay_amount", total);
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

                } else if (tradeState.equalsIgnoreCase("CLOSED")) {
                    txtOrderStatus.setText("订单已关闭");
                    message.what = ORDER_OK; // 停止获取订单状态
                    layout_progress.setVisibility(View.GONE);
                    mMoneyAudioUtil.Audio(context, 10, total);//收款失败
                } else if (tradeState.equalsIgnoreCase("PAYERROR")) {
                    txtOrderStatus.setText("支付失败");
                    message.what = ORDER_OK; // 停止获取订单状态
                    layout_progress.setVisibility(View.GONE);
                    mMoneyAudioUtil.Audio(context, 10, total);//收款失败
                } else if (tradeState.equalsIgnoreCase("REFUND")) {
                    txtOrderStatus.setText("该订单已转入退款状态");
                    message.what = ORDER_OK; // 停止获取订单状态
                    layout_progress.setVisibility(View.GONE);
                    mMoneyAudioUtil.Audio(context, 10, total);//收款失败
                } else if (tradeState.equalsIgnoreCase("REVOKED")) {//（刷卡支付）
                    txtOrderStatus.setText("该订单已撤销");
                    message.what = ORDER_OK; // 停止获取订单状态
                    layout_progress.setVisibility(View.GONE);
                    mMoneyAudioUtil.Audio(context, 10, total);//收款失败
                } else if (tradeState.equalsIgnoreCase("NOTPAY")) {
                    if (tradeStateDesc.equals("用户取消支付")) {
                        txtOrderStatus.setText("顾客取消支付");
                        mMoneyAudioUtil.Audio(context, 10, total);//收款失败
                    } else {
                        txtOrderStatus.setText("等待顾客支付支付中");
                    }
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
}