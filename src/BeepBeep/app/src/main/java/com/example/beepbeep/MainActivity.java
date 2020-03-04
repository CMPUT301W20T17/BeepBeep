package com.example.beepbeep;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check if user is logged in, if not logged in, go to login screen
        SharedPreferences sharedPref = MainActivity.this.getSharedPreferences("identity", MODE_PRIVATE);
        if(sharedPref.getAll().toString() == "{}"){
            Intent i = new Intent(this, Login.class);
            startActivity(i);
        }else{
            // TODO
            // connect to fire store and see if the password still matches the one recorded
            // if match, updated all values
            // if not match, logout and delete identity sp
        }

        // start main activity
//        Intent intent = new Intent(this, MapsActivity.class);
//        startActivity(intent);

    }
}
