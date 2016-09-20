package com.yf.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by wuyifeng on 2016/9/20.
 */

public class MyTextView extends TextView{

    private int mViewWidth = 0;
    private int mTranslate;
    private Paint mPaint;
    private LinearGradient mLinearGradient;
    private Matrix mGradientMatrix;

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(mViewWidth == 0){
            mViewWidth = getMeasuredWidth();
            if(mViewWidth > 0){
                mPaint = getPaint();
                mLinearGradient = new LinearGradient(0,0,mViewWidth,0,new int[]{
                        Color.BLUE,0xffffffff,Color.RED
                },null,
                        Shader.TileMode.CLAMP);
                mPaint.setShader(mLinearGradient);
                mGradientMatrix = new Matrix();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /*Paint mPaint1 = new Paint();
        mPaint1.setColor(getResources().getColor(R.color.colorAccent));
        mPaint1.setStyle(Paint.Style.FILL);
        canvas.drawRect(0,0,getMeasuredWidth(),getMeasuredHeight(),mPaint1);

        Paint mPaint2 = new Paint();
        mPaint2.setColor(getResources().getColor(R.color.colorPrimary));
        mPaint2.setStyle(Paint.Style.FILL);
        canvas.drawRect(10,10,getMeasuredWidth()-10,getMeasuredHeight()-10,mPaint2);*/

        //canvas.save();
        //canvas.translate(10,0);
        super.onDraw(canvas);
        //canvas.restore();

        if(mGradientMatrix != null){
            mTranslate += mViewWidth/5;
            if(mTranslate > mViewWidth){
                mTranslate = -mViewWidth;

            }
            mGradientMatrix.setTranslate(mTranslate,0);
            mLinearGradient.setLocalMatrix(mGradientMatrix);
            postInvalidateDelayed(100);
        }
    }
}
