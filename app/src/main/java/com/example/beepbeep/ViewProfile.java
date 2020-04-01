package com.example.beepbeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

/*
 Title: View  profile
 Author: Junqi Zou, Lyuyang Wang
 Date: 2020/03/06
 Last edited: 2020/03/12
*/

/**
 * view profile activity, view the information about the user self, or others in order
 * edit button will show if the view page is current user
 */

public class ViewProfile extends AppCompatActivity {

    FirebaseFirestore db;
    String email;
    String phone;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        // get the name of profile's owner from intent
        Intent intent = getIntent();
        final String profileName = intent.getStringExtra("profile_name");

        //Use SharedPreferences to get the name of user that currently logging in
        final SharedPreferences sharedPref = ViewProfile.this.getSharedPreferences("identity", Context.MODE_PRIVATE);
        final String loginName = sharedPref.getString("username", "");

        final ImageView editButton = findViewById(R.id.edit_profile_button);
        //Button Email and phone are used for intent of calling and emailing users
        Button emailButton = findViewById(R.id.email_button);
        Button callButton = findViewById(R.id.call_button);
        //when user clicks logout button, prompt user for confirmation
        Button logout = findViewById(R.id.logout_button);
        // If the user is viewing his/her own profile, set the edit and logout button to be visible
        if (loginName.equals(profileName)) {
            editButton.setVisibility(View.VISIBLE);
            logout.setVisibility(View.VISIBLE);
        }
        //If the user is viewing/someone else's profile, set the emailButton and call Button to visible.
        else {
            emailButton.setVisibility(View.VISIBLE);
            callButton.setVisibility(View.VISIBLE);
        }

        //Read data from FireStore and fill the TextView
        db = FirebaseFirestore.getInstance();

        DocumentReference userRef = db.collection("Accounts").document(profileName);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    TextView nameTextView = findViewById(R.id.profile_view_name);
                    TextView phoneTextView = findViewById(R.id.profile_view_phone);
                    TextView emailTextView = findViewById(R.id.profile_view_email);
                    TextView roleTextView = findViewById(R.id.profile_view_role);
                    TextView ratingTextView = findViewById(R.id.profile_view_rating);
                    final ImageView profilePicture = findViewById(R.id.profile_view_photo);

                    DocumentSnapshot doc = task.getResult();
                    email = (doc.get("email")).toString();
                    phone = (doc.get("phone")).toString();
                    String role = (doc.get("role")).toString();
                    // Retrieve the image from the database, the credit is located on the bottom where this happens again.
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageReference = storage.getReference().child("profileImages/" + loginName);
                    try {
                        final File file = File.createTempFile("image", "jpg");
                        storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                profilePicture.setImageBitmap(bitmap);
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        profilePicture.setImageResource(R.drawable.ic_launcher_foreground);
                                    }
                                });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    //if the current role is diver, then display the rating
                    if (role.equals("Driver")) {
                        String positive = (doc.get("positive")).toString();
                        String negative = (doc.get("negative")).toString();
                        ratingTextView.setVisibility(View.VISIBLE);

                        if (Integer.parseInt(positive) == 0 && Integer.parseInt(negative) == 0) {
                            ratingTextView.setText("No one reviewed this driver.");
                        } else { //calculate rating by using thumbs uop and thumbs down from fireStore
                            Integer positiveNum = Integer.parseInt(positive);
                            Integer negativeNum = Integer.parseInt(negative);
                            double rating = Double.parseDouble(positive) / (positiveNum + negativeNum) * 100;
                            ratingTextView.setText(String.format("%.2f", rating) + "% thumbs up out of " + String.format("%d", positiveNum + negativeNum) + " riders");
                        }
                    }

                    //show information
                    nameTextView.setText(profileName);
                    emailTextView.setText(email);
                    phoneTextView.setText(phone);
                    emailTextView.setText(email);
                    roleTextView.setText(role);
                }
            }
        });

        //when user click edit button, go to edit profile activity
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editP = new Intent(getApplicationContext(), EditProfileActivity.class);
                editP.putExtra("profile_name", profileName);
                startActivity(editP);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewProfile.this);
                builder.setTitle("Are you sure you want to logout?");
                builder.setNegativeButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SignOut.now(ViewProfile.this);
                    }
                });
                builder.setPositiveButton("NO", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailUser = new Intent(ViewProfile.this,EmailUser.class);
                emailUser.putExtra("toEmail", email);
                startActivity(emailUser);
            }
        });
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ViewProfile.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(ViewProfile.this, Manifest.permission.CALL_PHONE)) {
                        //Create AlertDialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(ViewProfile.this);
                        builder.setTitle("Grant those permission");
                        builder.setMessage("Phone Calls");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(ViewProfile.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CODE);
                            }
                        });
                        builder.setNegativeButton("Cancel", null);
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    } else {
                        ActivityCompat.requestPermissions(ViewProfile.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CODE);
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ViewProfile.this);
                    builder.setTitle("Are you sure you want to call " + phone + "?");
                    builder.setNegativeButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_CALL);
                            String[] phoneNumber = phone.split("-");
                            String phoneNumber1 = String.join("", phoneNumber);
                            intent.setData(Uri.parse("tel:" + phoneNumber1));
                            if (ActivityCompat.checkSelfPermission(ViewProfile.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            startActivity(intent);
                        }
                    });
                    builder.setPositiveButton("NO", null);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                //When permission are already granted
            }
        });
    }
    //change the edited valid email and phone to show correctly after returning to the view profile activity
    @Override
    protected void onResume() {
        super.onResume();
        // get the name of profile's owner from intent
        Intent intent = getIntent();
        final String profileName = intent.getStringExtra("profile_name");

        //Use SharedPreferences to get the name of user that currently logging in
        final SharedPreferences sharedPref = ViewProfile.this.getSharedPreferences("identity", Context.MODE_PRIVATE);
        final String loginName = sharedPref.getString("username","");

        // Set the edit button to be visible if user is viewing his/her own profile
        Button logout = findViewById(R.id.logout_button);
        final ImageView editButton = findViewById(R.id.edit_profile_button);
        Button email = findViewById(R.id.email_button);
        Button call = findViewById(R.id.call_button);
        if(loginName.equals(profileName)){
            editButton.setVisibility(View.VISIBLE);
            logout.setVisibility(View.VISIBLE);
        }
        else{
            email.setVisibility(View.VISIBLE);
            call.setVisibility(View.VISIBLE);
        }

        //Reload the email and phone from FireStore because they might be changed
        DocumentReference userRef = db.collection("Accounts").document(profileName);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    String email = (doc.get("email")).toString();
                    String phone = (doc.get("phone")).toString();
                    TextView phoneTextView = findViewById(R.id.profile_view_phone);
                    TextView emailTextView = findViewById(R.id.profile_view_email);
                    emailTextView.setText(email);
                    phoneTextView.setText(phone);
/*
 Title: Retrieve image for profile picture
 Author: Jonathan Martins, Gadgets and Technical field Android Tech
 Date: 2020/03/28
 Last edited: 2020/03/30
 Availability: https://www.youtube.com/watch?v=sGnazb9RjNs
*/
                    final ImageView profilePicture = findViewById(R.id.profile_view_photo);
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageReference = storage.getReference().child("profileImages/" + loginName);
                    try {
                        final File file = File.createTempFile("image", "jpg");
                        storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                profilePicture.setImageBitmap(bitmap);
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        profilePicture.setImageResource(R.drawable.ic_launcher_foreground);
                                    }
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewProfile.this);
                builder.setTitle("Are you sure you want to logout?");
                builder.setNegativeButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SignOut.now(ViewProfile.this);
                    }
                });
                builder.setPositiveButton("NO",null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }
}