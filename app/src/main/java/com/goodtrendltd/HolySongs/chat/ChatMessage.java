package com.goodtrendltd.HolySongs.chat;

import android.content.Context;
import android.text.format.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Julia on 9/22/2015.
 */
public class ChatMessage {
    private Integer id;
    private Date date;
    private String body;
    private boolean readFlag;
    private boolean saveToHistory = false;
    private String direction;
    private boolean sendingInProgress = false;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss", Locale.getDefault());
    DateFormat df = DateFormat.getDateTimeInstance();
//    fromDate = sdf.format(fromDateTime);

    public ChatMessage() {
    }
    public Integer getId() {return this.id;}
    public void setId(Integer id) {this.id = id;}
    public String getBody() {return this.body;}
    public void setBody(String body) {
        this.body = body;
    }
    public void setReadFlag(Boolean flag) { this.readFlag=flag; }
    public Boolean isRead() {return this.readFlag; }
    public void setDirection(String direction) { this.direction = direction; }
    public void setDate(Date date) {
        //Date is stored as date object
        this.date=date;
    }
    public void setDateFromString(String utcdate) throws ParseException {
        //From string, ex. 2015-09-22 07:11:49
        //to Date object
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.date = sdf.parse(utcdate);
    }
    public Date getDate() {
        return this.date;
    }
    public String getUTCDate() {
        //In UTC time zone: yyyy-MM-dd 1970-01-01
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(this.date);
    }
    public String getPrettyDate(Context ctxt) {
        //Date to String, ex. Dec 31, 1969 4:00:00 PM
        String prettyDate = "";
        // prettyDate = df.format(prettyDate);
        if(DateUtils.isToday(this.date.getTime())) { //today
            prettyDate = DateUtils.formatDateTime(ctxt,this.date.getTime(), DateUtils.FORMAT_SHOW_TIME);
        }
        else  { //relative format in days up to 3 days ago, otherwise standard format
            prettyDate = DateUtils.getRelativeDateTimeString(ctxt,this.date.getTime(), DateUtils.DAY_IN_MILLIS,3* DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_SHOW_WEEKDAY).toString();
        }
       return prettyDate;
    }

    public boolean isOutgoing(){
        if (this.direction.equals("in")) {
            //"in" means that the message is sent in to the OM's
            //For our purposes, "in" means received messages
            return true;
        }
        else {
            //"out" means message is sent from the OM's
            //In this code, "out" means sent messages from app
            return false;
        }
    }

    public boolean isSendingInProgress() {
        return this.sendingInProgress;
    }
    public void setSendingInProgress(Boolean status) {
        this.sendingInProgress = status;
    }
}
