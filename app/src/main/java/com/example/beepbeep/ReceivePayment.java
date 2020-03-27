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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Locale;
import java.util.Objects;

/*
 Title: How to Read QR Code Android Using Zxing Library
 Author: Yuqian Cao, Space-O Technologies
 Date: 2020/03/10
 Code version: N/A
 Availability: https://www.spaceotechnologies.com/qr-code-android-using-zxing-library/
*/

public class ReceivePayment extends AppCompatActivity {
    FirebaseFirestore db;

    String username;

    final String TAG = "FireStore";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_payment);

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = null;
        if(hasNetworkAccess()){
            result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

            if(result != null) {
                if(result.getContents() == null) { //cancelled by user
                    showDialog("No payment received");
                } else { //Scan successful
                    // get scan result
                    double amount = Double.parseDouble(result.getContents());
                    processTransaction(amount); // update cloud wallet balance
                }
            }
        }else{
            showDialog("No payment received\nA network connection is required to receive payment");
        }
        finish();
    }

    /**
     * Display a message as a toast to prompt user
     * @param message String
     */
    private void showDialog(String message) {
        Toast.makeText(ReceivePayment.this, message, Toast.LENGTH_SHORT).show();
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
                            showDialog(String.format(Locale.CANADA, "Received $%.2f", amount));
                        }else{
                            showDialog("Unable to receive payment, please try again");
                        }
                    }else{
                        Log.d(TAG, "Failed with:", task.getException());
                        showDialog("Unable to receive payment, please try again");
                    }
                }else{
                    showDialog("Invalid payment received, transaction cancelled");
                }
            }
        });
    }

    /**
     * check if device have network access
     * @return true if device have network access
     */
    private boolean hasNetworkAccess(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

