package com.dw.merchant;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.dw.merchant.util.UserConfig;

import java.io.File;

import io.fabric.sdk.android.Fabric;

/**
 * @Author: lvyongxu
 * @Date: 11:38 2015/10/13
 */
public class App extends Application {

//    private static final String TAG = "App_lyx";

    private static final String TAG = App.class.getSimpleName() + "_lyx";
    public static UserConfig userConfig;
    /**
     * 项目文件夹的名称 JinAFu
     */
    public static String projectName = "JinAFu";
    /**
     * 项目本地文件存放的根目录 /mnt/sdcard/JinAFu
     */
    public static String rootPath;
    private Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        this.mContext = this;

        userConfig = new UserConfig(mContext);

        /**
         * 存放项目文件的根目录文件夹 /mnt/sdcard/Test
         */
        File rootFile = new File(Environment.getExternalStorageDirectory(),
                projectName);
        if (rootFile.exists() && rootFile.isFile()) {
            rootFile.delete();
        }

//        CommUtils.deleteRecursive(rootFile);

        Log.e(TAG, "rootFile.exists()===" + rootFile.exists());

        if (!rootFile.exists()) {
            rootFile.mkdirs();
        }
        rootPath = rootFile.getPath();

        Log.e(TAG, "rootPath======" + rootPath);

    }
}
