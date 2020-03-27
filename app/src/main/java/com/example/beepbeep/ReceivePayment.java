package com.example.beepbeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Objects;

/*
 Title: How to Read QR Code Android Using Zxing Library
 Author: Yuqian Cao, Space-O Technologies
 Date: 2020/03/10
 Code version: N/A
 Availability: https://www.spaceotechnologies.com/qr-code-android-using-zxing-library/
*/

public class ReceivePayment extends AppCompatActivity {
    TextView messageDisplay;
    Button confirmButton;

    FirebaseFirestore db;

    String username;

    final String TAG = "FireStore";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_payment);

        // map View elements
        messageDisplay = findViewById(R.id.ReceivePayment_textDisplay);
        confirmButton = findViewById(R.id.ReceivePayment_confirmButton);

        // hide all View element
        messageDisplay.setVisibility(View.INVISIBLE);
        confirmButton.setVisibility(View.INVISIBLE);

        // get username from shared preference
        final SharedPreferences sharedPref = ReceivePayment.this.getSharedPreferences("identity", MODE_PRIVATE);
        username = sharedPref.getString("username","");

        // connect to fireStore
        db = FirebaseFirestore.getInstance();

        // scan QRCode
        IntentIntegrator scanner = new IntentIntegrator(this);
        scanner.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        scanner.setCameraId(0);
        scanner.setBeepEnabled(true);
        scanner.initiateScan();

        // when confirm Button is click, finish activity
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = null;
        if(hasNetworkAccess()){
            result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

            if(result != null) {
                if(result.getContents() == null) { //cancelled by user
                    messageDisplay.setVisibility(View.VISIBLE);
                    messageDisplay.setText("No payment received");
                } else { //Scan successful
                    // get scan result
                    double amount = Double.parseDouble(result.getContents());
                    processTransaction(amount); // update cloud wallet balance
                }
            }
        }else{
            messageDisplay.setVisibility(View.VISIBLE);
            messageDisplay.setText("No payment received\nA network connection is required to receive payment");
        }
    }

    /**
     * Will add the amount to the wallet balance
     * @param amount double
     */
    private void processTransaction(final double amount){
        // get user account balance from fireStore
        DocumentReference docIdRef = db.collection("Accounts").document(username);
        docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(amount > 0){
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            double balance = Double.parseDouble(Objects.requireNonNull(document.get("balance")).toString());
                            double newBalance = balance + amount;
                            db.collection("Accounts").document(username).update("balance", Double.toString(newBalance));
                            messageDisplay.setText("Received $" + amount);
                        }else{
                            messageDisplay.setText("Unable to receive payment, please try again");
                        }
                    }else{
                        Log.d(TAG, "Failed with:", task.getException());
                        messageDisplay.setText("Unable to receive payment, please try again");
                    }
                }else{
                    messageDisplay.setText("Invalid payment received, transaction cancelled");
                }
                messageDisplay.setVisibility(View.VISIBLE);
                confirmButton.setVisibility(View.VISIBLE);
            }
        });
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
}

