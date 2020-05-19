package com.abd.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class SettingActivity extends AppCompatActivity {
    private Button updateAccountSetting;
    private EditText userName,userStatus;
    private ImageView userProfileImage;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private  static final int galleryPick=1;
    private ProgressDialog loadingBar;

    private StorageReference userProfileImagesRef;
    private String profileUrl;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        InitializeFields();

        userName.setVisibility(View.INVISIBLE);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        updateAccountSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSetting();
            }
        });
        retrieveData();
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,galleryPick);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==galleryPick && resultCode==RESULT_OK && data !=null)
        {
            Uri imageUri =data.getData();
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode==RESULT_OK){
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please Wait your profile is updating");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();


                Uri resultUri = result.getUri();

                final StorageReference filePath = userProfileImagesRef.child(currentUserId+".jpg");


                final UploadTask uploadTask = filePath.putFile(resultUri);


                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl =uri.toString();
                            String profileUrl = downloadUrl;

                                Log.d("sl","url: "+downloadUrl);

                                rootRef.child("Users").child(currentUserId).child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(SettingActivity.this, "image save in database successfully", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();

                                        }
                                        else{
                                            String message =task.getException().toString();
                                            Toast.makeText(SettingActivity.this, "Error:"+message, Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                    }
                                });

                    }
                        });
            }
            });



    }

        }

    }

    private void retrieveData(){
        rootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image")) && (dataSnapshot.hasChild("status"))){

                    String retrieveUserName =dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus =dataSnapshot.child("status").getValue().toString();
                    String retrieveProfileImage =dataSnapshot.child("image").getValue().toString();

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveStatus);
                    Picasso.get().load(retrieveProfileImage).into(userProfileImage);


                }
                else if((dataSnapshot.hasChild("name")) &&(dataSnapshot.hasChild("status"))){

                    String retrieveUserName =dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus =dataSnapshot.child("status").getValue().toString();
                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveStatus);

                }
                else{
                    userName.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingActivity.this,"Please set and update profile Setting",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

}
    private void updateSetting() {
        String setUserName = userName.getText().toString();
        String setUserStatus = userStatus.getText().toString();



        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this,"Please write user name...",Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(setUserStatus)){
            Toast.makeText(this,"Please write Status....",Toast.LENGTH_LONG).show();
        }
        else{
            HashMap<String,Object> profileMap = new HashMap<>();
            profileMap.put("uid",currentUserId);
            profileMap.put("name",setUserName);
            profileMap.put("status",setUserStatus);



            rootRef.child("Users").child(currentUserId).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        sendUserToMainActivity();
                        Toast.makeText(SettingActivity.this,"profile updated successfully",Toast.LENGTH_LONG).show();
                    }
                    else{
                        String message = task.getException().toString();
                        Toast.makeText(SettingActivity.this,"Error:"+message,Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void InitializeFields() {
        updateAccountSetting = findViewById(R.id.update_status_button);
        userName =findViewById(R.id.set_user_name);
        userStatus =findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.set_profile_image);
        loadingBar =new ProgressDialog(this);
        mToolbar = findViewById(R.id.setting_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Account Setting ");
    }
    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
}
