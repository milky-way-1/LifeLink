package com.life.lifelink;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.life.lifelink.api.RetrofitClient;
import com.life.lifelink.model.Booking;
import com.life.lifelink.util.MapUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Url;


public class VehicleTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "VehicleTracking";

    private GoogleMap mMap;
    private String bookingId;
    private Marker pickupMarker;
    private Marker dropMarker;
    private Marker driverMarker;
    private boolean isMapReady = false;
    private FirebaseFirestore db;
    private ListenerRegistration locationListener;

    // Location data
    private com.life.lifelink.model.Location pickupLocation;
    private com.life.lifelink.model.Location dropLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_tracking);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        if (!validateAndGetIntentData()) {
            finish();
            return;
        }

        setupMap();
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private boolean validateAndGetIntentData() {
        Intent intent = getIntent();
        if (intent == null) {
            showError("Invalid intent data");
            return false;
        }

        bookingId = intent.getStringExtra("booking_id");
        double pickupLat = intent.getDoubleExtra("pickup_lat", 0);
        double pickupLng = intent.getDoubleExtra("pickup_lng", 0);
        double dropLat = intent.getDoubleExtra("dest_lat", 0);
        double dropLng = intent.getDoubleExtra("dest_lng", 0);

        if (bookingId == null || !isValidCoordinate(pickupLat, pickupLng) ||
                !isValidCoordinate(dropLat, dropLng)) {
            showError("Invalid location data");
            return false;
        }

        pickupLocation = new com.life.lifelink.model.Location(pickupLat, pickupLng);
        dropLocation = new com.life.lifelink.model.Location(dropLat, dropLng);

        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        isMapReady = true;

        setupMapUI();
        addMarkersToMap();
        startTrackingDriver();
    }

    private void setupMapUI() {
        if (mMap == null) return;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    private void addMarkersToMap() {
        if (!isMapReady) return;

        // Add pickup marker
        LatLng pickupLatLng = new LatLng(pickupLocation.getLatitude(), pickupLocation.getLongitude());
        pickupMarker = mMap.addMarker(new MarkerOptions()
                .position(pickupLatLng)
                .title("Pickup Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        // Add destination marker
        LatLng dropLatLng = new LatLng(dropLocation.getLatitude(), dropLocation.getLongitude());
        dropMarker = mMap.addMarker(new MarkerOptions()
                .position(dropLatLng)
                .title("Destination")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        showAllMarkersInView();
    }

    private void startTrackingDriver() {
        locationListener = db.collection("driver_locations")
                .document(bookingId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed", error);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        updateDriverLocation(snapshot);
                    }
                });
    }

    private void updateDriverLocation(DocumentSnapshot snapshot) {
        Double latitude = snapshot.getDouble("latitude");
        Double longitude = snapshot.getDouble("longitude");

        if (latitude == null || longitude == null) {

        }
        LatLng driverLocation = new LatLng(latitude, longitude);

        runOnUiThread(() -> {
            if (driverMarker == null) {
                driverMarker = mMap.addMarker(new MarkerOptions()
                        .position(driverLocation)
                        .title("Ambulance")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                showAllMarkersInView();
            } else {
                driverMarker.setPosition(driverLocation);
            }
        });
    }

    private void showAllMarkersInView() {
        if (!isMapReady) return;

        try {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            if (pickupMarker != null) builder.include(pickupMarker.getPosition());
            if (dropMarker != null) builder.include(dropMarker.getPosition());
            if (driverMarker != null) builder.include(driverMarker.getPosition());

            LatLngBounds bounds = builder.build();
            int padding = 100;
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.animateCamera(cu);
        } catch (Exception e) {
            Log.e(TAG, "Error showing all markers", e);
        }
    }

    private boolean isValidCoordinate(double lat, double lng) {
        if (lat == 0 && lng == 0) return false;
        return lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
    }

    private void showError(String message) {
        runOnUiThread(() -> {

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationListener != null) {
            locationListener.remove();
        }
    }
}