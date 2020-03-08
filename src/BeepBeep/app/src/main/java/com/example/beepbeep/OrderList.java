package com.example.beepbeep;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;

/*
 Title: OrderList class
 Author: Lyuyang Wang, Junqi Zhou
 Date: 2020/03/07
 Code version: N/A
 Availability: https://stackoverflow.com/questions/53332471/checking-if-a-document-exists-in-a-firestore-collection/53335711
*/

/**
 * orderList class is the list of all orders
 */

public class OrderList extends ArrayAdapter<Order> {
    private ArrayList<Order> orders;
    private Context context;
    private FirebaseFirestore db;


    public OrderList(@NonNull Context context, ArrayList<Order> orders) {
        super(context, 0,orders);
        this.orders = orders;
        this.context = context;

    }

    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //This function allow us to change the values in the view
        View view = convertView;

        if (view == null) {//If the ‘convertView’ object holds nothing, then we inflate the ‘content.xml’
            view = LayoutInflater.from(context).inflate(R.layout.order_content, parent, false);
        }

        Order order = orders.get(position);

        TextView otherIdentity = view.findViewById(R.id.name_rider_driver);
        TextView otherName = view.findViewById(R.id.rider_driver_text);
        TextView date = view.findViewById(R.id.date_text);
        TextView startLoca = view.findViewById(R.id.start_loca_text);
        TextView destination = view.findViewById(R.id.destination_text);

        //Set up the prompt for other user's name, and the order's information
        //if current user is driver, the get information about respond rider.
        //if current user is rider, then get information about respond driver.
        if (order.getUser().equals(order.getDriver())) {
            otherName.setText(order.getRider());
            otherIdentity.setText("   Rider:");
        }
        else{
            otherName.setText(order.getDriver());
            otherIdentity.setText("   Driver:");
        }

        date.setText(order.getDate());
        startLoca.setText(order.getStartLocation());
        destination.setText(order.getDestination());

        return view;
    }
}