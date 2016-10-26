package com.goodtrendltd.HolySongs.chat;

import android.util.Log;

import com.goodtrendltd.HolySongs.bus.UserMag;

import java.util.ArrayList;

/**
 * Created by Julia on 9/14/2015.
 */
public class ChatUser {
    public static final String INFO_FILE_NAME = "userinfo.xml";
    public static final String USER_KEY_PREF = "userkey";
    public static final String USER_NAME_PREF = "username";
    public static final String USER_DECISION_PREF = "decision";
    public static final String USER_KEY_STORED = "";
    //    public static final String USER_KEY_STORED = "fe488618456ecbf14f661f7d3cfa4469b5cf02e0edc662f4489f3d53f08cadd398074e" +
//            "139404553007c647f14e6e798155ac8b8c7d22112be8879e32a0af2008";
    //public static final String USER_KEY_STORED = "a4863c60927960800da8d227b6440cf43b41c187af608ddfa"+
    //        "6903d4c144c11df4879e14c2747b31675501de04f0e818fde7d94ba2f664390e2abe48f3f9cc639";
    public final ArrayList<ChatMessage> inbox = new ArrayList<>();
    public final ArrayList<Integer> readList = new ArrayList<>();

    private static ChatUser instance = null;
    private ChatUser() {
        // Exists only to defeat instantiation.
    }
    public static ChatUser getInstance() {
        if(instance == null) {
            instance = new ChatUser();
            instance.loadPrefs();
        }
        return instance;
    }
    public void loadPrefs() {
        this.key = UserMag.getUserKey();
        this.username = UserMag.getUsername();
        this.formMessageIds = UserMag.getFormMessageIds();
        Log.v("chat-user", "preferences loaded - username: " + username + " formMessageIds: " + formMessageIds.toString());
    }

    private String key;
    public String username = "";
    private String decision="none";
    private String formMessage="";
    private Boolean firstTime = true;
    private ArrayList<Integer> formMessageIds= null;
    public static String PRAYED = "prayed";
    public static String RECOMMITTED = "recommitted";
    public static String GROW = "grow";
    public static String NONE = "none";

    public void setKey(String key) {
        this.key = key;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getKey() {
        return this.key;
    }
    public String getUsername() {
        return this.username;
    }
    public String getDecision() {
        return this.decision;
    }

    public int getUnreadCount() {
        Log.d("bible-chat-user", "getUnreadCount()");
        if (UserMag.getChatBubble()) {
            Log.d("bible-chat-user", "UserMag.getChatBubble()");
            return 1;
        }
        else if (this.inbox==null) {
            Log.d("bible-chat-user", "this.inbox==null");
            return 0;
        }
        else {
            Log.d("bible-chat-user", "this.inbox.size()="+this.inbox.size());
            return this.inbox.size();
        }
    }
    public ArrayList<ChatMessage> getUnread() { return this.inbox; }

    public Boolean addToInbox(ChatMessage msg) {
        if (!this.inbox.contains(msg) && !this.readList.contains(msg.getId())) {
            this.inbox.add(msg);
            return true;
        } else {
            return false;
        }
    }

    public void moveFromInbox(int id) {
        int position = -1;
        for (ChatMessage msg:this.inbox ) {
            if (id==msg.getId()) {
                position = this.inbox.indexOf(msg);
                this.readList.add(id);
            }
        }
        if (position!=-1) {
            this.inbox.remove(position);
        }
    }

    public void emptyInbox() {
        if (this.inbox.size()>0) {
            for (ChatMessage msg:this.inbox ) {
                if (!this.readList.contains(msg.getId())) {
                    this.readList.add(msg.getId());
                }
            }
            this.inbox.clear();
        }
    }

    public void removeFromReadList(ArrayList<Integer> ids) {
        for (int id : ids) {
            if (readList.contains(id)) {
                this.readList.remove(this.readList.indexOf(id));
            }
        }
    }

    public ArrayList<Integer> getReadList() {
        return this.readList;
    }
    public void setFormMessage(String msg) {
        this.formMessage = msg;
    }
    public String getFormMessage() {
        return this.formMessage;
    }
    public void addFormMessageId(int id){
        this.formMessageIds.add(id);
    }
    public ArrayList<Integer> getFormMessageIds(){
        return this.formMessageIds;
    }
}
