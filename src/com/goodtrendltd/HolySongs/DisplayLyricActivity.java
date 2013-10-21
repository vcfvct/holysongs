package com.goodtrendltd.HolySongs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.LYRIC);

        // Create the text view
        TextView textView = new TextView(this);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setTextSize(20);
        textView.setText(message);
        textView.setLineSpacing(20, 1.0f);

        // Set the text view as the activity layout
        setContentView(textView);
    }
}