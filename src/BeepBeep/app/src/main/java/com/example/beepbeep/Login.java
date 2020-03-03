package com.example.beepbeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

/*
 Title: Checking if a document exists in a Firestore collection
 Author: Yuqian Cao, Alex Mamo
 Date: 2020/03/02
 Code version: N/A
 Availability: https://stackoverflow.com/questions/53332471/checking-if-a-document-exists-in-a-firestore-collection/53335711
*/

public class Login extends AppCompatActivity {
    TextView signUpPrompt;
    EditText usernameInput;
    EditText passwordInput;

    Button loginButton;

    ProgressBar progressBar;

    FirebaseFirestore db;

    final String TAG = "Sample";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // establish connection to firebase
        db = FirebaseFirestore.getInstance();

        // map view elements
        signUpPrompt = findViewById(R.id.Login_signupPrompt);
        usernameInput = findViewById(R.id.Login_inputUsername);
        passwordInput = findViewById(R.id.Login_inputPassword);
        loginButton = findViewById(R.id.Login_loginButton);
        progressBar = findViewById(R.id.Login_progressBar);

        // hide progress bar
        progressBar.setVisibility(View.INVISIBLE);

        // user clicked login, verify user credential
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show progress bar
                progressBar.setVisibility(View.VISIBLE);
                // check that username and password field aren't empty
                if(usernameInput.getText().toString().isEmpty()){
                    // prompt for error
                    AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                    builder.setMessage("You need to type in your username").setTitle("Error").show();
                }else if(passwordInput.getText().toString().isEmpty()){
                    // prompt for error
                    AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                    builder.setMessage("You need to type in your password").setTitle("Error").show();
                }else{
                    // check if username exist in database
                    DocumentReference docIdRef = db.collection("Accounts").document(usernameInput.getText().toString());
                    docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) { // username exist, check password
                                    Log.d(TAG, "Account exists!");
                                    // check if password hash match the one on record
                                    String salt = Objects.requireNonNull(document.get("salt")).toString();
                                    String hash = Objects.requireNonNull(document.get("password")).toString();
                                    try {
                                        String inputHash = SecurePasswordHashGenerator.rehashPassword(passwordInput.getText().toString(), salt);
                                        if(inputHash.substring(33,inputHash.length()).equals(hash)){ // password is a match
                                            // TODO change app state as logged in, change the shared preference, need to create the shared preference
                                            finish();
                                        }else{ // password does not match
                                            // prompt for error
                                            AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                                            builder.setMessage("Invalid Username or Password").setTitle("Error").show();
                                        }
                                    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    // prompt for error
                                    AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                                    builder.setMessage("Invalid Username or Password").setTitle("Error").show();
                                }
                            } else {
                                Log.d(TAG, "Failed with: ", task.getException());
                            }
                        }
                    });
                }
                // hide progress bar
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

        // user don't have an account, send user to the sign up page
        signUpPrompt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launchSignUpActivity = new Intent(getApplicationContext(), Signup.class);
                startActivity(launchSignUpActivity);
            }
        });
    }
}
