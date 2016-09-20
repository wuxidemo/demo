package com.dw.merchant.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工具类
 *
 * @Author: lvyongxu
 * @Date: 11:16 2015/12/15
 */
public class CommUtils {

    /**
     * 获取软件版本名称 1.1
     *
     * @param context
     * @return 软件版本号
     */
    public static String getAppVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "1.0";
        }
    }

    /**
     * 递归删除所有文件
     *
     * @param fileOrDirectory 文件或者文件夹
     */
    public static void deleteRecursive(File fileOrDirectory) {
        try {
            if (fileOrDirectory.isDirectory()) {
                for (File child : fileOrDirectory.listFiles()) {

                    Log.e("CommUtils", "child.getPath()==" + child.getPath());

                    child.delete();

                    deleteRecursive(child);
                }
            }
        } catch (Exception e) {
            Log.e("CommUtils", "deleteRecursive error:", e);
        }
    }

    /**
     * 显示键盘
     *
     * @param editText 要获取光标并显示键盘的控件
     */
    public static void showKeyboard(EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();

        InputMethodManager inputManager = (InputMethodManager) editText.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(editText, 0);
    }

    /**
     * 判断是否为整数
     *
     * @param str 传入的字符串
     * @return 是整数返回true, 否则返回false
     */
    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    /**
     * 手机号码验证
     *
     * @param phoneNumber 要验证的手机号码
     * @return
     */
    public static boolean isMobileNum(String phoneNumber) {
        // 验证手机号
        Pattern pattern = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        String regExp = "^[1]([3][0-9]{1}|59|58|88|89)[0-9]{8}$";

        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    /**
     * 小数点后保留两位小数
     *
     * @param total 要格式化的金额
     * @return
     */
    public static String keepTwoDecimal(double total) {
        DecimalFormat df = new DecimalFormat("######0.00");
        return df.format(total);
    }


    /**
     * 小数点后保留两位小数 String.format("%.2f", total)
     *
     * @param total 要格式化的金额
     * @return
     */
    public static String getTwoDecimal(double total) {
        return String.format("%.2f", total);
    }


    /**
     * 生成二维码
     *
     * @param string          二维码中包含的文本信息
     * @param mBitmap         logo图片
     * @param IMAGE_HALFWIDTH 图标高度
     * @return Bitmap 位图
     * @throws WriterException
     */
    public static Bitmap createImage(String string, Bitmap mBitmap, int IMAGE_HALFWIDTH) {
        Matrix m = new Matrix();
        float sx = (float) 2 * IMAGE_HALFWIDTH / mBitmap.getWidth();
        float sy = (float) 2 * IMAGE_HALFWIDTH / mBitmap.getHeight();
        m.setScale(sx, sy);//设置缩放信息
        //将logo图片按martix设置的信息缩放
        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
                mBitmap.getWidth(), mBitmap.getHeight(), m, false);
        MultiFormatWriter writer = new MultiFormatWriter();
        Hashtable<EncodeHintType, String> hst = new Hashtable();
        hst.put(EncodeHintType.CHARACTER_SET, "UTF-8");//设置字符编码
        BitMatrix matrix = null;//生成二维码矩阵信息
        try {
            matrix = writer.encode(string, BarcodeFormat.QR_CODE, 400, 400, hst);
        } catch (WriterException e) {
            e.printStackTrace();
        }
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

        return bitmap;
    }

    /**
     * 根据字符串生成相应的二维码图片
     *
     * @param string    要生成二维码的字符串
     * @param QR_WIDTH  图片宽度
     * @param QR_HEIGHT 图片高度
     * @return
     */
    private static Bitmap createImage(String string, int QR_WIDTH, int QR_HEIGHT) {
        if (string == null || "".equals(string) || string.length() < 1) {
            return null;
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
        return bitmap;
    }

}
