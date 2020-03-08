package com.example.beepbeep;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RiderMapActivity extends AppCompatActivity {
    final String TAG = "Account";
    FirebaseDatabase database;
    DatabaseReference ref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_main);
        ref = FirebaseDatabase.getInstance().getReference().child(TAG);

    }
}
