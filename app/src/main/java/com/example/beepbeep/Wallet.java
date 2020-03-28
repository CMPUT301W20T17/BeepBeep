package com.example.beepbeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;
import java.util.Objects;

public class Wallet extends AppCompatActivity {

    Button addMoneyButton;
    Button receivePaymentButton;
    Button makePaymentButton;

    TextView balanceDisplay;

    EditText amountEntry;

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
        amountEntry = findViewById(R.id.Wallet_amountEntry);

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
                if(hasNetworkAccess()){
                    addBalance(Double.parseDouble(amountEntry.getText().toString()));
                }else{
                    showDialog("Unable to add balance\nYou need a network connection to add balance");
                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        displayBalance();
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

    /**
     * connect to firestore and modify the account balance for the current user
     */
    private void addBalance(final double balance){
        DocumentReference docIdRef = db.collection("Accounts").document(username);
        docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) { // username exist, prompt error
                        double currentBalance = Double.parseDouble(document.get("balance").toString());
                        double newBalance = currentBalance + balance;
                        db.collection("Accounts").document(username).update("balance", Double.toString(newBalance));
                        showDialog(String.format(Locale.CANADA, "$%.2f has been added to your account", balance));
                        displayBalance();
                    }
                }
            }
        });
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
