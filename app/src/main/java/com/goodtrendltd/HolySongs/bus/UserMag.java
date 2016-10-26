package com.goodtrendltd.HolySongs.bus;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.goodtrendltd.HolySongs.CustomApplication;
import com.goodtrendltd.HolySongs.chat.ChatUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by vincent on 15/6/25.
 */
public class UserMag {
    public static String FORM_MESSAGE_ID_ARRAY = "form_message_id_array";
    public static String PREFS_FILE_NAME = "com.pressbible.view";

    public static String CHAT_BUBBLE = "chat_bubble";

    private UserMag() {

    }

    public static Boolean recordChatFirstMessage(Activity context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
        if(prefs.contains("sentFirstMessage"))  {
            return false;
        }
        else {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("sentFirstMessage", true);
            editor.apply();
            GlobalMediaStar.recordFirstChat(context);
            return true;
        }
    }


    public static void addFormMessageId(Context context, int id){
        JSONArray array = new JSONArray();
        try {
            array = loadJSONArray(context, FORM_MESSAGE_ID_ARRAY);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("chat-user","form message array error: "+e);
        }
        //add new value
        array.put(id);
        saveJSONArray(context, FORM_MESSAGE_ID_ARRAY, array);
    }

    public static ArrayList<Integer> getFormMessageIds(){
        Context context = CustomApplication.getContext();
        ArrayList<Integer> listdata = new ArrayList<Integer>();
        JSONArray jArray = new JSONArray();
        try {
            jArray = loadJSONArray(context, FORM_MESSAGE_ID_ARRAY);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("chat-user","form message array error: "+e);
        }
        if (jArray != null) {
            for (int i=0;i<jArray.length();i++){
                try {
                    listdata.add(jArray.getInt(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("chat-user", "form message array error: " + e);
                }
            }
        }
        return listdata;
    }

    public static void recordUserKey(String key) {
        Context context = CustomApplication.getContext();
        SharedPreferences userinfo = context.getSharedPreferences(PREFS_FILE_NAME, 0);
        SharedPreferences.Editor editor = userinfo.edit();
        editor.putString(ChatUser.USER_KEY_PREF, key).apply();
    }

    public static String getUserKey() {
        Context context = CustomApplication.getContext();
        String key;
        SharedPreferences userinfo = context.getSharedPreferences(PREFS_FILE_NAME, 0);
        key = userinfo.getString(ChatUser.USER_KEY_PREF,"");
        if (key.isEmpty() && !ChatUser.USER_KEY_STORED.isEmpty()) {
            key = ChatUser.USER_KEY_STORED;
            SharedPreferences.Editor editor = userinfo.edit();
            editor.putString(ChatUser.USER_KEY_PREF, key).apply();
        }
        return key;
    }

    public static String getUsername() {
        Context context = CustomApplication.getContext();
        SharedPreferences userinfo = context.getSharedPreferences(PREFS_FILE_NAME, 0);
        return userinfo.getString(ChatUser.USER_NAME_PREF, "");
    }

    public static Boolean getChatBubble() {
        Context context = CustomApplication.getContext();
        SharedPreferences userinfo = context.getSharedPreferences(PREFS_FILE_NAME, 0);
        Log.d("chat-usermag","getChatBubble(): returns "+userinfo.getBoolean(CHAT_BUBBLE, true));
        return userinfo.getBoolean(CHAT_BUBBLE, true);
    }

    public static void clearNewNotification() {
        Context context = CustomApplication.getContext();
        SharedPreferences userinfo = context.getSharedPreferences(PREFS_FILE_NAME, 0);
        Log.d("chat-usermag", "clearNewNotification() -> writing \"false\" to CHAT_BUBBLE");
        SharedPreferences.Editor editor = userinfo.edit();
        editor.putBoolean(CHAT_BUBBLE, false).apply();
    }

    public static void setNewNotification() {
        Context context = CustomApplication.getContext();
        Log.d("chat-usermag","setNewNotification() -> writing \"true\" to CHAT_BUBBLE");
        SharedPreferences userinfo = context.getSharedPreferences(PREFS_FILE_NAME, 0);
        SharedPreferences.Editor editor = userinfo.edit();
        editor.putBoolean(CHAT_BUBBLE, true).apply();
    }

    public static void saveJSONArray(Context c, String key, JSONArray array) {
        SharedPreferences settings = c.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, array.toString());
        editor.apply();
    }

    public static JSONArray loadJSONArray(Context c, String key) throws JSONException {
        SharedPreferences settings = c.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
        return new JSONArray(settings.getString(key, "[]"));
    }

}
