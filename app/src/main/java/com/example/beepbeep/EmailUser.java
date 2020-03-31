package com.example.beepbeep;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/*
Title: send email activity
Author: Jonathan Martins, Tech1st
Date: 2020/03/31
Last edited: 2020/03/31
Availability: https://www.youtube.com/watch?v=1Wxedj6Q31s
*/

public class EmailUser extends AppCompatActivity {
    EditText editTextsubject, editTextmessage;
    TextView editTextto;
    Button button;
    String email, subject, message;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        email = getIntent().getStringExtra("toEmail");
        setContentView(R.layout.activity_send_email);
        editTextto=(TextView)findViewById(R.id.editto);
        editTextsubject=(EditText)findViewById(R.id.editsubject);
        editTextmessage=(EditText)findViewById(R.id.editmessage);
        button=(Button)findViewById(R.id.sendEmailButton);
        editTextto.setText(email);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetData();

                intent=new Intent(Intent.ACTION_SEND);

                intent.putExtra(Intent.EXTRA_EMAIL,new String[]{email});
                intent.putExtra(Intent.EXTRA_SUBJECT,subject);
                intent.putExtra(Intent.EXTRA_TEXT,message);
                intent.setType("message/rfc822");
                startActivity(Intent.createChooser(intent,"Select Email Sending App"));
            }
        });
    }
    private void GetData(){
        subject=editTextsubject.getText().toString();
        message=editTextmessage.getText().toString();
    }
}