package com.goodtrendltd.HolySongs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.goodtrendltd.HolySongs.bus.GlobalMediaStar;
import com.goodtrendltd.HolySongs.helpers.CommonMethod;

public class ChatFormActivity extends AppCompatActivity {

    public static String CATEGORY = "category";
    Activity context;

    EditText txt_name;
    EditText txt_phone;
    EditText txt_address;
    Button btn_submit;
    private int categoryId=1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_form);
        context = this;
        initUI();
        initToolbar();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
    public void initUI() {

        txt_name = (EditText) findViewById(R.id.txt_name);
        txt_phone = (EditText) findViewById(R.id.txt_phone);
        txt_address = (EditText) findViewById(R.id.txt_address);

        btn_submit = (Button) findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkInfo()) {
                    return;
                }
                String message = makeMessage();
                //TODO: write to prefs?
                //start chat
                Intent intent = new Intent();
                intent.setClass(ChatFormActivity.this, ChatActivity.class);
                intent.putExtra(ChatActivity.CATEGORY, categoryId);
                intent.putExtra(ChatActivity.FORM_MESSAGE, message);
                intent.putExtra(ChatActivity.NAME, txt_name.getText().toString());
                Log.d("chat-form", ChatActivity.CATEGORY + ": " + categoryId);
                Log.d("chat-form", ChatActivity.FORM_MESSAGE + ": " + message);
                Log.d("chat-form", ChatActivity.NAME+": "+txt_name.getText().toString());
                ChatFormActivity.this.startActivity(intent);
                ChatFormActivity.this.finish();
            }
        });

    }

    protected void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        mTitle.setText(R.string.find_church_text);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayShowTitleEnabled(false);
            actionbar.setDisplayShowHomeEnabled(true);
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
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

    private Boolean checkInfo() {
        boolean check = true;
        if (txt_name.getText().toString().isEmpty()) {
            txt_name.setError(getResources().getString(R.string.name_prompt));
            check = false;
        }
        if (txt_phone.getText().toString().isEmpty()) {
            txt_phone.setError(getResources().getString(R.string.error_phone));
            check = false;
        }
        if (txt_address.getText().toString().isEmpty()) {
            txt_address.setError(getResources().getString(R.string.error_address));
            check = false;
        }
        return check;
    }

    private String makeMessage() {
        String msg = "";
        //[加入教会]:姓名:xxx；手机号码:13730211;地址:xxxxxx
        msg="[加入教会]:姓名:"+txt_name.getText()+";手机号码:"+txt_phone.getText()+";地址:"+txt_address.getText();
        if(CommonMethod.isNetworkAvailable(context))
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    GlobalMediaStar.recordChatMsg(context
                            , txt_name.getText().toString()
                            , txt_phone.getText().toString()
                            , txt_address.getText().toString()
                            , categoryId + ""
                    );
                }
            }).start();
        }
        return msg;
    }

    @Override
    public void onBackPressed() {
//        Intent intent = new Intent();
//        intent.setClass(ChatFormActivity.this, ChatPreActivity.class);
//        ChatFormActivity.this.startActivity(intent);
//        ChatFormActivity.this.finish();
        super.onBackPressed();
    }
}

