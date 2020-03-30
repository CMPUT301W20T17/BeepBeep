package com.example.beepbeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class RiderRatingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_rating);

        Intent intent = getIntent();
        final String driverName = intent.getStringExtra("driver_name");

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final ImageView driverPhoto = findViewById(R.id.driver_photo);
        Button skipButton = findViewById(R.id.skip_button);
        ImageButton thumbsUp = findViewById(R.id.thumbs_up_pic);
        ImageButton thumbsDown = findViewById(R.id.thumbs_down_pic);
        TextView driverNameText = findViewById(R.id.name_driver_text);
        driverNameText.setText(driverName);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference().child("profileImages/" + driverName);
        try {
            final File file = File.createTempFile("image", "jpg");
            storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    driverPhoto.setImageBitmap(bitmap);
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            driverPhoto.setImageResource(R.drawable.ic_launcher_foreground);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        thumbsDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference userRef = db.collection("Accounts").document(driverName);
                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            String negative = (doc.get("negative")).toString();
                            Integer negativeNum = Integer.parseInt(negative) + 1;
                            negative = Integer.toString(negativeNum);

                            DocumentReference userRef2 = db.collection("Accounts").document(driverName);
                            userRef2.update("negative", negative).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        finish();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });

        thumbsUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference userRef = db.collection("Accounts").document(driverName);
                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            String positive = (doc.get("positive")).toString();
                            Integer positiveNum = Integer.parseInt(positive) + 1;
                            positive = Integer.toString(positiveNum);

                            DocumentReference userRef2 = db.collection("Accounts").document(driverName);
                            userRef2.update("positive", positive).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        finish();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
    }
}
