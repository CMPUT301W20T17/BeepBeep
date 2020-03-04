package com.example.beepbeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Signup extends AppCompatActivity {

    Button signUpButton;

    EditText usernameInput;
    EditText passwordInput;
    EditText confirmPasswordInput;
    EditText emailInput;

    Switch statusSwitch;

    FirebaseFirestore db;

    final String TAG = "Account";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // establish connection to firebase
        db = FirebaseFirestore.getInstance();

        // map all view element
        usernameInput = findViewById(R.id.Signup_Usernameinput);
        passwordInput = findViewById(R.id.Signup_PasswordInput);
        confirmPasswordInput = findViewById(R.id.Signup_ConfirmPasswordInput);
        emailInput = findViewById(R.id.Signup_emailInput);
        statusSwitch = findViewById(R.id.Signup_statusSwitch);
        signUpButton = findViewById(R.id.Signup_signupButton);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validInput()){
                    registerUser();
                }else {
                    showDialog("Oops, looks like your inputs are invalid, please try again!");
                }
            }
        });
    }

    private void registerUser(){
        if(hasNetworkAccess()){
            final DocumentReference docIdRef = db.collection("Accounts").document(usernameInput.getText().toString());
            docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assert document != null;
                        if (document.exists()) { // username exist, prompt error
                            showDialog("Username taken, please enter a unique username.");
                        } else { // unique username, register user
                            // get password hash and salt
                            try{
                                String saltAndHash = SecurePasswordHashGenerator.generateNewStrongPasswordHash(passwordInput.getText().toString());
                                String password = saltAndHash.substring(33);
                                String salt = saltAndHash.substring(0,32);
                                Map<String, String> account = new HashMap<>();
                                account.put("email", emailInput.getText().toString());
                                account.put("password", password);
                                account.put("salt", salt);
                                account.put("status", statusSwitch.isChecked() ? "Driver" : "Rider");
                                if(statusSwitch.isChecked()){ // if register as a driver, will get a rating field in profile
                                    account.put("rating", "0");
                                }
                                db.collection("Accounts").document(usernameInput.getText().toString()).set(account);
                            }catch (NoSuchAlgorithmException | InvalidKeySpecException e){
                                Log.d("KeyGenError", e.toString());
                            }
                            showDialog("You are all signed up!");
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable(){
                                @Override
                                public void run(){
                                    // do something
                                }
                            }, 3000);
                            finish();
                        }
                    } else {
                        Log.d(TAG, "Failed with: ", task.getException());
                    }
                }
            });
        }else{ // no internet
            showDialog("No Internet Connection");
        }
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

    /**
     * Display a message as a toast to prompt user
     * @param message String
     */
    private void showDialog(String message) {
        Toast.makeText(Signup.this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean validInput(){
        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();
        String email = emailInput.getText().toString();
        if(username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty()){ // check if any input in empty
            return false;
        }else if(!passwordInput.getText().toString().equals(confirmPasswordInput.getText().toString())){ // check if the 2 password match
            return false;
        }else if(!validEmail(email)){ // validate email with regex
            return false;
        }else{
            return true;
        }
    }

    private boolean validEmail(String email){
        String pattern = "[a-zA-Z0-9\\-!#$%&'*+/=?^_`{|}~.]+@\\w+\\.\\w+";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(email);
        return m.find();
    }
}
