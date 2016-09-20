package com.dw.merchant.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @Author: lvyongxu
 * @Date: 10:55 2015/10/13
 */
public class UserConfig {

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    public UserConfig(Context context) {
        this.sp = context.getSharedPreferences("UserConfig", Context.MODE_PRIVATE);
        this.editor = sp.edit();
        editor.commit();
    }

    public String getID() {
        return sp.getString("id", "");
    }

    public void setID(String id) {
        editor.putString("id", id);
        editor.commit();
    }

    public String getAccount() {
        return sp.getString("account", "");
    }

    public void setAccount(String account) {
        editor.putString("account", account);
        editor.commit();
    }

    public String getPassword() {
        return sp.getString("password", "");
    }

    public void setPassword(String password) {
        editor.putString("password", password);
        editor.commit();
    }

    public boolean isRemembered() {
        return sp.getBoolean("isRemembered", false);
    }

    public void setRemember(boolean remember) {
        editor.putBoolean("isRemembered", remember);
        editor.commit();
    }


    public String getWeiXinPayId() {
        return sp.getString("mchId", "");
    }

    public void setWeiXinPayId(String mchId) {
        editor.putString("mchId", mchId);
        editor.commit();
    }

    public String getAliPayId() {
        return sp.getString("sellerId", "");
    }

    public void setAliPayId(String sellerId) {
        editor.putString("sellerId", sellerId);
        editor.commit();
    }

}
