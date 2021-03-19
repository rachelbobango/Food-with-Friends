package com.example.foodwithfriends;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.UploadTask;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import android.location.Location;


import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class NavigationActivity extends FragmentActivity {

    private GoogleApi mGoogleApiClient;
    private Location mLastLocation;

    /*
    For Making changes to handling location storage
    private FirebaseStorage storage;
    private StorageReference storageReference;
     */


    private static final String TAG = "LocationFragment";

    /*fused location provider stuff */
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

   // public static final int PRIORITY_HIGH_ACCURACY = 100;
    private int locationRequestCode = 1000;
    private double wayLatitude = 0.0, wayLongitude = 0.0;

    /*
    For manually having the user turn on GPS if it's not turned on
    public static final int GPS_REQUEST = 1001;
    public static final int LOCATION_REQUEST = 1000;
    private boolean isContinue = true;
    private boolean isGPS = false;
    static final int REQUEST_LOCATION_ACCESS = 1;
    */
    String test = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        Log.d(TAG, "one");
        //
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000); // 10 seconds
        locationRequest.setFastestInterval(5 * 1000); // 5 seconds
        Log.d(TAG, "two");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);
            //Goes to onPermissionResult and gets location if permission is granted by user

        } else {
            //Permission already granted
            Log.d(TAG, "permission already granted, now get location");
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if(location != null){
                    Log.d(TAG, "location not null apparently in first else statement");
                    wayLatitude = location.getLatitude();
                    wayLongitude = location.getLongitude();
                    test += "wayLatitude: " + wayLatitude +  "WayLongitude: " + wayLongitude;
                    //Log.d("location detected", "WayLatitude: " + wayLatitude + "wayLongitude: " + wayLongitude);
                    Log.d("Location detected", test);
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    GeoPoint geoPoint = new GeoPoint(wayLatitude, wayLongitude);

                    db.collection("users")
                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid()).update("location", geoPoint);


                }
            });


        }
        Log.d(TAG, "three");

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult){
                if(locationResult == null){
                    Log.d(TAG, "location returning null");
                    return;
                }
                for(Location location : locationResult.getLocations()){
                    Log.d(TAG, "getting into for loop");
                    if(location != null){
                        Log.d(TAG, "location not null 2");
                        wayLatitude = location.getLatitude();
                        wayLongitude = location.getLongitude();
                        test += "wayLatitude: " + wayLatitude +  "WayLongitude: " + wayLongitude;
                        Toast.makeText(getApplicationContext(), test, Toast.LENGTH_LONG).show();
                        //Log.d("location detected", "WayLatitude: " + wayLatitude + "wayLongitude: " + wayLongitude);
                        Log.d("Location detected", test);
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        GeoPoint geoPoint = new GeoPoint(wayLatitude, wayLongitude);

                        db.collection("users")
                                .document(FirebaseAuth.getInstance().getCurrentUser().getUid()).update("location", geoPoint);

                    }
                }
            }
        };


    }

    /*For adding prompt to turn GPS on for user

    @SuppressLint("MissingPermission")
    private void getLocation(){
        if (ActivityCompat.checkSelfPermission(NavigationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(NavigationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(NavigationActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_REQUEST);

        } else {
            if (isContinue) {
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            } else {
                mFusedLocationClient.getLastLocation().addOnSuccessListener(NavigationActivity.this, location -> {
                    if (location != null) {
                        wayLatitude = location.getLatitude();
                        wayLongitude = location.getLongitude();
                    } else {
                        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    }
                });
            }
        }
    }

    */

    //Suppressing here, since permissions are checked earlier in the code
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Add different types of request to prioritize accuracy or battery life with the switch statement
        switch (requestCode) {
            case 1000: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                        if (location != null) {
                            wayLatitude = location.getLatitude();
                            wayLongitude = location.getLongitude();
                            //txtLocation.setText(String.format(Locale.US, "%s -- %s", wayLatitude, wayLongitude));
                            test += "wayLatitude: " + wayLatitude +  "WayLongitude: " + wayLongitude;
                            Toast.makeText(getApplicationContext(), test, Toast.LENGTH_LONG).show();
                            //Log.d("location detected", "WayLatitude: " + wayLatitude + "wayLongitude: " + wayLongitude);
                            Log.d("Location detected", test);
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            GeoPoint geoPoint = new GeoPoint(wayLatitude, wayLongitude);

                            db.collection("users")
                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid()).update("location", geoPoint);
                        }
                    });
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
    /* Prompt user to turn on gps if it's off after they have given location permissions
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == GPS_REQUEST){
                isGPS = true; //
            }
        }
    }

    */
}