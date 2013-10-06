package com.example.HolySongs;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManager;
import android.widget.*;
import com.example.HolySongs.helpers.ChineseCharComp;
import com.example.HolySongs.helpers.HanziHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class MainActivity extends ListActivity implements AbsListView.OnScrollListener{
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mReady) {
            char firstLetter = (HanziHelper.words2Pinyin((String)getListView().getItemAtPosition(firstVisibleItem))).charAt(0);
            if (!mShowing && firstLetter != mPrevLetter) {
                mShowing = true;
                mDialogText.setVisibility(View.VISIBLE);
            }
            mDialogText.setText(((Character)firstLetter).toString().toUpperCase());
            mHandler.removeCallbacks(mRemoveWindow);
            mHandler.postDelayed(mRemoveWindow, 2000);
            mPrevLetter = firstLetter;
        }    }

    private final class RemoveWindow implements Runnable {
        public void run() {
            removeWindow();
        }
    }
    public final static String LYRIC = "com.goodtrendltd.LYRIC";

    private Map<String, String> songLyricMap = new HashMap<String, String>();

    private RemoveWindow mRemoveWindow = new RemoveWindow();
    Handler mHandler = new Handler();
    private WindowManager mWindowManager;
    private TextView mDialogText;
    private boolean mShowing;
    private boolean mReady;
    private char mPrevLetter = Character.MIN_VALUE;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        String myData = getJsonFromAsset("songs.json");
        setData(myData);
        final List<String> titleList = new ArrayList<String>(songLyricMap.keySet());
        Collections.sort(titleList, new ChineseCharComp());
//        String[] titles = titleList.toArray(new String[songLyricMap.size()]);
//        setListAdapter(new ArrayAdapter<String>(this, R.layout.main, titles));

        ListView listView = getListView();
        listView.setTextFilterEnabled(true);
        listView.setAdapter(new SongTitleAdapter(getApplicationContext(), titleList));
        Sidebar sb = (Sidebar) findViewById(R.id.side_bar);
        sb.bindListView(getListView());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String songName = ((TextView) view).getText().toString();
                String songName = titleList.get(position);
                navigateToLyric(songName);
//                Toast.makeText(getApplicationContext(), ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
            }
        });
        setupIndicator();
    }

    private void navigateToLyric(String songName) {
        String lyric = songLyricMap.get(songName);
        Intent intent = new Intent(this, DisplayLyricActivity.class);
        intent.putExtra(LYRIC, lyric);
        startActivity(intent);
    }

    private String getJsonFromAsset(String fileName) {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(getAssets().open(fileName)));
            String temp;
            while ((temp = br.readLine()) != null)
                sb.append(temp);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close(); // stop reading
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    private void setData(String jsonString) {
        try {
            // Creating JSONObject from String
            JSONObject jsonObjMain = new JSONObject(jsonString);

            // Creating JSONArray from JSONObject
            JSONArray jsonArray = jsonObjMain.getJSONArray("songs");

            // JSONArray has four JSONObject
            for (int i = 0; i < jsonArray.length(); i++) {

                // Creating JSONObject from JSONArray
                JSONObject jsonObj = jsonArray.getJSONObject(i);

                // Getting data from individual JSONObject
                String name = jsonObj.getString("name");
                String lyric = jsonObj.getString("lyric");
                if (lyric != null) {
                    lyric = lyric.replaceAll("!!LineBreak!!", System.getProperty("line.separator"));
                }
                songLyricMap.put(name, lyric);
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void setupIndicator() {
        mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        getListView().setOnScrollListener(this);
        LayoutInflater inflate = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mDialogText = (TextView) inflate.inflate(R.layout.list_position, null);
        mDialogText.setVisibility(View.INVISIBLE);

        mHandler.post(new Runnable() {

            public void run() {
                mReady = true;
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
                mWindowManager.addView(mDialogText, lp);
            }});
    }

    private void removeWindow() {
        if (mShowing) {
            mShowing = false;
            mDialogText.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mReady = true;
    }


    @Override
    protected void onPause() {
        super.onPause();
        removeWindow();
        mReady = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWindowManager.removeView(mDialogText);
        mReady = false;
    }
}
