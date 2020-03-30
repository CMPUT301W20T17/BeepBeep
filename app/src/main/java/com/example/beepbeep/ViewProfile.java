package com.example.beepbeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

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
        String loginName = sharedPref.getString("username","");

        final ImageView editButton = findViewById(R.id.edit_profile_button);

        // If the user is viewing his/her own profile, set the edit button to be visible
        if(loginName.equals(profileName)){
            editButton.setVisibility(View.VISIBLE);
        }
        else{
            editButton.setVisibility(View.INVISIBLE);
        }

        //Read data from FireStore and fill the TextView
        db = FirebaseFirestore.getInstance();

        DocumentReference userRef = db.collection("Accounts").document(profileName);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){
                    TextView nameTextView = findViewById(R.id.profile_view_name);
                    TextView phoneTextView = findViewById(R.id.profile_view_phone);
                    TextView emailTextView = findViewById(R.id.profile_view_email);
                    TextView roleTextView = findViewById(R.id.profile_view_role);
                    TextView ratingTextView = findViewById(R.id.profile_view_rating);
                    final ImageView profilePicture = findViewById(R.id.profile_view_photo);

                    DocumentSnapshot doc = task.getResult();
                    String email = (doc.get("email")).toString();
                    String phone = (doc.get("phone")).toString();
                    String role = (doc.get("role")).toString();
// THIS PART IS NEEDED BY GROUP 2 FOR RETRIEVING THE IMAGE
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageReference = storage.getReference().child("profileImages/"+ email);
                    try{
                        final File file = File.createTempFile("image","jpg");
                        storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                profilePicture.setImageBitmap(bitmap);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    //if the current role is diver, then display the rating
                    if (role.equals("Driver")){
                        String positive = (doc.get("positive")).toString();
                        String negative = (doc.get("negative")).toString();
                        ratingTextView.setVisibility(View.VISIBLE);

                        if(Integer.parseInt(positive) == 0 && Integer.parseInt(negative) == 0){
                            ratingTextView.setText("No one reviewed this driver.");
                        }
                        else { //calculate rating by using thumbs uop and thumbs down from fireStore
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
        String loginName = sharedPref.getString("username","");

        // Set the edit button to be visible if user is viewing his/her own profile
        final ImageView editButton = findViewById(R.id.edit_profile_button);
        if(loginName.equals(profileName)){
            editButton.setVisibility(View.VISIBLE);
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
                    final ImageView profilePicture = findViewById(R.id.profile_view_photo);
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageReference = storage.getReference().child("profileImages/"+ email);
                    try{
                        final File file = File.createTempFile("image","jpg");
                        storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                profilePicture.setImageBitmap(bitmap);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}