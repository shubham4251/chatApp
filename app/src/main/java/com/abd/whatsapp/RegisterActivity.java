package com.abd.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import javax.xml.transform.Templates;

public class RegisterActivity extends AppCompatActivity {
    private Button createAccountButton;
    private EditText userEmail,userPassword;
    private TextView alreadyHaveAccount;

    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        rootRef  =FirebaseDatabase.getInstance().getReference();

        InitializeFields();
        alreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLoginActivity();

            }
        });
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });
    }

    private void createNewAccount() {
        String email = userEmail.getText().toString().trim();
        String password = userPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(RegisterActivity.this, "Please enter email..", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(RegisterActivity.this, "Please enter password..", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please Wait while we are creating account for you...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){


                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            String currentUserId = mAuth.getCurrentUser().getUid();
                            rootRef.child("Users").child(currentUserId).setValue("");

                            rootRef.child("Users").child("device_token").setValue(deviceToken);
                            sendUserToMainActivity();
                            Toast.makeText(RegisterActivity.this,"Account created successfully",Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();
                        }
                        else{
                            String message = task.getException().toString();
                            Toast.makeText(RegisterActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
        }

    }

    private void InitializeFields() {
        createAccountButton = findViewById(R.id.register_button);


        userEmail = findViewById(R.id.register_email);
        userPassword=findViewById(R.id.register_password);

        alreadyHaveAccount = findViewById(R.id.already_have_account_link);
        loadingBar = new ProgressDialog(this);

    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginIntent);

    }
    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
}
