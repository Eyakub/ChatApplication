package com.example.user.chatapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.chatapplication.adapter.MessagingAdapter;
import com.example.user.chatapplication.model.GetTimeAgo;
import com.example.user.chatapplication.model.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ChatActivity extends AppCompatActivity {

    protected String user_name;
    protected String user_image;
    private Long last_seen;
    private Toolbar mToolbar;
    protected FirebaseAuth mAuth;


    protected String user_current_id;
    private DatabaseReference userRef;
    private DatabaseReference mRootref;
    protected String user_id;

    private ImageButton mChatAdd,mChatSend;
    private EditText mChatEt;
    protected RecyclerView chat_recyclerview;
    private SwipeRefreshLayout mRefreshLayout;
    private LinearLayoutManager layoutManager;
    private List<Message> message_list = new ArrayList<>();
    protected MessagingAdapter mAdapter;

    private static final int TOTAL_ITEM_TO_LOAD = 10;
    private int mCurrentPage = 1;
    private int itemPosition = 0;
    private String mLastKey = "";
    private String prevKey = "";

    private static final int GELLARY_PICK = 1;
    private ProgressDialog mProgress;
    private StorageReference mImageStorage;
    private DatabaseReference mReference;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mToolbar = (Toolbar)findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolbar);

        mAuth = FirebaseAuth.getInstance();
        user_current_id = mAuth.getCurrentUser().getUid();
        mRootref = FirebaseDatabase.getInstance().getReference();

        user_id = getIntent().getStringExtra("userID");
        user_name = getIntent().getStringExtra("userName");
        user_image = getIntent().getStringExtra("userImage");

        mAdapter = new MessagingAdapter(message_list,user_current_id,user_image);

        //initialize chat message task
        mChatAdd = (ImageButton) findViewById(R.id.chat_add_btn);
        mChatSend = (ImageButton) findViewById(R.id.chat_send_btn);
        mChatEt = (EditText) findViewById(R.id.chat_edit_text);
        chat_recyclerview = (RecyclerView)findViewById(R.id.chat_recyclerview);
        mRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.message_swipe_layout);


        mChatEt.setText("");

        layoutManager = new LinearLayoutManager(ChatActivity.this);
        chat_recyclerview.setHasFixedSize(true);
        chat_recyclerview.setLayoutManager(layoutManager);



        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);

        //last_seen = getIntent().getStringExtra("lastOnline");
        //user_online = getIntent().getBooleanExtra("isOnline",true);


        mImageStorage = FirebaseStorage.getInstance().getReference();

        //load data from our database
        loadMessages();


        chat_recyclerview.setAdapter(mAdapter);

        actionBar.setDisplayHomeAsUpEnabled(true);

        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);

        TextView userName = (TextView)findViewById(R.id.user_chat_name);
        final TextView user_last_seen = (TextView)findViewById(R.id.chat_status);
        CircleImageView chat_user_image = (CircleImageView)findViewById(R.id.custom_user_image);


        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final boolean userOnline = (boolean) dataSnapshot.child("online").getValue();
                last_seen = (Long) dataSnapshot.child("last_online").getValue();
                //Toast.makeText(ChatActivity.this,last_seen+" "+userOnline,Toast.LENGTH_SHORT).show();
                if(userOnline == true){
                    user_last_seen.setText("online");
                }else{
                    user_last_seen.setText(GetTimeAgo.getTimeAgo(last_seen,ChatActivity.this));
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        userName.setText(user_name);

        if(!user_image.equals(null)) {
            Picasso.with(this).load(String.valueOf(user_image)).placeholder(R.drawable.proimg).into(chat_user_image);
        }


        //working with send message
        mRootref.child("chats").child(user_current_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(user_id)){

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timeStamp", ServerValue.TIMESTAMP);


                    Map chatUserMap = new HashMap();

                    chatUserMap.put("chats/" + user_current_id + "/" + user_id,chatAddMap);
                    chatUserMap.put("chats/" + user_id + "/" + user_current_id,chatAddMap);

                    mRootref.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){

                                Log.d("chat application:",databaseError.getMessage().toString());

                            }
                        }
                    });

                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mChatSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage();
            }
        });

    /*    mChatAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent gellaryIntent = new Intent();
                gellaryIntent.setType("image*//*");
                gellaryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gellaryIntent,"select image"),GELLARY_PICK);


            }
        });*/


        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;
                //message_list.clear();

                itemPosition = 0;

                loadMoreMessages();

            }
        });

    }

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final String curernt_user_ref = "messages/" + user_current_id + "/" + user_id;
        final String chat_user_ref = "messages/" + user_id + "/" + user_current_id;
        DatabaseReference user_message_push = mRootref.child("messages").child(user_current_id)
                .child(user_id).push();
        final String push_id = user_message_push.getKey();



        if (requestCode == GELLARY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);// start cropping activity for pre-acquired image saved on the device

        }

            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {

                    Uri resultUri = result.getUri();

                    //get the file
                    File thumb_file = new File(resultUri.getPath());
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    //set the file into compress to bitmap and decreased the quality of that bitmap file
                    try {
                        Bitmap thumb_bitmap = new Compressor(ChatActivity.this)
                                .setMaxWidth(300)
                                .setMaxHeight(300)
                                .setQuality(50)
                                .compressToBitmap(thumb_file);

                        thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,50, baos);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    final byte[] byte_data = baos.toByteArray();


                    StorageReference filepath = mImageStorage.child("message_images").child(user_id + ".jpg");
                    final StorageReference thumb_filepath = mImageStorage.child("message_images").child("thumb").child(random() + ".jpg");


                    thumb_filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if (task.isSuccessful()) {

                                UploadTask uploadTask = thumb_filepath.putBytes(byte_data);

                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                        final String downloadUrl = task.getResult().getDownloadUrl().toString();

                                        Map messageMap = new HashMap();
                                        messageMap.put("message", downloadUrl);
                                        messageMap.put("seen", false);
                                        messageMap.put("type", "image");
                                        messageMap.put("time", ServerValue.TIMESTAMP);
                                        messageMap.put("from", user_current_id);

                                        Map messageUserMap = new HashMap();
                                        messageUserMap.put(curernt_user_ref + "/" + push_id, messageMap);
                                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                                        mChatEt.setText(null);

                                        mRootref.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                                finish();
                                                startActivity(getIntent());

                                                if (databaseError != null) {

                                                    Log.d("chat application:", databaseError.getMessage().toString());

                                                }
                                            }
                                        });


                                    }
                                });
                            }
                        }
                    });

                }
            }

        }*/

    private void loadMoreMessages() {


        DatabaseReference messageRef =  mRootref.child("messages").child(user_current_id)
                .child(user_id);

        messageRef.keepSynced(true);

        Query message_query = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);
        message_query.keepSynced(true);

        message_query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Message message = dataSnapshot.getValue(Message.class);
                String message_key = dataSnapshot.getKey();
                if(!prevKey.equals(message_key)) {
                    if(!message.equals(null)){
                        message_list.add(itemPosition++, message);
                    }

                }else{
                    prevKey = mLastKey;
                }
                if(itemPosition == 1){
                    mLastKey = message_key;
                }

                mAdapter.notifyDataSetChanged();

                //chat_recyclerview.scrollToPosition(message_list.size() - 1);

                mRefreshLayout.setRefreshing(false);

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
        });

    }

    private void loadMessages() {

        DatabaseReference messageRef =  mRootref.child("messages").child(user_current_id)
                .child(user_id);

        Query message_query = messageRef.limitToLast(mCurrentPage * TOTAL_ITEM_TO_LOAD);
        messageRef.keepSynced(true);
        message_query.keepSynced(true);
        mRootref.keepSynced(true);


        message_query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Message message = dataSnapshot.getValue(Message.class);

                itemPosition++;

                if(itemPosition == 1){
                    String message_key = dataSnapshot.getKey();
                    mLastKey = message_key;
                    prevKey = message_key;

                }
                if(!message.equals(null)){
                    message_list.add(message);
                }
                mAdapter.notifyDataSetChanged();

                mRefreshLayout.setRefreshing(false);
                layoutManager.scrollToPositionWithOffset(10,0);

                chat_recyclerview.scrollToPosition(message_list.size() - 1);

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
        });



    }

    private void sendMessage() {

        String message = mChatEt.getText().toString();

        if(!TextUtils.isEmpty(message)){

            String curernt_user_ref = "messages/" + user_current_id + "/" + user_id;
            String chat_user_ref = "messages/" + user_id + "/" +user_current_id;

            DatabaseReference user_message_push = mRootref.child("messages").child(user_current_id)
                    .child(user_id).push();
            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",user_current_id);

            Map messageUserMap = new HashMap();
            messageUserMap.put(curernt_user_ref + "/" + push_id,messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id,messageMap);


            mRootref.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError != null){

                        Log.d("chat application:",databaseError.getMessage().toString());

                    }

                }
            });

            mChatEt.setText(null);
        }

    }


    @Override
    protected void onStop() {
        super.onStop();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        userRef.child("online").onDisconnect().setValue(false);

    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(50);
        int tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (int) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

}
