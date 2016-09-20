package com.yf.demo;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;


public class MyActivity extends Activity {

private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_my);

        TopBar mTopBar = (TopBar) findViewById(R.id.mytopbar);
        mTopBar.setButtonVisable(0,true);
        mTopBar.setButtonVisable(1,false);

        mTopBar.setOnTopBarClickListener(new TopBarInterface() {
            @Override
            public void leftClick() {
                Toast.makeText(context,"left click",Toast.LENGTH_LONG).show();
            }

            @Override
            public void rightClick() {
                Toast.makeText(context,"right click",Toast.LENGTH_LONG).show();
            }
        });
    }

}
