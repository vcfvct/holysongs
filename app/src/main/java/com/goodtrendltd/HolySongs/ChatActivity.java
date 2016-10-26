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
import android.support.v7.app.ActionBar;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

import com.goodtrendltd.HolySongs.bus.BibleSetting;
import com.goodtrendltd.HolySongs.bus.GlobalMediaStar;
import com.goodtrendltd.HolySongs.bus.UserMag;
import com.goodtrendltd.HolySongs.chat.ApplicationSessionStateCallback;
import com.goodtrendltd.HolySongs.chat.BaseChatActivity;
import com.goodtrendltd.HolySongs.chat.Chat;
import com.goodtrendltd.HolySongs.chat.ChatAdapter;
import com.goodtrendltd.HolySongs.chat.ChatMessage;
import com.goodtrendltd.HolySongs.chat.ChatService;
import com.goodtrendltd.HolySongs.chat.ChatService.UserTask;
import com.goodtrendltd.HolySongs.chat.ChatUser;
import com.goodtrendltd.HolySongs.chat.HttpClientUtil;
import com.goodtrendltd.HolySongs.entities.ChatThreadScheduler;
import com.goodtrendltd.HolySongs.entities.NavigationCategory;
import com.goodtrendltd.HolySongs.helpers.CommonMethod;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.request.QBRequestGetBuilder;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ChatActivity extends BaseChatActivity implements ApplicationSessionStateCallback, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "ChatActivity";

    public static final String EXTRA_DIALOG = "dialog";
    private final String PROPERTY_SAVE_TO_HISTORY = "save_to_history";
    public static final String CHAT_INFO_DATE = "chat_info_date";

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
    int updateTries = 0;
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
    ChatMessage infoMessage = null;
    Boolean registerSuccess = false;
    static int TRIES_MAX = 3;

    public static void start(Context context, Bundle bundle) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    protected void initToolbar() {
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayShowTitleEnabled(false);
            actionbar.setDisplayShowHomeEnabled(true);
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
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

        Log.v(TAG,"onCreate fromGospel: "+fromGospel + " --");
        initViews();
        initToolbar();
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
        Log.v(TAG, "onResume");
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        Log.v(TAG, "onBackPressed");
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

        adapter = new ChatAdapter(ChatActivity.this, messageList);
        messagesContainer.setAdapter(adapter);
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
            Log.v(TAG,fromGospel + " --");
            if(fromGospel){
                Intent intent = new Intent();
                if(CommonMethod.isNetworkAvailable(content))
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
                Log.d(TAG,"form message: "+chatMessage.getBody());
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
            if (fromGospel) {
                showDecisionDialog(true);
            }
            else
            {
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
        }
        else { // has a userkey
            if (fromGospel) {
                showDecisionDialog(false);
            }
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

    private void register(ChatUser user) {
        final ChatUser finalUser = user;
        if (registerSuccess){
            Log.v("chat-activity", "register already succeeded!");
            //catch called twice
            return;
        }
        showAlert(getString(R.string.registering));
        ChatService.register(token, new UserTask() {
            @Override
            protected void onSuccess() {
                registerSuccess = true;
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
                    showAlert("Unable to register");
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
        if (errorDialog != null) {
            errorDialog.dismiss();
        }
        sendButton.setEnabled(false);
    }

    private void onPreparing() {
        progressBar.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        finishedLoading = false;
        sendButton.setEnabled(false);
    }

    private void onLoadFinish() {
        if (ChatActivity.this.isSessionActive()) {
            Log.v(TAG, "onLoadFinish()");
            progressBar.setVisibility(View.GONE);
            sendButton.setEnabled(true);
            if (errorDialog != null) {
                errorDialog.dismiss();
            }
            finishedLoading = true;
            chatUser.emptyInbox();
//            navBar.setNotificationBubble();
        }
    }

    private void updateUser(final ChatUser user) {
        ChatService.updateUser(user.getUsername(), user.getDecision(), new UserTask() {
            @Override
            protected void onSuccess() {
                Log.v(TAG, "updateUser() successful");
            }

            @Override
            protected void onFailure() {
                if (!ChatActivity.this.isFinishing()) {
//                    onNetworkFailure(UPDATE_USER);
                    if (updateTries < TRIES_MAX) {
                        updateTries++;
                        updateUser(user);
                        //TODO: Deal with this from return and still unupdated
                    }
                }
            }
        });
    }


    @Override
    public void onRefresh() {
        Log.d(TAG, "Loading Messages");
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
                if (msgResponse.getPageNext() > 0) {
                    swipeLayout.setEnabled(true);
                } else {
                    swipeLayout.setEnabled(false);
                    showInfoMessage();
                }
                ArrayList<ChatMessage> messages = msgResponse.getMessages();
                showMessages(messages, true);
                progressBar.setVisibility(View.GONE);
                finishedLoading = true;
                retry = SERVER_CONNECT_ATTEMPTS; //reset attempts
            }

            @Override
            protected void onFailure() {
                if (retry > 0) {
                    retry--;
                    if (errorDialog == null) {
                        showAlert("Could not connect to server. Retrying...");
                    }
                    loadPastMessages();
                } else {
                    onNetworkFailure(LOAD_MESSAGES);
                }
                Log.e(TAG, "Error Receiving Messages");
            }
        });
    }

    private void showAlert(final String string) {
        final Handler handler = new Handler();
        if (errorDialog == null) {
            errorDialogBuilder = new AlertDialog.Builder(ChatActivity.this);
            errorDialogBuilder.setCancelable(true);
            errorDialog = errorDialogBuilder.create();
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!finishedLoading && isSessionActive()) {
                    errorDialog.setMessage(string);
                    errorDialog.setCanceledOnTouchOutside(true);
                    if (ChatActivity.this.isSessionActive()) {
                        errorDialog.show();
                    }
                }
            }
        }, 3);
        Log.e("ChatActivity-register", "chat alert: " + string);
    }

    public void showMessages(ArrayList<ChatMessage> messages, Boolean displayAll) {
        final ArrayList<Integer> ids = new ArrayList<Integer>();
        for (ChatMessage msg : messages) {
            if (!msg.isOutgoing() || displayAll) {
                showMessage(msg);
                chatUser.moveFromInbox(msg.getId());
                ids.add(msg.getId());
                Log.v("receiver", "moved message " + msg);
                Log.v("receiver", "inbox now: " + chatUser.getUnread());
                Log.v("receiver", "readbox now: " + chatUser.getReadList());

            }
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
        if (adapter != null) {
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
        } else {
            messageList.add(message);
            if (adapter!=null) adapter.notifyDataSetChanged();
        }
    }

    public void showInfoMessage() {
        if (infoMessage==null) {
            String storedDate = BibleSetting.SysConfigQuery(this, CHAT_INFO_DATE);

            Log.v(TAG, "showInfoMessage infoMessage==null");
            infoMessage = new ChatMessage();
            if (category==ChatPreActivity.CHURCH) {
                infoMessage.setBody(getResources().getString(R.string.chat_welcome_message_church));
            } else {
                infoMessage.setBody(getResources().getString(R.string.chat_welcome_message));
            }
            infoMessage.setDirection("out");
            if (storedDate==null || storedDate.isEmpty()) {
                infoMessage.setDate(Calendar.getInstance().getTime());
                BibleSetting.SysConfigUpdate(this, CHAT_INFO_DATE, infoMessage.getUTCDate());
            } else {
                try {
                    infoMessage.setDateFromString(storedDate);
                } catch (ParseException e) {
                    infoMessage.setDate(Calendar.getInstance().getTime());
                    e.printStackTrace();
                }
            }
            infoMessage.setId(-12);
        }
        if (!messageList.contains(infoMessage)) {
            Log.v(TAG, "showInfoMessage !messageList.contains(infoMessage) ");
            messageList.add(0,infoMessage);
            if (adapter==null) {
                Log.v(TAG, "showInfoMessage adding to adapter ");
                adapter = new ChatAdapter(this, messageList);
                messagesContainer.setAdapter(adapter);

            }
            adapter.notifyDataSetChanged();
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
            if (messages != null) {
                if (finishedLoading) {
                    showMessages(messages, false);
                } else {
                    Log.d("chat-receiver", "not finished loading");
                }
            } else {
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
        chatUser.setDecision(ChatUser.NONE);
        finishedLoading=false;
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        LayoutInflater inflater = ChatActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_chat_name, null);
        final EditText nameEditText = (EditText) dialogView.findViewById(R.id.nameTextView);
        builder.setView(dialogView)
                .setPositiveButton(R.string.submit, null)
                .setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        swipeLayout.setEnabled(false);
                        register(chatUser);
                    }
                });
        final AlertDialog dialog = builder.create();

        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface mdialog) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // get name
                        String name = nameEditText.getText().toString();
                        if (name.isEmpty()) {
                            nameEditText.setError(getResources().getString(R.string.name_prompt_error));
                            return;
                        }
                        chatUser.setUsername(name);
                        register(chatUser);
                        dialog.dismiss();
                        swipeLayout.setEnabled(false);
                    }
                });
            }
        });
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                // Prevent dialog close on back press button
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    onBackPressed();
                }
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });
        dialog.show();
    }

    protected ChatUser showDecisionDialog(final Boolean needsRegister) {
        final ChatUser user = ChatUser.getInstance();
        decisionDialog = new Dialog(this);
        decisionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        decisionDialog.setContentView(R.layout.dialog_chat_introduction);
        decisionDialog.setCanceledOnTouchOutside(false);
        decisionDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                // Prevent dialog close on back press button
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    onBackPressed();
                }
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });

        final EditText nameEditText = (EditText) decisionDialog.findViewById(R.id.nameTextView);
        final TextView nameTitle = (TextView) decisionDialog.findViewById(R.id.introduction_title);
        if (!needsRegister) {
            nameEditText.setVisibility(View.GONE);
            nameTitle.setVisibility(View.GONE);
        }
        final RadioGroup decisionGroup = (RadioGroup) decisionDialog.findViewById(R.id.buttons_select_decision);
        Button button = (Button) decisionDialog.findViewById(R.id.button_submit);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // get name
                String name = nameEditText.getText().toString();
                if (!name.isEmpty()) {
                    chatUser.setUsername(name);
                }

                // get selected radio button from radioGroup
                int selectedId = decisionGroup.getCheckedRadioButtonId();

                //Toast.makeText(getApplicationContext(), "SelectedId: "+selectedId, Toast.LENGTH_SHORT).show();
                // find the radiobutton by returned id
                if (selectedId != -1) {
                    switch (selectedId) {
                        case R.id.radioButton1:
                            chatUser.setDecision(ChatUser.PRAYED);
                            break;
                        case R.id.radioButton2:
                            chatUser.setDecision(ChatUser.RECOMMITTED);
                            break;
                        case R.id.radioButton3:
                            chatUser.setDecision(ChatUser.GROW);
                            break;
                    }
                    decisionDialog.dismiss();
                    swipeLayout.setEnabled(false);
                    if (needsRegister) {
                        register(chatUser);
                    } else {
                        updateUser(chatUser);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.decision_error, Toast.LENGTH_SHORT).show();
                }
            }
        });

        decisionDialog.show();

        return user;
    }
}

