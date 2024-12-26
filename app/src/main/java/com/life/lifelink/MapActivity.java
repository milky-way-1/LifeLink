package com.life.lifelink;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.location.Address;
import android.location.Geocoder;
import androidx.annotation.NonNull;
import android.content.Intent;
import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private TextView locationText;
    private Button proceedButton;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize views
        locationText = findViewById(R.id.locationText);
        proceedButton = findViewById(R.id.proceedButton);

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        FloatingActionButton currentLocationButton = findViewById(R.id.currentLocationButton);
        currentLocationButton.setOnClickListener(v -> moveToCurrentLocation());


        setupProceedButton();
    }

    private void moveToCurrentLocation() {
        if (currentLocation != null) {
            LatLng latLng = new LatLng(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude()
            );
            moveMapCamera(latLng);
        } else {
            getCurrentLocation();
        }
    }

    private void moveMapCamera(LatLng latLng) {
        if (mMap != null) {
            mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                    500,
                    null
            );
        }
    }

    private void setupProceedButton() {
        proceedButton.setOnClickListener(v -> {
            if (mMap != null) {
                returnLocationResult(mMap.getCameraPosition().target);
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (checkLocationPermission()) {
            setupMap();
        }

        // Update location when camera stops moving
        mMap.setOnCameraIdleListener(() -> {
            LatLng center = mMap.getCameraPosition().target;
            updateLocationText(center);
        });
    }

    private void setupMap() {
        if (mMap == null) return;

        try {
            mMap.setMyLocationEnabled(false); // Disable default location button
            mMap.getUiSettings().setMyLocationButtonEnabled(false); // Hide default button
            mMap.getUiSettings().setZoomControlsEnabled(true);

            getCurrentLocation();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentLocation = location;
                            LatLng latLng = new LatLng(
                                    location.getLatitude(),
                                    location.getLongitude()
                            );
                            moveMapCamera(latLng);
                        }
                    });
        }
    }

    private void updateLocationText(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressText = address.getAddressLine(0);
                locationText.setText(addressText);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Show coordinates if geocoding fails
            String coordinates = String.format(Locale.getDefault(),
                    "Lat: %.6f, Long: %.6f",
                    latLng.latitude, latLng.longitude);
            locationText.setText(coordinates);
        }
    }

    private void returnLocationResult(LatLng location) {
        Intent resultIntent = new Intent();
        String address = locationText.getText().toString();
        resultIntent.putExtra("address", address);
        resultIntent.putExtra("latitude", location.latitude);
        resultIntent.putExtra("longitude", location.longitude);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMap();
            }
        }
    }
}