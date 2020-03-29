package com.example.beepbeep;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.firestore.FirebaseFirestore;
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
    private LatLng destination;

    private String pickupName;
    private String destinationName;

    private Marker mpickup;
    private Marker mdestination;

    Polyline currentPolyline;

    private MarkerOptions opickup;
    private MarkerOptions odestination;


    private String uniqueID;
    Button getDirection;

    FloatingActionButton bentoMenu;


    //Request to driver related variables
    private ListView qulifiedListView;
    ArrayList<String> qulifiedListData = new ArrayList<String>();

    ArrayList<String> qulifiedId = new ArrayList<String>();
    ArrayList<String> qulifiedPickUp = new ArrayList<String>();
    ArrayList<String> qulifiedDestination = new ArrayList<String>();
    ArrayList<String> qulifiedPrice = new ArrayList<String>();


    private View mapView;


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

//        qulifiedId.add("User:Loading..");
//        qulifiedPickUp.add("PickUp:Loading..");
//        qulifiedDestination.add("Destination:Loading..");
//        qulifiedPrice.add("Price:Loading..");






        final MyAdapter adapter = new MyAdapter(DriverMapActivity.this, qulifiedId, qulifiedPickUp, qulifiedDestination, qulifiedPrice);
        qulifiedListView.setAdapter(adapter);

        //set the Buttom confirm, and send the request information to firestore
        Button confirm_button;
        confirm_button = findViewById(R.id.confirm_);
        confirm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast toast=Toast. makeText(getApplicationContext(),"Hello Javatpoint",Toast. LENGTH_SHORT);
//                toast. show();


                final LinearLayout changeLayout = DriverMapActivity.this.findViewById(R.id.invis_linear);
                changeLayout.setVisibility(View.VISIBLE);
                //get shared preference and UserName
                final SharedPreferences sharedPref = DriverMapActivity.this.getSharedPreferences("identity", MODE_PRIVATE);
                final String username = sharedPref.getString("username", "");

                //TODO ???
                //connect to firestone
                db = FirebaseFirestore.getInstance();
                db.collection("Requests")
                        .whereEqualTo("Type", "active")
                        .whereEqualTo("DriverID", "")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                final List<String> requestNameList = new ArrayList<>();
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String id = document.getId();
                                        requestNameList.add(id);
                                    }
                                }


                                if (requestNameList != null) {
                                    for (int i = 0; i < requestNameList.size(); i++) {
                                        final String requestName = requestNameList.get(i);
                                        final DocumentReference doc = db.collection("Requests").document(requestName);
                                        doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();
                                                    if (document.exists()) {
                                                        GeoPoint pickupgeo = (GeoPoint) document.get("PickUpPoint");
                                                        float[] disResults = new float[1];
                                                        Location.distanceBetween(pickup.latitude, pickup.longitude, pickupgeo.getLatitude(), pickupgeo.getLongitude(), disResults);
                                                        int some = (int) (disResults[0] / 1000);
                                                        if (some < 5) {
                                                            qulifiedListData.add(requestName);
                                                            db.collection("Requests").document(requestName).get()
                                                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                            if (task.isSuccessful()) {
                                                                                DocumentSnapshot document = task.getResult();
                                                                                if (document.exists()) {
                                                                                    //get address of destination location
                                                                                    final GeoPoint destination = document.getGeoPoint("Destination");
                                                                                    final double desti_lat = destination.getLatitude();
                                                                                    final double desti_long = destination.getLongitude();
                                                                                    String destination_address = getAddress(desti_lat, desti_long);
                                                                                    qulifiedDestination.add("Destination: " + destination_address);

                                                                                    //get address of pickup location
                                                                                    final GeoPoint pickup = document.getGeoPoint("PickUpPoint");
                                                                                    final double pick_lat = pickup.getLatitude();
                                                                                    final double pick_long = pickup.getLongitude();
                                                                                    String pickup_address = getAddress(pick_lat, pick_long);
                                                                                    qulifiedPickUp.add("PickUp: " + pickup_address);

                                                                                    //get price
                                                                                    String price = document.get("Price").toString();
                                                                                    qulifiedPrice.add("Price: " + price);

                                                                                    //get rider
                                                                                    String rider = document.get("RiderID").toString();
                                                                                    qulifiedId.add("User: " + rider);
                                                                                }
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                    }

                                }
//                                qulifiedId.clear();
//                                qulifiedPickUp.clear();
//                                qulifiedDestination.clear();
//                                qulifiedPrice.clear();
                                adapter.notifyDataSetChanged();

                            }
                        });
//                Toast.makeText(DriverMapActivity.this, "size: " + qulifiedId.size(), Toast.LENGTH_SHORT).show();
                qulifiedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        if (i == 0) {
                            Toast.makeText(DriverMapActivity.this, "THE FIRST", Toast.LENGTH_SHORT).show();
                        }
                        if (i == 1) {
                            Toast.makeText(DriverMapActivity.this, "THE 2", Toast.LENGTH_SHORT).show();
                        }
                        if (i == 2) {
                            Toast.makeText(DriverMapActivity.this, "THE 3", Toast.LENGTH_SHORT).show();
                        }
                        if (i == 2) {
                            Toast.makeText(DriverMapActivity.this, "THE 4", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
//                adapter.notifyDataSetChanged();
                //start collecting information of ride
//                qulifiedId.clear();
//                qulifiedPickUp.clear();
//                qulifiedDestination.clear();
//                qulifiedPrice.clear();
//                for (int i = 0; i< qulifiedListData.size(); i++){
//                    db.collection("Requests").document(qulifiedListData.get(i)).get()
//                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                @Override
//                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                    if(task.isSuccessful()){
//                                        DocumentSnapshot document = task.getResult();
//                                        if (document.exists()){
//                                            //get address of destination location
//                                            final GeoPoint destination = document.getGeoPoint("Destination");
//                                            final double desti_lat = destination.getLatitude();
//                                            final double desti_long = destination.getLongitude();
//                                            String destination_address = getAddress(desti_lat,desti_long);
//                                            qulifiedDestination.add("Destination: "+ destination_address);
//
//                                            //get address of pickup location
//                                            final GeoPoint pickup = document.getGeoPoint("PickUpPoint");
//                                            final double pick_lat = pickup.getLatitude();
//                                            final double pick_long = pickup.getLongitude();
//                                            String pickup_address = getAddress(pick_lat,pick_long);
//                                            qulifiedPickUp.add("PickUp: " + pickup_address);
//
//                                            //get price
//                                            String price = document.get("Price").toString();
//                                            qulifiedPrice.add("Price: "+price);
//
//                                            //get rider
//                                            String rider = document.get("RiderID").toString();
//                                            qulifiedId.add("User: "+ rider);
//
//                                        }
//                                    }
//                                }
//                            });
//                }
//                adapter.notifyDataSetChanged();

            }
        });

        //Set the complete button, switch to the make payment activity since it's the rider want to complete
        /*
        Button completeButton;
        completeButton = findViewById(R.id.btn_complete);
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference order = db.collection("Requests").document(uniqueID);
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
            }
        }); */

        //show direction
        getDirection = findViewById(R.id.direction_);
        getDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((odestination != null) && (opickup != null)) {
                    new FetchURL(DriverMapActivity.this).execute(getUrl(opickup.getPosition(), odestination.getPosition(), "driving"), "driving");
                }
            }
        });
//        if ((odestination!=null)&&(opickup!= null)){
//            new FetchURL(RiderMapActivity.this).execute(getUrl(opickup.getPosition(), odestination.getPosition(), "driving"), "driving");
//        }

    }

    private String getAddress(double LAT, double LONG) {
        String address = "";
        Geocoder geocoder = new Geocoder(DriverMapActivity.this, Locale.getDefault());
        try {
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
            } else {
                Log.w("My Current loction address", "No Address returned!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
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


    //TODO:delete the marker after remove the place name auto
    //     change the marker to the round point
    //     auto set the current location as the pick up location at beginning
    //     delete the marker after remove the place name auto
    private void getAutocompletePickup() {
        //search the location by autocomplete
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.location);
        assert autocompleteFragment != null;


        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.LAT_LNG,Place.Field.NAME));
        autocompleteFragment.setHint("Enter location to search requests");
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull final Place place) {
                if (place.getLatLng() != null) {
                    pickup = place.getLatLng();

                    pickupName = place.getName();
                }
//                Toast.makeText(getApplicationContext(), String.valueOf(pickup), Toast.LENGTH_SHORT).show();

                opickup = new MarkerOptions();
                opickup.position(pickup);
                opickup.title(pickupName);
                opickup.zIndex(1.0f);

//                mMap.clear();
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        if (mpickup != null) {
                            mpickup.remove();
                        } else if (pickupName == null) {
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

    private void setGetDirection(){
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
        else{
            Log.i(TAG, "Please enter the pick up location and destination.");
        }
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

        boolean success = googleMap.setMapStyle(new MapStyleOptions(getResources()
                .getString(R.string.style_json)));
        if (!success) {
            Log.e(TAG, "Style parsing failed.");
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                destination = latLng;
                destinationName = getAddress(latLng.latitude,latLng.longitude);
                odestination = new MarkerOptions()
                        .position(new LatLng(latLng.latitude,latLng.longitude))
                        .title(destinationName)
                        .zIndex(1.0f);
                if (mdestination != null){
                    mdestination.remove();
                }
                mdestination = mMap.addMarker(odestination);
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
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
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
