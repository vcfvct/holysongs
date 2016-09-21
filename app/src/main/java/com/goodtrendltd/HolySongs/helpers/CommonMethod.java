package com.goodtrendltd.HolySongs.helpers;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.provider.Settings;
import android.text.ClipboardManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by vincent on 15/5/27.
 */
public class CommonMethod {

    public static String defaultBookDirectory() {
        return cardDirectory() + "/videobible";
    }

    public static String cardDirectory() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return Environment.getExternalStorageDirectory().getPath();
        }

        final List<String> dirNames = new LinkedList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/self/mounts"));
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] parts = line.split("\\s+");
                if (parts.length >= 4 &&
                        parts[2].toLowerCase().indexOf("fat") >= 0 &&
                        parts[3].indexOf("rw") >= 0) {
                    final File fsDir = new File(parts[1]);
                    if (fsDir.isDirectory() && fsDir.canWrite()) {
                        dirNames.add(fsDir.getPath());
                    }
                }
            }
        }
        catch (Throwable e) {

        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {

                }
            }
        }

        for (String dir : dirNames) {
            if (dir.toLowerCase().indexOf("media") > 0) {
                return dir;
            }
        }
        if (dirNames.size() > 0) {
            return dirNames.get(0);
        }

        return Environment.getExternalStorageDirectory().getPath();
    }

    //获取当前时间
    public static String GetCurrentDate(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        long time=System.currentTimeMillis();
        String s = sdf.format(new Date(time));
        return s;
    }


    //获取当前时间
    public static String GetCurrentMillisecondDate(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss SSS");
        long time=System.currentTimeMillis();
        String s = sdf.format(new Date(time));
        return s;
    }


    //短日期格式
    public static String ConvertShortDate(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String s = sdf.format(date);
        return s;
    }

    //拷贝功能
    public static void Copy(Context context,String content) {
        ClipboardManager cmb = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content.trim());
    }

    //从毫秒转换成指定时间显示
    public static String GetFormatedDateTime(String pattern, long dateTime) {
        SimpleDateFormat sDateFormat = new SimpleDateFormat(pattern);
        return sDateFormat.format(new Date(dateTime + 0));
    }

    //从毫秒转换成指定时间显示
    public static String GetFormatedDateTime(String pattern, Date date) {
        SimpleDateFormat sDateFormat = new SimpleDateFormat(pattern);
        return sDateFormat.format(date);
    }

    //检测网络是否可用
    public static boolean isNetworkAvailable(Context con) {
        ConnectivityManager cm = (ConnectivityManager)
                con.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public static Boolean alreadyOpenHomeActivity(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(10);
        String cmpNameTemp = null;
        Boolean isNotification = false;
        for(ActivityManager.RunningTaskInfo item : runningTaskInfos){
            if(item.baseActivity.toString().indexOf("MainActivity")>=0)
            {
                isNotification = true;
            }
        }
        return isNotification;
    }

    //获取本地语言
    public static String getLocallanguage(Context context){
        return context.getResources().getConfiguration().locale.getLanguage();
    }

    //获取屏幕亮度
    public static int GetScreenBrightness(Activity activity) {
        int value = 0;
        ContentResolver cr = activity.getContentResolver();
        try {
            value = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {

        }
        return value;
    }

    /** * 根据手机的分辨率从dp 的单位 转成为px(像素) */
    public static int Dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /** * 根据手机的分辨率从px(像素) 的单位 转成为dp */
    public static int Px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
