package com.example.HolySongs.helpers;

import java.text.Collator;
import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: LeOn
 * Date: 13-10-5
 * Time: 下午10:51
 */
public class ChineseCharComp implements Comparator<String> {
    @Override
    public int compare(String lhs, String rhs) {
        Collator myCollator = Collator.getInstance(java.util.Locale.CHINA);
        if (myCollator.compare(lhs, rhs) < 0)
            return -1;
        else if (myCollator.compare(lhs, rhs) > 0)
            return 1;
        else
            return 0;
    }
}
