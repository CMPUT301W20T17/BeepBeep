package com.example.beepbeep;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

public class Setting extends AppCompatActivity {

    Switch displaySwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        final SharedPreferences sharedPref = Setting.this.getSharedPreferences("identity", MODE_PRIVATE);
        final boolean darkmode = sharedPref.getBoolean("darkmode", false);
        final SharedPreferences.Editor editor = sharedPref.edit();

        // map ui
        displaySwitch = findViewById(R.id.Setting_DisplaySwitch);
        Button apply = findViewById(R.id.ApplyButton);
        displaySwitch.setChecked(darkmode);
        displaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor.putBoolean("darkmode", true);
                    editor.commit();
                    displaySwitch.setChecked(true);

                } else {
                    editor.putBoolean("darkmode", false);
                    editor.commit();
                    displaySwitch.setChecked(false);
                }
            }
        });
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDisplaySwitch();
            }
        });

    }

    public void setDisplaySwitch(){
        SharedPreferences sharedPref = Setting.this.getSharedPreferences("identity", MODE_PRIVATE);
        String role = sharedPref.getString("role", "");
        if(role.equals("Driver")){
            Intent intent = new Intent(Setting.this, DriverMapActivity.class);
            startActivity(intent);
            finishAffinity();
        }else{
            Intent intent = new Intent(Setting.this, RiderMapActivity.class);
            startActivity(intent);
            finishAffinity();
        }
    }
}
