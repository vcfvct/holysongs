package com.goodtrendltd.HolySongs.chat;

/**
 * Created by igorkhomenko on 4/29/15.
 */
public interface ApplicationSessionStateCallback {
    void onStartSessionRecreation();
    void onFinishSessionRecreation(boolean success);
}
