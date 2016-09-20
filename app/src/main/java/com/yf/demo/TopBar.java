package com.yf.demo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by wuyifeng on 2016/9/20.
 */

public class TopBar extends RelativeLayout {

    private Button mLeftButton;
    private Button mRightButton;
    private TextView mTitleView;

    private int mLeftTextColor;
    private Drawable mLeftBackground;
    private String mLeftText;

    private int mRightTextColor;
    private Drawable mRightBackground;
    private String mRightText;

    private float mTitleTextSize;
    private int mTitleTextColor;
    private String mTitle;

    private LayoutParams mLeftParams;
    private LayoutParams mRightParams;
    private LayoutParams mTitleParams;

    private TopBarInterface mListener;

    public TopBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.TopBar);//将attrs.xml的属性值存储到ta中
        mLeftTextColor = ta.getColor(R.styleable.TopBar_leftTextColor,0);
        mLeftBackground = ta.getDrawable(R.styleable.TopBar_leftBackground);
        mLeftText = ta.getString(R.styleable.TopBar_leftText);

        mRightTextColor = ta.getColor(R.styleable.TopBar_leftTextColor,0);
        mRightBackground = ta.getDrawable(R.styleable.TopBar_leftBackground);
        mRightText = ta.getString(R.styleable.TopBar_leftText);

        mTitleTextSize = ta.getDimension(R.styleable.TopBar_titleTextSize,10);
        mTitleTextColor = ta.getColor(R.styleable.TopBar_titleTextColor,0);
        mTitle = ta.getString(R.styleable.TopBar_title);

        ta.recycle();//释放ta,资源回收，避免重复创建出错

        mLeftButton = new Button(context);
        mRightButton = new Button(context);
        mTitleView = new TextView(context);

        //为3个组件赋值
        mLeftButton.setTextColor(mLeftTextColor);
        mLeftButton.setBackground(mLeftBackground);
        mLeftButton.setText(mLeftText);

        mRightButton.setTextColor(mRightTextColor);
        mRightButton.setBackground(mRightBackground);
        mRightButton.setText(mRightText);

        mTitleView.setText(mTitle);
        mTitleView.setTextSize(mTitleTextSize);
        mTitleView.setTextColor(mTitleTextColor);
        mTitleView.setGravity(Gravity.CENTER);

        //为组件元素设置相应的布局元素
        mLeftParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        mLeftParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,TRUE);
        //添加到view group
        addView(mLeftButton,mLeftParams);

        //为组件元素设置相应的布局元素
        mRightParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        mRightParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,TRUE);
        //添加到view group
        addView(mRightButton,mRightParams);

        //为组件元素设置相应的布局元素
        mTitleParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        mTitleParams.addRule(RelativeLayout.CENTER_IN_PARENT,TRUE);
        //添加到view group
        addView(mTitleView,mTitleParams);

        mLeftButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.leftClick();

            }
        });

        mRightButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.rightClick();
            }
        });

    }

    public void setOnTopBarClickListener(TopBarInterface mListener){
        this.mListener = mListener;
    }

    /*
    *
    *
    * @param id id
    * @param flag 是否显示
     */
    public void setButtonVisable(int id,boolean flag){
        if(flag){
            if(id == 0){
                mLeftButton.setVisibility(View.VISIBLE);
            }else{
                mRightButton.setVisibility(View.VISIBLE);
            }
        }else{
            if(id == 0){
                mLeftButton.setVisibility(View.GONE);
            }else{
                mRightButton.setVisibility(View.GONE);
            }
        }
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}
