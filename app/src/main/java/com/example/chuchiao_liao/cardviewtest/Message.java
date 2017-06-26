package com.example.chuchiao_liao.cardviewtest;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Chuchiao_Liao on 2016/10/25.
 */
public class Message {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    private String mName;
    private String mMessage;
    private Date mDate;
    private int mCount;
    private ArrayList<Reply> mChildReply;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------
    public Message() {
      /*Blank default constructor essential for Firebase*/
    }

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String address) {
        this.mMessage = address;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        this.mCount = count;
    }

    public ArrayList<Reply> getChildReply() {
        return mChildReply;
    }

    public void setChildReply(ArrayList<Reply> childReply) {
        this.mChildReply = childReply;
    }
}
