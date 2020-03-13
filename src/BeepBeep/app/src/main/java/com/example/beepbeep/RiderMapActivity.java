package com.example.beepbeep;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.Timestamp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/*
 Title: Rider map activity class
 Author: Junyao Cui, Xiyuan Shen
 Date: 2020/03/07
 Code version: 2.1
 Availability: https://stackoom.com/question/2Zl7c/%E5%9C%A8%E8%87%AA%E5%8A%A8%E5%AE%8C%E6%88%90%E6%90%9C%E7%B4%A2%E4%BD%8D%E7%BD%AE%E8%AE%BE%E7%BD%AE%E6%A0%87%E8%AE%B0
*/

import java.util.Map;
import java.util.UUID;




public class RiderMapActivity extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback{

    final String TAG1 = "Account";
    FirebaseFirestore db;
    DocumentReference ref;


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

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private List[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;

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

    //
    Polyline currentPolyline;

    private MarkerOptions opickup;
    private MarkerOptions odestination;


    private String uniqueID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            CameraPosition mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_rider_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Construct a PlacesClient
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }
        mPlacesClient = Places.createClient(this);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //active the autocomplete place selection for destination
        getAutocompleteDestination();

        //active the autocomplete place selection for pickup location
        getAutocompletePickup();

        //set the Buttom confirm, and send the request information to firestore
        uniqueID = UUID.randomUUID().toString();
        Button confirm_button;
        confirm_button = findViewById(R.id.confirm);
        confirm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get shared preference and user now
                final SharedPreferences sharedPref = RiderMapActivity.this.getSharedPreferences("identity", MODE_PRIVATE);
                final String username = sharedPref.getString("username", "");
                //connect to firestore and get unique ID
                db = FirebaseFirestore.getInstance();

                Map<String, Object> docData = new HashMap<>();

                //prepare the data in specific type
                Date startTime = Calendar.getInstance().getTime(); //start time
                String startTime2 = startTime.toString();
                //get lat and long
                double pickupLat = pickup.latitude; //pickup geolocation
                double pickupLng =  pickup.longitude;
//                double pickupLat = 53.542100;
//                double pickupLng = -113.507890;
                GeoPoint pickupGeo = new GeoPoint(pickupLat,pickupLng);
                double destinLat = destination.latitude; //destination geolocation
                double destinLng = destination.longitude;
//                double destinLat = 53.523220 ;
//                double destinLng = -113.526321;
                GeoPoint destinaitonGeo = new GeoPoint(destinLat,destinLng);
//                Toast.makeText(getApplicationContext(), String.valueOf(pickupLat), Toast.LENGTH_SHORT).show();
//                Toast.makeText(getApplicationContext(), String.valueOf(destinLat), Toast.LENGTH_SHORT).show();

                //set the storing data
                docData.put("Type", "inactive");
                docData.put("RiderID", username);
                docData.put("DriverID", "");
                docData.put("StartTime",startTime2);
                docData.put("FinishTime","");
                docData.put("Price",20);
                docData.put("PickUpPoint",pickupGeo);
                docData.put("Destination",destinaitonGeo);

                //connect to firestore and store the data
                db.collection("Requests").document(uniqueID)
                        .set(docData)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully written!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error writing document", e);
                            }
                        });

                //pass the unique ID into the fragment
                Bundle bundle = new Bundle();
                bundle.putString("IDkey",uniqueID);
                request_fragment request_frag = new request_fragment();
                request_frag.setArguments(bundle);
                request_frag.show(getSupportFragmentManager(),"SHOW_REQUEST");

            }
        });

//        new FetchURL(RiderMapActivity.this).execute(getUrl(opickup.getPosition(), odestination.getPosition(), "driving"), "driving");

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

    private String getUrl(LatLng origin, LatLng dest, String directionMode){
        //origin of route
        String str_origin = "origin" + origin.latitude +","+origin.longitude;
        //destination of route
        String star_dest = "destination"+dest.latitude+","+dest.longitude;
        //mode
        String mode = "mode="+directionMode;
        //building the parameters to the web service
        String parameter = str_origin+"&"+star_dest+"&"+mode;
        //output format
        String output = "jeson";
        //building the url to the web service
        String url = "//maps.googleapis.com/maps/api/directions/"+output+"?"+parameter+"&keys"+getString(R.string.google_api_key);
        return url;
    }



    //TODO:delete the marker after remove the place name auto
    private void getAutocompleteDestination() {
        //search the location by autocomplete
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.destination);
        assert autocompleteFragment != null;

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull final Place place) {
                if (place.getLatLng() != null){
                    destination = place.getLatLng();
                    destinationName = place.getName();
                }
//                Toast.makeText(getApplicationContext(), String.valueOf(destination), Toast.LENGTH_SHORT).show();

                odestination = new MarkerOptions();
                odestination.position(destination);
                odestination.title(destinationName);
//                mMap.clear();
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        if (mdestination != null){
                            mdestination.remove();
                        }else if(destinationName == null){
                            mdestination.remove();
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 11));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destination, 12.0f));
                        mdestination = mMap.addMarker(odestination);
                    }
                });
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i("Destination", "An error occurred: " + status);

            }
        });
    }

    //TODO:delete the marker after remove the place name auto
    //     change the marker to the round point
    //     auto set the current location as the pick up location at beginning
    private void getAutocompletePickup() {
        //search the location by autocomplete
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.pickup_location);
        assert autocompleteFragment != null;

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.LAT_LNG,Place.Field.NAME));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull final Place place) {
                if (place.getLatLng() != null){
                    pickup = place.getLatLng();

                    pickupName = place.getName();
                }
//                Toast.makeText(getApplicationContext(), String.valueOf(pickup), Toast.LENGTH_SHORT).show();

                opickup = new MarkerOptions();
                opickup.position(pickup);
                opickup.title(pickupName);
//                mMap.clear();
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        if (mpickup != null){
                            mpickup.remove();
                        }else if(pickupName == null){
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
        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
//        mMap.addMarker(new MarkerOptions().position(new LatLng(pickup.latitude, pickup.longitude)).title("Maker"));

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.map_info_content,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = infoWindow.findViewById(R.id.title);
                title.setText(marker.getTitle());

                TextView snippet = infoWindow.findViewById(R.id.snippet);
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });
//        if(mMap != null){
//            mMap.addMarker(new MarkerOptions().position(pickup).title("Pick-Up"));
//        }


        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

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
        } catch (SecurityException e)  {
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
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

}
