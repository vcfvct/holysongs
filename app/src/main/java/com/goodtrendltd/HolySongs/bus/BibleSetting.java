package com.goodtrendltd.HolySongs.bus;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.goodtrendltd.HolySongs.CustomApplication;
import com.goodtrendltd.HolySongs.R;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.UUID;

public class BibleSetting
{

    private static final String LANGUAGE = "bibleLanguage";
    public static final String SETTINGS_PREF = "com.pressbible.view";
    static Typeface fontTypeFace=null;
    static Typeface biblefontTypeFace=null;
    static Typeface boldFace=null;
    static Typeface italicFace=null;
    static Typeface yaHeiTypeFace=null;
    static Typeface asapItalicTF=null, asapRegularTF=null, asapBoldTF=null;
//    static DatabaseSetting db = new DatabaseSetting();

    private static Hashtable<String, Typeface> fontCache = new Hashtable<String, Typeface>();

    public static String SysConfigQuery(Context context, String key)
    {
        SharedPreferences read = context.getSharedPreferences(key, Activity.MODE_WORLD_READABLE);
        //步骤2：获取文件中的值
        String val = read.getString(key, "");
        return val;

    }

    public static void SysConfigUpdate(Context context, final String key, final String value){
        SharedPreferences.Editor editor = context.getSharedPreferences(key, Activity.MODE_WORLD_WRITEABLE).edit();
        //步骤2-2：将获取过来的值放入文件
        editor.putString(key, value);
        //步骤3：提交
        editor.commit();
    }

    //获得 第一次启动
    public static int getFirstStart(Context context){
        String lastDate = SysConfigQuery(context, "getFirstStart");
        int isFirst = 0;
        if(lastDate.equals("")){
            isFirst = 0;
        }
        else
        {
            //first install ever
            isFirst = Integer.parseInt(lastDate);
        }

        int code = getPackageVersionCode(context);
        if(code!=isFirst){
            isFirst = 1;
            UserMag.setNewNotification();
            SysConfigUpdate(context, "getFirstStart",String.valueOf(code));
        }
        else
        {
            isFirst = 0;
        }

        return isFirst;
    }

    //获得 第一次启动
    public static int getLocalNotificationCode(Context context){
//        String localnotification = DatabaseSetting.SysConfigQuery("locallocalnotificationorder");
//        int order = 10001;
//        if(localnotification.equals("")){
//            order = 10001;
//        }
//        else
//        {
//            //first install ever
//            order = Integer.parseInt(localnotification);
//            if(order >= 10016){
//                order = 10001;
//            }
//            else
//            {
//                order = order + 1;
//            }
//        }
//        db.SysConfigUpdate("locallocalnotificationorder",order+"");
//        return order;
        return 1;
    }

    public static String getBibleLanguage(Context context) {
        return SysConfigQuery(context, BibleSetting.LANGUAGE);
    }

    public static Boolean getPushPreference(Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    //获取 版本信息
    public static int getAppVersion(Context context){
        String appversion = SysConfigQuery(context, "appversion");
        if(appversion.equals("")){
            appversion = "0";
        }
        return Integer.parseInt(appversion);
    }

    //设置 版本信息
    public static void setAppVersion(Context context,String version){
        SysConfigUpdate(context, "appversion",version);
    }

    public static Boolean getNightMode(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_pref), Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(context.getString(R.string.night_mode_pref_key), true);
    }

    public static String getChatAPIKey(Context context) {
        return "1mjl1Qryo9zkx8XQsYBxF8sTDj6nGtaBEGaYueC2FTKW3lRhJ05VBraq9B6BWrN";
    }

    public static String getGMOEventAPIKey(Context context) {
        return "nSNXrKQpZthbHxFuREVTSAqljFUcDipZ1ZFdnpHiXMLKK08giVpcKyPD5agbZUg";
    }

    public static String getAppName(Context context) {
        if (getLocalLanguage(context)== LocalLanguage.SIMPLIFIED_CHINESE ||
                getLocalLanguage(context)==LocalLanguage.TRADITIONAL_CHINESE) {
            return "Video Bible";
        } else {
            return "Press Bible";
        }
    }

    public static String getDeviceIDHash() {
        Context context = CustomApplication.getContext();
        if (context==null) return "";
        String anonymousIDKey = "anonymous-id";

        //Get from local
        SharedPreferences prefs = context.getSharedPreferences(SETTINGS_PREF, Context.MODE_PRIVATE);
        if (prefs.contains(anonymousIDKey)) {
            return prefs.getString(anonymousIDKey, "");
        }

        //Else, make new id
        String androidID = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        if (androidID==null || androidID.isEmpty()) {
            androidID = UUID.randomUUID().toString();
        }

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.e("setting","getDeviceIDHash: "+e);
        }

        try {
            md.update(androidID.getBytes("UTF-8")); // Change this to "UTF-16" if needed
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e("setting","getDeviceIDHash: "+e);
        }
        byte[] digest = md.digest();

        //Write to local
        String hashID = String.format("%064x", new java.math.BigInteger(1, digest));
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(anonymousIDKey,hashID).apply();

        return hashID;
    }

    public static void SetChatCategory(Context context,String chatCategory){

        SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("chatcategory", chatCategory);
        editor.commit();
    }

    //安装后，首次加载设置
    public static void FirstLoadSetting(Context context){
//        Log.d("bible-setting", "BibleSetting firstLoadSetting()");
//        int verseCode = ActivityHelper.getPackageVersionCode(context);
//        String code = SysConfigQuery(context, "verseCode");
//        Boolean isNeedDelete = false;
//        if(code.equals("")==true )
//        {
//            isNeedDelete = true;
//        }else if(Integer.parseInt(code)<verseCode)
//        {
//            isNeedDelete = true;
//        }
//
//        if(isNeedDelete == true){
//            db.SysConfigUpdate("verseCode",verseCode+"");
//            File path=new File(DatabaseScripture.DB_DIR);
//            delete(path);
//        }

    }

    /**
     * 获取版本号
     * @return 当前应用的版本号
     */
    public static int getPackageVersionCode(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取本地语言
     * @return 当前本地语言
     */
    public static LocalLanguage getLocalLanguage(Context context) {
        LocalLanguage localLanguage;
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.toString(); // 获得语言码
        if (language.endsWith("zh_CN"))
        {
            localLanguage = LocalLanguage.SIMPLIFIED_CHINESE;
        }
        else if (language.endsWith("zh_TW"))
        {
            localLanguage = LocalLanguage.TRADITIONAL_CHINESE;
        }
        else
        {
            localLanguage = LocalLanguage.ENGLISH;
        }

        return localLanguage;
    }

    public static void delete(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }

        if(file.isDirectory()){
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }

            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }
}