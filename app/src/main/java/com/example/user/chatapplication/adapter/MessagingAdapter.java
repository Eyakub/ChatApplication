package com.example.user.chatapplication.adapter;

import android.graphics.Color;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.user.chatapplication.R;
import com.example.user.chatapplication.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by User on 03/02/2018.
 */

public class MessagingAdapter extends RecyclerView.Adapter<MessagingAdapter.MessageViewHolder>{

    private List<Message> mMessageList = new ArrayList<>();
    private FirebaseAuth mAuth;

    public MessagingAdapter(List<Message> list){
        mMessageList = list;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);

        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();

        Message message = mMessageList.get(position);

        String from_user = message.getFrom();

        String fromUser = message.getFrom();
        String mesage_type = message.getType();

        if(from_user.equals(current_user_id)){

            holder.message_text.setBackgroundColor(Color.WHITE);
            holder.message_text.setTextColor(Color.BLACK);

        }else {

            holder.message_text.setBackgroundResource(R.drawable.message_text_background);
            holder.message_text.setTextColor(Color.WHITE);

        }

        holder.message_text.setText(message.getMessage());

       if(mesage_type.equals("text")){
           holder.message_text.setText(message.getMessage());
           holder.messageImage.setVisibility(View.INVISIBLE);
       }else{

           holder.message_text.setVisibility(View.INVISIBLE);

           Picasso.with(holder.circle_image.getContext()).load(message.getMessage())
                   .into(holder.messageImage);

       }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder{


        public TextView message_text;
        public CircleImageView circle_image;
        public ImageView messageImage;

        public MessageViewHolder(View v) {
            super(v);

            message_text = (TextView)v.findViewById(R.id.chat_user_message);
            circle_image = (CircleImageView) v.findViewById(R.id.chat_user_image);
            messageImage = (ImageView)v.findViewById(R.id.chat_message_image);

        }
    }

}
