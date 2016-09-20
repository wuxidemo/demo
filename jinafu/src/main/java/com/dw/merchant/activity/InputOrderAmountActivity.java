package com.dw.merchant.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dw.merchant.R;

/**
 * Created by Acer on 2015/11/11.
 */
public class InputOrderAmountActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = InputOrderAmountActivity.class.getSimpleName() + "_lyx";
    private static final int TO_SCAN = 1001;
    private Context context;
    private EditText editTotalAmount;
    private Button btnNum1, btnNum2, btnNum3, btnNum4, btnNum5, btnNum6, btnNum7, btnNum8, btnNum9,
            btnNum0, btnPoint, btnClean, btnSure;
    private String digitNum = "";

    @Override
    protected int addLayout() {

        this.context = this;

        return R.layout.activity_input_order_amount;
    }

    @Override
    protected void initLayout() {

        type = getIntent().getStringExtra("type");
        merchant_name = getIntent().getStringExtra("merchant_name");
        merchant_id = getIntent().getStringExtra("merchant_id");

        if (type.equals("zfb"))
            title = "支付宝支付";
        else if (type.equals("wx"))
            title = "微信支付";

        flag = "zf";//支付页面ZfbGateActivity

        editTotalAmount = (EditText) findViewById(R.id.edt_total_amount);

        editTotalAmount.setInputType(InputType.TYPE_NULL);//禁止弹出系统键盘

        btnNum1 = (Button) findViewById(R.id.digitkeypad_1);
        btnNum2 = (Button) findViewById(R.id.digitkeypad_2);
        btnNum3 = (Button) findViewById(R.id.digitkeypad_3);
        btnNum4 = (Button) findViewById(R.id.digitkeypad_4);
        btnNum5 = (Button) findViewById(R.id.digitkeypad_5);
        btnNum6 = (Button) findViewById(R.id.digitkeypad_6);
        btnNum7 = (Button) findViewById(R.id.digitkeypad_7);
        btnNum8 = (Button) findViewById(R.id.digitkeypad_8);
        btnNum9 = (Button) findViewById(R.id.digitkeypad_9);
        btnNum0 = (Button) findViewById(R.id.digitkeypad_0);
        btnPoint = (Button) findViewById(R.id.digitkeypad_point);
        btnClean = (Button) findViewById(R.id.digitkeypad_clean);
        btnSure = (Button) findViewById(R.id.digitkeypad_sure);

        btnNum1.setOnClickListener(this);
        btnNum2.setOnClickListener(this);
        btnNum3.setOnClickListener(this);
        btnNum4.setOnClickListener(this);
        btnNum5.setOnClickListener(this);
        btnNum6.setOnClickListener(this);
        btnNum7.setOnClickListener(this);
        btnNum8.setOnClickListener(this);
        btnNum9.setOnClickListener(this);
        btnNum0.setOnClickListener(this);
        btnPoint.setOnClickListener(this);

        btnClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                digitNum = "";
                editTotalAmount.setText(digitNum);
            }
        });

        btnSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String total_amount = editTotalAmount.getText().toString().trim();

                if (total_amount.isEmpty()) {
                    Toast.makeText(context, R.string.total_amount, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (total_amount.endsWith(".")) {
                    total_amount += "00";
                    digitNum = total_amount;
                    editTotalAmount.setText(total_amount);
                }
                if (!total_amount.contains(".")) {
                    total_amount += ".00";
                    digitNum = total_amount;
                    editTotalAmount.setText(total_amount);
                } else {
                    String data[] = total_amount.split("\\.");
                    Log.e(TAG, "data.length======" + data.length);
                    if (data.length == 2) {
                        int decimalLength = data[1].length();
                        if (decimalLength == 1) {
                            total_amount += "0";
                            digitNum = total_amount;
                            editTotalAmount.setText(total_amount);
                        }
                    }
                }

                double totalDouble = Double.parseDouble(total_amount);

                int total = (int) (totalDouble * 100);

                Log.e(TAG, "total======" + total);

                if (total > 0) {
                    Intent intent = new Intent();
                    intent.putExtra("total", total_amount);
                    intent.putExtra("merchant_id", merchant_id);
                    intent.putExtra("merchant_name", merchant_name);
                    intent.putExtra("type", type);
                    intent.setClass(context, ScanPrePayActivity.class);
                    startActivityForResult(intent, TO_SCAN);

//                    startActivity(intent);
                } else {
                    digitNum = "";
                    editTotalAmount.setText(digitNum);
                    Toast.makeText(context, R.string.total_amount, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e(TAG, "requestCode===" + requestCode + "&&resultCode===" + resultCode);

        if (requestCode == TO_SCAN && resultCode == Activity.RESULT_OK) {
            digitNum = "";
            editTotalAmount.setText(digitNum);
        }
    }

    @Override
    public void onClick(View view) {

        if (digitNum.length() < 8) {
            if (numIsLegal(digitNum)) {
                switch (view.getId()) {
                    case R.id.digitkeypad_1:
                        inputNumber(R.id.digitkeypad_1);
                        break;
                    case R.id.digitkeypad_2:
                        inputNumber(R.id.digitkeypad_2);
                        break;
                    case R.id.digitkeypad_3:
                        inputNumber(R.id.digitkeypad_3);
                        break;
                    case R.id.digitkeypad_4:
                        inputNumber(R.id.digitkeypad_4);
                        break;
                    case R.id.digitkeypad_5:
                        inputNumber(R.id.digitkeypad_5);
                        break;
                    case R.id.digitkeypad_6:
                        inputNumber(R.id.digitkeypad_6);
                        break;
                    case R.id.digitkeypad_7:
                        inputNumber(R.id.digitkeypad_7);
                        break;
                    case R.id.digitkeypad_8:
                        inputNumber(R.id.digitkeypad_8);
                        break;
                    case R.id.digitkeypad_9:
                        inputNumber(R.id.digitkeypad_9);
                        break;
                    case R.id.digitkeypad_0:
                        if (!digitNum.startsWith("0") || digitNum.contains(".")) {
                            digitNum += 0;
                        }
                        break;
                    case R.id.digitkeypad_point:

                        if (digitNum.length() == 0) {
                            digitNum = "0" + digitNum;
                        }

//                        if (digitNum.startsWith(".")){
//                            digitNum  = "0" +digitNum;
//                        }
                        if (!digitNum.contains(".")) {
                            digitNum += ".";
                        }
                        break;
                }
            } else {
                Toast.makeText(context, "金额精确到分", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "最多支持输入8位", Toast.LENGTH_SHORT).show();
        }
        editTotalAmount.setText(digitNum);
        editTotalAmount.setSelection(null != digitNum ? digitNum.length() : 0);
    }


    private void inputNumber(int btnId) {

        String number = ((Button) findViewById(btnId)).getText().toString();
        digitNum += number;
        if (digitNum.startsWith("0") && !digitNum.contains(".")) {
            digitNum = digitNum.substring(1, digitNum.length());
        }
    }

    /**
     * 判断输入的金额是否合法（精确到分，小数点后只可以输入两位）
     * 注意 "." 小数点为特殊字符，使用它作为分割符时需要转译
     *
     * @param string 需要验证的内容
     * @return true 合法；false 不合法
     */
    private boolean numIsLegal(String string) {
        Log.e(TAG, "numIsLegal & string======" + string);
        if (string.contains(".") && !(string.endsWith("."))) {
            String data[] = string.split("\\.");
            int decimalLength = data[1].length();
            if (decimalLength < 2) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
}
