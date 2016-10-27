package com.goodtrendltd.HolySongs;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.goodtrendltd.HolySongs.entities.ChatCategory;
import com.goodtrendltd.HolySongs.helpers.CommonMethod;

import java.util.ArrayList;


public class ChatPreActivity extends AppCompatActivity {

    static final int FAITH = 2;//信仰辅导
    static final int CHURCH = 3;//信仰辅导

    Activity context;
    Toolbar toolbar;

    ArrayList<ChatCategory> list = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_pre);
        context = this;
        initViews();
        initToolbar();
//        PushAgent.getInstance(context).onAppStart();
//        SyncUserhandler.postAtTime(runSyncUser, 1000 * 60 * 2);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
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

    protected void initToolbar() {
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayShowTitleEnabled(false);
            actionbar.setDisplayShowHomeEnabled(true);
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
    }


    public void initViews(){
        View chatNowView = findViewById(R.id.chat_now_view);
        View findChurchView = findViewById(R.id.find_church_view);
        View skipTextView = findViewById(R.id.skip_text_view);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                int id = v.getId();
                if (v.getClass()==ImageView.class) {
                    ImageView view = (ImageView) v;
                    view.setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
                }
                if (id==R.id.chat_now_view || id==R.id.skip_text_view) {
                    intent.setClass(ChatPreActivity.this, ChatActivity.class);
                    intent.putExtra(ChatActivity.CATEGORY, FAITH);
                    ChatPreActivity.this.startActivity(intent);
                }
                else {
                    intent.setClass(ChatPreActivity.this, ChatFormActivity.class);
                    intent.putExtra(ChatFormActivity.CATEGORY, CHURCH);
                    ChatPreActivity.this.startActivity(intent);
                }
                finish();
            }
        };

        chatNowView.setOnClickListener(clickListener);
        findChurchView.setOnClickListener(clickListener);
        skipTextView.setOnClickListener(clickListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

