package com.example.beepbeep;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class driverConfirmDialog extends AppCompatDialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final String[] asd = getArguments().getStringArray("key");
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        final LinearLayout tempview = getActivity().findViewById(R.id.temp);
        final LinearLayout butconf = getActivity().findViewById(R.id.but_conf);
        final LinearLayout changeLayout = getActivity().findViewById(R.id.invis_linear);
        final RelativeLayout afterconfirm = getActivity().findViewById(R.id.after_confirm);
        //TODO:
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.request_confirm,null);
        final TextView user = view.findViewById(R.id.driver_scroll_user);
        final TextView start = view.findViewById(R.id.driver_scroll_start);
        final TextView end = view.findViewById(R.id.driver_scroll_end);
        final TextView prices = view.findViewById(R.id.driver_scroll_price);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Request Confirm")
                .setMessage("Accept this request?")
                .setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //change view and update driver in firestore
//                        Toast.makeText(getActivity(), "Request Accepted!", Toast.LENGTH_LONG).show();
                        changeLayout.setVisibility(View.INVISIBLE);
                        Map<String, Object> docData = new HashMap<>();
                        docData.put("DriverID",asd[1]);
                        docData.put("Type","on route");

                        db.collection("Requests")
                                .document(asd[0])
                                .update(docData);

                        //set the new view on driver_request
                        tempview.setVisibility(View.INVISIBLE);
                        butconf.setVisibility(View.INVISIBLE);
                        afterconfirm.setVisibility(View.VISIBLE);
                        db.collection("Requests")
                                .document(asd[0])
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()){
                                            DocumentSnapshot document = task.getResult();
                                            if(document.exists()){
//                                                Toast. makeText(getActivity(),"Hello Javatpoint",Toast. LENGTH_SHORT).show();
                                                //get address of destination location
                                                final GeoPoint destination = document.getGeoPoint("Destination");
                                                final double desti_lat = destination.getLatitude();
                                                final double desti_long = destination.getLongitude();
                                                String destination_address = getAddress(desti_lat, desti_long);

                                                //get address of pickup location
                                                final GeoPoint pickup = document.getGeoPoint("PickUpPoint");
                                                final double pick_lat = pickup.getLatitude();
                                                final double pick_long = pickup.getLongitude();
                                                String pickup_address = getAddress(pick_lat,pick_long);

                                                //get price
                                                String price = document.get("Price").toString();

                                                //get rider
                                                String rider = document.get("RiderID").toString();

                                                //set view of the fragment
                                                String riderString = "User: " + rider;
                                                String pickupString = "PickUpPoint: "+pickup_address;
                                                String destinationString = "Destination: "+destination_address;
                                                String priceString = "Estimate Price: "+price;

                                                //set String type
//                                                SpannableString ss1 = new SpannableString(pickupString);
//                                                StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
//                                                ss1.setSpan(boldSpan,0,12, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                                                SpannableString ss2 = new SpannableString(destinationString);
//                                                ss2.setSpan(boldSpan,0,12, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                                                SpannableString ss3 = new SpannableString(priceString);
//                                                ss3.setSpan(boldSpan,0,5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                                user.setText(riderString);
                                                start.setText(pickupString);
                                                end.setText(destinationString);
                                                prices.setText(priceString);
                                            }
                                        }

                                    }
                                });

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        return builder.create();
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


