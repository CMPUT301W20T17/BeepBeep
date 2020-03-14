package com.example.beepbeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

/*
 Title: History Order Activity
 Author: Lyuyang Wang, Junqi Zou
 Date: 2020/03/07
 Last edited: 2020/03/12
*/

/**
 * view the history order and can view the information of other roles in order list by clicking the view button
 */

public class OrderHistoryActivity extends AppCompatActivity{
    private ListView orderList;
    private ArrayList<Order> orderDataList;
    private ArrayAdapter<Order> orderArrayAdapter;
    FirebaseFirestore db;
    final String TAG = "Accounts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        //Get user name from SharedPreferences to see whose order history the activity should display
        final SharedPreferences sharedPref = OrderHistoryActivity.this.getSharedPreferences("identity", Context.MODE_PRIVATE);
        final String userName = sharedPref.getString("username","");

        orderList = findViewById(R.id.history_order_listview);
        orderDataList = new ArrayList<>();
        //Get the document of current user from database
        db = FirebaseFirestore.getInstance();
        DocumentReference userInfo = db.collection("Accounts").document(userName);
        //final List<String> cloned;

        userInfo.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    List<String> orders = (List<String>)document.get("order");
                    //cloned.addAll(orders);

                    if (orders!=null){
                        for (int i = 0; i < orders.size(); i++){
                            db = FirebaseFirestore.getInstance();
                            String name = orders.get(i);
                            DocumentReference doc = db.collection("Requests").document(name);
                            doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        DocumentSnapshot doc = task.getResult();
                                        Order order = doc.toObject(Order.class);
                                        order.setUser(userName);
                                        orderDataList.add(order);
                                        orderArrayAdapter = new OrderList(OrderHistoryActivity.this, orderDataList);
                                        orderList.setAdapter(orderArrayAdapter);
                                        Log.d(TAG, doc.getId() + " => " + doc.getData());
                                    }
                                    else{
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                    }


                    }
                    else{
                        orderArrayAdapter = new OrderList(OrderHistoryActivity.this, orderDataList);
                        orderList.setAdapter(orderArrayAdapter);
                    }
                    Log.d(TAG, document.getId() + " => " + document.getData());
                }
                else{
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
        if (order.getUser().equals(order.getDriverID())){
            profileName = order.getRiderID();
        }
        else{
            profileName = order.getDriverID();
        }

        //change to new intent to view profile
        Intent viewP = new Intent(getApplicationContext(), ViewProfile.class);
        viewP.putExtra("profile_name", profileName);
        startActivity(viewP);
    }
}
