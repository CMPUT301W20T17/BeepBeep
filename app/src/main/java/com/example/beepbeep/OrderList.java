package com.example.beepbeep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

/*
 Title: OrderList class
 Author: Lyuyang Wang, Junqi Zou
 Date: 2020/03/07
 Code version: N/A
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
        TextView startTime = view.findViewById(R.id.start_time_text);
        TextView endTime = view.findViewById(R.id.end_time_text);
        TextView pickUpPoint = view.findViewById(R.id.start_loca_text);
        TextView destination = view.findViewById(R.id.destination_text);
        TextView price = view.findViewById(R.id.price_text);
        TextView status = view.findViewById(R.id.status_text); //same as Type in database
        ImageView ViewButton = view.findViewById(R.id.view_contact_button);

        //Set up the prompt for other user's name, and the order's information
        //if current user is driver, the get information about respond rider.
        //if current user is rider, then get information about respond driver.
        if (order.getRiderID()==""||order.getDriverID()==""){
            ViewButton.setVisibility(View.INVISIBLE);
        }
        else{
            //change to new intent to view profile
            ViewButton.setVisibility(View.VISIBLE);
        }
        if (order.getUser().equals(order.getDriverID())) {
            otherName.setText(order.getRiderID());
            otherIdentity.setText("   Rider:");
        }
        else{
            otherName.setText(order.getDriverID());
            otherIdentity.setText("   Driver:");
        }

        startTime.setText(order.getStartTime());
        endTime.setText(order.getFinishTime());
        pickUpPoint.setText(getAddress(order.getPickupPoint()));
        destination.setText(getAddress(order.getDestination()));
        price.setText(order.getPrice() + " CAD");
        status.setText(order.getType());


        return view;
    }

    private String getAddress(GeoPoint location){
        double LAT  = location.getLatitude();
        double LONG = location.getLongitude();
        String address = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try{
            //get address in list
            List<Address> addresses = geocoder.getFromLocation(LAT, LONG, 1);
            //if there is address
            if (addresses != null) {
                //get the returned addresses
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");
                //set the returned address in string
                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                address = strReturnedAddress.toString();
            }
            else{
                Log.w("My Current location address", "No Address returned!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }
}