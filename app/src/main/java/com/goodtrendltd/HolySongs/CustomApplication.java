package com.goodtrendltd.HolySongs;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.goodtrendltd.HolySongs.chat.ChatService;
import com.goodtrendltd.HolySongs.entities.ChatThreadScheduler;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class CustomApplication extends Application
{
    public static CustomApplication instance = null;
    private static final String VALUE = "Harvey";
    static Context context;
    final Handler SyncDataHandler = new Handler();
    private static ScheduledThreadPoolExecutor exec;
    public static String FLURRY_API_KEY = "4H89NXRH68DJ7ZZ5Y52R";

    // for Google Analytics
    public static GoogleAnalytics analytics;
    public Tracker tracker = null;

    public CustomApplication() {
        // Exists only to defeat instantiation.
        instance = this;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    public static CustomApplication getInstance() {
        if(instance == null) {
            instance = new CustomApplication();
        }
        return instance;
    }

    synchronized public Tracker getDefaultTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            tracker = analytics.newTracker("UA-66336761-1");
        }
        return tracker;
    }
    public void scheduleMessageRetrieve() {

        ChatService.initIfNeed(this);
        ChatService.initChatService(this);
        ChatService.getInstance();
        ChatThreadScheduler.getInstance().scheduleCheckMessages();
    }

    public void AppSyncData(){

        try {
            Class.forName("android.os.AsyncTask");
        }
        catch(Throwable ignore) {
            // ignored
        }
        context = getBaseContext();
        scheduleMessageRetrieve();
    }


    /**
     * Service for sending data about usage
     */
    class AnalyticsService extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {

            return START_STICKY;
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }

    public static Context getContext()
    {
        if (instance==null) {
            Log.w("bible-application", "WARNING: Tried to get custom application context, which was null");
            instance = getInstance();
        }
        return instance;
    }
}