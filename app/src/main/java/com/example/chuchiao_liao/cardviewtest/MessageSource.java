package com.example.chuchiao_liao.cardviewtest;

import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Chuchiao_Liao on 2016/10/25.
 */
public class MessageSource {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    public static final Firebase FIREBASE_REF = new Firebase("https://hostingtest-20944.firebaseio.com/");
    private static SimpleDateFormat DATA_FORMAT = new SimpleDateFormat("yyyyMMddmmss");
    private static final String TAG = "MessageDataSource";
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
    public static void saveMessage (Message message, String convId){
        Date date = message.getDate();
        String key = DATA_FORMAT.format(date);
        HashMap<String, String> msg = new HashMap<>();
        msg.put(COLUMN_NAME, message.getName());
        msg.put(COLUMN_TEXT, message.getMessage());
        FIREBASE_REF.child(convId).child(key).setValue(msg);
    }

    public static Long getChildCount(String convId, String key) {
        final long[] count = new long[1];
        FIREBASE_REF.child(convId).child(key).child("SubReply").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                count[0] = dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        return count[0];
    }

    public static MessagesListener addMessagesListener (String convId, final MessagesCallbacks callbacks){
        MessagesListener listener = new MessagesListener(callbacks);
        //sFirebaseRef.child(convId).limitToLast(5).addChildEventListener(listener);
        FIREBASE_REF.child(convId).addChildEventListener(listener);
        FIREBASE_REF.child(convId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        return listener;
    }

    public static void stop (MessagesListener listener){
        FIREBASE_REF.removeEventListener(listener);
    }

    public interface MessagesCallbacks {
        void onMessageAdded(Message message);
    }

    public static class MessagesListener implements ChildEventListener{
        private MessagesCallbacks callbacks;
        MessagesListener(MessagesCallbacks callbacks){
            this.callbacks = callbacks;
        }
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap<String, Object> msg = (HashMap) dataSnapshot.getValue();
            Message message = new Message();
            message.setName(msg.get(COLUMN_NAME).toString());
            message.setMessage(msg.get(COLUMN_TEXT).toString());
            if (msg.get("ReplyCount") != null){
                message.setCount(Integer.valueOf(msg.get("ReplyCount").toString()));
            } else {
                message.setCount(0);
            }

            try {
                message.setDate(DATA_FORMAT.parse(dataSnapshot.getKey()));
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (callbacks != null) {
                callbacks.onMessageAdded(message);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            HashMap<String, Long> msg = (HashMap) dataSnapshot.getValue();
            Message message = new Message();
            if (msg.get("ReplyCount") != null) {
                message.setCount(msg.get("ReplyCount").intValue());
            } else {
                message.setCount(0);
            }

            if (callbacks != null) {
                callbacks.onMessageAdded(message);
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    }
}
