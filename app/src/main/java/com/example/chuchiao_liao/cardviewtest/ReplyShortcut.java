package com.example.chuchiao_liao.cardviewtest;

import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Chuchiao_Liao on 2016/10/31.
 */
public class ReplyShortcut {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    private static final Firebase sFirebaseRef = new Firebase("https://hostingtest-20944.firebaseio.com/");
    private static SimpleDateFormat sDataFormat = new SimpleDateFormat("yyyyMMddmmss");
    private static final String sTAG = "ReplyShortcutDataSource";
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
    public static ReplyShortcut.ReplysShortcutListener addReplysShortcutListener(String convId, String key, final ReplyShortcut.ReplysShortcutCallbacks callbacks){
        ReplyShortcut.ReplysShortcutListener listener = new ReplyShortcut.ReplysShortcutListener(callbacks);
        sFirebaseRef.child(convId).child(key).child("SubReply").limitToLast(6).addChildEventListener(listener);

        return listener;
    }

    public static void stop (ReplysShortcutListener listener){
        sFirebaseRef.removeEventListener(listener);
    }

    public interface ReplysShortcutCallbacks {
        void onReplyShortcutAdded(Reply reply);
    }

    public static class ReplysShortcutListener implements ChildEventListener {
        private ReplyShortcut.ReplysShortcutCallbacks callbacks;
        ReplysShortcutListener(ReplyShortcut.ReplysShortcutCallbacks callbacks){
            this.callbacks = callbacks;
        }
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap<String, String> msg = (HashMap) dataSnapshot.getValue();
            Reply reply = new Reply();
            reply.setName(msg.get(COLUMN_NAME));
            reply.setMessage(msg.get(COLUMN_TEXT));
            try {
                reply.setDate(sDataFormat.parse(dataSnapshot.getKey()));
            } catch (Exception e) {
                Log.e(sTAG, e.toString());
            }
            if (callbacks != null) {
                callbacks.onReplyShortcutAdded(reply);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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
