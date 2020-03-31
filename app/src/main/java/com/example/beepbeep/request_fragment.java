package com.example.beepbeep;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class request_fragment extends DialogFragment {
    private TextView show_start;
    private TextView show_end;
    private TextView show_price;

    //set filestore related variables
    private FirebaseFirestore db;
    final String TAG = "Requests";
    //set firestore variable to store
    private String riderID;
    private String pickup_address;
    private String destination_address;
    private String price;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInsatanceState){
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.request_confirm,null);
        show_start = view.findViewById(R.id.show_pickup);
        show_end = view.findViewById(R.id.show_destination);
        show_price = view.findViewById(R.id.show_price);
        //get the uniqueID from Bundle
        Bundle args = this.getArguments();
        final String uniqueID = args.getString("IDkey","id123456");
        //get shared preference and user now
        final SharedPreferences sharedPref = this.getActivity().getSharedPreferences("identity", MODE_PRIVATE);
        final String username = sharedPref.getString("username", "");
        riderID = username;
        //get firesotre information about the request
        //TODO: get the id from the mapsActivity to start showing the inf
        //final DocumentReference docIdRef = db.collection("Requests").document(username);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference RequestIdInf = db.collection("Requests").document(uniqueID);
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
                        destination_address = getAddress(desti_lat,desti_long);

                        //get address of pickup location
                        final GeoPoint pickup = document.getGeoPoint("PickUpPoint");
                        final double pick_lat = pickup.getLatitude();
                        final double pick_long = pickup.getLongitude();
                        pickup_address = getAddress(pick_lat,pick_long);

                        //get price
                        price = document.get("Price").toString();

                        //set view of the fragment
                        String pickupString = "PickUpPoint: "+pickup_address;
                        String destinationString = "Destination: "+destination_address;
                        String priceString = "Estimate Price: "+price;

                        //set String type
                        SpannableString ss1 = new SpannableString(pickupString);
                        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
                        ss1.setSpan(boldSpan,0,12, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        SpannableString ss2 = new SpannableString(destinationString);
                        ss2.setSpan(boldSpan,0,12, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        SpannableString ss3 = new SpannableString(priceString);
                        ss3.setSpan(boldSpan,0,15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        show_start.setText(ss1);
                        show_end.setText(ss2);
                        show_price.setText(ss3);

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
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //update the type
                        Map<String,Object> update_type= new HashMap<>();
                        update_type.put("Type","inactive");
                        RequestIdInf.set(update_type, SetOptions.merge());

                        //change layout from the first show to the second show
                        final RelativeLayout theFirstLayout = getActivity().findViewById(R.id.thefirstshow);
                        theFirstLayout.setVisibility(View.INVISIBLE);
                        final RelativeLayout theSecondLayout = getActivity().findViewById(R.id.thesecondshow);
                        theSecondLayout.setVisibility(View.VISIBLE);
                        //set scroll view
                        TextView scrollStart = getActivity().findViewById(R.id.scroll_start);
                        scrollStart.setText("Start: " + pickup_address);
                        TextView scrollEnd = getActivity().findViewById(R.id.scroll_end);
                        scrollEnd.setText("End: " + destination_address);
                        TextView scrollPrice = getActivity().findViewById(R.id.scroll_price);
                        scrollPrice.setText("Price: " + price);
                        TextView scrollUser = getActivity().findViewById(R.id.scroll_user);
                        scrollUser.setText("User: " + riderID + "\n");
                        TextView scrollD = getActivity().findViewById(R.id.scroll_driver);
                        scrollUser.setText("Driver: Finding.."  + "\n");
                        //set button
                        Button btnCancelRequest = getActivity().findViewById(R.id.btn_cancel_request);
                        btnCancelRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                theFirstLayout.setVisibility(View.VISIBLE);
                                theSecondLayout.setVisibility(View.INVISIBLE);

                                final String[] typenow = new String[1];
                                final DocumentReference doc = db.collection("Requests").document(uniqueID);
                                doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()){
                                            DocumentSnapshot doc = task.getResult();
                                            typenow[0] = (doc.get("Type")).toString();
                                        }
                                    }
                                });

                                if(typenow[0] == "inactive"){
                                    //delete the order history
                                    final DocumentReference Accountref = db.collection("Accounts").document(username);
                                    Accountref.update("order", FieldValue.arrayRemove(uniqueID));
                                    //delete the requestID
                                    db.collection("Requests").document(uniqueID)
                                            .delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error deleting document", e);
                                                }
                                            });
                                }else{
                                    final DocumentReference Accountref = db.collection("Accounts").document(username);
                                    Accountref.update("order", FieldValue.arrayRemove(uniqueID));
                                    Map<String, Object> docData = new HashMap<>();
                                    docData.put("DriverID","");
                                    docData.put("Type","Deleted");
                                    db.collection("Requests").document(uniqueID).update(docData);
                                }

                            }
                        });

                        //add request ID into order history
                        final DocumentReference Accountref = db.collection("Accounts").document(username);
                        Accountref.update("order", FieldValue.arrayUnion(uniqueID));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    //detele the request
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        db.collection("Requests").document(uniqueID)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error deleting document", e);
                                    }
                                });
                    }
                })
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
