package com.example.user.chatapplication.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.chatapplication.ProfileActivity;
import com.example.user.chatapplication.R;
import com.example.user.chatapplication.model.FriendRequest;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestFragment extends Fragment {


    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String mCurrentUser;
    private RecyclerView mReqRecycler;
    private LinearLayoutManager layoutManager;


    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_request, container, false);

        mReqRecycler = (RecyclerView)v.findViewById(R.id.request_recycler_view);
        layoutManager = new LinearLayoutManager(getContext());
        mReqRecycler.setLayoutManager(layoutManager);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mCurrentUser = mUser.getUid();

        mRootRef = FirebaseDatabase.getInstance().getReference();


        return v;
    }

    @Override
    public void onStart() {
        super.onStart();


        DatabaseReference requestRef = mRootRef.child("friend_req").child(mCurrentUser);
        final DatabaseReference userRef = mRootRef.child("users");

        FirebaseRecyclerAdapter<FriendRequest, RequestViewHolder> recyclerAdapter =
                new FirebaseRecyclerAdapter<FriendRequest, RequestViewHolder>(

                        FriendRequest.class,
                        R.layout.req_single_layout,
                        RequestViewHolder.class,
                        requestRef
                ) {
                    @Override
                    protected void populateViewHolder(final RequestViewHolder viewHolder, FriendRequest model, int position) {


                        final String chat_user_id = getRef(position).getKey();

                        userRef.child(chat_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                String name = (String) dataSnapshot.child("name").getValue();
                                String status = (String)dataSnapshot.child("status").getValue();
                                String url_img = (String)dataSnapshot.child("thumb_image").getValue();


                                //Toast.makeText(getActivity(),name +"/n"+ status +"/n"+url_img,Toast.LENGTH_SHORT).show();

                                viewHolder.setData(name,status,url_img,getContext());


                                viewHolder.btn_cancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        Map friendMap = new HashMap();
                                        friendMap.put("friend_req/" + mCurrentUser + "/" + chat_user_id,null);
                                        friendMap.put("friend_req/" + chat_user_id + "/" +mCurrentUser,null);


                                        mRootRef.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                                if(databaseError == null) {

                                                    Log.d("Unfriend", "successful");

                                                }else{
                                                    Log.d("Unfriend", "unsuccessful");
                                                }
                                            }
                                        });


                                    }
                                });

                                viewHolder.btn_accept.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        final String currentDate;

                                        java.text.DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                                        currentDate = dateFormat.format(Calendar.getInstance().getTime());


                                        Map friendMap = new HashMap();
                                        friendMap.put("friends/" + mCurrentUser + "/" + chat_user_id +"/date",currentDate);
                                        friendMap.put("friends/" + chat_user_id + "/" + mCurrentUser +"/date",currentDate);

                                        friendMap.put("friend_req/" + mCurrentUser + "/" + chat_user_id,null);
                                        friendMap.put("friend_req/" + chat_user_id + "/" +mCurrentUser,null);


                                        mRootRef.updateChildren(friendMap, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                                if(databaseError == null) {

                                                    Log.d("Unfriend", "successful");

                                                }else{
                                                    Log.d("Unfriend", "unsuccessful");
                                                }
                                            }
                                        });



                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });



                    }
                };

        mReqRecycler.setAdapter(recyclerAdapter);
    }
    public static class RequestViewHolder extends RecyclerView.ViewHolder{

        public View mView;
        private  TextView txtName;
        private TextView txtStatus;
        private CircleImageView profileImage;
        private ImageButton btn_accept,btn_cancel;

        public RequestViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            txtName = (TextView)mView.findViewById(R.id.user_single_name);
            txtStatus = (TextView)mView.findViewById(R.id.user_single_status);
            profileImage = (CircleImageView)mView.findViewById(R.id.user_single_image);
            btn_accept = (ImageButton)mView.findViewById(R.id.accept_req);
            btn_cancel = (ImageButton)mView.findViewById(R.id.cancel_req);

        }

        public void setData(String name, String status, String url_img, Context ctx){


            txtName.setText(name);
            txtStatus.setText(status);

            if(!url_img.equals(null)) {
                Picasso.with(ctx).load(url_img).placeholder(R.drawable.proimg).into(profileImage);
            }
        }

    }

}
