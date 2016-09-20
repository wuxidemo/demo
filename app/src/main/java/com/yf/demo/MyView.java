package com.yf.demo;


import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by wuyifeng on 2016/9/20.
 */

public class MyView extends View{

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.setMeasuredDimension(getMeasureWidth(widthMeasureSpec),getMeasureHeight(heightMeasureSpec));
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    int getMeasureWidth(int widthMeasureSpec){
        int result = 0;
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specsize = MeasureSpec.getSize(widthMeasureSpec);
        if(specMode == MeasureSpec.EXACTLY){
            //match_parent或者 设定了具体width
            result = specsize;
        }else{
            result = 200;
            if(specMode == MeasureSpec.AT_MOST){
                //wrap_content
                result = Math.min(result,specsize);
            }
        }
        return result;
    }

    int getMeasureHeight(int heightMeasureSpec){
        int result = 0;
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        int specsize = MeasureSpec.getSize(heightMeasureSpec);
        if(specMode == MeasureSpec.EXACTLY){
            //match_parent或者 设定了具体height px/dp
            result = specsize;
        }else{
            result = 200;//default value
            if(specMode == MeasureSpec.AT_MOST){
                //wrap_content
                result = Math.min(result,specsize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
