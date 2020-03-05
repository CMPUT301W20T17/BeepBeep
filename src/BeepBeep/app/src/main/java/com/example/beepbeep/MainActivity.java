package com.example.beepbeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    final String TAG = "Account";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check if user is logged in, if not logged in, go to login screen
        final SharedPreferences sharedPref = MainActivity.this.getSharedPreferences("identity", MODE_PRIVATE);
        if(!sharedPref.contains("initialized")){
            Intent i = new Intent(this, Login.class);
            startActivity(i);
        }else{
            if(hasNetworkAccess()){
                // get a connection to firebase
                final FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Get all current activity
                final String password = sharedPref.getString("password", "");
                final String salt = sharedPref.getString("salt", "");
                final String role = sharedPref.getString("role", "");
                final String username = sharedPref.getString("username", "");
                //get cloud user profile
                DocumentReference docIdRef = db.collection("Accounts").document(username);
                docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) { // username exist, check password
                                Log.d(TAG, "Account exists!");

                                // check if password and salt match the one on record, if match updated all identity, else, sign out
                                if (!salt.equals(Objects.requireNonNull(document.get("salt")).toString())
                                        || !password.equals(Objects.requireNonNull(document.get("password")).toString())) {
                                    showDialog("You need to login again!");
                                    SignOut.now(MainActivity.this);
                                }

                                // update identity
                                sharedPref.edit().clear().apply();
                                sharedPref.edit().putBoolean("initialized", true).apply();
                                sharedPref.edit().putString("password", password).apply();
                                sharedPref.edit().putString("username", username).apply();
                                sharedPref.edit().putString("salt", salt).apply();
                                sharedPref.edit().putString("role", role).apply();
                                sharedPref.edit().putString("email", Objects.requireNonNull(document.get("email")).toString()).apply();
                                sharedPref.edit().putString("phone", Objects.requireNonNull(document.get("phone")).toString()).apply();
                                if(role.equals("Driver")){
                                    sharedPref.edit().putString("positive", Objects.requireNonNull(document.get("positive")).toString()).apply();
                                    sharedPref.edit().putString("negative", Objects.requireNonNull(document.get("negative")).toString()).apply();
                                }
                            } else {
                                // prompt for error
                                showDialog("You need to login again!");
                                SignOut.now(MainActivity.this);
                            }
                        } else {
                            Log.d(TAG, "Failed with:", task.getException());
                            showDialog("something is wrong");
                        }
                    }
                });
            }else{
                showDialog("You are currently offline!");
            }
        }

        // start main activity
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
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
