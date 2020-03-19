package com.example.beepbeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class Wallet extends AppCompatActivity {

    Button addMoneyButton;
    Button receivePaymentButton;
    Button makePaymentButton;

    TextView balanceDisplay;

    FirebaseFirestore db;

    String username;

    final String TAG = "Account";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        // map view element
        makePaymentButton = findViewById(R.id.Wallet_MakePaymentButton);
        receivePaymentButton = findViewById(R.id.Wallet_ReveivePaymentButton);
        addMoneyButton = findViewById(R.id.Wallet_AddBalanceButton);
        balanceDisplay = findViewById(R.id.Wallet_BalanceDisplay);

        // establish connection to fire store
        db = FirebaseFirestore.getInstance();

        // get username
        final SharedPreferences sharedPref = Wallet.this.getSharedPreferences("identity", MODE_PRIVATE);
        this.username = sharedPref.getString("username", "");

        // get current balance
        displayBalance();

        // add button listener
        makePaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a = new Intent(Wallet.this, MakePayment.class);
                startActivity(a);
            }
        });
        receivePaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a = new Intent(Wallet.this, ReceivePayment.class);
                startActivity(a);
            }
        });
        addMoneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        displayBalance();
    }

    /**
     * connect to firestore and get the account balance for the current user
     */
    private void displayBalance(){
        DocumentReference docIdRef = db.collection("Accounts").document(username);
        docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        balanceDisplay.setText(Objects.requireNonNull(document.get("balance")).toString());
                    }else{
                        showDialog("Something is wrong, you need to login again!");
                        SignOut.now(Wallet.this);
                    }
                }else{
                    showDialog("Unable to connect, please check your connection");
                }
            }
        });
    }

    /**
     * Display a message as a toast to prompt user
     * @param message String
     */
    private void showDialog(String message) {
        Toast.makeText(Wallet.this, message, Toast.LENGTH_SHORT).show();
    }
}
