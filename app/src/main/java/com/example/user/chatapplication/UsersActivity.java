package com.example.user.chatapplication;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.chatapplication.model.Users;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToobar;
    private RecyclerView mRecyclerUsers;
    private DatabaseReference mDatabasereference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToobar = (Toolbar)findViewById(R.id.users_app_bar);
        setSupportActionBar(mToobar);
        getSupportActionBar().setTitle("User List");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabasereference = FirebaseDatabase.getInstance().getReference().child("users");

        mRecyclerUsers = (RecyclerView)findViewById(R.id.users_recyclerview);
        mRecyclerUsers.setHasFixedSize(true);
        mRecyclerUsers.setLayoutManager(new LinearLayoutManager(this));


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Users,UserViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UserViewHolder>(

                Users.class,
                R.layout.users_row_layout,
                UserViewHolder.class,
                mDatabasereference

        ) {
            @Override
            protected void populateViewHolder(UserViewHolder viewHolder, final Users model, int position) {

                viewHolder.setData(model.getName(),model.getStatus(),model.getImage(),getApplicationContext());

                final String user_id = getRef(position).getKey();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(UsersActivity.this,ProfileActivity.class);
                        intent.putExtra("userID",user_id);
                        startActivity(intent);

                        //Toast.makeText(UsersActivity.this,user_id,Toast.LENGTH_SHORT).show();
                    }
                });

            }
        };
        mRecyclerUsers.setAdapter(firebaseRecyclerAdapter);

    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public UserViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setData(String name, String status, String thumb_url, final Context context){
            final TextView txtName = (TextView) mView.findViewById(R.id.user_single_name);
            TextView txtStatus = (TextView) mView.findViewById(R.id.user_single_status);
            CircleImageView profileImage = (CircleImageView) mView.findViewById(R.id.user_single_image);

            txtName.setText(name);
            txtStatus.setText(status);
            if(!thumb_url.equals(null)) {
                Picasso.with(context).load(thumb_url).placeholder(R.drawable.proimg).into(profileImage);
            }
        }

    }

}
