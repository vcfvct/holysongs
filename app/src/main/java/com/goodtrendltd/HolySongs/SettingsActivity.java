package com.goodtrendltd.HolySongs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: LeOn
 * Date: 13-10-26
 * Time: 下午4:52
 */
public class SettingsActivity extends Activity {
    private SharedPreferences sharedPreferences;
    private List<String> fontSizes;
    Spinner fontSizeSpinner;
    Switch nightModeSwitch;
    private boolean isNightModeToggled = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(getString(R.string.app_pref), MODE_PRIVATE);
        if (sharedPreferences.getBoolean(getString(R.string.night_mode_pref_key), true)) {
            setTheme(android.R.style.Theme_Holo);
        } else {
            setTheme(android.R.style.Theme_Holo_Light);
        }
        setContentView(R.layout.settings_view);

        //start of font size spinner
        fontSizes = new ArrayList<String>();
        fontSizes.add(getString(R.string.font_size_prompt) + sharedPreferences.getInt(getString(R.string.font_size_pref_key), 20));
        Resources res = getResources();
        fontSizes.addAll(Arrays.asList(res.getStringArray(R.array.font_size_array)));

        fontSizeSpinner = (Spinner) findViewById(R.id.fontSizeSpinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, fontSizes);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fontSizeSpinner.setAdapter(dataAdapter);

        fontSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    String sizeString = parent.getItemAtPosition(position).toString();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(getString(R.string.font_size_pref_key), Integer.parseInt(sizeString));
                    editor.commit();
                    fontSizes.set(0, getString(R.string.font_size_prompt) + sharedPreferences.getInt(getString(R.string.font_size_pref_key), 20));
                    fontSizeSpinner.setSelection(0);
                    Toast.makeText(parent.getContext(), "歌词字体已设置为 : " + sizeString, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //end of font size spinner

        //start of theme switch
        nightModeSwitch = (Switch) findViewById(R.id.nightModeSwitch);
        nightModeSwitch.setChecked(sharedPreferences.getBoolean(getString(R.string.night_mode_pref_key), true));

    }

    /**
     * Called when the user clicks the resume default font size button
     */
    public void resumeDefaultSize(View view) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getString(R.string.font_size_pref_key), 20);
        editor.commit();
        fontSizes.set(0, getString(R.string.font_size_prompt) + 20);
        //have to use notifyDataSetChanged() to refresh the spinner since here we did not change selection(still position 0) in spinner.
        BaseAdapter adapter = (BaseAdapter) fontSizeSpinner.getAdapter();
        adapter.notifyDataSetChanged();
        Toast.makeText(getBaseContext(), "歌词字体已恢复为 : 20", Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when the night mode switch clicked
     */
    public void onNightModeSwitch(View view) {
        boolean on = ((Switch) view).isChecked();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (on) {
            editor.putBoolean(getString(R.string.night_mode_pref_key), true);
        } else {
            editor.putBoolean(getString(R.string.night_mode_pref_key), false);
        }
        editor.commit();
        isNightModeToggled = !isNightModeToggled;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isNightModeToggled) {
            Intent a = new Intent(this,MainActivity.class);
            a.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(a);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}