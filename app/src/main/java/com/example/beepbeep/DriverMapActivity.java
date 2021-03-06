package com.example.beepbeep;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;


import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.Distribution;
import com.google.firebase.firestore.CollectionReference;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.protobuf.Internal;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class DriverMapActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {
    FirebaseFirestore db;


    private GoogleMap mMap;
    private static final String TAG = RiderMapActivity.class.getSimpleName();

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // The entry point to the Places API.
    private PlacesClient mPlacesClient;


    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;

    // define the permission request
    static public final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;


    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;

    //set two geo point
    private LatLng pickup;
    private LatLng destinationLat;

    private String pickupName;
    private String destinationName;

    private Marker mpickup;
    private Marker mdestination;

    Polyline currentPolyline;

    private MarkerOptions opickup;
    private MarkerOptions odestination;

    AutocompleteSupportFragment autocompletePickup;


    private String uniqueID;
    Button getDirection;

    FloatingActionButton bentoMenu;


    int count;
    //Request to driver related variables
    private ListView qulifiedListView;
    ArrayList<String> qulifiedListData = new ArrayList<String>();

    ArrayList<String> qulifiedId = new ArrayList<String>();
    ArrayList<String> qulifiedPickUp = new ArrayList<String>();
    ArrayList<String> qulifiedDestination = new ArrayList<String>();
    ArrayList<String> qulifiedPrice = new ArrayList<String>();

    private View mapView;

    private AlertDialog deleteNotice;
    private AlertDialog compeleNotice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            CameraPosition mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_driver_main);

        //setup the bentomenu on the activity screen
        bentoMenu = findViewById(R.id.bentoView_);

        SharedPreferences sharedPref = this.getSharedPreferences("identity", Context.MODE_PRIVATE);
        final String username = sharedPref.getString("username", "");
        bentoMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a = new Intent(DriverMapActivity.this, Menu.class);
                startActivity(a);
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();
        autocompletePickup = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.location);
        // Construct a PlacesClient
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }
        mPlacesClient = Places.createClient(this);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        //active the autocomplete place selection for pickup location
        getAutocompletePickup();

        //set data list and adapter
        qulifiedListView = findViewById(R.id.qulifiedRequest);


        final MyAdapter adapter = new MyAdapter(DriverMapActivity.this, qulifiedId, qulifiedPickUp, qulifiedDestination, qulifiedPrice);
        qulifiedListView.setAdapter(adapter);

        //set the Buttom confirm, and send the request information to firestore
        Button confirm_button;
        confirm_button = findViewById(R.id.confirm_);
        confirm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pickupName != null) {
                    final LinearLayout changeLayout = DriverMapActivity.this.findViewById(R.id.invis_linear);
                    changeLayout.setVisibility(View.VISIBLE);

                    //get shared preference and UserName
                    final SharedPreferences sharedPref = DriverMapActivity.this.getSharedPreferences("identity", MODE_PRIVATE);
                    final String username = sharedPref.getString("username", "");

                    //connect to firestone
                    db = FirebaseFirestore.getInstance();
                    db.collection("Requests")
                            .whereEqualTo("Type", "inactive")
                            .whereEqualTo("DriverID", "")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    final List<String> requestNameList = new ArrayList<>();
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                            String id = document.getId();
                                            requestNameList.add(id);
                                        }
                                    }
                                    if (requestNameList != null) {
                                        count = 0;
                                        for (int i = 0; i < requestNameList.size(); i++) {
                                            final String requestName = requestNameList.get(i);
                                            final DocumentReference doc = db.collection("Requests").document(requestName);
                                            doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        DocumentSnapshot document = task.getResult();
                                                        assert document != null;
                                                        if (document.exists()) {
                                                            GeoPoint pickupgeo = (GeoPoint) document.get("PickUpPoint");
                                                            float[] disResults = new float[1];
                                                            assert pickupgeo != null;
                                                            Location.distanceBetween(pickup.latitude, pickup.longitude, pickupgeo.getLatitude(), pickupgeo.getLongitude(), disResults);
                                                            int some = (int) (disResults[0] / 1000);
                                                            if (some < 5) {
                                                                setQulifiedData(requestName, adapter);
                                                            } else {
                                                                count += 1;
                                                            }
                                                            if (count == requestNameList.size() && changeLayout.getVisibility() == View.VISIBLE) {
                                                                changeLayout.setVisibility(View.INVISIBLE);
                                                                Toast toast = Toast.makeText(getApplicationContext(), "There is no request appear during 5km round.", Toast.LENGTH_SHORT);
                                                                toast.show();
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                        adapter.notifyDataSetChanged();
                                        if (requestNameList.size() == 0) {
                                            changeLayout.setVisibility(View.INVISIBLE);
                                            Toast toast = Toast.makeText(getApplicationContext(), "There is no request appear during 5km round.", Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                    }
                                    qulifiedId.clear();
                                    qulifiedPickUp.clear();
                                    qulifiedDestination.clear();
                                    qulifiedPrice.clear();

                                }
                            });
                    qulifiedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            String theRequestID = qulifiedListData.get(i);
                            uniqueID = theRequestID;
                            opendialog(theRequestID, username);
                            final DocumentReference Accountref = db.collection("Accounts").document(username);
                            Accountref.update("order", FieldValue.arrayUnion(uniqueID));
                        }
                    });
                } else {
                    Toast errorToast = Toast.makeText(getApplicationContext(), "Please enter the location to search requests.", Toast.LENGTH_SHORT);
                    errorToast.show();
                }
            }
        });

        //TODO: TO START BUTTON
        final Button toStartButton = findViewById(R.id.ToStartBtn);
        toStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> docData = new HashMap<>();
                docData.put("Type","inprocess");
                DocumentReference order = db.collection("Requests").document(uniqueID);
                order.update(docData);
                Toast.makeText(DriverMapActivity.this,"Route start",Toast. LENGTH_SHORT).show();
                toStartButton.setVisibility(view.INVISIBLE);
            }
        });

        //Set the complete button, switch to the make payment activity since it's the rider want to complete
        Button completeButton;
        completeButton = findViewById(R.id.driver_btn_complete);
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference order = db.collection("Requests").document(uniqueID);
                order.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot doc = task.getResult();
                            String typenow =   doc.getString("Type");
                            if (typenow.equals("inprocess")){
                                Map<String, Object> docData = new HashMap<>();
                                Date finishTime = Calendar.getInstance().getTime();
                                String finishTime2 = finishTime.toString();
                                docData.put("Type","completed");
                                docData.put("FinishTime",finishTime2);
                                db.collection("Requests")
                                        .document(uniqueID)
                                        .update(docData);
                            }
                            else{
                                Toast.makeText(getApplicationContext(),"Cannot complete route now", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                });
            }
        });
    }

    public void setGetDirection(){
        odestination = new MarkerOptions();
        odestination.position(destinationLat);
        odestination.title(destinationName);
        odestination.zIndex(1.0f);
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                if (mdestination != null) {
                    mdestination.remove();
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationLat, 11));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLat, 12.0f));
                mdestination = mMap.addMarker(odestination);
                if (odestination != null && opickup != null) {
                    new FetchURL(DriverMapActivity.this).execute(getUrl(opickup.getPosition(), odestination.getPosition(), "driving"), "driving");
                }
                else if (opickup == null && odestination != null && pickup != null){
                    opickup = new MarkerOptions();
                    opickup.position(pickup);
                    opickup.title(pickupName);
                    opickup.zIndex(1.0f);
                    opickup.icon(getBitmapFromVector(getApplicationContext(), R.drawable.ic_custom_map_marker));
                    mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickup, 11));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pickup, 15.0f));
                            mpickup = mMap.addMarker(opickup);
                        }
                    });
                    new FetchURL(DriverMapActivity.this).execute(getUrl(opickup.getPosition(), odestination.getPosition(), "driving"), "driving");
                }
            }
        });
    }

    public void setQulifiedData(final String requestName, final MyAdapter adapter) {
        qulifiedListData.add(requestName);
        db.collection("Requests").document(requestName).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            assert document != null;
                            if (document.exists()) {
                                //get address of destination location
                                final GeoPoint destination = document.getGeoPoint("Destination");
                                assert destination != null;
                                final double desti_lat = destination.getLatitude();
                                final double desti_long = destination.getLongitude();
                                String destination_address = getAddress(desti_lat, desti_long);
                                qulifiedDestination.add("Destination: " + destination_address);

                                //get address of pickup location
                                final GeoPoint pickup = document.getGeoPoint("PickUpPoint");
                                assert pickup != null;
                                final double pick_lat = pickup.getLatitude();
                                final double pick_long = pickup.getLongitude();
                                String pickup_address = getAddress(pick_lat, pick_long);
                                qulifiedPickUp.add("PickUp: " + pickup_address);

                                //get price
                                String price = Objects.requireNonNull(document.get("Price")).toString();
                                qulifiedPrice.add("Price: " + "$" + price);

                                //get rider
                                String rider = Objects.requireNonNull(document.get("RiderID")).toString();
                                qulifiedId.add("User: " + rider);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }

    class MyAdapter extends ArrayAdapter<String> {
        Context context;
        ArrayList<String> listviewID;
        ArrayList<String> listviewPickUp;
        ArrayList<String> listviewDestination;
        ArrayList<String> listviewPrice;

        MyAdapter(Context c, ArrayList listviewID, ArrayList listviewPickUp, ArrayList listviewDestination, ArrayList listviewPrice) {
            super(c, R.layout.request_row, R.id.listview_id, listviewID);
            this.context = c;
            this.listviewID = listviewID;
            this.listviewPickUp = listviewPickUp;
            this.listviewDestination = listviewDestination;
            this.listviewPrice = listviewPrice;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.request_row, parent, false);
            ImageView listviewImage = row.findViewById(R.id.listview_image);
            TextView myID = row.findViewById(R.id.listview_id);
            TextView myPickUp = row.findViewById(R.id.listview_start);
            TextView myDestinaiton = row.findViewById(R.id.listview_end);
            TextView myPrice = row.findViewById(R.id.listview_price);

            //set the data in listivew
            myID.setText(listviewID.get(position));
            myPickUp.setText(listviewPickUp.get(position));
            myDestinaiton.setText(listviewDestination.get(position));
            myPrice.setText(listviewPrice.get(position));

            return row;
        }
    }

    public void opendialog(final String requestID, final String username){
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        final LinearLayout tempview = DriverMapActivity.this.findViewById(R.id.temp);
        final LinearLayout butconf = DriverMapActivity.this.findViewById(R.id.but_conf);
        final LinearLayout changeLayout = DriverMapActivity.this.findViewById(R.id.invis_linear);
        final RelativeLayout afterconfirm = DriverMapActivity.this.findViewById(R.id.after_confirm);
        final TextView user = DriverMapActivity.this.findViewById(R.id.driver_scroll_user);
        final TextView driver = DriverMapActivity.this.findViewById(R.id.driver_scroll_driver);
        final TextView start = DriverMapActivity.this.findViewById(R.id.driver_scroll_start);
        final TextView end = DriverMapActivity.this.findViewById(R.id.driver_scroll_end);
        final TextView prices = DriverMapActivity.this.findViewById(R.id.driver_scroll_price);
        AlertDialog.Builder builder = new AlertDialog.Builder(DriverMapActivity.this);
        builder.setTitle("Request Confirm")
                .setMessage("Accept this request?")
                .setNegativeButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        changeLayout.setVisibility(View.INVISIBLE);
                        Map<String, Object> docData = new HashMap<>();
                        docData.put("DriverID",username);
                        docData.put("Type","active");

                        db.collection("Requests")
                                .document(requestID)
                                .update(docData);

                        //set the new view on driver_request
                        tempview.setVisibility(View.INVISIBLE);
                        butconf.setVisibility(View.INVISIBLE);
                        afterconfirm.setVisibility(View.VISIBLE);
                        db.collection("Requests")
                                .document(requestID)
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
                                                destinationName = destination_address;
                                                destinationLat = new LatLng(destination.getLatitude(), destination.getLongitude());
                                                setGetDirection();

                                                //get address of pickup location
                                                final GeoPoint pickup = document.getGeoPoint("PickUpPoint");
                                                final double pick_lat = pickup.getLatitude();
                                                final double pick_long = pickup.getLongitude();
                                                String pickup_address = getAddress(pick_lat,pick_long);

                                                //get price
                                                String price = document.get("Price").toString();

                                                //get rider
                                                final String rider = document.get("RiderID").toString();

                                                //set view of the fragment
                                                String riderString = "User: " + rider;
                                                String pickupString = "PickUpPoint: "+pickup_address;
                                                String destinationString = "Destination: "+destination_address;
                                                String priceString = "Price: "+ "$" + price;
                                                String driverString = "Driver: " + username + "\n";

                                                //set String type
                                                SpannableString ss1 = new SpannableString(pickupString);
                                                StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
                                                ss1.setSpan(boldSpan,0,12, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                SpannableString ss2 = new SpannableString(destinationString);
                                                ss2.setSpan(boldSpan,0,12, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                SpannableString ss3 = new SpannableString(priceString);
                                                ss3.setSpan(boldSpan,0,6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                                SpannableString ss = new SpannableString(riderString);
                                                int len_rider = rider.length();
                                                ForegroundColorSpan fcsBlue = new ForegroundColorSpan(Color.BLUE);
                                                ss.setSpan(fcsBlue, 5, 6 + len_rider, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                user.setText(ss);
                                                user.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        Intent profile = new Intent(DriverMapActivity.this, ViewProfile.class);
                                                        profile.putExtra("profile_name", rider);
                                                        startActivity(profile);
                                                    }
                                                });
                                                driver.setText(driverString);
                                                start.setText(ss1);
                                                end.setText(ss2);
                                                prices.setText(ss3);
                                            }
                                        }

                                    }
                                });
                    }
                })
                .setPositiveButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
        final DocumentReference docRef = db.collection("Requests").document(uniqueID);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                if(documentSnapshot != null && documentSnapshot.exists()){
                    final String type = documentSnapshot.get("Type").toString();
                    if(type.equals("Deleted")){
                        if (deleteNotice != null && deleteNotice.isShowing()) return;
                        AlertDialog.Builder builder = new AlertDialog.Builder(DriverMapActivity.this);
                        builder.setTitle("Important Message")
                                .setMessage("Your request was canceled by rider.")
                                .setPositiveButton("Fine", new DialogInterface.OnClickListener() {
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
                                });
                        deleteNotice = builder.create();
                        deleteNotice.show();
                        RelativeLayout changeLayout = DriverMapActivity.this.findViewById(R.id.after_confirm);
                        LinearLayout butconf = DriverMapActivity.this.findViewById(R.id.but_conf);
                        LinearLayout tempview = DriverMapActivity.this.findViewById(R.id.temp);
                        changeLayout.setVisibility(View.INVISIBLE);
                        butconf.setVisibility(View.VISIBLE);
                        tempview.setVisibility(View.VISIBLE);
                        //TODO: remove direction first time not work
                        //      the cancel dialog pop up sevral times
                        pickup = null;
                        pickupName = null;
                        opickup = null;
                        if (odestination != null) {
                            destinationLat = null;
                            destinationName = null;
                            odestination = null;
                        }
                        autocompletePickup.setText("");
                        if (mpickup != null) {
                            mpickup.remove();
                        }
                        if (mdestination != null){
                            mdestination.remove();
                            mMap.clear();
                        }
                    }
                }
            }
        });
        checkComplete();
    }
    private void checkComplete(){
        final DocumentReference docRef = db.collection("Requests").document(uniqueID);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                if(documentSnapshot != null && documentSnapshot.exists()){
                    final String type = documentSnapshot.get("Type").toString();
                    if(type.equals("completed")){
                        DocumentReference order = db.collection("Requests").document(uniqueID);
                        final Button toStart = findViewById(R.id.ToStartBtn);
                        toStart.setVisibility(View.VISIBLE);
                        final LinearLayout theFirstLayout = findViewById(R.id.temp);
                        theFirstLayout.setVisibility(View.VISIBLE);
                        final LinearLayout butconf = findViewById(R.id.but_conf);
                        butconf.setVisibility(View.VISIBLE);
                        final RelativeLayout theSecondLayout = findViewById(R.id.after_confirm);
                        theSecondLayout.setVisibility(View.INVISIBLE);
                        order.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentSnapshot doc = task.getResult();
                                    String price = (doc.get("Price")).toString();
                                    Intent a = new Intent(DriverMapActivity.this, ReceivePayment.class);
                                    a.putExtra("Price", price);
                                    startActivity(a);
                                }
                            }
                        });
                        if (compeleNotice != null && compeleNotice.isShowing()) return;
                        final AlertDialog.Builder builder = new AlertDialog.Builder(DriverMapActivity.this);
                        builder.setTitle("Request Notification")
                                .setMessage("Your request is completed !")
                                .setPositiveButton("OK", null);
                                compeleNotice = builder.create();
                                compeleNotice.show();
                    }
                }
            }
        });
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }


    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
    }

    private void getAutocompletePickup() {
        //search the location by autocomplete
        autocompletePickup.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.LAT_LNG,Place.Field.NAME));
        autocompletePickup.setHint("Enter location to search requests");
        autocompletePickup.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull final Place place) {
                if (place.getLatLng() != null) {
                    pickup = place.getLatLng();

                    pickupName = place.getName();
                }
                opickup = new MarkerOptions();
                opickup.position(pickup);
                opickup.title(pickupName);
                opickup.zIndex(1.0f);
                opickup.icon(getBitmapFromVector(getApplicationContext(),R.drawable.ic_custom_map_marker));
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        if (mpickup != null) {
                            mpickup.remove();
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickup, 11));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pickup, 12.0f));
                        mpickup = mMap.addMarker(opickup);
                    }
                });
            }
            @Override
            public void onError(@NonNull Status status) {
                Log.i("PickUp", "An error occurred: " + status);

            }
        });
        View clearButton = autocompletePickup.getView().findViewById(R.id.places_autocomplete_clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickup = null;
                pickupName = null;
                opickup = null;
                if (odestination != null) {
                    destinationLat = null;
                    destinationName = null;
                    odestination = null;
                }
                autocompletePickup.setText("");
                if (mpickup != null) {
                    mpickup.remove();
                }
                if (mdestination != null){
                    mdestination.remove();
                    mMap.clear();
                }
                LinearLayout linearLayout = DriverMapActivity.this.findViewById(R.id.invis_linear);
                if (linearLayout.getVisibility() == View.VISIBLE){
                    linearLayout.setVisibility(View.INVISIBLE);
                    qulifiedListData.clear();
                }
            }
        });
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private String getAddress(double LAT, double LONG){
        String address = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
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

    private BitmapDescriptor getBitmapFromVector(@NonNull Context context, @DrawableRes int vectorResourceId){
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResourceId);
        if (vectorDrawable == null) {
            Log.e(TAG, "Requested vector resource was not found");
            return BitmapDescriptorFactory.defaultMarker();
        }
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        SharedPreferences sharedPref = DriverMapActivity.this.getSharedPreferences("identity", Context.MODE_PRIVATE);
        Boolean darkmode = sharedPref.getBoolean("darkmode", false);
        if (darkmode) {
            boolean success = googleMap.setMapStyle(new MapStyleOptions(getResources()
                    .getString(R.string.style_json)));
            getDelegate().setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        }else{
            boolean success = googleMap.setMapStyle(new MapStyleOptions(getResources()
                    .getString(R.string.standard)));
            getDelegate().setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        }


        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                pickup = latLng;
                pickupName = getAddress(latLng.latitude,latLng.longitude);
                opickup = new MarkerOptions()
                        .position(new LatLng(latLng.latitude,latLng.longitude))
                        .title(destinationName)
                        .zIndex(1.0f)
                        .icon(getBitmapFromVector(getApplicationContext(), R.drawable.ic_custom_map_marker));
                if (mpickup != null){
                    mpickup.remove();
                }
                autocompletePickup.setText(pickupName);
                mpickup = mMap.addMarker(opickup);
            }
        });



        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            // position on right bottom
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            rlp.setMargins(0,0,40,350);
        }


        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == AutocompleteActivity.RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(intent);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(intent);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == AutocompleteActivity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = (Location) task.getResult();
                            assert mLastKnownLocation != null;
                            pickup =  new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                            pickupName = getAddress(pickup.latitude,pickup.longitude);
                            autocompletePickup.setText(pickupName);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }
}