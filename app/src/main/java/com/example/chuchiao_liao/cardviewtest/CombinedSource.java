package com.example.chuchiao_liao.cardviewtest;

import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Chuchiao_Liao on 2016/11/2.
 */
public class CombinedSource {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    public static final Firebase FIREBASE_REF = new Firebase("https://hostingtest-20944.firebaseio.com/");
    private static SimpleDateFormat DATA_FORMAT = new SimpleDateFormat("yyyyMMddhhmmssSSS");
    private static final String TAG = "CombinedDataSource";
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
    private static String mLast = " ";
    private static boolean mRequest;

    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
    public static void saveMessage (Message message, String convId, int count){
        long date = message.getDate().getTime();
        String key = DATA_FORMAT.format(date);
        HashMap<String, String> msg = new HashMap<>();
        msg.put(COLUMN_NAME, message.getName());
        msg.put(COLUMN_TEXT, message.getMessage());
        FIREBASE_REF.child(convId).child("MsgBox").child(key).setValue(msg);
        HashMap<String, Object> msg2 = new HashMap<>();
        msg2.put("MsgCount", count);
        FIREBASE_REF.child(convId).updateChildren(msg2);
        mLast = key;
        mRequest = false;
    }

    public static CombinedSource.CombinedListener addCombinedListener(String convId, final CombinedCallbacks callbacks, int numReq, String lastId){
        CombinedSource.CombinedListener listener = new CombinedSource.CombinedListener(callbacks);
        if (lastId == null) {
            mRequest = false;
            FIREBASE_REF.child(convId).child("MsgBox").orderByKey().limitToLast(numReq).addChildEventListener(listener);
        } else {
            mRequest = true;
            FIREBASE_REF.child(convId).child("MsgBox").orderByKey().endAt(lastId).limitToLast(numReq).addChildEventListener(listener);
        }

        return listener;
    }

    public static void stop (CombinedSource.CombinedListener listener){
        FIREBASE_REF.removeEventListener(listener);
    }

    public interface CombinedCallbacks {
        void onCombinedAdded(Message message);
        void onCombinedReq(Message message);
        void onCombinedRemoved();
    }

    public static class CombinedListener implements ChildEventListener/*, ReplyShortcut.ReplysShortcutCallbacks*/ {

        ArrayList<Reply> mReply = new ArrayList<>();
        private CombinedSource.CombinedCallbacks callbacks;
        CombinedListener(CombinedSource.CombinedCallbacks callbacks){
            this.callbacks = callbacks;
        }
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if (s == null) s = "";
            if (!s.equals(mLast)) {
                HashMap<String, Object> msg = (HashMap) dataSnapshot.getValue();
                String key = "";
                int size;
                JSONObject object;
                ArrayList<Reply> replies = new ArrayList<>();
                Message message = new Message();
                message.setName(msg.get(COLUMN_NAME).toString());
                message.setMessage(msg.get(COLUMN_TEXT).toString());

                if (msg.get("ReplyCount") != null) {
                    message.setCount(Integer.valueOf(msg.get("ReplyCount").toString()));
                    size = Integer.valueOf(msg.get("ReplyCount").toString());
                    int bound = size - 6;
                    if (bound < 0) {
                        bound = 0;
                    }
                    try {
                        object = new JSONObject((Map) msg.get("SubReply"));
                        Iterator<?> iterator = object.keys();
                        List<JSONObject> jsonArray = new ArrayList<>();
                        for (int i = 0; i < size; i++) {
                            iterator.hasNext();
                            String kkk = (String) iterator.next();
                            JSONObject tmpO = new JSONObject();
                            tmpO.put("key", kkk.substring(1));
                            tmpO.put("obj", object.getJSONObject(kkk));
                            jsonArray.add(tmpO);
                        }
                        Collections.sort(jsonArray, new Comparator<JSONObject>() {
                            @Override
                            public int compare(JSONObject lhs, JSONObject rhs) {
                                String lid = null;
                                try {
                                    lid = lhs.getString("key");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                String rid = null;
                                try {
                                    rid = rhs.getString("key");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                // Here you could parse string id to integer and then compare.
                                return lid.compareTo(rid);
                            }
                        });
                        JSONArray sortedArray = new JSONArray(jsonArray);

                        for (int i = bound; i < size; i++) {
                            JSONObject replyObject = sortedArray.getJSONObject(i);
                            Reply reply = new Reply();
                            reply.setDate(DATA_FORMAT.parse(replyObject.getString("key")));
                            reply.setMessage(replyObject.getJSONObject("obj").getString(COLUMN_TEXT));
                            reply.setName(replyObject.getJSONObject("obj").getString(COLUMN_NAME));
                            replies.add(reply);
                        }
                        message.setChildReply(replies);
                    } catch (JSONException e) {
                        Log.e("object", e.toString());
                    } catch (ParseException e) {
                        Log.e("parse", e.toString());
                    }
                } else {
                    message.setCount(0);
                    Reply reply = new Reply();
                    reply.setMessage("");
                    reply.setName("");
                    replies.add(reply);
                    message.setChildReply(replies);
                }

                try {
                    key = dataSnapshot.getKey();
                    message.setDate(DATA_FORMAT.parse(key));
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                if (callbacks != null) {
                    //ReplyShortcut.ReplysShortcutListener mChildListener = ReplyShortcut.addReplysShortcutListener(mConvId, key, this);
                    if (!mRequest) {
                        callbacks.onCombinedAdded(message);
                    } else {
                        callbacks.onCombinedReq(message);
                    }
                    mLast = s;
                }
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