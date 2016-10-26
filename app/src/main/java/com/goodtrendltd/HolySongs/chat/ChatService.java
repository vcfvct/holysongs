package com.goodtrendltd.HolySongs.chat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.goodtrendltd.HolySongs.ChatActivity;
import com.goodtrendltd.HolySongs.CustomApplication;
import com.goodtrendltd.HolySongs.R;
import com.goodtrendltd.HolySongs.bus.BibleSetting;
import com.goodtrendltd.HolySongs.bus.LocalLanguage;
import com.goodtrendltd.HolySongs.bus.UserMag;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatService {

    static final String SENDER_ID="868640157488";

    private static final String TAG = ChatService.class.getSimpleName();

    static final int AUTO_PRESENCE_INTERVAL_IN_SECONDS = 30;
    static final int REGISTER_TASK = 1;
    static final int UPDATE_TASK = 2;
    static final int GETMESSAGES_TASK = 3;
    static final int MARKASREAD_TASK = 4;
    static final int SENDMESSAGE_TASK = 5;
    public static final String COUNT = "count";
    public static final String MSG_CONTENT = "content";
    public static final String NEW_MESSAGES = "new-message";

    private static ChatService instance;
    private static Context context;
    private static ChatUser chatUser = ChatUser.getInstance();
    private static int mId =0;

    public static void initChatService(Context context) {
        ChatService.context = context;
    }

    public static synchronized ChatService getInstance() {
        if(instance == null) {
            instance = new ChatService();
        }
        return instance;
    }

    public static boolean initIfNeed(Context ctx) {
        if (!QBChatService.isInitialized()) {
            QBChatService.setDebugEnabled(true);
            QBChatService.init(ctx);
            Log.d(TAG, "Initialise QBChatService");

            return true;
        }

        return false;
    }

    private QBChatService chatService;

    private ChatService() {
        chatService = QBChatService.getInstance();
        chatService.addConnectionListener(chatConnectionListener);
    }

    public void addConnectionListener(ConnectionListener listener){
        chatService.addConnectionListener(listener);
    }

    public void removeConnectionListener(ConnectionListener listener){
        chatService.removeConnectionListener(listener);
    }

    public static Integer register(String push_token, UserTask usertask){

        Integer code;
        try
        {
            Log.v("ChatService-post-https", "Executing register task...");
            Map<String, String> params = new HashMap<String, String>();
            Map<String,Integer> task = new HashMap<String, Integer>();

            params.put("app_key", BibleSetting.getChatAPIKey(CustomApplication.getContext()));
            Log.v("ChatService-post-https", "Executing register task 1...");
            params.put("platform", "android");
            params.put("push_token", push_token);
            Log.v("ChatService-post-https", "Executing register task 2...");
            task.put("task", ChatService.REGISTER_TASK);

            Log.v("ChatService-post-https", "Still registering......");
            usertask.execute(params, task);

            //HttpsUtil.post("https://commchannels.globalmediaoutreach.com/api/app/users", key, values);
            code=0;
        }
        catch(Exception ex){
            code=1;
            System.out.println(" \t " + ex.getMessage());
            Log.d("ChatService-register:", "register failed, exception: " + ex+" "+ex.getMessage());
        }

        // Create REST API session
        //
        return code;
    }

    public static void updateUser(String username, String decision, UserTask userTask) {
        //write updated user info to shared preferences
        String userkey = UserMag.getUserKey();
        SharedPreferences userinfo = context.getSharedPreferences(UserMag.PREFS_FILE_NAME, 0);
        SharedPreferences.Editor editor = userinfo.edit();
        editor.putString(ChatUser.USER_NAME_PREF, username);
        editor.putString(ChatUser.USER_DECISION_PREF, decision);
        editor.apply();

        try {
            Log.d("ChatService-post-https", "Executing updateUser task...");
            Map<String, String> params = new HashMap<String, String>();
            Map<String, Integer> task = new HashMap<String, Integer>();

            params.put("user_key", userkey);
            params.put("name", username);
            params.put("decision", decision);
            task.put("task", ChatService.UPDATE_TASK);

            userTask.execute(params, task);
        }
        catch (Exception e) {
            Log.e("ChatService-updateUser", e.toString());
        }
    }

    public static void markAsRead(ArrayList<Integer> ids, MessageTask messageTask){
        //write updated user info to shared preferences
        String userkey = UserMag.getUserKey();
        //TODO: {"success":false,"errorType":"InvalidParameter","code":452,"message":"\u0027ids\u0027 should be an array of integers"}
        try {
            Log.d("ChatService-put-https", "Executing markAsRead...");
            Map<String, String> params = new HashMap<String, String>();
            Map<String, Integer> task = new HashMap<String, Integer>();

            params.put("user_key", userkey);
            for (Integer i : ids) {
                params.put("ids[]", i.toString());
            }
            Log.d("ChatService-post-https", "ids being marked: " + ids.toString());
            task.put("task", ChatService.MARKASREAD_TASK);

            messageTask.execute(params, task);

        }
        catch (Exception e) {
            Log.e("ChatService-markAsRead", e.toString());
        }
    }

    public static void loadMoreMessages(Integer msgCount, MessageTask messageTask) {
        Integer minPageSize = 5;
        Integer pageSize = (msgCount % minPageSize) + minPageSize;
        Integer pageNumber = (msgCount/pageSize) + 1;
        Log.d("ChatServie-messages", "loading pageSize:"+pageSize);
        Log.d("ChatServie-messages", "loading pageNumber:" + pageNumber);
        getMessages(false, pageSize, pageNumber, messageTask);
    }

    public static Integer getMessages(Boolean onlyNew, Integer count, Integer page, MessageTask messageTask) {
        String fromDate="";
        String toDate="";

        if (count<1 || count>1000) {
            count=50;
            Log.d("ChatService-messages", "Message count must be between 1 and 1000. Count set to 50.");
        }
        if (page<1) {
            page=1;
            Log.d("ChatService-messages", "Page count must be greater than 1. Page count set to 1.");
        }

        String userkey = UserMag.getUserKey();

        try {
            Log.d("ChatService-messages", "Executing getMessages task...");
            Map<String, String> params = new HashMap<String, String>();
            Map<String, Integer> task = new HashMap<String, Integer>();

            params.put("user_key", userkey);
//            params.put("from_date", fromDate);
//            params.put("to_date", toDate);
            params.put("only_new", onlyNew.toString());
            params.put("count", count.toString());
            params.put("page", page.toString());
            task.put("task", ChatService.GETMESSAGES_TASK);

            Log.d("ChatService-messages", "user_key:" + userkey);
            messageTask.execute(params, task);
        }
        catch (Exception e) {
            Log.e("ChatService-messages", e.toString());
            return 1;
        }
        return 0;
    }

    public static class CheckMessages implements Runnable {
        @Override
        public void run() {

            Log.v("chat", "CheckMessages!");

            if (UserMag.getUserKey()=="") {
                return;
            }

            //check for new messages
            ChatService.getMessages(Boolean.TRUE, 30, 1, new MessageTask() {
                @Override
                protected void onSuccessfulReceive(HttpClientUtil.ResponseObject msgResponse) {
                    int count = 0;
                    ArrayList<ChatMessage> messages = msgResponse.getMessages();

                    for (ChatMessage msg : messages) {
                        if (!msg.isOutgoing()) {
                            if (chatUser.addToInbox(msg)) {
                                makeNotification(msg);
                            };
                            count++;
                        }
                    }

                    if (count > 0) {
                        Log.v("chat", "making notification. Count: " + count);
                        sendNotification(count, messages);
                    }
                }
            });
        }

        // Send an Intent with an action named "my-event".
        private void sendNotification(int count, ArrayList<ChatMessage> messages) {
            Intent intent = new Intent(ChatService.NEW_MESSAGES);
            // add data
            intent.putExtra(ChatService.COUNT, count);
            LocalBroadcastManager.getInstance(ChatService.context).sendBroadcast(intent);
        }
    }

    private static void makeNotification(ChatMessage message) {
        String text = message.getBody();
        long when = message.getDate().getTime();
        Intent notificationIntent = new Intent(context, ChatActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        mId=mId+1;
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        //Detailed version
        Notification notification  = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.chat_notification_title))
                .setContentText(text)
                .setWhen(when)
                .setSmallIcon(R.drawable.icon)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_menu_view, "View details", contentIntent)
                .setContentIntent(contentIntent)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setSound(alarmSound).build();

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(mId, notification);
    }

    public static Integer sendMessage(String message, MessageTask messageTask) {
        String userkey = UserMag.getUserKey();
        String language="";

        if(BibleSetting.getLocalLanguage(context)== LocalLanguage.SIMPLIFIED_CHINESE
                || BibleSetting.getLocalLanguage(context)== LocalLanguage.TRADITIONAL_CHINESE )
            language="zh";
        else
            language="en";

        try {
            Map<String, String> params = new HashMap<String, String>();
            Map<String, Integer> task = new HashMap<String, Integer>();

            Log.d("ChatService-messages", "send message: " + message);
            params.put("user_key", userkey);
            params.put("message", message);
            params.put("language", language);
            task.put("task", ChatService.SENDMESSAGE_TASK);

            messageTask.execute(params, task);
        }
        catch (Exception e) {
            Log.e("ChatService-messages", e.toString());
            return 1;
        }
        return 0;
    }

    public static String getToken(Context context){
        String android_id=null;
        try {
            /*InstanceID instanceID = InstanceID
                    .getInstance(context);*/

            android_id = Secure.getString(context.getContentResolver(),
                    Secure.ANDROID_ID);
            Log.d("token", "android_id retrieved: " + android_id);
        } catch (Exception ex) {
            //e.printStackTrace();
            Log.d("token", "android_id could not be retrieved: " + ex);
        }
        return android_id;
    }

    public void login(final QBUser user, final QBEntityCallback callback){

        // Create REST API session
        //

        QBAuth.createSession(user, new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession session, Bundle args) {

                user.setId(session.getUserId());

                // login to Chat
                //
                loginToChat(user, new QBEntityCallbackImpl() {

                    @Override
                    public void onSuccess() {
                        callback.onSuccess();
                    }

                    @Override
                    public void onError(List errors) {
                        callback.onError(errors);
                    }
                });
            }

            @Override
            public void onError(List<String> errors) {
                callback.onError(errors);
            }
        });
    }

    private void loginToChat(final QBUser user, final QBEntityCallback callback){
    }


    public static class UserTask extends AsyncTask<Map, Integer, String> {
        String content=null;
        static Integer task=0;

        @Override
        protected String doInBackground(Map... params) {
            UserTask.task = ((Integer) params[1].get("task"));
            Log.v("ChatService-UserTask", "task number: " + task);

            try
            {
                Log.v("ChatService-UserTask", "/user task started");
                if (context==null) {
                    context = CustomApplication.getContext();
                }
                if (context!=null) {
                    // Instantiate the custom HttpClient
                    HttpClientUtil client = new HttpClientUtil();
                    client.setContext(context);
                    // Execute the Post call and obtain the response
                    if (task==ChatService.REGISTER_TASK) {
                        content = client.sendHttpsRequestByPost("/users", params[0]);
                    }
                    else if (task==ChatService.UPDATE_TASK) {
                        content = client.sendHttpsRequestByPut("/users", params[0]);
                    }
                    else {
                        Log.e("ChatService-UserTask", "Task not set.");
                    }
                }
                else {
                    Log.e("ChatService-UserTask", "ERROR: context is null");
                }
            }
            catch(Exception ex){
                Log.d("ChatService-UserTask", "usertask failed, exception: " + ex);
                content=null;
            }
            catch(Error e) {
                Log.e("ChatService-UserTask", "usertask failed, exception: " + e);
                content=null;
            }
            return content;
        }

        protected void onProgressUpdate(String... content){
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);
            Log.d("ChatService-UserTask", "UserTask response received -----  " + content);
            Boolean success=false;
            if (content!=null) {
                success = HttpClientUtil.parseSuccessResponse(content);
            }
            if (!success) {
                onFailure();
            }
            else { //Task successful, process content
                switch (task) {
                    case ChatService.REGISTER_TASK:
                        String userkey;
                        userkey = HttpClientUtil.parseRegisterResponse(content);

                        //write userkey to sharedpreferences for storage
                        UserMag.recordUserKey(userkey);
                        ChatUser.getInstance().setKey(userkey);

                        Log.d("ChatService-UserTask", "ChatUser key parsed:" + userkey);
                        onSuccess();
                        break;
                    case ChatService.UPDATE_TASK:
                        //write updated values to SharedPreferences
                        Log.d("ChatService-UserTask", "Update successful");
                        onSuccess();
                        break;
                    default:
                        //oops
                        Log.d("ChatService-UserTask", "Post task not set! Task number: "+task);
                }
            }
        }

        protected void onSuccess() {

        }
        protected void onFailure() {

        }

    }

    public static class MessageTask extends AsyncTask<Map, Integer, String> {
        String content=null;
        Integer task=0;

        @Override
        protected String doInBackground(Map... params) {
            task = ((Integer) params[1].get("task"));
            Log.d("ChatService-messages", "task number: " + task);

            try
            {
                Log.d("ChatService-messages", "message task started");

                if (context==null) {
                    context = CustomApplication.getContext();
                }
                if (context!=null) {
                    // Instantiate the custom HttpClient
                    HttpClientUtil client = new HttpClientUtil();
                    HttpClientUtil.setContext(context);
                    // Execute the Post call and obtain the response
                    if (task==ChatService.GETMESSAGES_TASK) {
                        Log.d("ChatService-messages", "getting messages");
                        content = client.sendHttpsRequestByGet("/messages", params[0]);
                    }
                    else if (task==ChatService.MARKASREAD_TASK) {
                        Log.d("ChatService-messages", "marking as read");
                        content = client.sendHttpsRequestByPut("/messages", params[0]);
                    }
                    else if (task==ChatService.SENDMESSAGE_TASK) {
                        Log.d("ChatService-messages", "sending message");
                        content = client.sendHttpsRequestByPost("/messages", params[0]);
                    }
                    else {
                        Log.e("ChatService-messages", "Task not set correctly.");
                    }
                }
                else {
                    Log.e("ChatService-messages", "ERROR: context is null");
                }

            }
            catch(Exception ex){
                Log.d("ChatService-messages", "messagetask failed, exception: " + ex);
            }
            catch(Error e) {
                Log.e("ChatService-messages", "messagetask failed, exception: " + e);
            }
            return content;
        }

        protected void onProgressUpdate(String... content){
        }

        protected void onPostExecute(String result){
            super.onPostExecute(result);
            Log.d("ChatService-messages", "Message Task response received -----  " + content);

            //Finished task, process content
            if (content!=null) {
                HttpClientUtil.ResponseObject msgResponse = HttpClientUtil.parseMessageResponse(content);
                //HttpClientUtil.ResponseObject msgResponse = HttpClientUtil.makeDummyMessages(2);
                switch (task) {
                    case ChatService.GETMESSAGES_TASK:
                        //get Paging
                        if (msgResponse.isSuccessful()){
                            Log.d("ChatService-messages", "Message Count:" + msgResponse.getMessageCount());
                            Log.d("ChatService-messages", "Page Next:" + msgResponse.getPageNext());
                            Log.d("ChatService-messages", "Page Previous: " + msgResponse.getPagePrev());
                            Log.d("ChatService-messages", "success: " + msgResponse.isSuccessful());
                            onSuccessfulReceive(msgResponse);
                        }
                        else {
                            Log.e("ChatService-messages", "failed receiving messages");
                            onFailure();
                        }
                        break;
                    case ChatService.MARKASREAD_TASK:
                        //Mark messages as read
                        if (msgResponse.isSuccessful()) {
                            onSuccessfulMark();
                            Log.d("ChatService-messages", "Message marked as read");
                        }
                        else {
                            Log.e("ChatService-messages", "MarkAsRead failed.");
                        }
                        break;
                    case ChatService.SENDMESSAGE_TASK:
                        //Message has been sent
                        Log.d("ChatService-message", "Message sent.");
                        if (msgResponse.isSuccessful()) {
                            onSuccessfulSend(msgResponse.getId());
                        }
                        else {
                            Log.e("ChatService-messages", "failed sending message");
                            onFailure();
                        }
                        break;
                    default:
                        //oops
                        Log.d("ChatService-messages", "Post task not set!");
                        Log.d("ChatService-messages", "Task number: " + task);
                        onFailure();
                }
            }
            else {
                //HttpClientUtil.ResponseObject msgResponse = HttpClientUtil.makeDummyMessages(10);
                //onSuccessfulReceive(msgResponse);
                onFailure();
            }
        }

        protected void onSuccessfulReceive(HttpClientUtil.ResponseObject msgResponse) {
            Log.d("ChatService-http", "Messages Received successfully.");
        }
        protected void onSuccessfulSend(int id) {
            Log.d("ChatService-http", "Message Sent successfully.");
        }
        protected void onSuccessfulMark() {
            Log.d("ChatService-http", "Message Marked successfully.");
        }
        protected void onFailure() {
            Log.e("ChatService-http", "message method failed.");
        }

    }

    ConnectionListener chatConnectionListener = new ConnectionListener() {
        @Override
        public void connected(XMPPConnection connection) {
            Log.i(TAG, "connected");
        }

        @Override
        public void authenticated(XMPPConnection connection) {
            Log.i(TAG, "authenticated");
        }

        @Override
        public void connectionClosed() {
            Log.i(TAG, "connectionClosed");
        }

        @Override
        public void connectionClosedOnError(final Exception e) {
            Log.i(TAG, "connectionClosedOnError: " + e.getLocalizedMessage());
        }

        @Override
        public void reconnectingIn(final int seconds) {
            if(seconds % 5 == 0) {
                Log.i(TAG, "reconnectingIn: " + seconds);
            }
        }

        @Override
        public void reconnectionSuccessful() {
            Log.i(TAG, "reconnectionSuccessful");
        }

        @Override
        public void reconnectionFailed(final Exception error) {
            Log.i(TAG, "reconnectionFailed: " + error.getLocalizedMessage());
        }
    };
}

