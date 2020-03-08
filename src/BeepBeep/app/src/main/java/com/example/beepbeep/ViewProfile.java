package com.example.beepbeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/*
 Title: View  profile
 Author: Junqi Zhou, Lyuyang Wang
 Date: 2020/03/06
 Code version: N/A
 Availability: https://stackoverflow.com/questions/53332471/checking-if-a-document-exists-in-a-firestore-collection/53335711
*/

/**
 * view profile activity, view the information about the user self, or others in order
 * edit button will show if the view page is current user
 */

public class ViewProfile extends AppCompatActivity {
    /**
     * @param savedInstanceState
     */

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        Intent intent = getIntent();
        final String profileName = intent.getStringExtra("profile_name");

        db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Accounts").document(profileName);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){

                    TextView nameTextView = findViewById(R.id.profile_view_name);
                    TextView phoneTextView = findViewById(R.id.profile_view_phone);
                    TextView emailTextView = findViewById(R.id.profile_view_email);
                    TextView roleTextView = findViewById(R.id.profile_view_role);
                    TextView ratingTextView = findViewById(R.id.profile_view_rating);

                    DocumentSnapshot doc = task.getResult();
                    String email = (doc.get("email")).toString();
                    String phone = (doc.get("phone")).toString();
                    String role = (doc.get("role")).toString();

                    //if the current role is diver, then get the rating
                    if (role.equals("Driver")){
                        String positive = (doc.get("positive")).toString();
                        String negative = (doc.get("negative")).toString();
                        ratingTextView.setVisibility(View.VISIBLE);

                        if(Integer.parseInt(positive) == 0 && Integer.parseInt(negative) == 0){
                            ratingTextView.setText("No one reviewed this driver.");
                        }
                        else { //calculate rating by using thumbs uop and thumbs down from fireStore
                            Integer positiveNum = Integer.parseInt(positive);
                            Integer negativeNum = Integer.parseInt(negative);
                            double rating = Double.parseDouble(positive) / (positiveNum + negativeNum) * 100;
                            ratingTextView.setText(String.format("%.2f", rating) + "% thumbs up out of " + String.format("%d", positiveNum + negativeNum) + " riders");
                        }
                    }

                    //show information
                    nameTextView.setText(profileName);
                    emailTextView.setText(email);
                    phoneTextView.setText(phone);
                    emailTextView.setText(email);
                    roleTextView.setText(role);
                }
            }
        });

        //****************************************************
        //Use shared preference to determine whether set edit button to be visible or not
        //when user click edit button, go to edit profile activity
        final Button editButton = findViewById(R.id.edit_profile_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editP = new Intent(getApplicationContext(), EditProfileActivity.class);
                editP.putExtra("profile_name", profileName);
                startActivity(editP);
            }
        });

    }

    //change the edited valid email and phone to show correctly
    @Override
    protected void onResume() {
        super.onResume();
        // get contact information from firestore and update the textView on ViewProfile
        //**************************************************************************************************
        // the document path should be changed later to use shared preference
        DocumentReference userRef = db.collection("Accounts").document("123");
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    String email = (doc.get("email")).toString();
                    String phone = (doc.get("phone")).toString();
                    TextView phoneTextView = findViewById(R.id.profile_view_phone);
                    TextView emailTextView = findViewById(R.id.profile_view_email);

                    emailTextView.setText(email);
                    phoneTextView.setText(phone);
                }
            }
        });
    }
}