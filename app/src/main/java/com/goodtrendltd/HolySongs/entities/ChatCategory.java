package com.goodtrendltd.HolySongs.entities;

import java.io.Serializable;

/**
 * Created by vincent on 16/3/23.
 */
public class ChatCategory implements Serializable
{
    public String id;
    public int status;
    public String info;
    public String title;

    public int getId() {
        int intId = -1;
        try {
            intId = Integer.parseInt(id);
        } catch(NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
        }
        return intId;
    }
}
