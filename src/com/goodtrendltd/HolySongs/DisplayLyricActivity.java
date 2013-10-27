package com.goodtrendltd.HolySongs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * User: LeOn
 * Date: 13-10-5
 * Time: 下午10:13
 */
public class DisplayLyricActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_pref), MODE_PRIVATE);
        if (sharedPreferences.getBoolean(getString(R.string.night_mode_pref_key), true)) {
            setTheme(android.R.style.Theme_Holo);
        } else {
            setTheme(android.R.style.Theme_Holo_Light);
        }
        setContentView(R.layout.lyric_view);

        Intent intent = getIntent();
        String lyric = intent.getStringExtra(MainActivity.LYRIC);
        String songName = intent.getStringExtra(MainActivity.SONG_NAME);

        // Create the text view
        TextView lyricTextView = (TextView) findViewById(R.id.lyricContent);
        lyricTextView.setMovementMethod(new ScrollingMovementMethod());
        lyricTextView.setTextSize(sharedPreferences.getInt(getString(R.string.font_size_pref_key),20));
        lyricTextView.setText(lyric);
        lyricTextView.setLineSpacing(20, 1.0f);

        TextView titleTextView = (TextView) findViewById(R.id.title);
        titleTextView.setText(songName);
        titleTextView.setTextSize(25);
        titleTextView.setTextColor(Color.parseColor("#00BFFF"));

    }
}