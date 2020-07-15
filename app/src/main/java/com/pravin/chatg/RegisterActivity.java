package com.pravin.chatg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {
    private Button CreateAccountButton;
    private EditText UserEmail,UserPassword;
    private TextView AlreadyHaveAccountLink;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private ProgressDialog LoadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth=FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();
        InitializeFields();
        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();

            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount()
    {
        String email=UserEmail.getText().toString();
        String password =UserPassword.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"please enter email..",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"please enter password..",Toast.LENGTH_SHORT).show();
        }
        else
        {
            LoadingBar.setTitle("Creating new account");
            LoadingBar.setMessage("Please wait,while we are creating your account");
            LoadingBar.setCanceledOnTouchOutside(true);
            LoadingBar.show();
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                String currentUserID = mAuth.getCurrentUser().getUid();
                                RootRef.child("Users").child(currentUserID).setValue("");
                                RootRef.child("Users").child(currentUserID).child("device_token")
                                        .setValue(deviceToken);

                                SendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this,"account created succesfully",Toast.LENGTH_SHORT).show();
                                LoadingBar.dismiss();
                            }
                            else{
                                String message=task.getException().toString();
                                Toast.makeText(RegisterActivity.this,"error:"+message,Toast.LENGTH_SHORT).show();
                                LoadingBar.dismiss();

                            }

                        }
                    });

        }
    }

    private void InitializeFields() {
        CreateAccountButton=(Button)findViewById(R.id.register_button);
        UserEmail=(EditText)findViewById(R.id.register_email);
        UserPassword=(EditText)findViewById(R.id.register_password);
        AlreadyHaveAccountLink=(TextView)findViewById(R.id.already_new_account_link);
        LoadingBar=new ProgressDialog(this);

    }

    private void SendUserToLoginActivity() {
        Intent LoginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(LoginIntent);
    }
    private void SendUserToMainActivity() {
        Intent MainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }
}
