package com.example.beepbeep;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class RiderSearchCurrentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_map);
        AlertDialog.Builder builder = new AlertDialog.Builder(RiderSearchCurrentActivity.this);
        builder.setTitle("Request Search")
                .setMessage("Searching if there is any current request ")
                .setPositiveButton("OK", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent a = new Intent(RiderSearchCurrentActivity.this, RiderMapActivity.class);
                        startActivity(a);
                        finishAffinity();
                    }
                })
                .create()
                .show();
    }
}
