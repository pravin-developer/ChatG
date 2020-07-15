package com.pravin.chatg;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
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
import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {
    private Button UpdateAccountSettings;
    private EditText userName,userStatus;
    private CircleImageView userProfileImage;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private static final int GalleryPic=1;
    private StorageReference UserProfileImagesRef;
    private ProgressDialog loadingBar;
    private Toolbar SettingsToolBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();
        UserProfileImagesRef= FirebaseStorage.getInstance().getReference().child("Profile Images");
        InitializeFields();
        userName.setVisibility(View.INVISIBLE);
        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }

        });
        RetrieveUserInfo();
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/'");
                startActivityForResult(galleryIntent,GalleryPic);
            }
        });
    }


    private void InitializeFields()
    {
        UpdateAccountSettings=(Button)findViewById(R.id.update_settings_Button);
        userName=(EditText)findViewById(R.id.set_user_name);
        userStatus=(EditText)findViewById(R.id.set_profile_status);
        userProfileImage=(CircleImageView)findViewById(R.id.set_profile_image);
        loadingBar=new ProgressDialog(this);
        SettingsToolBar=(Toolbar)findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GalleryPic && resultCode==RESULT_OK && data!=null)
        {
            Uri ImageUri=data.getData();


            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .setMinCropWindowSize(500, 500)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("set Profile Image");
                loadingBar.setMessage("your profile image is uploading please wait");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();


                Uri resultUri = result.getUri();
                StorageReference filePath=UserProfileImagesRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        final Task<Uri>firebaseUri=taskSnapshot.getStorage().getDownloadUrl();
                        firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl=uri.toString();
                                RootRef.child("Users").child(currentUserID).child("image")
                                        .setValue(downloadUrl)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if(task.isSuccessful())
                                                {
                                                    Toast.makeText(SettingActivity.this, "Image saved into database Successfully" , Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();


                                                }
                                                else
                                                    {
                                                        String message=task.getException().toString();
                                                        Toast.makeText(SettingActivity.this, "Error "+ message , Toast.LENGTH_SHORT).show();
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

    private void UpdateSettings() {
        String setUsername = userName.getText().toString();
        String setStatus = userStatus.getText().toString();
        if (TextUtils.isEmpty(setUsername)) {
            Toast.makeText(SettingActivity.this, "please enter user name", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setStatus)) {
            Toast.makeText(SettingActivity.this, "please write ur status", Toast.LENGTH_SHORT).show();

        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", setUsername);
            profileMap.put("Status", setStatus);
            RootRef.child("Users").child(currentUserID).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                SendUserToMainActivity();
                                Toast.makeText(SettingActivity.this, "profile updated sucessfully", Toast.LENGTH_SHORT).show();
                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(SettingActivity.this, "error" + message, Toast.LENGTH_SHORT).show();

                            }

                        }
                    });
        }

    }
    private void RetrieveUserInfo()
    {
        RootRef.child("Users").child(currentUserID)
        .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))) {

                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrievesStatus = dataSnapshot.child("Status").getValue().toString();
                    String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();
                    userName.setText(retrieveUserName);
                    userStatus.setText(retrievesStatus);

                    Picasso.get().load(retrieveProfileImage).resize(100,100).centerCrop().into(userProfileImage);




                } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrievesStatus = dataSnapshot.child("Status").getValue().toString();
                    userName.setText(retrieveUserName);
                    userStatus.setText(retrievesStatus);




                } else {
                    userName.setVisibility(View.VISIBLE);

                    Toast.makeText(SettingActivity.this, "please update your profile info", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }



            private void SendUserToMainActivity()
        {
                Intent MainIntent = new Intent(SettingActivity.this,MainActivity.class);
                MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(MainIntent);
                finish();
        }


}