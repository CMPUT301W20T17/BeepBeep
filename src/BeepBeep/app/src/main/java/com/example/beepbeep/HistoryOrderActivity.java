package com.example.beepbeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;

/*
 Title: History Order Activity
 Author: Lyuyang Wang, Junqi Zhou
 Date: 2020/03/07
 Code version: N/A
 Availability: https://stackoverflow.com/questions/53332471/checking-if-a-document-exists-in-a-firestore-collection/53335711
*/

/**
 * view the history order and can view the information of other roles in order list by clicking the view button
 */

public class HistoryOrderActivity extends AppCompatActivity{
    private ListView orderList;
    private ArrayList<Order> orderDataList;
    private ArrayAdapter<Order> orderArrayAdapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    final String TAG = "Accounts";
    private CollectionReference collectionReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        //change to shared preference
        //****************************************************************************

        Intent intent = getIntent();
        final String user = intent.getStringExtra("profile_name");

        orderList = findViewById(R.id.history_order_listview);
        orderDataList = new ArrayList<>();

        //go to firStore and read all ride history form collection
        collectionReference = db.collection("Accounts")
                .document(user)
                .collection("order");

        collectionReference
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //add to the order list history from fireStore
                                Order order = document.toObject(Order.class);
                                order.setUser(user);
                                orderDataList.add(order);
                                orderArrayAdapter = new OrderList(HistoryOrderActivity.this, orderDataList);
                                orderList.setAdapter(orderArrayAdapter);
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    //use the button of VIEW to view other role's information, go to view profile activity
    public void viewOtherProfile(View view){
        final int position = orderList.getPositionForView((View)view.getParent());
        Order order = orderDataList.get(position);
        String profileName;
        if (order.getUser().equals(order.getDriver())){
            profileName = order.getRider();
        }
        else{
            profileName = order.getDriver();
        }

        //change to new intent to view profile
        Intent viewP = new Intent(getApplicationContext(), ViewProfile.class);
        viewP.putExtra("profile_name", profileName);
        startActivity(viewP);
    }
}