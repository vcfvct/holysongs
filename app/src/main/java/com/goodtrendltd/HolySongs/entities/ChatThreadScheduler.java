package com.goodtrendltd.HolySongs.entities;

import android.util.Log;


import com.goodtrendltd.HolySongs.chat.ChatService;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Julia on 2/2/2016.
 */
public class ChatThreadScheduler {
    private static ChatThreadScheduler instance;
    private static ScheduledThreadPoolExecutor exec = null;
    private static Object o = null;
    public ChatService.CheckMessages checkMessageTask = new ChatService.CheckMessages();
    public static int LONG_PERIOD = 300; //period to check messages, in seconds.
    public static int SHORT_PERIOD = 30;

    private ChatThreadScheduler() {
    }

    public static synchronized ChatThreadScheduler getInstance() {
        if(instance == null) {
            instance = new ChatThreadScheduler();
        }
        return instance;
    }

    public void scheduleCheckMessages() {
        if (exec==null && o == null) {
            o = 1;
            Log.v("chat-exec", "Scheduled");
            long period = LONG_PERIOD; // the period between successive executions
            exec = new ScheduledThreadPoolExecutor(1);
            exec.scheduleAtFixedRate(checkMessageTask, 0, period, TimeUnit.SECONDS);
        }
        else {
            Log.v("chat-exec", "exec.getActiveCount() = " + exec.getActiveCount());
            Log.v("chat", "exec.getQueue() = " + exec.getQueue());
        }
    }

    public void rescheduleCheckMessages(long periodInSeconds) {
        if(exec==null){
            scheduleCheckMessages();
        }
        exec.shutdownNow();
        Log.v("chat-exec", "exec.getActiveCount() = " + exec.getActiveCount());
        Log.v("chat-exec", "exec.getQueue() = " + exec.getQueue());
        Log.v("chat-exec", "rescheduled");
        ScheduledThreadPoolExecutor newExec = new ScheduledThreadPoolExecutor(1);
        newExec.scheduleAtFixedRate(checkMessageTask, 0, periodInSeconds, TimeUnit.SECONDS);
        exec = newExec;
    }
}
