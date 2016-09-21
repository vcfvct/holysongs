package com.goodtrendltd.HolySongs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import com.goodtrendltd.HolySongs.bus.GlobalMediaStar;
import com.goodtrendltd.HolySongs.bus.UserMag;
import com.goodtrendltd.HolySongs.chat.ApplicationSessionStateCallback;
import com.goodtrendltd.HolySongs.chat.BaseChatActivity;
import com.goodtrendltd.HolySongs.chat.Chat;
import com.goodtrendltd.HolySongs.chat.ChatAdapter;
import com.goodtrendltd.HolySongs.chat.ChatMessage;
import com.goodtrendltd.HolySongs.chat.ChatService;
import com.goodtrendltd.HolySongs.chat.ChatUser;
import com.goodtrendltd.HolySongs.chat.HttpClientUtil;
import com.goodtrendltd.HolySongs.entities.ChatThreadScheduler;
import com.goodtrendltd.HolySongs.entities.NavigationCategory;
import com.goodtrendltd.HolySongs.helpers.CommonMethod;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.request.QBRequestGetBuilder;


import java.util.ArrayList;
import java.util.Calendar;

public class ChatActivity extends BaseChatActivity implements ApplicationSessionStateCallback, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "ChatActivity";

    public static final String EXTRA_DIALOG = "dialog";
    private final String PROPERTY_SAVE_TO_HISTORY = "save_to_history";

    private EditText messageEditText;
    private ListView messagesContainer;
    private Button sendButton;
    private ProgressBar progressBar;
    private ChatAdapter adapter;
    private SwipeRefreshLayout swipeLayout;

    private Chat chat;
    private QBDialog dialog;
    private RelativeLayout container;
    private String userkey = "";
    private Boolean finishedLoading = false;
    private String token = null; //app push token
    private Boolean fromGospel = false;
    private ArrayList<ChatMessage> messageList = new ArrayList<ChatMessage>();
    private Integer mId=0;
    private Integer SERVER_CONNECT_ATTEMPTS = 3;//number of attempts to get chat history
    private Integer retry = SERVER_CONNECT_ATTEMPTS;
    private AlertDialog errorDialog = null;
    AlertDialog.Builder errorDialogBuilder = null;
    private ChatActivity content;
    private static ChatUser chatUser = null;
    private LinearLayout errorView = null;
    private int lastTask = 0;
    private int LOAD_MESSAGES = 1;
    private int REGISTER = 2;
    private int UPDATE_USER = 3;
    public static String CATEGORY = "category";
    public static String FORM_MESSAGE = "form message";
    private int category = -1;
    private String formMessage = "", name = "";
    public static String NAME = "name";
    Dialog decisionDialog;

    public static void start(Context context, Bundle bundle) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        content = this;
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            if (extras.getInt("fromGospel", 0) != 0) {
                fromGospel = true;
            }
            category = extras.getInt(CATEGORY, -1);
            formMessage = extras.getString(FORM_MESSAGE);
            name = extras.getString(NAME);
        }

        Log.v("test",fromGospel + " --");
        initViews();

        // Init chat if the session is active
        if (isSessionActive()) {
            initChat();
        }

        ChatService.getInstance().addConnectionListener(chatConnectionListener);

        new Thread(new Runnable() {
            @Override
            public void run()
            {
                GlobalMediaStar.recordOpenChat(content);
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        lastTask=0;
        ChatThreadScheduler.getInstance().rescheduleCheckMessages(ChatThreadScheduler.SHORT_PERIOD);
        Log.v("chat", "onResume");
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(ChatService.NEW_MESSAGES));
        // Clear all notifications
        NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancelAll();
    }

    @Override
    public void onPause() {
        Log.v(TAG, "onPause()");
        // Unregister since the activity is not visible
        ChatThreadScheduler.getInstance().rescheduleCheckMessages(ChatThreadScheduler.LONG_PERIOD);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "onStop()");
        ChatThreadScheduler.getInstance().rescheduleCheckMessages(ChatThreadScheduler.LONG_PERIOD);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy()");
        ChatThreadScheduler.getInstance().rescheduleCheckMessages(ChatThreadScheduler.LONG_PERIOD);
        ChatService.getInstance().removeConnectionListener(chatConnectionListener);
    }

    @Override
    public void onBackPressed() {
        if (chat!=null) {
            try {
                chat.release();
            } catch (XMPPException e) {
                Log.e(TAG, "failed to release chat", e);
            }
        }
        super.onBackPressed();
        if (decisionDialog!=null) {
            decisionDialog.dismiss();
        }
        if(fromGospel){
            Intent intent = new Intent();
            if(CommonMethod.isNetworkAvailable(content)==true)
            {
                intent.putExtra("page",NavigationCategory.Home);
            }
            else
            {
                intent.putExtra("page", NavigationCategory.Library);
            }
            intent.setClass(ChatActivity.this, MainActivity.class);
            ChatActivity.this.startActivity(intent);
            ChatActivity.this.finish();
        }
        else
        {
            finish();
        }
    }

    private void initViews() {
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messagesContainer.setOverScrollMode(View.OVER_SCROLL_NEVER);
        messageEditText = (EditText) findViewById(R.id.messageEdit);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        errorView = (LinearLayout) findViewById(R.id.error_view);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setRefreshing(false);
        //加载颜色是循环播放的，只要没有完成刷新就会一直循环，color1>color2>color3>color4
        swipeLayout.setColorSchemeResources(
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);

        Intent intent = getIntent();
        dialog = (QBDialog) intent.getSerializableExtra(EXTRA_DIALOG);
        container = (RelativeLayout) findViewById(R.id.container);
        chatUser = ChatUser.getInstance();

//        LinearLayout pickCategoryButton = (LinearLayout)findViewById(R.id.linear_chat_category);
//        pickCategoryButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setClass(ChatActivity.this, ChatPreActivity.class);
//                ChatActivity.this.startActivity(intent);
//            }
//        });

        // Send button
        //
        sendButton = (Button) findViewById(R.id.chatSendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageEditText.getText().toString();
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }
                sendChatMessage(messageText);

            }
        });
        errorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastTask == LOAD_MESSAGES) {
                    onPreparing();
                    loadPastMessages();
                }
                else if (lastTask == REGISTER) {
                    onPreparing();
                    register(chatUser);
                }
                else if (lastTask == UPDATE_USER){
                    onPreparing();
                    updateUser(chatUser);
                }
            }
        });

        onPreparing();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK )
        {
            Log.v("test",fromGospel + " --");
            if(fromGospel || CommonMethod.alreadyOpenHomeActivity(content) == false){
                Intent intent = new Intent();
                if(CommonMethod.isNetworkAvailable(content)==true)
                {
                    intent.putExtra("page",NavigationCategory.Home);
                }else
                {
                    intent.putExtra("page", NavigationCategory.Library);
                }
                intent.setClass(ChatActivity.this, MainActivity.class);
                ChatActivity.this.startActivity(intent);
                ChatActivity.this.finish();
            }
            else
            {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showKeyboard() {
        ((InputMethodManager) messageEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(messageEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        //TODO: Make this actually work
        ((InputMethodManager) messageEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(messageEditText.getWindowToken(), 0);
    }


    private void sendChatMessage(String messageText) {
        sendChatMessage(messageText, true);
    }

    private void sendChatMessage(String messageText, Boolean visible) {
        UserMag.recordChatFirstMessage(ChatActivity.this);
        final ChatMessage chatMessage = new ChatMessage();
        chatMessage.setBody(messageText);
        //chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, "1");
        chatMessage.setDate(Calendar.getInstance().getTime());
        chatMessage.setDirection("in");
        chatMessage.setSendingInProgress(true);

        try {
            if (visible) {
                showMessage(chatMessage);
                final Integer msgIndex = messageList.indexOf(chatMessage);
                ChatService.sendMessage(chatMessage.getBody(), new ChatService.MessageTask() {
                    @Override
                    protected void onSuccessfulSend(int id) {
                        messageList.get(msgIndex).setSendingInProgress(false);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
            else {
                Log.d("chat-form","form message: "+chatMessage.getBody());
                ChatService.sendMessage(chatMessage.getBody(), new ChatService.MessageTask() {
                    @Override
                    protected void onSuccessfulSend(int id) {
                        UserMag.addFormMessageId(getBaseContext(), id);
                        chatUser.addFormMessageId(id);
                        Log.d("chat-form", "set form message id: "+id);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "failed to send a message", e);
        }
        messageEditText.setText("");
    }

    public void setContentBottomPadding(int padding) {
        container.setPadding(0, 0, 0, padding);
    }

    private void initChat() {
        token = ChatService.getToken(this);

        // Restore preferences
        userkey = ChatUser.getInstance().getKey();

        if (userkey.isEmpty()) { //need to register
            Log.d("chat-activity-token", "token being used: " + token);
            //pick a category
            switch (category) {
                case -1:
                    ShowChatCategory();
                    break;
                case ChatPreActivity.FAITH:
                    showNameDialog();
                    break;
                default: //already picked a category
                    chatUser.setUsername(name);
                    chatUser.setFormMessage(formMessage);
                    register(chatUser);
                    swipeLayout.setEnabled(false);
                    break;
            }
        }
        else
        { // has a userkey
            loadChatHistory();

            //send category message
            if (category!=-1 && category!=ChatPreActivity.FAITH) {
                chatUser.setFormMessage(formMessage);
                sendChatMessage(chatUser.getFormMessage(), false);
            }
            Log.d("ChatActivity", "ChatUser key being used: " + userkey);
            Log.d("ChatActivity-token", "token being used: " + token);

        }
    }

    private void register(ChatUser user){
        final ChatUser finalUser = user;
        Integer code = ChatService.register(token, new ChatService.UserTask() {
            @Override
            protected void onSuccess(){
                updateUser(finalUser); //after registering, update with user info from dialog
                if (!finalUser.getFormMessage().isEmpty()) {
                    sendChatMessage(finalUser.getFormMessage(),false);
                }
                onLoadFinish();
                showInfoMessage();
            }
            @Override
            protected void onFailure() {
                if (!ChatActivity.this.isFinishing()) {
                    onNetworkFailure(REGISTER);
                }
            }
        });
    }

    private void onNetworkFailure(int task) {
        //showErrorsAlert("网络失败");
        lastTask = task;
        progressBar.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        finishedLoading = false;
        sendButton.setEnabled(false);
    }

    private void onPreparing() {
        progressBar.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        finishedLoading = false;
        sendButton.setEnabled(false);
    }

    private void onLoadFinish() {
        progressBar.setVisibility(View.GONE);
        sendButton.setEnabled(true);
        finishedLoading = true;
    }

    private void updateUser(ChatUser user){
        ChatService.updateUser(user.getUsername(), user.getDecision(), new ChatService.UserTask() {
            @Override
            protected void onSuccess() {
                onLoadFinish();
            }

            @Override
            protected void onFailure() {
                if (!ChatActivity.this.isFinishing()) {
                    onNetworkFailure(UPDATE_USER);
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        Log.d("ChatActivity", "Loading Messages");
        ChatService.loadMoreMessages(adapter.getCount(), new ChatService.MessageTask() {
            @Override
            protected void onSuccessfulReceive(HttpClientUtil.ResponseObject msgResponse) {
                ArrayList<ChatMessage> messages = msgResponse.getMessages();
                Integer i;
                for (i = messages.size() - 1; i >= 0; i--) {
                    messageList.add(0, messages.get(i));
                }
                adapter.notifyDataSetChanged();
                swipeLayout.setRefreshing(false);
                if (msgResponse.getPageNext() == 0) {
                    swipeLayout.setEnabled(false);
                    messagesContainer.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
                }
            }
        });
    }

    private void loadChatHistory() {
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setPagesLimit(100);
        customObjectRequestBuilder.sortDesc("date_sent");
        swipeLayout.setEnabled(false);
        hideKeyboard();
        loadPastMessages();
    }

    private void loadPastMessages() {
        ChatService.getMessages(Boolean.FALSE, 10, 1, new ChatService.MessageTask() {
            @Override
            protected void onSuccessfulReceive(HttpClientUtil.ResponseObject msgResponse) {
                onLoadFinish();
                ArrayList<ChatMessage> messages = msgResponse.getMessages();
                adapter = new ChatAdapter(ChatActivity.this, messageList);
                messagesContainer.setAdapter(adapter);
                showMessages(messages, true);
                if (msgResponse.getPageNext() > 0) {
                    swipeLayout.setEnabled(true);
                } else {
                    swipeLayout.setEnabled(false);
                    showInfoMessage();
                }
                retry = SERVER_CONNECT_ATTEMPTS; //reset attempts
            }

            @Override
            protected void onFailure() {
                if (retry > 0) {
                    retry--;
                    if (errorDialog == null) {
                        showErrorsAlert("Could not connect to server. Retrying...");
                    }
                    loadPastMessages();
                } else {
                    onNetworkFailure(LOAD_MESSAGES);
                }
                Log.e("ChatActivity", "Error Receiving Messages");
            }
        });
    }

    public void showInfoMessage() {
        View header = getLayoutInflater().inflate(R.layout.listitem_chat_info, null);
//        TextView txtView = (TextView) header.findViewById(R.id.info_text_view);
        messagesContainer.addHeaderView(header);
        if (adapter==null) {
            adapter = new ChatAdapter(ChatActivity.this, messageList);
            messagesContainer.setAdapter(adapter);
        }
    }

    private void showErrorsAlert(final String string) {
        final Handler handler = new Handler();
        if (errorDialog==null) {
            errorDialogBuilder = new AlertDialog.Builder(ChatActivity.this);
            errorDialogBuilder.setCancelable(true);
            errorDialog = errorDialogBuilder.create();
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                errorDialog.setMessage(string);
                errorDialog.setCanceledOnTouchOutside(true);
                if (ChatActivity.this.isSessionActive()) {
                    errorDialog.show();
                }
            }
        }, 3000);
        Log.e("ChatActivity-register", "chat error: " + string);
    }

    public void showMessages(ArrayList<ChatMessage> messages, Boolean displayAll) {
        final ArrayList<Integer> ids = new ArrayList<Integer>();
        final ArrayList<Integer> hideIds = chatUser.getFormMessageIds();
        for (ChatMessage msg : messages) {
            if (!msg.isOutgoing() || displayAll && !hideIds.contains(msg.getId()) ) {
                showMessage(msg);
//                chatUser.moveFromInbox(msg.getId());
                ids.add(msg.getId());
                Log.v("receiver", "moved message " + msg);
                Log.v("receiver", "inbox now: " + chatUser.getUnread());
                Log.v("receiver", "readbox now: " + chatUser.getReadList());
            }
        }

        for(Integer id : ids){
            chatUser.moveFromInbox(id);
        }


        if (!ids.isEmpty()) {
            ChatService.markAsRead(ids, new ChatService.MessageTask() {
                @Override
                protected void onSuccessfulMark() {
                    adapter.notifyDataSetChanged();
                    chatUser.removeFromReadList(ids);
                }
            });
        }
    }

    public void showMessage(ChatMessage message) {
        if (adapter!=null) {
            if (!messageList.contains(message)) {
                messageList.add(message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        scrollDown();
                    }
                });
            }
        }
        else {
            messageList.add(message);
            adapter = new ChatAdapter(ChatActivity.this, messageList);
            messagesContainer.setAdapter(adapter);
        }
    }

    // handler for received Intents for the "new messages" event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            int count = intent.getIntExtra(ChatService.COUNT, 0);
            Log.d("chat-receiver", "Got number of messages: " + count);
            ArrayList<ChatMessage> messages = chatUser.getUnread();
            if (messages!=null) {
                if (finishedLoading) {
                    showMessages(messages, false);
                }
            }
            else {
                Log.d("chat-receiver", "messages are null");
            }
        }
    };

    private void scrollDown() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
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
            if (seconds % 5 == 0) {
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

    @Override
    public void onStartSessionRecreation() {

    }

    @Override
    public void onFinishSessionRecreation(final boolean success) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (success) {
                    initChat();
                }
            }
        });
    }

    protected void ShowChatCategory()
    {
        Intent intent = new Intent();
        intent.setClass(ChatActivity.this, ChatPreActivity.class);
        UserMag.clearNewNotification();
        this.startActivity(intent);
        this.finish();
    }

    protected void showNameDialog() {
        chatUser.loadPrefs();
        chatUser.setDecision(ChatUser.NONE);
        register(chatUser);
        swipeLayout.setEnabled(false);
    }

}

