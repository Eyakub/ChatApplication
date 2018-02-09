package com.example.user.chatapplication.adapter;

import android.graphics.Color;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.user.chatapplication.R;
import com.example.user.chatapplication.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;


public class MessagingAdapter extends RecyclerView.Adapter<MessagingAdapter.MessageViewHolder>{

    private List<Message> mMessageList = new ArrayList<>();
    private FirebaseAuth mAuth;
    private String current_user_id;
    private String from_user,cUser;
    private String mesage_type;
    private String chat_user_image;

    public MessagingAdapter(List<Message> list,String cUser,String user_pic){
        mMessageList = list;
        this.cUser = cUser;
        chat_user_image = user_pic;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);

        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {


        Message message = mMessageList.get(position);

        from_user = message.getFrom();
        mesage_type = message.getType();

        if(mesage_type.equals("text")){

            holder.message_text.setText(message.getMessage());
           // holder.messageImage.setVisibility(View.INVISIBLE);

        }else{

            holder.message_text.setVisibility(View.INVISIBLE);
            /*Picasso.with(holder.circle_image.getContext()).load(message.getMessage())
                    .into(holder.messageImage);*/

        }

        if(!from_user.equals(current_user_id)){

            holder.message_text.setBackgroundColor(Color.WHITE);
            holder.message_text.setTextColor(Color.BLACK);
            holder.circle_image.setVisibility(View.VISIBLE);
            holder.relativeLayout.setGravity(Gravity.LEFT | Gravity.START);

            Picasso.with(holder.circle_image.getContext()).load(chat_user_image)
                    .into(holder.circle_image);

        }else {

            holder.message_text.setBackgroundResource(R.drawable.message_text_background);
            holder.message_text.setTextColor(Color.WHITE);
            holder.circle_image.setVisibility(View.INVISIBLE);
            holder.relativeLayout.setGravity(Gravity.RIGHT | Gravity.END);

        }

        holder.message_text.setText(message.getMessage());


    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder{


        public TextView message_text;
        public CircleImageView circle_image;
        public ImageView messageImage;
        public RelativeLayout relativeLayout;

        public MessageViewHolder(View v) {
            super(v);

            message_text = (TextView)v.findViewById(R.id.chat_user_message);
            circle_image = (CircleImageView) v.findViewById(R.id.chat_user_image);
           // messageImage = (ImageView)v.findViewById(R.id.chat_message_image);
            relativeLayout = (RelativeLayout)v.findViewById(R.id.message_single_layout);

        }
    }

}
