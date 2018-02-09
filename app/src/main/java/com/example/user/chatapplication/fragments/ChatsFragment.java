package com.example.user.chatapplication.fragments;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.chatapplication.ChatActivity;
import com.example.user.chatapplication.R;
import com.example.user.chatapplication.model.Chats;
import com.example.user.chatapplication.model.GetTimeAgo;
import com.example.user.chatapplication.model.Message;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {


    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;
    private String mCurrentUser;
    private RecyclerView chat_list_recycler;
    private LinearLayoutManager layoutManager;
    private int itemPosition = 1;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        chat_list_recycler = (RecyclerView)view.findViewById(R.id.chat_list_recyclerview);
        layoutManager = new LinearLayoutManager(getContext());
        chat_list_recycler.setLayoutManager(layoutManager);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        mCurrentUser = mUser.getUid();

        mRootRef = FirebaseDatabase.getInstance().getReference();


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        final DatabaseReference chatRef = mRootRef.child("chats").child(mCurrentUser);
        final DatabaseReference userRef = mRootRef.child("users");
        final DatabaseReference messageRef= mRootRef.child("messages");

        FirebaseRecyclerAdapter<Chats,ChatListHolder> chatsRecyclerAdapter  = new FirebaseRecyclerAdapter<Chats, ChatListHolder>(

                Chats.class,
                R.layout.users_row_layout,
                ChatListHolder.class,
                chatRef
        ) {
            @Override
            protected void populateViewHolder(final ChatListHolder viewHolder, Chats model, int position) {

                final String chat_user_id = getRef(position).getKey();

                userRef.child(chat_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String chat_user_name = (String) dataSnapshot.child("name").getValue();
                        final String profile_image = (String) dataSnapshot.child("thumb_image").getValue();
                        final boolean isOnline = (boolean) dataSnapshot.child("online").getValue();


                        final Query lastMessageQuery = messageRef.child(mCurrentUser).child(chat_user_id).limitToLast(1);



                        lastMessageQuery.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                final String push_id = dataSnapshot.getRef().getKey();

                                String last_message= (String) dataSnapshot.child("message").getValue();
                                String type = (String) dataSnapshot.child("type").getValue();
                                final String from = (String) dataSnapshot.child("from").getValue();
                                Long last_seen = (Long) dataSnapshot.child("seen_time").getValue();
                                final boolean seen = (boolean)dataSnapshot.child("seen").getValue();
                                if(!type.equals("text")){
                                    last_message = chat_user_name + " received a file";
                                }if(seen == true && from.equals(mCurrentUser)){

                                    String seen_time = GetTimeAgo.getTimeAgo(last_seen,getContext());

                                    if(seen_time != null){

                                        last_message = last_message +"\n\nseen "+seen_time;

                                    }else{

                                        last_message = last_message +"\n\nseen ";

                                    }

                                }

                                viewHolder.setData(chat_user_name,profile_image,isOnline,last_message,seen,getContext());

                                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        if(seen == false && !from.equals(mCurrentUser)) {

                                            messageRef.child(chat_user_id).child(mCurrentUser).child(push_id).child("seen").setValue(true);
                                            messageRef.child(chat_user_id).child(mCurrentUser).child(push_id)
                                                    .child("seen_time").setValue(ServerValue.TIMESTAMP);

                                            messageRef.child(mCurrentUser).child(chat_user_id).child(push_id).child("seen").setValue(true);
                                            messageRef.child(mCurrentUser).child(chat_user_id).child(push_id)
                                                    .child("seen_time").setValue(ServerValue.TIMESTAMP);
                                        }

                                        Intent intent = new Intent(getContext(),ChatActivity.class);
                                        intent.putExtra("userID",chat_user_id);
                                        intent.putExtra("userName",chat_user_name);
                                        intent.putExtra("userImage",profile_image);
                                        intent.putExtra("isOnline",isOnline);
                                        startActivity(intent);

                                    }
                                });


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

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



            }
        };


        chat_list_recycler.setAdapter(chatsRecyclerAdapter);

    }


    public static class ChatListHolder extends RecyclerView.ViewHolder{
        View mView;
        private TextView txt_name,txt_message;
        private CircleImageView profile_image;
        private ImageView online_image;

        public ChatListHolder(View v) {
            super(v);

            mView = v;
        }

        public void setData(String name,String image_url,boolean online,String message,boolean seen,Context ctx){

            txt_name = (TextView) mView.findViewById(R.id.user_single_name);
            txt_message = (TextView) mView.findViewById(R.id.user_single_status);
            profile_image = (CircleImageView)mView.findViewById(R.id.user_single_image);
            online_image = (ImageView)mView.findViewById(R.id.active);


            txt_name.setText(name);
            txt_message.setText(message);
            if(seen == true){
                txt_message.setTextColor(Color.GRAY);
            }

            if(online == true){
                online_image.setVisibility(View.VISIBLE);
            }else{
                online_image.setVisibility(View.INVISIBLE);
            }

            if(!image_url.equals(null)) {
                Picasso.with(ctx).load(image_url).placeholder(R.drawable.proimg).into(profile_image);
            }

        }


    }

}
