package com.example.chuchiao_liao.cardviewtest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        /*MessageSource.MessagesCallbacks*/ CombinedSource.CombinedCallbacks {
    private Firebase myFirebaseRef;
    public static final String USER_EXTRA = "USER";
    public static final String TAG = "ChatActivity";
    private ArrayList<Message> mMessages;
    private ArrayList<Reply> mChildMessages;
    private MessagesAdapter mAdapter;
    private ListView mListView;
    private Date mLastMessageDate = new Date();
    private String mConvId;
    private CombinedSource.CombinedListener mListener;
    private EditText mMessageView;
    public static final String CON = "conv";
    public static final String CHI = "child";
    private ChildMessagesAdapter mChildAdapter;
    private ReplyShortcut.ReplysShortcutListener mChildListener;
    public static int mMsgCount;
    private int howMany;
    private Firebase firebaseRef;
    private String mLastId;
    private int index;
    private final int numReq = 10;
    private int deleteIndex;
    private boolean mIsReq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(this);
        firebaseRef = new Firebase("https://hostingtest-20944.firebaseio.com/");

        mConvId = "CommentSystem";
        howMany = numReq + 1;

        mListView = (ListView) findViewById(R.id.messageList);
        mMessageView = (EditText) findViewById(R.id.messageWrite);
        mMessages = new ArrayList<>();
        mAdapter = new MessagesAdapter(this, mMessages);
        mListView.setAdapter(mAdapter);
        mListView.setDividerHeight(0);
        Button sendMessage = (Button) findViewById(R.id.messageSend);
        sendMessage.setOnClickListener(this);

        firebaseRef.child(mConvId).child("MsgCount")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMsgCount = Integer.valueOf(dataSnapshot.getValue().toString());
                mListener = CombinedSource.addCombinedListener(mConvId,
                        MainActivity.this, howMany, null);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onClick(View v) {
        EditText messageSender = (EditText) findViewById(R.id.messageSender);
        String newMessage = mMessageView.getText().toString();
        String sender = messageSender.getText().toString();
        mMessageView.setText("");
        Message msg = new Message();
        msg.setDate(new Date());
        msg.setMessage(newMessage);
        msg.setName(sender);
        mMsgCount = mMsgCount + 1;

        CombinedSource.saveMessage(msg, mConvId, mMsgCount);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CombinedSource.stop(mListener);
    }

    @Override
    public void onCombinedAdded(Message message) {
        mMessages.add(message);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCombinedReq(Message message) {
        if (mIsReq) {
            if (index > 0) {
                mMessages.add(index, message);
            } else {
                mMessages.set(0, message);
            }
            index = index + 1;

            if (index > numReq) {
                mIsReq = false;
            }
        } else {
            mMessages.add(message);
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCombinedRemoved() {
        mMessages.remove(deleteIndex);
        mAdapter.notifyDataSetChanged();
    }

    public void requestMore(String lastId) {
        index = 0;
        mIsReq = true;
        mListener = CombinedSource.addCombinedListener(
                mConvId, MainActivity.this, numReq + 1, lastId);
    }

    private class MessagesAdapter extends BaseAdapter/* implements ReplyShortcut.ReplysShortcutCallbacks*/{

        private class ViewHolder{
            ImageView icon;
            TextView sender;
            TextView message;
            Long key;
            int count;
            ChildListView childList;
            TextView date;
        }
        private ArrayList<Message> items;
        private LayoutInflater mInflater;
        public MessagesAdapter(MainActivity context, ArrayList<Message> items){
            mInflater = LayoutInflater.from(context);
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            String key = "";
            View nullView = mInflater.inflate(R.layout.more_item, null);
            if (mMsgCount > 0) {
                if (mMsgCount > howMany) {
                    if (position == 0) {
                        convertView = nullView;
                        convertView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                howMany = howMany + numReq;
                                if (howMany > mMsgCount) {
                                    howMany = mMsgCount;
                                }
                                long time = items.get(0).getDate().getTime();
                                String lastId = new SimpleDateFormat("yyyyMMddhhmmssSSS").format(time);
                                requestMore(lastId);
                            }
                        });
                    } else {
                        convertView = mInflater.inflate(R.layout.item, parent, false);
                        holder = new ViewHolder();
                        holder.sender = (TextView) convertView.findViewById(R.id.userName);
                        holder.icon = (ImageView) convertView.findViewById(R.id.userImage);
                        holder.message = (TextView) convertView.findViewById(R.id.userMessage);
                        holder.childList = (ChildListView) convertView.findViewById(R.id.childList);
                        holder.date = (TextView) convertView.findViewById(R.id.messageTime);
                        convertView.setTag(holder);
                        holder.icon.setImageResource(R.drawable.ic_sentiment_satisfied_black_24px);
                        holder.sender.setText(String.valueOf(position) + ". "
                                + items.get(position).getName());
                        holder.message.setText(items.get(position).getMessage());
                        if (items.get(position).getDate() != null) {
                            holder.key = items.get(position).getDate().getTime();
                            key = new SimpleDateFormat("yyyyMMddhhmmssSSS").format(holder.key);
                            holder.date.setText(new SimpleDateFormat("yyyy/MM/dd hh:mm")
                                    .format(items.get(position).getDate().getTime()));
                        }
                        holder.count = items.get(position).getCount();

                        final String finalKey = key;
                        convertView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MainActivity.this, ReplyActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString(CHI, finalKey);
                                bundle.putString(CON, mConvId);
                                bundle.putInt("Count", holder.count);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        });
                        convertView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                firebaseRef.child(mConvId).child("MsgBox")
                                        .child(finalKey).removeValue();
                                mMsgCount = mMsgCount - 1;
                                HashMap<String, Object> msg2 = new HashMap<>();
                                msg2.put("MsgCount", mMsgCount);
                                firebaseRef.child(mConvId).updateChildren(msg2);
                                deleteIndex = position;
                                return true;
                            }
                        });
                        mChildMessages = items.get(position).getChildReply();
                        mChildAdapter = new ChildMessagesAdapter(MainActivity.this,
                                mChildMessages, key, holder.count);
                        holder.childList.setAdapter(mChildAdapter);
                        holder.childList.setDividerHeight(0);
                    }
                } else {
                    convertView = mInflater.inflate(R.layout.item, parent, false);
                    holder = new ViewHolder();
                    holder.sender = (TextView) convertView.findViewById(R.id.userName);
                    holder.icon = (ImageView) convertView.findViewById(R.id.userImage);
                    holder.message = (TextView) convertView.findViewById(R.id.userMessage);
                    holder.childList = (ChildListView) convertView.findViewById(R.id.childList);
                    holder.date = (TextView) convertView.findViewById(R.id.messageTime);
                    convertView.setTag(holder);
                    holder.icon.setImageResource(R.drawable.ic_sentiment_satisfied_black_24px);
                    holder.sender.setText(String.valueOf(position) + ". "
                            + items.get(position).getName());
                    holder.message.setText(items.get(position).getMessage());
                    if (items.get(position).getDate() != null) {
                        holder.key = items.get(position).getDate().getTime();
                        key = new SimpleDateFormat("yyyyMMddhhmmssSSS").format(holder.key);
                        holder.date.setText(new SimpleDateFormat("yyyy/MM/dd hh:mm").format(
                                items.get(position).getDate().getTime()));
                    }
                    holder.count = items.get(position).getCount();

                    final String finalKey = key;
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(MainActivity.this, ReplyActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString(CHI, finalKey);
                            bundle.putString(CON, mConvId);
                            bundle.putInt("Count", holder.count);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    });
                    convertView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            firebaseRef.child(mConvId).child("MsgBox").child(finalKey).removeValue();
                            mMsgCount = mMsgCount - 1;
                            HashMap<String, Object> msg2 = new HashMap<>();
                            msg2.put("MsgCount", mMsgCount);
                            firebaseRef.child(mConvId).updateChildren(msg2);
                            deleteIndex = position;
                            return true;
                        }
                    });
                    mChildMessages = items.get(position).getChildReply();
                    mChildAdapter = new ChildMessagesAdapter(MainActivity.this, mChildMessages, key,
                            holder.count);
                    holder.childList.setAdapter(mChildAdapter);
                    holder.childList.setDividerHeight(0);
                }
            } else {
                convertView = nullView;
                TextView textView = (TextView) convertView.findViewById(R.id.requestText);
                textView.setHeight(0);
                textView.setEnabled(false);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.refImg);
                imageView.setVisibility(View.GONE);
            }

            return convertView;
        }
    }
    private class ChildMessagesAdapter extends BaseAdapter{
        private class ViewHolder{
            ImageView icon;
            TextView sender;
            TextView message;
        }
        private ArrayList<Reply> items;
        private LayoutInflater mInflater;
        private String mKey;
        private int mCount;
        public ChildMessagesAdapter(MainActivity context, /*ArrayList<Message> items*/ ArrayList<Reply> items, String key, int count){
            mInflater = LayoutInflater.from(context);
            this.items = items;
            this.mKey = key;
            this.mCount = count;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View nullView = mInflater.inflate(R.layout.null_item, null);
            if (mCount > 0) {
                if (mCount > 5) {
                    if (position == 0) {
                        convertView = nullView;
                        TextView textView = (TextView) convertView.findViewById(R.id.nullText);
                        textView.setText("View " + String.valueOf(mCount - 5) + " more replies");
                    } else {
                        convertView = mInflater.inflate(R.layout.child_item, parent, false);
                        holder = new ViewHolder();
                        holder.sender = (TextView) convertView.findViewById(R.id.userName);
                        holder.icon = (ImageView) convertView.findViewById(R.id.userImage);
                        holder.message = (TextView) convertView.findViewById(R.id.userMessage);
                        convertView.setTag(holder);
                        holder.icon.setImageResource(R.drawable.ic_sentiment_satisfied_black_24px);
                        holder.sender.setText(items.get(position).getName());
                        holder.message.setText(items.get(position).getMessage());
                    }
                } else {
                    if (convertView == null) {
                        convertView = mInflater.inflate(R.layout.child_item, parent, false);
                        holder = new ViewHolder();
                        holder.sender = (TextView) convertView.findViewById(R.id.userName);
                        holder.icon = (ImageView) convertView.findViewById(R.id.userImage);
                        holder.message = (TextView) convertView.findViewById(R.id.userMessage);
                        convertView.setTag(holder);
                    } else {
                        holder = (ViewHolder) convertView.getTag();
                    }
                    holder.icon.setImageResource(R.drawable.ic_sentiment_satisfied_black_24px);
                    holder.sender.setText(items.get(position).getName());
                    holder.message.setText(items.get(position).getMessage());
                }
            } else {
                convertView = nullView;
                TextView textView = (TextView) convertView.findViewById(R.id.nullText);
                textView.setHeight(0);
                textView.setEnabled(false);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ReplyActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(CHI, mKey);
                    bundle.putString(CON, mConvId);
                    bundle.putInt("Count", mCount);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
            return convertView;
        }
    }
}
