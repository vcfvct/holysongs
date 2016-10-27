package com.goodtrendltd.HolySongs;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.goodtrendltd.HolySongs.bus.BibleSetting;
import com.goodtrendltd.HolySongs.helpers.HanziHelper;

/**
 * Created with IntelliJ IDEA.
 * User: LeOn
 * Date: 13-10-6
 * Time: 上午10:00
 */
public class SongTitleAdapter extends ArrayAdapter<String> implements SectionIndexer {
    private SparseIntArray alphaMap;
    private List<String> items;
    private Context context;
    Boolean nightMode = true;

    public SongTitleAdapter(Context context, List<String> items) {
        super(context, R.layout.song_item, items);
        this.alphaMap = new SparseIntArray(Sidebar.ALPHABET_ARRAY.length);
        this.items = items;
        this.context = context;
        nightMode = BibleSetting.getNightMode(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.song_item, null);
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String p = items.get(position);
        viewHolder.name.setText(p);
        if (!nightMode) {
            viewHolder.name.setTextColor(Color.parseColor("#000000"));
//            convertView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
//        else {
//            viewHolder.name.setTextColor(Color.parseColor("#FFFFFF"));
//            convertView.setBackgroundColor(Color.parseColor("#000000"));
//        }
        return convertView;
    }

    private class ViewHolder {

        public TextView name;

    }

    @Override
    public int getPositionForSection(int section) {
        // Check bounds
        if (section <= 0) {
            return 0;
        }
        if (section >= Sidebar.ALPHABET_ARRAY.length) {
            section = Sidebar.ALPHABET_ARRAY.length - 1;
        }
        String targetLetter = Sidebar.ALPHABET_ARRAY[section];
        int key = targetLetter.charAt(0);
        int start = 0;
        int end = items.size();
        int pos;
        //if pos already exists
        if ((pos = alphaMap.get(key, Integer.MIN_VALUE)) != Integer.MIN_VALUE) {
            return pos;
        } else {
            //FIXME haven't considered non-alphabet characters
            //estimate the start and end search position, 
            //if couldn't find the right end position, then search its next letter's position until find it or reach the end.
            //if couldn't find the right start position, then search its prev letter's position until fint it or reach the start.
            end = getApproximateEndPosOfLetter(items, section);
            start = getApproximateStartPosOfLetter(items, section - 1);
        }
        while (start < end) {
            pos = (start + end) / 2;
            String curLetter = Character.toString(HanziHelper.words2Pinyin(items.get(pos)).charAt(0));
            int result = curLetter.compareToIgnoreCase(targetLetter);
            if (result < 0) {
                start = pos + 1;
            } else if (result > 0) {
                end = pos - 1;
            } else if (result == 0) {
                while (Character.toString(HanziHelper.words2Pinyin(items.get(pos)).charAt(0))
                        .compareToIgnoreCase(targetLetter) == 0) {
                    if (pos >= 1) {
                        pos--;
                    } else if (pos == 0) {
                        alphaMap.put(key, pos);
                        return pos;
                    }
                }
                break;
            }
        }
        pos++;
        alphaMap.put(key, pos);
        return pos;
    }

    @Override
    public int getSectionForPosition(int position) {
        String sourceLetter = HanziHelper.words2Pinyin(items.get(position));
        for (int i = 0, alphabetLength = Sidebar.ALPHABET_ARRAY.length;
             i < alphabetLength; i++) {
            String targetLetter = Sidebar.ALPHABET_ARRAY[i];
            if (sourceLetter.compareTo(targetLetter) == 0) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public Object[] getSections() {
        return Sidebar.ALPHABET_ARRAY;
    }

    private int getApproximateStartPosOfLetter(List<String> source, int section) {
        int result = -1;
        ;
        while (section >= 0) {
            result = getFirstAppearanceOfLetter(source, Sidebar.ALPHABET_ARRAY[section]);
            if (result != -1) {
                while (Sidebar.ALPHABET_ARRAY[section].equals(
                        Character.toString(HanziHelper.words2Pinyin(source.get(result)).charAt(0)))) {
                    if (result < source.size() - 1) {
                        result++;
                    } else {
                        return result;
                    }
                }
                return --result;
            } else {
                section--;
            }
        }
        return result;
    }

    private int getApproximateEndPosOfLetter(List<String> source, int section) {
        int result = -1;
        ;
        while (section < Sidebar.ALPHABET_ARRAY.length) {
            result = getFirstAppearanceOfLetter(source, Sidebar.ALPHABET_ARRAY[section]);
            if (result != -1) {
                while (Sidebar.ALPHABET_ARRAY[section].equals(
                        Character.toString(HanziHelper.words2Pinyin(source.get(result)).charAt(0)))) {
                    if (result >= 1) {
                        result--;
                    } else {
                        return result;
                    }
                }
                return ++result;
            } else {
                section++;
            }
        }
        return result;
    }

    private int getFirstAppearanceOfLetter(List<String> source, String targetLetter) {
        int appearance = -1;
        int tmpStart = 0;
        int tmpEnd = source.size();
        while (tmpStart < tmpEnd) {
            int tmpPos = (tmpStart + tmpEnd) / 2;
            String curLetter = Character.toString(HanziHelper.words2Pinyin(source.get(tmpPos)).charAt(0));
            int result = curLetter.compareToIgnoreCase(targetLetter);
            if (result < 0) {
                tmpStart = tmpPos + 1;
            } else if (result > 0) {
                tmpEnd = tmpPos - 1;
            } else if (result == 0) {
                appearance = tmpPos;
                break;
            }
        }
        return appearance;
    }
}
