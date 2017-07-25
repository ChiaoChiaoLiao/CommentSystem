package com.example.chuchiao_liao.cardviewtest;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import static com.example.chuchiao_liao.cardviewtest.MainActivity.sDatabaseReference;

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
    private static SimpleDateFormat DATA_FORMAT = new SimpleDateFormat("yyyyMMddmmss");
    private static final String TAG = "ReplyShortcutDataSource";
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
        sDatabaseReference.child(convId).child(key).child("SubReply").limitToLast(6).addChildEventListener(listener);

        return listener;
    }

    public static void stop (ReplysShortcutListener listener){
        sDatabaseReference.removeEventListener(listener);
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
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap<String, String> msg = (HashMap) dataSnapshot.getValue();
            Reply reply = new Reply();
            reply.setName(msg.get(COLUMN_NAME));
            reply.setMessage(msg.get(COLUMN_TEXT));
            try {
                reply.setDate(DATA_FORMAT.parse(dataSnapshot.getKey()));
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (callbacks != null) {
                callbacks.onReplyShortcutAdded(reply);
            }
        }
    }
}
