package com.example.beepbeep;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class Menu extends AppCompatActivity {
    CardView profileMenu;
    CardView mapMenu;
    CardView payMenu;
    CardView scanMenu;
    CardView historyMenu;
    CardView logoutMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        profileMenu = findViewById(R.id.profileMenu);
        mapMenu = findViewById(R.id.mapMenu);
        payMenu = findViewById(R.id.payMenu);
        scanMenu = findViewById(R.id.scanMenu);
        historyMenu = findViewById(R.id.historyMenu);
        logoutMenu = findViewById(R.id.logoutMenu);

        SharedPreferences sharedPref = this.getSharedPreferences("identity", Context.MODE_PRIVATE);
        final String username = sharedPref.getString("username", "");

        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a = new Intent(Menu.this, ViewProfile.class);
                a.putExtra("profile_name", username);
                startActivity(a);
            }
        });

        mapMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a = new Intent(Menu.this, RiderMapActivity.class);
                startActivity(a);
            }
        });

        payMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a = new Intent(Menu.this, MakePayment.class);
                startActivity(a);
            }
        });

        scanMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a = new Intent(Menu.this, ReceivePayment.class);
                startActivity(a);
            }
        });

        historyMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a = new Intent(Menu.this, OrderHistoryActivity.class);
                a.putExtra("profile_name", username);
                startActivity(a);
            }
        });

        logoutMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignOut.now(Menu.this);
            }
        });

    }
}
