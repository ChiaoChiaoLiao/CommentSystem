package com.example.chuchiao_liao.cardviewtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.Firebase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Chuchiao_Liao on 2016/10/27.
 */
public class ReplyActivity extends AppCompatActivity implements View.OnClickListener, ReplySource.ReplysCallbacks  {
    //    private Firebase myFirebaseRef;
//    public static final String USER_EXTRA = "USER";
//    public static final String TAG = "ChatActivity";
    private ArrayList<Message> mReplys;
    private ReplyActivity.ReplysAdapter mAdapter;
    private String mReciptent;
    private ListView mListView;
    private Date mLastReplyDate = new Date();
    private String mConvId;
    private ReplySource.ReplysListener mListener;
    private EditText mReplyView;
    private String mKey;
    private int mCount;
    private int deleteIndex;
    private static final Firebase sFirebaseRef = new Firebase("https://hostingtest-20944.firebaseio.com/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(this);
        Bundle bundle = getIntent().getExtras();
        mConvId = bundle.getString(MainActivity.CON);
        mKey = bundle.getString(MainActivity.CHI);
        mCount = bundle.getInt("Count");

        mReciptent = "Rohit";
        mListView = (ListView) findViewById(R.id.messageList);
        mReplyView = (EditText) findViewById(R.id.messageWrite);
        mReplys = new ArrayList<>();
        mAdapter = new ReplyActivity.ReplysAdapter(this, mReplys);
        mListView.setAdapter(mAdapter);
        mListView.setDividerHeight(0);
        //mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
//        if (mReplyView.isFocused()){
//            mListView.setVerticalScrollbarPosition(mListView.getCount()-1);
//        }

//        setTitle(mReciptent);
//        if (getSupportActionBar() != null){
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        }
        Button sendReply = (Button) findViewById(R.id.messageSend);
        sendReply.setOnClickListener(this);

        mListener = ReplySource.addReplysListener(mConvId, mKey, this);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


    }

    @Override
    public void onClick(View v) {
        EditText replySender = (EditText) findViewById(R.id.messageSender);
//        if (mMessageView.isFocused()){
//            Log.d("focus", "yes");
//            mListView.setVerticalScrollbarPosition(mListView.getAdapter().getCount()-1);
//        }
        String newReply = mReplyView.getText().toString();
        String sender = replySender.getText().toString();
        mReplyView.setText("");
        Message msg = new Message();
        msg.setDate(new Date());
        msg.setMessage(newReply);
        msg.setName(sender);
        mCount = mCount + 1;
        msg.setCount(mCount);

        ReplySource.saveReply(msg, mConvId, mKey, mCount);
    }

    @Override
    public void onReplyAdded(Message message){
        mReplys.add(message);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onReplyRemoved() {
        mReplys.remove(deleteIndex);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ReplySource.stop(mListener);
    }

    private class ReplysAdapter extends BaseAdapter {
        private class ViewHolder{
            ImageView icon;
            TextView sender;
            TextView reply;
            TextView date;
        }
        private ArrayList<Message> items;
        private LayoutInflater mInflater;
        public ReplysAdapter(ReplyActivity context, ArrayList<Message> items){
            mInflater = LayoutInflater.from(context);
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ReplyActivity.ReplysAdapter.ViewHolder holder;
            //View nullView = mInflater.inflate(R.layout.null_item, null);
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item, parent, false);
                holder = new ReplyActivity.ReplysAdapter.ViewHolder();
                holder.sender = (TextView) convertView.findViewById(R.id.userName);
                holder.icon = (ImageView) convertView.findViewById(R.id.userImage);
                holder.reply = (TextView) convertView.findViewById(R.id.userMessage);
                holder.date = (TextView) convertView.findViewById(R.id.messageTime);
                convertView.setTag(holder);
            } else {
                holder = (ReplyActivity.ReplysAdapter.ViewHolder) convertView.getTag();
            }
            holder.date.setText(new SimpleDateFormat("yyyy/MM/dd hh:mm").format(items.get(position).getDate().getTime()));
            holder.icon.setImageResource(R.drawable.ic_sentiment_satisfied_black_24px);
            holder.sender.setText(items.get(position).getName());
            holder.reply.setText(items.get(position).getMessage());
            final String key = new SimpleDateFormat("yyyyMMddhhmmssSSS").format(items.get(position).getDate().getTime());
           /* convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    sFirebaseRef.child(mConvId).child("MsgBox").child(mKey).child("SubReply").child(key).removeValue();
                    mCount = mCount - 1;
                    HashMap<String, Object> msg2 = new HashMap<>();
                    msg2.put("ReplyCount", mCount);
                    sFirebaseRef.child(mConvId).child("MsgBox").child(mKey).updateChildren(msg2);
                    deleteIndex = position;
                    return true;
                }
            });*/
//            if (items.size() > 5) {
//                if (position == 1) {
//                    convertView = nullView;
//                    TextView textView = (TextView) convertView.findViewById(R.id.nullText);
//                    textView.setText("View " + String.valueOf(items.size()-5) + " more reply");
//                } else if (position > items.size()-5) {
//                    //if (convertView == null) {
//                    convertView = mInflater.inflate(R.layout.item, parent, false);
//                    holder = new ViewHolder();
//                    holder.sender = (TextView) convertView.findViewById(R.id.userName);
//                    holder.icon = (ImageView) convertView.findViewById(R.id.icon);
//                    holder.message = (TextView) convertView.findViewById(R.id.userMessage);
//                    convertView.setTag(holder);
//                    //} else {
//                        holder = (ViewHolder) convertView.getTag();
//                    //}
//                    holder.sender.setText(items.get(position).getName());
//                    holder.message.setText(items.get(position).getMessage());
//                } else {
//                    convertView = nullView;
//                    TextView textView = (TextView) convertView.findViewById(R.id.nullText);
//                    textView.setHeight(0);
//                }
//            }
            return convertView;
        }
    }
}
