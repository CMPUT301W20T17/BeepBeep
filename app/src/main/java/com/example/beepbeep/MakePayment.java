package com.example.beepbeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.WriterException;

import java.util.Locale;
import java.util.Objects;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class MakePayment extends AppCompatActivity {
    EditText amountInput;
    Button confirmButton;
    ImageView QRCodeDisplay;
    TextView basePrice;

    FirebaseFirestore db;

    String username;

    final String TAG = "FireStore";

    double fair;

    Bitmap bitmap;
    QRGEncoder qrgEncoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_payment);

        // map View elements
        amountInput = findViewById(R.id.MakePayment_amountInput);
        confirmButton = findViewById(R.id.MakePayment_confirmButton);
        QRCodeDisplay = findViewById(R.id.MakePayment_QRbuckCode);
        basePrice = findViewById(R.id.MakePayment_BasePrice);

        // get base price
        Intent i = getIntent();
        if(i.getStringExtra("Price") != null){
            fair = Double.parseDouble(Objects.requireNonNull(i.getStringExtra("Price")));
        }else{
            fair = 0;
        }
        basePrice.setText(String.format(Locale.CANADA, "Price: $%.2f", fair));

        // get username from shared preference
        final SharedPreferences sharedPref = MakePayment.this.getSharedPreferences("identity", MODE_PRIVATE);
        username = sharedPref.getString("username","");

        // connect to Firestore
        db = FirebaseFirestore.getInstance();

        // when confirm Button is click, check if balance is greater than amount entered, if yes, generate and display QR code
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasNetworkAccess()){
                    double amount = Double.parseDouble(amountInput.getText().toString());
                    processTransaction(amount+fair);
                }else{
                    showDialog("Unable to make payment\nYou must have a network connection");
                }
            }
        });
    }

    /**
     * This function will check if the account has enough
     * balance to generate the QRBuck, if there are enough balance,
     * it will create and display the QR code, then deduct the amount generated
     * from user's account. If transaction can't be processed, it will prompt the user.
     * @param amount double, the amount of QRbuck to generate
     */
    private void processTransaction(final double amount){
        if(hasNetworkAccess()){
            if(amount > 0){
                // get user account balance from firestore
                DocumentReference docIdRef = db.collection("Accounts").document(username);
                docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                double balance = Double.parseDouble(Objects.requireNonNull(document.get("balance")).toString());
                                if(balance >= amount){ // transaction can be made
                                    // update new balance to cloud
                                    double newBalance = balance - amount;
                                    db.collection("Accounts").document(username).update("balance", Double.toString(newBalance));
                                    // display qr code
                                    WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
                                    Display display = manager.getDefaultDisplay();
                                    Point point = new Point();
                                    display.getSize(point);
                                    int width = point.x;
                                    int height = point.y;
                                    int smallerDimension = Math.min(width, height);
                                    smallerDimension = smallerDimension * 3 / 4;

                                    qrgEncoder = new QRGEncoder(Double.toString(amount), null, QRGContents.Type.TEXT, smallerDimension);
                                    try {
                                        bitmap = qrgEncoder.encodeAsBitmap();
                                        QRCodeDisplay.setImageBitmap(bitmap);
                                        confirmButton.setVisibility(View.INVISIBLE);
                                        basePrice.setVisibility(View.INVISIBLE);
                                    } catch (WriterException e) {
                                        Log.v(TAG, e.toString());
                                    }
                                }else{
                                    showDialog("Insufficient Balance");
                                }
                            }
                        }else{
                            Log.d(TAG, "Failed with:", task.getException());
                            showDialog("Unable to connect with FireStore");
                        }
                    }
                });
            }else{
                showDialog("Amount must be positive");
            }
        }
    }

    /**
     * Display a message as a toast to prompt user
     * @param message String
     */
    private void showDialog(String message) {
        Toast.makeText(MakePayment.this, message, Toast.LENGTH_SHORT).show();
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
