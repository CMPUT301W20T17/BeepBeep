package com.example.beepbeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
import java.util.Arrays;
import java.util.Collections;
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
    EditText phoneInput;

    Switch roleSwitch;

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
        roleSwitch = findViewById(R.id.Signup_roleSwitch);
        signUpButton = findViewById(R.id.Signup_signupButton);
        phoneInput = findViewById(R.id.Signup_phoneInput);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validInput()){
                    registerUser();
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

                                Map<String, Object> account = new HashMap<>();
                                account.put("email", emailInput.getText().toString());
                                account.put("password", password);
                                account.put("salt", salt);
                                account.put("role", roleSwitch.isChecked() ? "Driver" : "Rider");
                                account.put("phone", phoneInput.getText().toString());
                                account.put("balance", "0");
                                account.put("order", Collections.emptyList());
                                if(roleSwitch.isChecked()){ // if register as a driver, will get a rating field in profile
                                    account.put("positive", "0");
                                    account.put("negative", "0");
                                }
                                db.collection("Accounts").document(usernameInput.getText().toString()).set(account);
                            }catch (NoSuchAlgorithmException | InvalidKeySpecException e){
                                Log.d("KeyGenError", e.toString());
                            }
                            showDialog("You are all signed up!");
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
     * check if every input is valid and not empty
     * @return true is every input is valid and not empty
     */
    private boolean validInput(){
        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();
        String email = emailInput.getText().toString();
        String phone = phoneInput.getText().toString();
        if(username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty()){ // check if any input in empty
            showDialog("You must fill in all the fields");
            return false;
        }else if(!validUsername(username)){
            showDialog("Your username must be 5 to 15 characters long and contains only Alphabets and Numbers");
            return false;
        }else if(!password.equals(confirmPassword)){ // check if the 2 password match
            showDialog("Your 2 password entry does not match");
            return false;
        }else if(password.length() < 8){
            showDialog("You password must be 8 characters or longer");
            return false;
        }else if(!validEmail(email)){ // validate email with regex
            showDialog("Invalid Email Address");
            return false;
        } else if (!validPhone(phone)) {
            showDialog("Invalid Phone Number");
            return false;
        }
        return true;
    }

    /**
     * Check if a given string of email address are valid
     * @param email String
     * @return true if email is the right format
     */
    static public boolean validEmail(String email){
        String pattern = "^[a-zA-Z0-9\\-!#$%&'*+/=?^_`{|}~.]+@\\w+\\.\\w+$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(email);
        return m.find();
    }

    /**
     * Check if a given string of phone number are valid
     * @param phone String
     * @return true if phone is the right format
     */
    static public boolean validPhone(String phone){
        String pattern = "^(\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(phone);
        return m.find();
    }

    /**
     * Check if a given string of username are valid
     * @param username String
     * @return true if username is the right format
     */
    static public boolean validUsername(String username){
        String pattern = "^[A-Za-z0-9_-]{5,15}$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(username);
        return m.find();
    }

    /**
     * Display a message as a toast to prompt user
     * @param message String
     */
    private void showDialog(String message) {
        Toast.makeText(Signup.this, message, Toast.LENGTH_SHORT).show();
    }

}
