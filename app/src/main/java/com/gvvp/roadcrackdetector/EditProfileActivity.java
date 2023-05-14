package com.gvvp.roadcrackdetector;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.theartofdev.edmodo.cropper.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private CircleImageView profileImageView;
    private TextView changeimagebtn;
    private Button editprofilebtn;
    private EditText passwordtf, phonenotf, nametf;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String myUri = "";
    private StorageTask uploadTask;
    private StorageReference storageProfilePicsReference;
    private ProgressDialog progressDialog;
    private String email;
    private Uri uri;
    private TextView textViewname, textViewemail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_edit_profile);

        changeimagebtn = findViewById(R.id.change_profile_button);
        profileImageView = findViewById(R.id.ep_profileimage);
        editprofilebtn = findViewById(R.id.ep_button);
        passwordtf = findViewById(R.id.ep_password_et);
        nametf = findViewById(R.id.ep_name_et);
        phonenotf = findViewById(R.id.ep_phoneno_et);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("Users").document(uid);

        textViewname = findViewById(R.id.textView);
        textViewemail = findViewById(R.id.textView2);

        passwordtf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordtf.setText("");
            }
        });

        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("fullname");
                    String phone = documentSnapshot.getString("phoneno");
                    String email = documentSnapshot.getString("email");
                    String imageUrl = documentSnapshot.getString("image");
                    //do nothing
                    if(imageUrl==null) {
                        Toast.makeText(EditProfileActivity.this, "No Profile Picture Found",Toast.LENGTH_SHORT).show();
                        //do nothing
                    }
                    else{
                        Glide.with(EditProfileActivity.this)
                                .load(imageUrl)
                                .into(profileImageView);
                    }
                    // Set the retrieved data in EditText fields
                    nametf.setText(name);
                    phonenotf.setText(phone);
                    textViewname.setText(name);
                    textViewemail.setText(email);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfileActivity.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
            }
        });

        changeimagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON).start(EditProfileActivity.this);
            }
        });

        editprofilebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(EditProfileActivity.this);
                progressDialog.setMessage("Updating profile...");
                progressDialog.show();
                email = mAuth.getCurrentUser().getEmail();
                HashMap<String, Object> data = new HashMap<>();
                data.put("fullname", nametf.getText().toString().trim());
                data.put("phoneno", phonenotf.getText().toString().trim());
                if (uri != null) {
                    storageProfilePicsReference = FirebaseStorage.getInstance().getReference().child("profile");
                    final StorageReference fileRef = storageProfilePicsReference.child(mAuth.getCurrentUser().getUid() + ".jpg");

                    uploadTask = fileRef.putFile(uri);
                    uploadTask.continueWithTask(new Continuation() {
                        @Override
                        public Object then(@NonNull Task task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            return fileRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                myUri = downloadUri.toString();

                                data.put("image", myUri);

                                // Update the user data in Firestore
                                db.collection("Users").document(mAuth.getCurrentUser().getUid()).update(data)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(EditProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    // Update the user data in Firestore without the image
                    db.collection("Users").document(mAuth.getCurrentUser().getUid()).update(data)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully without profile image", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                                }
                            });
                }

                // Update the password using Firebase Auth
                if (!passwordtf.getText().toString().isEmpty() && !(passwordtf.getText().toString().equals("Change"))) {
                    mAuth.getCurrentUser().updatePassword(passwordtf.getText().toString().trim())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(EditProfileActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(EditProfileActivity.this, "Logout and Sign in again to change Password.",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK){
                uri = result.getUri();
                profileImageView.setImageURI(uri);
            }
        }
    }
}