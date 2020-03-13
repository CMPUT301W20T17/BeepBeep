package com.example.beepbeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * Login screen
 */
public class Login extends AppCompatActivity {
    TextView signUpPrompt;
    EditText usernameInput;
    EditText passwordInput;

    Button loginButton;

    ProgressBar progressBar;

    FirebaseFirestore db;

    final String TAG = "Account";

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
                // get value from input field
                String username = usernameInput.getText().toString();
                String password = passwordInput.getText().toString();
                // check that username and password field aren't empty
                if(username.isEmpty()){
                    // prompt for error
                    showDialog("Username cannot be empty");
                }else if(password.isEmpty()){
                    // prompt for error
                    showDialog("Password cannot be empty");
                }else {
                    // verify user credential
                    login(username, password);
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

    /**
     * Display a message as a toast to prompt user
     * @param message String
     */
    private void showDialog(String message) {
        Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * check if device have network access
     * @return true if device have network access
     */
    private boolean hasNetworkAccess(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void saveIdentity(final String username, final String passwordHash, final String salt, DocumentSnapshot document){
        Context context = Login.this;
        SharedPreferences sharedPref = context.getSharedPreferences("identity", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("initialized", true);
        editor.putString("username", username);
        editor.putString("password", passwordHash);
        editor.putString("salt", salt);
        editor.putString("email", Objects.requireNonNull(document.get("email")).toString());
        editor.putString("role", Objects.requireNonNull(document.get("role")).toString());
        editor.putString("phone", Objects.requireNonNull(document.get("phone")).toString());
        editor.putString("balance", Objects.requireNonNull(document.get("balance")).toString());
        if(Objects.requireNonNull(document.get("role")).toString().equals("Driver")){
            editor.putString("positive", Objects.requireNonNull(document.get("positive")).toString());
            editor.putString("negative", Objects.requireNonNull(document.get("negative")).toString());
        }
        editor.apply();
    }

    /**
     * Connect to database to verify the inputted credentials
     * @param username String
     * @param password String
     */
    private void login(final String username, final String password){
        if(hasNetworkAccess()){
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
                                String inputHash = SecurePasswordHashGenerator.rehashPassword(password, salt);
                                if(inputHash.substring(33).equals(hash)){ // password is a match
                                    saveIdentity(username, hash, salt, document);
                                    //finish();
                                    //ask the permission for using geo location and display
                                    ActivityCompat.requestPermissions(Login.this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            1);
                                    if (ContextCompat.checkSelfPermission(Login.this,
                                            Manifest.permission.ACCESS_FINE_LOCATION)
                                            != PackageManager.PERMISSION_GRANTED) {
                                    }
                                    else {
                                        Intent intent = new Intent(Login.this, RiderMapActivity.class);
                                        startActivity(intent);
                                    }
                                }else{ // password does not match
                                    // prompt for error
                                    showDialog("Invalid Username or Password");
                                }
                            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // prompt for error
                            showDialog("Invalid Username or Password");
                        }
                    } else {
                        Log.d(TAG, "Failed with: ", task.getException());
                    }
                }
            });
        }else{// no internet
            showDialog("No Internet Connection");
        }
    }
}