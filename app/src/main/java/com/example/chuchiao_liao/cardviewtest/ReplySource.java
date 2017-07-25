package com.example.chuchiao_liao.cardviewtest;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import static com.example.chuchiao_liao.cardviewtest.MainActivity.sDatabaseReference;

/**
 * Created by Chuchiao_Liao on 2016/10/27.
 */
public class ReplySource {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static SimpleDateFormat DATA_FORMAT = new SimpleDateFormat("yyyyMMddhhmmssSSS");
    private static final String TAG = "ReplyDataSource";
    private static final String COLUMN_TEXT = "text";
    private static final String COLUMN_NAME = "name";

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
    public static void saveReply(Message message, String convId, String key1, int count){
        long date = message.getDate().getTime();
        String key = "s" + DATA_FORMAT.format(date);
        HashMap<String, String> msg = new HashMap<>();
        msg.put(COLUMN_NAME, message.getName());
        msg.put(COLUMN_TEXT, message.getMessage());
        sDatabaseReference.child(convId).child("MsgBox").child(key1).child("SubReply")
                .child(key).setValue(msg);
        HashMap<String, Object> msg2 = new HashMap<>();
        msg2.put("ReplyCount", count);
        sDatabaseReference.child(convId).child("MsgBox").child(key1).updateChildren(msg2);
    }

    public static ReplySource.ReplysListener addReplysListener(String convId, String key,
                                                               final ReplysCallbacks callbacks){
        ReplySource.ReplysListener listener = new ReplySource.ReplysListener(callbacks);

        sDatabaseReference.child(convId).child("MsgBox").child(key).child("SubReply")
                .addChildEventListener(listener);
        sDatabaseReference.child(convId).child("MsgBox").child(key).child("SubReply")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        return listener;
    }

    public static void stop (ReplySource.ReplysListener listener){
        sDatabaseReference.removeEventListener(listener);
    }

    public interface ReplysCallbacks {
        void onReplyAdded(Message message);
        void onReplyRemoved();
    }

    public static class ReplysListener implements ChildEventListener {
        private ReplySource.ReplysCallbacks callbacks;
        ReplysListener(ReplySource.ReplysCallbacks callbacks){
            this.callbacks = callbacks;
        }
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap<String, String> msg = (HashMap) dataSnapshot.getValue();
            Message message = new Message();
            message.setName(msg.get(COLUMN_NAME));
            message.setMessage(msg.get(COLUMN_TEXT));
            try {
                message.setDate(DATA_FORMAT.parse(dataSnapshot.getKey().substring(1)));
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (callbacks != null) {
                callbacks.onReplyAdded(message);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            if (callbacks != null) {
                callbacks.onReplyRemoved();
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }
}
