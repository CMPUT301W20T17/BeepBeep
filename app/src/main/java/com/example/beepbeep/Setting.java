package com.example.beepbeep;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Switch;

public class Setting extends AppCompatActivity {

    Switch displaySwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // map ui
        displaySwitch = findViewById(R.id.Setting_DisplaySwitch);
    }
}
