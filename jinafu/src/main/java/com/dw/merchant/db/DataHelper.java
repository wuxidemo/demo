package com.dw.merchant.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class DataHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = ReceiveRecord.class.getSimpleName() + ".db"; // 数据库名
    private static final int DATABASE_VERSION = 1; // 数据库版本号,以后如果对实体类进行了修改,想要更新一下数据库的话,改一下版本号就行了

    public DataHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        // 程序第一次安装好启动后数据库创建自动执行,以后不会再自动执行
        try {
            TableUtils.createTable(connectionSource, ReceiveRecord.class);
            Log.i(DataHelper.class.getName(), "数据库创建成功！");
        } catch (Exception e) {
            Log.i(DataHelper.class.getName(), "数据库创建失败！", e);
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
                          int arg2, int arg3) {
        // 数据库检测到版本号不同时自动执行,在这里我们应该：
        try {
            // 1:删除原有的旧表
            TableUtils.dropTable(connectionSource, ReceiveRecord.class, true);
            // 2:创建现在的新表
            onCreate(db, connectionSource);
            Log.e(DataHelper.class.getName(), "更新数据库成功");
        } catch (SQLException e) {
            Log.e(DataHelper.class.getName(), "更新数据库失败", e);
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        super.close();
    }

}
