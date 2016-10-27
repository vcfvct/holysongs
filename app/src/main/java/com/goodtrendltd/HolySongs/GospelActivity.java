package com.goodtrendltd.HolySongs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.goodtrendltd.HolySongs.bus.BibleSetting;
import com.goodtrendltd.HolySongs.bus.GlobalMediaStar;
import com.goodtrendltd.HolySongs.helpers.CommonMethod;

import java.util.ArrayList;

/**
 * Created by Julia on 5/3/2016.
 */
public class GospelActivity extends AppCompatActivity {

    Activity context;
    ViewPager viewPager;
    ArrayList<View> viewList;
    String TAG = "GospelActivity";
    String origin = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v("init-trace",CommonMethod.GetCurrentMillisecondDate() + " BibleInitSlider 01 ");
        context = this;

        if (BibleSetting.getNightMode(this)) {
            setTheme(R.style.DarkAppTheme);
        } else {
            setTheme(R.style.BaseAppTheme);
        }
        // Press Bible - Install

        Log.v("init-trace",CommonMethod.GetCurrentMillisecondDate() + " BibleInitSlider 02 ");
        Log.d("bible-init", " -- firstLoad -- ");

        context = this;

        setContentView(R.layout.activity_bible_init_slider);
        viewPager = (ViewPager)findViewById(R.id.viewPager);
        viewList = new ArrayList<View>();

        InitButton();

        LayoutInflater inflater =  (LayoutInflater)context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.activity_bible_init_knowgod, null);
        SetFontTypeFace(view,R.id.txt_one);
        SetFontTypeFace(view,R.id.txt_two);
        SetFontTypeFace(view,R.id.txt_three);
        SetFontTypeFace(view,R.id.txt_four);
        SetFontTypeFace(view,R.id.txt_five);
        SetFontTypeFace(view,R.id.txt_six);
        SetFontTypeFace(view,R.id.txt_seven);
        SetFontTypeFace(view, R.id.txt_eight);

        Button btnYes = (Button)view.findViewById(R.id.btn_yes);
        Button btnReadBible = (Button)view.findViewById(R.id.btn_read_bible);

        Bundle extras = getIntent().getExtras();
        if (extras!=null) {
            origin = extras.getString("origin", " ");
            if (origin.equals("main")) {
                btnReadBible.setText(R.string.done);
            } else {
                btnReadBible.setText(R.string.skip);
                initToolbar();
            }
        }

        btnReadBible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable(){
                    @Override
                    public void run()
                    {
                        GlobalMediaStar.decision(context);
                    }
                }).start();

                Intent intent = new Intent(GospelActivity.this, ChatActivity.class);
                intent.putExtra("fromGospel",1);
                startActivity(intent);
                GospelActivity.this.finish();
            }
        });

        viewList.add(view);
        viewPager.setAdapter(new GuidePageAdapter());
        viewPager.setOnPageChangeListener(new GuidePageChangeListener());

        Log.v(TAG," -- recordGospel -- ");
        if(CommonMethod.isNetworkAvailable(context))
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    GlobalMediaStar.recordGospel(context);
                }
            }).start();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 指引页面数据适配器
    private class GuidePageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return viewList.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(viewList.get(arg1));
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(viewList.get(arg1));
            return viewList.get(arg1);
        }


    }

    // 指引页面更改事件监听器
    private class GuidePageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onPageSelected(int arg0) {
            //遍历数组让当前选中图片下的小圆点设置颜色
        }
    }

    private void InitButton(){

        findViewById(R.id.btn_silder_one).setVisibility(View.GONE);
        findViewById(R.id.btn_silder_two).setVisibility(View.GONE);
        findViewById(R.id.btn_silder_three).setVisibility(View.GONE);
    }

    private void SetFontTypeFace(View view,int id){
        TextView txtview = (TextView)view.findViewById(id);
//        if (txtview.getTypeface()==null) {
//            txtview.setTypeface(BibleSetting.getDefaultFont(this));
//            return;
//        }
//        //TODO: Null pointer!!!
//        if (txtview.getTypeface().isBold()) {
//            txtview.setTypeface(BibleSetting.getDefaultFont(this, Typeface.BOLD));
//        }
//        else if (txtview.getTypeface().isItalic()) {
//            txtview.setTypeface(BibleSetting.getDefaultFont(this, Typeface.ITALIC));
//        }
//        else {
//            txtview.setTypeface(BibleSetting.getDefaultFont(this));
//        }
    }

}