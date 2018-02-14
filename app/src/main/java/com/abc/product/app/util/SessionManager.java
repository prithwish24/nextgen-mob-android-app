package com.abc.product.app.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.abc.product.app.android.LoginActivity;

import java.util.HashMap;

/**
 * Created by prithwish on 2/12/2018.
 */

public class SessionManager {
    private static final String SHARED_PREF_NAME = "CarRentalPref";
    private static final String IS_LOGGED_IN = "isUserLoggedIn";

    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    public boolean isLoggedIn(){
        return sharedPreferences.getBoolean(IS_LOGGED_IN, false);
    }

    public void createLoginSession(String name, String email) {
        editor.putBoolean(IS_LOGGED_IN, true);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.commit();
    }

    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(KEY_NAME, sharedPreferences.getString(KEY_NAME, null));
        map.put(KEY_EMAIL, sharedPreferences.getString(KEY_EMAIL, null));
        return map;
    }

    public String getData(String key) {
        return sharedPreferences.getString(key, "");
    }

    public void checkLogin(){
        if(!this.isLoggedIn()){
            Intent i = new Intent(context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

    public void logoutUser(){
        editor.clear();
        editor.commit();

        Intent i = new Intent(context, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(i);
    }

}
