package com.example.foolishguy.geo_fire_demo;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static String TAG = "MainActivity";

    private static int LOC_REQ_CODE = 2;
    private Location mlaLatLng;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mApiClient;

    private GeoQuery geoQuery;

    private GeoFire geoFire;

    private FirebaseAuth auth;

    private FirebaseDatabase database;

    private Button btnSignout;

    private Button setGeoLocation;

    private Button btnView;

    private TextView txtLat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isLocationPermissionProvided()) {
            requestLocationPermission();
        }

        buildAPIClient();

        buildLocationRequest();

        auth = FirebaseAuth.getInstance();

        btnSignout = (Button) findViewById(R.id.btnsignout);
        setGeoLocation = (Button) findViewById(R.id.btnsetGeo);
        txtLat = (TextView) findViewById(R.id.txtlat);
        btnView = (Button) findViewById(R.id.btnView);

        btnSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                stopLocationUpdates();
                finish();
            }
        });

        setGeoLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setGeoLocation();
            }
        });



        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, com.example.foolishguy.geo_fire_demo.Map.class));
            }
        });



    }
    public void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mApiClient, this);
    }
    public synchronized void buildAPIClient () {
        Log.i (TAG, "Building API client");
        if (mApiClient == null) {
            mApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    public void buildLocationRequest () {
        Log.i (TAG, "Building LocationRequest");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mApiClient.connect();
    }

    @Override
    protected void onStop() {
        mApiClient.disconnect();
        super.onStop();
    }


    public boolean isLocationPermissionProvided() {
        int res_1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int res_2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (res_1 == PackageManager.PERMISSION_GRANTED
                    && res_2 == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    public void requestLocationPermission () {
        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION},
                LOC_REQ_CODE);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission();
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient,
              mLocationRequest, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOC_REQ_CODE) {
            if (grantResults.length > 0 && grantResults [0] == PackageManager.PERMISSION_GRANTED
                                        && grantResults [1] == PackageManager.PERMISSION_GRANTED ) {
                Log.i(TAG, "Location Permission granted");
            } else {
                Log.e(TAG, "Location permission denied");
                requestLocationPermission();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Connection Failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        mlaLatLng = location;
        if (mlaLatLng != null) {
            Log.d(TAG, "Location Detail Latitude:" + String.valueOf(mlaLatLng.getLatitude()));
            Log.d(TAG, "Location Detail Longitude:" + String.valueOf(mlaLatLng.getLongitude()));
            setGeoLocation.setVisibility(View.VISIBLE);

            txtLat.setText(String.valueOf(mlaLatLng.getLatitude()));
        }
    }

    public void setGeoLocation () {
        if (mlaLatLng != null) {
            if (auth.getCurrentUser() != null) {
                database = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = database.getReference("geofire");

                geoFire = new GeoFire(databaseReference);
                geoFire.setLocation(auth.getCurrentUser().getUid(),
                        new GeoLocation(mlaLatLng.getLatitude(), mlaLatLng.getLongitude()),
                        new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                if (error == null) {
                                    Log.i(TAG, "onComplete: OKAY SAVED at the key : " + key);
                                } else {
                                    Log.e(TAG, "onComplete: FAILED " + String.valueOf(error));
                                }
                            }
                });
            }
        }
    }
}