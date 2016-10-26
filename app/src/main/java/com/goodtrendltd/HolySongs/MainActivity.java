package com.goodtrendltd.HolySongs;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.view.WindowManager.LayoutParams;
import android.widget.*;

import com.goodtrendltd.HolySongs.bus.BibleSetting;
import com.goodtrendltd.HolySongs.bus.GlobalMediaStar;
import com.goodtrendltd.HolySongs.helpers.ChineseCharComp;
import com.goodtrendltd.HolySongs.helpers.CommonMethod;
import com.goodtrendltd.HolySongs.helpers.HanziHelper;
import com.goodtrendltd.HolySongs.helpers.XMLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.*;
import java.util.*;

public class MainActivity extends AppCompatActivity implements AbsListView.OnScrollListener {


    public final static String LYRIC = "com.goodtrendltd.LYRIC";
    public final static String SONG_NAME = "com.goodtrendltd.SONG_NAME";

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

        int firstLoad = BibleSetting.getFirstStart(this);
        if(firstLoad!=0)
        {
            Intent i = new Intent();
            i.setClass(this,GospelActivity.class);
            i.putExtra("origin","first");
            recordInstall();
            startActivity(i);
        }
        if (BibleSetting.getNightMode(this)) {
            setTheme(R.style.DarkAppTheme);
        } else {
            setTheme(R.style.BaseAppTheme);
        }
        setContentView(R.layout.main);

        recordRun();
        XMLParser parser = new XMLParser();
        String myData = getXml("songs.xml");
        setDataFromXML(parser, myData);
        final List<String> titleList = new ArrayList<String>(songLyricMap.keySet());
        Collections.sort(titleList, new ChineseCharComp());

        ListView listView = getListView();
        listView.setTextFilterEnabled(true);
        listView.setAdapter(new SongTitleAdapter(getApplicationContext(), titleList));
        Sidebar sb = (Sidebar) findViewById(R.id.side_bar);
        sb.bindListView(getListView());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String songName = titleList.get(position);
                navigateToLyric(songName);
            }
        });
        setupIndicator();

        if (getActionBar()!=null) {
            getActionBar().setTitle(R.string.app_name);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_find_church);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(MainActivity.this, ChatActivity.class);
                startActivity(i);
            }
        });
    }

    private void recordInstall() {

        if(CommonMethod.isNetworkAvailable(this))
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    GlobalMediaStar.recordInstall(MainActivity.this);
                }
            }).start();
        }
    }

    private void recordRun() {

        if (CommonMethod.isNetworkAvailable(this)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    GlobalMediaStar.recordRun(MainActivity.this);
                }
            }).start();
        }
    }

    private void navigateToLyric(String songName) {
        String lyric = songLyricMap.get(songName);
        Intent intent = new Intent(this, DisplayLyricActivity.class);
        intent.putExtra(LYRIC, lyric);
        intent.putExtra(SONG_NAME, songName);
        startActivity(intent);
    }

    private String getXml(String path) {

        String xmlString = null;
        try {
            InputStream is = getAssets().open(path);
            int length = is.available();
            byte[] data = new byte[length];
            is.read(data);
            xmlString = new String(data);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        return xmlString == null ? null : xmlString.replaceAll(" ", "");
    }

    private void setDataFromXML(XMLParser parser, String xml) {
        Document doc = parser.getDomElement(xml);
        NodeList nl = doc.getElementsByTagName("song");
        // looping through all item nodes <item>
        for (int i = 0; i < nl.getLength(); i++) {
            // creating new HashMap
            Element e = (Element) nl.item(i);
            // adding each child node to HashMap key => value
            String name = parser.getValue(e, "name");
            String lyric = parser.getValue(e, "lyric");
            songLyricMap.put(name, lyric);
        }

    }

    private void setupIndicator() {
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        getListView().setOnScrollListener(this);
        LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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
            }
        });
    }

    private void removeWindow() {
        if (mShowing) {
            mShowing = false;
            mDialogText.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mReady) {
            char firstLetter = (HanziHelper.words2Pinyin((String) getListView().getItemAtPosition(firstVisibleItem))).charAt(0);
            if (!mShowing && firstLetter != mPrevLetter) {
                mShowing = true;
                mDialogText.setVisibility(View.VISIBLE);
            }
            mDialogText.setText(((Character) firstLetter).toString().toUpperCase());
            mHandler.removeCallbacks(mRemoveWindow);
            mHandler.postDelayed(mRemoveWindow, 2000);
            mPrevLetter = firstLetter;
        }
    }

    private final class RemoveWindow implements Runnable {
        public void run() {
            removeWindow();
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


    //begin of menu related
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.about:
                openAbout();
                return true;
            case R.id.settings:
                openSettings();
                return true;
            case R.id.sharing:
                openSharing();
                return true;
            case R.id.gospel:
                openGospel();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openSharing() {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "敬拜赞美诗");
            StringBuilder stringBuffer = new StringBuilder();
            stringBuffer.append("\n敬拜赞美诗App安装地址: \n\n ");
            stringBuffer.append(" 安卓：").append(getString(R.string.app_url_android)).append(" \n\n");
            stringBuffer.append(" 苹果：").append(getString(R.string.app_url_ios)).append(" \n\n");
            i.putExtra(Intent.EXTRA_TEXT, stringBuffer.toString());
            startActivity(Intent.createChooser(i, "选择用于分享的app"));
        } catch (Exception e) { //e.toString();
        }
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void openAbout() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private void openGospel() {
        Intent intent = new Intent(this, GospelActivity.class);
        intent.putExtra("origin","main");
        startActivity(intent);
    }

    private ListView getListView() {
        return (ListView) findViewById(R.id.list);
    }
}
