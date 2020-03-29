package com.example.beepbeep;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

/*
 Title: OrderList class
 Author: Jonathan Martins
 Date: 2020/03/16
 Code version: N/A
*/

public class Menu extends AppCompatActivity {
    CardView profileMenu;
    CardView walletMenu;
    CardView payMenu;
    CardView scanMenu;
    CardView historyMenu;
    CardView settingsMenu;

    //initialize all the CardView objects stored inside GridView
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        //set content to activity_menu.xml

        profileMenu = findViewById(R.id.profileMenu);
        walletMenu = findViewById(R.id.walletMenu);
        payMenu = findViewById(R.id.payMenu);
        scanMenu = findViewById(R.id.scanMenu);
        historyMenu = findViewById(R.id.historyMenu);
        settingsMenu = findViewById(R.id.settingsMenu);
        //link all the CardView objects with their id in the xml.

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

        walletMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a = new Intent(Menu.this, Wallet.class);
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

        settingsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignOut.now(Menu.this);
            }
        });

    }
}
