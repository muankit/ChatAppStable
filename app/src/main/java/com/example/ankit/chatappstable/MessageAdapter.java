package com.example.ankit.chatappstable;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Ankit on 09-Mar-18.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;

    public MessageAdapter( List<Messages> mMessageList){
        this.mMessageList = mMessageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent , int viewType){
        View  v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout , parent ,false);
        return new MessageViewHolder(v);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView  messageText;
        public CircleImageView profileImage;

        public MessageViewHolder(View view){
            super(view);

            messageText = (TextView) view.findViewById(R.id.name_text_layout);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_layout);

        }
    }

    @Override
    public void onBindViewHolder(MessageViewHolder viewHolder , int i) {

            mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            String current_user_id = mAuth.getCurrentUser().getUid();

            Messages c = mMessageList.get(i);

            String from_user = c.getFrom();

            if (from_user.equals(current_user_id)) {

                viewHolder.messageText.setBackgroundColor(Color.WHITE);
                viewHolder.messageText.setBackgroundColor(Color.WHITE);
            } else {
                viewHolder.messageText.setBackgroundResource(R.drawable.message_text_background);
                viewHolder.messageText.setTextColor(Color.WHITE);
            }

            viewHolder.messageText.setText(c.getMessage());
        }
    }

    @Override
    public int getItemCount(){
        return mMessageList.size();
    }
}
