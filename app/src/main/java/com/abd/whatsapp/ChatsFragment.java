package com.abd.whatsapp;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {
    private View privateChatsView;
    private RecyclerView chatList;

    private DatabaseReference chatsRef,userRef;
    private FirebaseAuth mAuth;
    private String currentUserId;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatsView= inflater.inflate(R.layout.fragment_chats, container, false);

        chatList = privateChatsView.findViewById(R.id.chats_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));


        mAuth =FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        chatsRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return privateChatsView;

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsRef,Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts,chatsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, chatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final chatsViewHolder chatsViewHolder, int i, @NonNull Contacts contacts) {
                    final String usersIDs = getRef(i).getKey();
                final String[] retrieveImage = {"default_image"};

                    userRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                if(dataSnapshot.hasChild("image")){
                                    retrieveImage[0] = dataSnapshot.child("image").getValue().toString();
                                    Picasso.get().load(retrieveImage[0]).placeholder(R.drawable.profile_image).into(chatsViewHolder.profileImage);
                                }
                                final String retrieveName = dataSnapshot.child("name").getValue().toString();
                                final String retrieveStatus = dataSnapshot.child("status").getValue().toString();

                                chatsViewHolder.userName.setText(retrieveName);
                                chatsViewHolder.userStatus.setText("Last seen: "+"\n"+"Date "+" Time ");
                                chatsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                        chatIntent.putExtra("visit_user_id",usersIDs);
                                        chatIntent.putExtra("visit_user_name",retrieveName);
                                        chatIntent.putExtra("visit_user_image", retrieveImage[0]);
                                        startActivity(chatIntent);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

            }

            @NonNull
            @Override
            public chatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout,parent,false);
                return new chatsViewHolder(view);
            }
        };
        chatList.setAdapter(adapter);
        adapter.startListening();
    }
    public static class chatsViewHolder extends RecyclerView.ViewHolder{
        TextView userName,userStatus;
        ImageView profileImage;
        public chatsViewHolder(@NonNull View itemView) {
            super(itemView);


            userName =itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }
}
