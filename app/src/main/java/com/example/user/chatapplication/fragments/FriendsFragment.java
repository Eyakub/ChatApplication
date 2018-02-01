package com.example.user.chatapplication.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.chatapplication.ProfileActivity;
import com.example.user.chatapplication.R;
import com.example.user.chatapplication.model.Friends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsFragment extends Fragment {

    private RecyclerView friend_recycler;
    private DatabaseReference mFriendRef;
    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth;
    private View mView;
    private String user_id;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_friends, container, false);
        friend_recycler = (RecyclerView) mView.findViewById(R.id.friend_recycler_view);

        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();

        mFriendRef = FirebaseDatabase.getInstance().getReference().child("friends").child(user_id);
        mUserRef = FirebaseDatabase.getInstance().getReference().child("users");
        mFriendRef.keepSynced(true);


        friend_recycler.setHasFixedSize(true);
        friend_recycler.setLayoutManager(new LinearLayoutManager(getActivity()));


        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends,FriendHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendHolder>(

                Friends.class,
                R.layout.users_row_layout,
                FriendHolder.class,
                mFriendRef

        ) {
            @Override
            protected void populateViewHolder(final FriendHolder viewHolder, final Friends model, int position) {

                String list_user_id = getRef(position).getKey();

                mUserRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String userName = dataSnapshot.child("name").getValue().toString();
                        String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                        viewHolder.setData(userName,model.getDate(),thumb_image,getContext());

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                Toast.makeText(getActivity(),list_user_id,Toast.LENGTH_SHORT).show();

            }
        };


        friend_recycler.setAdapter(firebaseRecyclerAdapter);


    }

    public static class FriendHolder extends RecyclerView.ViewHolder{


        View view;

        public FriendHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setData(String name, String date, String thumb_url, final Context context){
            final TextView txtName = (TextView) view.findViewById(R.id.user_single_name);
            TextView txtTime = (TextView) view.findViewById(R.id.user_single_status);
            CircleImageView profileImage = (CircleImageView) view.findViewById(R.id.user_single_image);

            txtName.setText(name);
            txtTime.setText(date);
            if(!thumb_url.equals(null)) {
                Picasso.with(context).load(thumb_url).placeholder(R.drawable.proimg).into(profileImage);
            }
        }

    }

}
