package com.abd.whatsapp;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
public class RequestsFragment extends Fragment {
    private View requestContactView;
    private RecyclerView myRequestList;

    private DatabaseReference chatRequestRef,userRef,contactRef;
    private FirebaseAuth mAuth;
    private String currentUserId;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestContactView = inflater.inflate(R.layout.fragment_requests, container, false);
        myRequestList = requestContactView.findViewById(R.id.contact_request_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        mAuth  = FirebaseAuth.getInstance();
        currentUserId  = mAuth.getCurrentUser().getUid();






        return requestContactView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRequestRef.child(currentUserId),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,chatRequestViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, chatRequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final chatRequestViewHolder chatRequestViewHolder, int i, @NonNull Contacts contacts) {
                chatRequestViewHolder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                chatRequestViewHolder.itemView.findViewById(R.id.request_decline_btn).setVisibility(View.VISIBLE);

                final String chat_request_list = getRef(i).getKey();
                Toast.makeText(getContext(),  chat_request_list, Toast.LENGTH_LONG).show();
                DatabaseReference getTypeRef = getRef(i).child("request_type").getRef();
                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Toast.makeText(getContext(), "onDataChanged13", Toast.LENGTH_SHORT).show();
                        if(dataSnapshot.exists()){
                            String type = dataSnapshot.getValue().toString();
                            Toast.makeText(getContext(), "type: "+type, Toast.LENGTH_LONG).show();
                            System.out.println("type: "+type);
                            if(type.equals("received")){

                                userRef.child(chat_request_list).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild("image")){

                                            final String requestUserImage = dataSnapshot.child("image").getValue().toString();
                                            Picasso.get().load(requestUserImage).placeholder(R.drawable.profile_image).into(chatRequestViewHolder.profileImage);
                                        }

                                        final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                        final String requestUserStatus= dataSnapshot.child("status").getValue().toString();
                                        chatRequestViewHolder.userName.setText(requestUserName);
                                        chatRequestViewHolder.userStatus.setText("wants to connect with you");

                                        chatRequestViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence option [] = new CharSequence[]{
                                                        "Accept",
                                                        "Decline"
                                                };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle(requestUserName+ "  Chat Request");
                                                builder.setItems(option, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        if(which==0){
                                                            contactRef.child(currentUserId).child(chat_request_list).child("Contacts")
                                                                    .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
                                                                            contactRef.child(chat_request_list).child(currentUserId).child("Contacts")
                                                                                    .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        chatRequestRef.child(currentUserId).child(chat_request_list)
                                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if(task.isSuccessful()){
                                                                                                        chatRequestRef.child(chat_request_list).child(currentUserId)
                                                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                if(task.isSuccessful()){
                                                                                                                    Toast.makeText(getContext(), "New Contact Saved", Toast.LENGTH_SHORT).show();

                                                                                                                }
                                                                                                            }
                                                                                                        });
                                                                                                    }
                                                                                            }
                                                                                        });

                                                                                    }
                                                                                }
                                                                            });

                                                                        }
                                                                }
                                                            });


                                                        }
                                                        if(which==1){
                                                            chatRequestRef.child(currentUserId).child(chat_request_list)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        chatRequestRef.child(chat_request_list).child(currentUserId)
                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){
                                                                                    Toast.makeText(getContext(), "Contact Declined", Toast.LENGTH_SHORT).show();

                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });



                                                        }

                                                    }
                                                });
                                                builder.show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    System.out.println("databaseError"+databaseError);
                                    }
                                });

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @NonNull
            @Override
            public chatRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout,parent,false);
                chatRequestViewHolder holder = new chatRequestViewHolder(view);
                return holder;
            }
        };
        myRequestList.setAdapter(adapter);
        adapter.startListening();
    }
    public static class chatRequestViewHolder extends RecyclerView.ViewHolder{
        TextView userName,userStatus;
        ImageView profileImage;
        Button acceptButton,declineButton;
        public chatRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            acceptButton = itemView.findViewById(R.id.request_accept_btn);
            declineButton = itemView.findViewById(R.id.request_decline_btn);
        }
    }
}
