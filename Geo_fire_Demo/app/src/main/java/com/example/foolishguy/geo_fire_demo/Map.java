package com.example.foolishguy.geo_fire_demo;


import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Map extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraMoveListener, GeoQueryEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap map;
    private Circle circle;

    private FirebaseAuth firebaseAuth;

    private FirebaseDatabase firebaseDatabase;

    private GeoFire geoFire;

    private GeoQuery geoQuery;

    private GoogleApiClient mApiClient;

    private java.util.Map<String, Marker> markes;

    private static String TAG = "MapActivity";

    private RecyclerView recyclerView;

    private RecyclerView.LayoutManager layoutManager;

    private UserListAdapter userListAdapter;

    private java.util.Map<String, Detail> details;

    private SeekBar seekBar;

    private TextView textViewProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        buildAPIClient();

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

        markes = new HashMap<String, Marker>();

        details = new HashMap<String, Detail>();

        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();

        final DatabaseReference databaseReference = firebaseDatabase.getReference("geofire");

        geoFire = new GeoFire(databaseReference);

        recyclerView = (RecyclerView) findViewById(R.id.userlist);

        layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        seekBar = (SeekBar) findViewById(R.id.seekradius);

        seekBar.setProgress(0);

        seekBar.incrementProgressBy(1);

        seekBar.setMax(20);

        textViewProgress = (TextView) findViewById(R.id.textSeek);

        textViewProgress.setText(String.valueOf(seekBar.getProgress()));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textViewProgress.setText(String.valueOf(seekBar.getProgress()));
                geoQuery.setRadius(seekBar.getProgress());
                circle.setRadius(seekBar.getProgress() * 10);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setOnCameraMoveStartedListener(this);
        map.setOnCameraMoveListener(this);
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
    @Override
    public void onCameraMoveStarted(int i) {

    }

    @Override
    protected void onStart() {
        mApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onCameraMove() {
        CameraPosition cameraPosition = map.getCameraPosition();
        LatLng latLng = cameraPosition.target;
        circle.setCenter(latLng);
        circle.setRadius(seekBar.getProgress() * 10);
        geoQuery.setCenter(new GeoLocation(latLng.latitude, latLng.longitude));

    }

    @Override
    public void onKeyEntered(final String key, GeoLocation location) {
        Log.e ("Map", "KEY : " + key);
        Marker marker = this.map.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)));
        markes.put(key, marker);
        DatabaseReference mref = firebaseDatabase.getReference().child("users/");
        mref.child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                java.util.Map<String, Object> user_data = (HashMap<String, Object>) dataSnapshot.getValue();

                Log.e(TAG, "Name : " + String.valueOf(user_data.get("name")));
                Log.e(TAG, "Phone : " + String.valueOf(user_data.get("phone")));
                details.put(key, new Detail(String.valueOf(user_data.get("name")),
                        String.valueOf(user_data.get("phone"))));
                userListAdapter = new UserListAdapter(details);
                recyclerView.setAdapter(userListAdapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onKeyExited(String key) {
        Marker marker = markes.get(key);
        if (marker != null) {
            marker.remove();
            markes.remove(key);

            details.remove(key);
            userListAdapter = new UserListAdapter(details);
            recyclerView.setAdapter(userListAdapter);
        }
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {

    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(TAG, "API Connected");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mApiClient);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));

        circle = map.addCircle(new CircleOptions().center(latLng).radius(400));

        geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 0.6);

        geoQuery.addGeoQueryEventListener(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "API Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "API Connection Failed");
    }
}
