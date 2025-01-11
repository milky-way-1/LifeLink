package com.life.lifelink;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
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
import com.life.lifelink.util.MapUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


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

    private MaterialButton cprInfoButton;
    private Polyline driverToPickupPath;
    private TextView distanceText;

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

        distanceText = findViewById(R.id.distanceText);

        setupMap();
        setupCPRButton();
    }

    private void setupCPRButton() {
        cprInfoButton = findViewById(R.id.cprInfoButton);
        cprInfoButton.setOnClickListener(v -> {
            Intent intent = new Intent(VehicleTrackingActivity.this, CprInfoActivity.class);
            startActivity(intent);
        });
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
            return;
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

            // Update distance and path
            updateDistanceAndPath(driverLocation);
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
    private void updateDistanceAndPath(LatLng driverLocation) {
        // Get pickup location
        LatLng pickupLatLng = new LatLng(pickupLocation.getLatitude(), pickupLocation.getLongitude());

        // Calculate and show distance
        calculateDistance(driverLocation, pickupLatLng);

        // Draw path
        getDirections(driverLocation, pickupLatLng);
    }

    private void calculateDistance(LatLng origin, LatLng destination) {
        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?" +
                "origins=" + origin.latitude + "," + origin.longitude +
                "&destinations=" + destination.latitude + "," + destination.longitude +
                "&mode=driving" +
                "&key=" + MapUtil.getApiKey(this);

        new Thread(() -> {
            try {
                URL urlObj = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray rows = jsonResponse.getJSONArray("rows");
                JSONObject elements = rows.getJSONObject(0);
                JSONArray elementsArray = elements.getJSONArray("elements");
                JSONObject element = elementsArray.getJSONObject(0);
                JSONObject distance = element.getJSONObject("distance");
                JSONObject duration = element.getJSONObject("duration");

                String distanceText = distance.getString("text");
                String durationText = duration.getString("text");

                runOnUiThread(() -> {
                    this.distanceText.setText("Distance: " + distanceText + "\nETA: " + durationText);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void getDirections(LatLng origin, LatLng destination) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin.latitude + "," + origin.longitude +
                "&destination=" + destination.latitude + "," + destination.longitude +
                "&mode=driving" +
                "&key=" + MapUtil.getApiKey(this);

        new Thread(() -> {
            try {
                URL urlObj = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray routes = jsonResponse.getJSONArray("routes");
                JSONObject route = routes.getJSONObject(0);
                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                String encodedPath = overviewPolyline.getString("points");

                List<LatLng> decodedPath = decodePolyline(encodedPath);

                runOnUiThread(() -> {
                    if (driverToPickupPath != null) {
                        driverToPickupPath.remove();
                    }

                    PolylineOptions polylineOptions = new PolylineOptions()
                            .addAll(decodedPath)
                            .color(Color.BLUE)
                            .width(10);

                    driverToPickupPath = mMap.addPolyline(polylineOptions);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(p);
        }
        return poly;
    }
}