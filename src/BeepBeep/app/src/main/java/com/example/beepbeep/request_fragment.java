package com.example.beepbeep;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class request_fragment extends DialogFragment {
    private TextView show_start;
    private TextView show_end;
    private TextView show_price;
    //set filestore related variables
    private FirebaseFirestore db;
    final String TAG = "Requests";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInsatanceState){
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.request_confirm,null);
        show_start = view.findViewById(R.id.show_pickup);
        show_end = view.findViewById(R.id.show_destination);
        show_price = view.findViewById(R.id.show_price);
        //get shared preference and user now
        final SharedPreferences sharedPref = this.getActivity().getSharedPreferences("identity", MODE_PRIVATE);
        final String username = sharedPref.getString("username", "");
        //get firesotre information about the request
        //TODO: get the id from the mapsActivity to start showing the inf
        //final DocumentReference docIdRef = db.collection("Requests").document(username);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference RequestIdInf = db.collection("Requests").document("id123456");
        RequestIdInf.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        //get address of destination location
                        final GeoPoint destination = document.getGeoPoint("Destination");
                        final double desti_lat = destination.getLatitude();
                        final double desti_long = destination.getLongitude();
                        String destination_address = getAddress(desti_lat,desti_long);
                        //get address of pickup location
                        final GeoPoint pickup = document.getGeoPoint("PickUpPoint");
                        final double pick_lat = pickup.getLatitude();
                        final double pick_long = pickup.getLongitude();
                        String pickup_address = getAddress(pick_lat,pick_long);
                        //get price
                        final String price = document.get("Price").toString();
                        show_start.setText("Start: "+pickup_address);
                        show_end.setText("End: "+destination_address);
                        show_price.setText("Price: "+price);

                    }
                    else{
                        Log.d(TAG, "No such document");
                    }
                }
                else{
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder
                .setView(view)
                .setTitle("Request information confirm")
                .setPositiveButton("Confirm",null)
                .setNegativeButton("Cancel",null)
                .create();
    }

    private String getAddress(double LAT, double LONG){
        String address = "";
        Geocoder geocoder = new Geocoder(this.getActivity(), Locale.getDefault());
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
                Log.w("My Current loction address", "No Address returned!");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return address;
    }


}
