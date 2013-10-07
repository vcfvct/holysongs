package com.example.HolySongs;

import android.app.Activity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * User: LeOn
 * Date: 13-10-6
 * Time: 下午10:33
 * To change this template use File | Settings | File Templates.
 */
public class AboutActivity extends Activity {
    private String ABOUT_CONTENT = "\n我们是位于马里兰州Germantown的德国镇基督教会，欢迎大家光临。http://www.cccgermantown.org/ " +
            "\n\n 本app为方便团契或其他聚会时大家敬拜之用，至少可以省去打印的麻烦:-) " +
            "\n\n任何意见，请反馈至: vcfvct@gmail.com \n感谢Katie的鼓励和添加歌曲^_^。";
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView textView = new TextView(this);
        textView.setLineSpacing(10, 1);
        textView.setAutoLinkMask(Linkify.ALL);
        textView.setText(ABOUT_CONTENT);
        setContentView(textView);
    }
}