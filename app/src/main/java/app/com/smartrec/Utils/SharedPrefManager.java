package app.com.smartrec.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    SharedPreferences sharedPreferences;
    Context mContext;
    // shared pref mode
    int PRIVATE_MODE = 0;
    // Shared preferences file name
    private static final String PREF_NAME = "sessionPref";
    SharedPreferences.Editor editor;

    public SharedPrefManager(Context context) {
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }

    public void saveToken(Context context, String toke){
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ID_TOKEN", toke);
        editor.commit();
    }

    public String getUserToken(){
        //mContext = context;
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        return sharedPreferences.getString("ID_TOKEN", "");
    }

    public void saveUiD(Context context, String UID){
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("UID_USER", UID);
        editor.commit();
    }

    public String getUserUiD(){
        //mContext = context;
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        return sharedPreferences.getString("UID_USER", "");
    }

    public void saveEmail(Context context, String email){
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("EMAIL", email);
        editor.commit();
    }

    public String getUserEmail(){
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        return sharedPreferences.getString("EMAIL", null);
    }

    public void savePhonenumber(Context context, String phonenumber){
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Phone number", phonenumber);
        editor.commit();
    }

    public String getPhonenumber(){
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        return sharedPreferences.getString("Phone number", null);
    }


    public void saveName(Context context, String name){
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("NAME", name);
        editor.commit();
    }

    public String getName(){
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        return sharedPreferences.getString("NAME", null);
    }

    public void saveLon(Context context, String loc){
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("LOC", loc);
        editor.commit();
    }

    public String getLon(){
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        return sharedPreferences.getString("LOC", null);
    }

    public void saveLat(Context context, String loc){
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("LOC", loc);
        editor.commit();
    }

    public String getLat(){
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        return sharedPreferences.getString("LOC", null);
    }

    public void clear(){
        editor.clear();
        editor.apply();
    }
}
