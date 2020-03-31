package com.example.beepbeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

    FloatingActionButton refreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        // map refresh Button
        refreshButton = findViewById(R.id.history_order_refreshButton);

        // add refresh Button listenser
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
            }
        });

        refresh();
    }

    void refresh(){
        //Get user name from SharedPreferences to see whose order history the activity should display
        final SharedPreferences sharedPref = OrderHistoryActivity.this.getSharedPreferences("identity", Context.MODE_PRIVATE);
        final String userName = sharedPref.getString("username","");

        orderList = findViewById(R.id.history_order_listview);
        orderDataList = new ArrayList<>();

        //Get the document of current user from database
        db = FirebaseFirestore.getInstance();
        DocumentReference userInfo = db.collection("Accounts").document(userName);

        if(hasNetworkAccess()){
            // launch offline record manager
            OrderRecordManager orm = new OrderRecordManager(this);
            orm.saveRecord();

            userInfo.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        List<String> orders = (List<String>) document.get("order");
                        //cloned.addAll(orders);

                        if (orders != null && !orders.isEmpty()){
                            for (int i = orders.size() - 1; i >= 0; i--){
                                db = FirebaseFirestore.getInstance();
                                final String name = orders.get(i);
                                DocumentReference doc = db.collection("Requests").document(name);
                                doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            DocumentSnapshot doc = task.getResult();
                                            if(doc != null && doc.exists()){
                                                Order order = doc.toObject(Order.class);
                                                order.setUser(userName);
                                                orderDataList.add(order);
                                                orderArrayAdapter = new OrderList(OrderHistoryActivity.this, orderDataList);
                                                orderList.setAdapter(orderArrayAdapter);
                                                Log.d(TAG, doc.getId() + " => " + doc.getData());
                                            }else{
                                                Log.d(TAG, "Error getting order: ", task.getException());
                                            }
                                        }else{
                                            Log.d(TAG, "Error getting documents: ", task.getException());
                                        }
                                    }
                                });
                            }
                        }else{
                            orderArrayAdapter = new OrderList(OrderHistoryActivity.this, orderDataList);
                            orderList.setAdapter(orderArrayAdapter);
                        }
                        Log.d(TAG, document.getId() + " => " + document.getData());
                    } else{
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }
            });
        }else{ // if don't have internet
            // launch offline record manager
            OrderRecordManager orm = new OrderRecordManager(this);
            orderDataList = orm.getRecord();
            orderArrayAdapter = new OrderList(OrderHistoryActivity.this, orderDataList);
            orderList.setAdapter(orderArrayAdapter);
        }
    }

    //use the button of VIEW to view other role's information, go to view profile activity
    public void viewOtherProfile(View view){
        ImageView ViewButton = findViewById(R.id.view_contact_button);
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
        //ViewButton.setVisibility(View.VISIBLE);
        Intent viewP = new Intent(getApplicationContext(), ViewProfile.class);
        viewP.putExtra("profile_name", profileName);
        startActivity(viewP);
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
