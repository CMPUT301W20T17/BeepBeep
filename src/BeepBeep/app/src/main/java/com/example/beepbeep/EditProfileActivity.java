package com.example.beepbeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/*
 Title: Edit  profile
 Author: Junqi Zou, Lyuyang Wang
 Date: 2020/03/06
 Last edited: 2020/03/12
*/

    /**
     *Edit the user profile, which includes email and phone
     */

    public class EditProfileActivity extends AppCompatActivity {

        FirebaseFirestore db;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_edit_profile);
            Button saveButton = findViewById(R.id.save_button);
            Button cancelButton = findViewById(R.id.cancel_button);

            Intent intent = getIntent();
            final String profileName = intent.getStringExtra("profile_name");
            db = FirebaseFirestore.getInstance();

            //when finished edit, set email and phone to the data stored in fireStore
            DocumentReference userRef = db.collection("Accounts").document(profileName);
            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        EditText phoneEditText = findViewById(R.id.phone_editText);
                        EditText emailEditText = findViewById(R.id.email_editText);
                        DocumentSnapshot doc = task.getResult();
                        String email = (doc.get("email")).toString();
                        String phone = (doc.get("phone")).toString();
                        emailEditText.setText(email);
                        phoneEditText.setText(phone);
                    }
                }
            });

            //when click save button, the app should check if the new input is valid or not
            //if the new input is valid, then fireStore, hared preference and view profile page should update
            //if the new input is invalid, the error shows up and user can reedit
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText phoneEditText = findViewById(R.id.phone_editText);
                    EditText emailEditText = findViewById(R.id.email_editText);
                    final String phoneEdit = phoneEditText.getText().toString();
                    final String emailEdit = emailEditText.getText().toString();

                    //check if the input is valid
                    boolean phoneValid = Signup.validPhone(phoneEdit);
                    final boolean emailValid = Signup.validEmail(emailEdit);

                    //update phone and email to fireStore
                    if (phoneValid && emailValid){
                        DocumentReference userRef = db.collection("Accounts").document(profileName);
                        userRef.update("phone", phoneEdit,
                                "email", emailEdit).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    finish();
                                }
                            }
                        });

                        final SharedPreferences sharedPref = EditProfileActivity.this.getSharedPreferences("identity", Context.MODE_PRIVATE);

                        sharedPref.edit().putString("email", emailEdit).apply();
                        sharedPref.edit().putString("phone", phoneEdit).apply();
                    }
                    else{
                        //input invalid, shows error message
                        //invalid input email
                        if (!emailValid){
                            String message = "Your email is not valid.";
                            Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                        //invalid input phone
                        if(!phoneValid){
                            String message = "Your phone is not valid.";
                            Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            cancelButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    finish();
                }
            });
        }
    }

