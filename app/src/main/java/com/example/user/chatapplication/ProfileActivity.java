package com.example.user.chatapplication;

import android.app.ProgressDialog;
import android.icu.text.DateFormat;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.icu.text.DateFormat.getDateTimeInstance;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileName,profileFriends,profileStatus;
    private ImageView profileImage;
    private Button friendRequestBtn,friendRequestCancelBtn;
    private String user_key;
    private DatabaseReference mDatabaseRef;
    private ProgressDialog mProgress;
    private String mCurrentState;
    private DatabaseReference mFriendRequestdatabase;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationdatabase;
    private DatabaseReference mRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        profileName = (TextView)findViewById(R.id.profile_user_name);
        profileStatus = (TextView)findViewById(R.id.profile_status);
        profileFriends = (TextView)findViewById(R.id.profile_user_friends);
        profileImage = (ImageView)findViewById(R.id.user_profile_image);
        friendRequestBtn = (Button)findViewById(R.id.profile_friend_request);
        friendRequestCancelBtn = (Button)findViewById(R.id.profile_friend_request_decline);
        friendRequestCancelBtn.setVisibility(View.GONE);

        mCurrentState = "not_friend";

        mProgress = new ProgressDialog(this);
        mProgress.setTitle("loading..");
        mProgress.setMessage("wait a while");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        user_key = getIntent().getStringExtra("userID");

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(user_key);
        mFriendRequestdatabase = FirebaseDatabase.getInstance().getReference().child("friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("friends");
        mNotificationdatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                profileName.setText(dataSnapshot.child("name").getValue().toString());
                profileStatus.setText(dataSnapshot.child("status").getValue().toString());

                Picasso.with(ProfileActivity.this).load(dataSnapshot.child("image").getValue().toString()).
                        placeholder(R.drawable.proimg).into(profileImage);


                //friend list/request feature

                mFriendRequestdatabase.child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_key)) {

                            String req_type = dataSnapshot.child(user_key).child("request_type").getValue().toString();

                            if(req_type.equals("received")){

                                mCurrentState = "req_received";
                                friendRequestBtn.setText("accept frined request");
                                friendRequestCancelBtn.setVisibility(View.VISIBLE);

                            }else if(req_type.equals("sent")){
                                mCurrentState = "req_sent";
                                friendRequestBtn.setText("cancel friend request");
                                friendRequestCancelBtn.setVisibility(View.GONE);
                            }

                            mProgress.dismiss();

                        }else{

                            mFriendDatabase.child(mCurrentUser.getUid()).
                                    addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(user_key)) {

                                        mCurrentState = "friends";
                                        friendRequestBtn.setText("unfriend this person");
                                        friendRequestCancelBtn.setVisibility(View.GONE);
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    mProgress.dismiss();

                                }
                            });

                        }
                        mProgress.dismiss();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        friendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                friendRequestBtn.setEnabled(false);

                //not friend state

                if(mCurrentState.equals("not_friend")){

                    DatabaseReference newNotificationRef = mRootRef.child("notifications").child(user_key).push();
                    String notificationId = newNotificationRef.getKey();

                    HashMap<String,String> notificationData = new HashMap<String, String>();
                    notificationData.put("from",mCurrentUser.getUid());
                    notificationData.put("type","request");

                    Map requestMap = new HashMap();
                    requestMap.put("friend_req/" + mCurrentUser.getUid() +"/"+ user_key +"/request_type","sent");
                    requestMap.put("friend_req/" +user_key +"/"+ mCurrentUser.getUid()+ "/request_type","received");
                    requestMap.put("notifications/" + user_key + "/" + notificationId,notificationData);


                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                            if(databaseError != null){
                                Toast.makeText(ProfileActivity.this,"Failed",Toast.LENGTH_SHORT).show();
                            }

                            friendRequestBtn.setEnabled(true);
                            mCurrentState = "req_sent";
                            friendRequestBtn.setText("cancel friend request");

                        }
                    });

                }

                //unfriend user

                if(mCurrentState.equals("friends")){

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("friends/" + mCurrentUser.getUid() +"/" + user_key,null);
                    unfriendMap.put("friends/" + user_key + "/" + mCurrentUser.getUid(),null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){

                                mCurrentState = "not_friend";
                                friendRequestBtn.setText("sent friend request");

                                friendRequestCancelBtn.setVisibility(View.GONE);
                                friendRequestCancelBtn.setEnabled(false);

                            }else{

                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();

                            }

                            friendRequestBtn.setEnabled(true);

                        }
                    });

                }

                //cancel request state
                if(mCurrentState.equals("req_sent")){
                    mFriendRequestdatabase.child(mCurrentUser.getUid()).child(user_key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendRequestdatabase.child(user_key).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    friendRequestBtn.setEnabled(true);
                                    mCurrentState = "not_friend";
                                    friendRequestBtn.setText("sent friend request");

                                }
                            });
                        }
                    });
                }


                //req received state

                if(mCurrentState.equals("req_received")){

                    final String currentDate;

                    java.text.DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                    currentDate = dateFormat.format(Calendar.getInstance().getTime());

                    Map friendMap = new HashMap();
                    friendMap.put("friends/" + mCurrentUser.getUid() + "/" + user_key +"/date",currentDate);
                    friendMap.put("friends/" + user_key + "/" + mCurrentUser.getUid() +"/date",currentDate);

                    friendMap.put("friend_req/" + mCurrentUser.getUid() + "/" + user_key,null);
                    friendMap.put("friend_req/" + user_key + "/" + mCurrentUser.getUid(),null);


                    mRootRef.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){

                                friendRequestBtn.setEnabled(true);
                                mCurrentState = "friends";
                                friendRequestBtn.setText("unfriend this person");

                                friendRequestCancelBtn.setVisibility(View.GONE);
                                friendRequestCancelBtn.setEnabled(false);

                            }else{

                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();

                            }

                        }
                    });



                }
            }
        });
    }
}
