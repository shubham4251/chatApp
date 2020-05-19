package com.abd.whatsapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    public MessageAdapter(List<Messages> userMessagesList){
        this.userMessagesList = userMessagesList;
    }
    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView senderMessageText,receiverMessageText;
        public ImageView receiverProfileImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);

        }
    }
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout,parent,false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
            String messageSenderId = mAuth.getCurrentUser().getUid();
            Messages  messages = userMessagesList.get(position);

            String fromUserId = messages.getFrom();
            String fromMessagesType = messages.getType();

            userRef =FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild("image")){
                        String receiverImage = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            if(fromMessagesType.equals("text")){
                holder.receiverMessageText.setVisibility(View.INVISIBLE);
                holder.receiverProfileImage.setVisibility(View.INVISIBLE);
                holder.senderMessageText.setVisibility(View.INVISIBLE);
                if(fromUserId.equals(messageSenderId)){
                    holder.senderMessageText.setVisibility(View.VISIBLE);

                    holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                    holder.senderMessageText.setText(messages.getMessage());
                }
                else {
                    holder.receiverProfileImage.setVisibility(View.VISIBLE);
                    holder.receiverMessageText.setVisibility(View.VISIBLE);

                    holder.receiverMessageText.setBackgroundResource(R.drawable.receivers_message_layout);
                    holder.receiverMessageText.setText(messages.getMessage());

                }
            }

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }


}
