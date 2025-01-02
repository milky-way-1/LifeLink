package com.life.lifelink;

import android.app.AlertDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import com.life.lifelink.api.RetrofitClient;
import com.life.lifelink.model.DriverLocation;
import com.life.lifelink.model.HospitalResponse;
import com.life.lifelink.util.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehicleTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String driverId;
    private Handler locationUpdateHandler;
    private static final int UPDATE_INTERVAL = 5000; // 5 seconds
    private Marker driverMarker;
    private Polyline routePolyline;
    private SessionManager sessionManager;
    private LatLng pickupLocation;
    private LatLng hospitalLocation;
    private GeoApiContext geoApiContext;
    private static final float DESTINATION_RADIUS = 100; // meters
    private boolean isPickupComplete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_tracking);

        sessionManager = new SessionManager(this);

        // Get data from intent
        driverId = getIntent().getStringExtra("driver_id");
        double pickupLat = getIntent().getDoubleExtra("pickup_lat", 0);
        double pickupLng = getIntent().getDoubleExtra("pickup_lng", 0);
        double destLat = getIntent().getDoubleExtra("dest_lat", 0);
        double destLng = getIntent().getDoubleExtra("dest_lng", 0);

        pickupLocation = new LatLng(pickupLat, pickupLng);
        hospitalLocation = new LatLng(destLat, destLng);

        // Initialize Google Maps API context
        try {
            ApplicationInfo ai = getPackageManager()
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            String apiKey = ai.metaData.getString("com.google.android.geo.API_KEY");
            geoApiContext = new GeoApiContext.Builder()
                    .apiKey(apiKey)
                    .build();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        locationUpdateHandler = new Handler();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        String hospitalName = getIntent().getStringExtra("hospital_name");

        // Add markers for pickup and hospital
        mMap.addMarker(new MarkerOptions()
                .position(pickupLocation)
                .title("Pickup Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        mMap.addMarker(new MarkerOptions()
                .position(hospitalLocation)
                .title(hospitalName)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // Create ambulance marker
        MarkerOptions ambulanceMarker = new MarkerOptions()
                .title("Ambulance")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_ambulance_marker))
                .visible(false);
        driverMarker = mMap.addMarker(ambulanceMarker);

        // Show all markers
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(pickupLocation);
        builder.include(hospitalLocation);
        LatLngBounds bounds = builder.build();

        // Move camera to show all markers with padding
        int padding = 100;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);

        // Start location updates
        startLocationUpdates();
    }

    private final Runnable locationUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateDriverLocation();
            locationUpdateHandler.postDelayed(this, UPDATE_INTERVAL);
        }
    };

    private void startLocationUpdates() {
        locationUpdateHandler.post(locationUpdateRunnable);
    }

    private void stopLocationUpdates() {
        locationUpdateHandler.removeCallbacks(locationUpdateRunnable);
    }

    private void updateDriverLocation() {
        String token = "Bearer " + sessionManager.getToken();

        RetrofitClient.getInstance()
                .getApiService()
                .getDriverLocation(token, driverId)
                .enqueue(new Callback<DriverLocation>() {
                    @Override
                    public void onResponse(Call<DriverLocation> call, Response<DriverLocation> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            DriverLocation location = response.body();
                            LatLng driverLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            updateMapWithNewLocation(driverLatLng);
                            updateRoute(driverLatLng);

                            // Check if driver has reached destination
                            checkDestinationReached(location);
                        }
                    }

                    @Override
                    public void onFailure(Call<DriverLocation> call, Throwable t) {
                        Toast.makeText(VehicleTrackingActivity.this,
                                "Failed to get driver location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateMapWithNewLocation(LatLng newPosition) {
        if (driverMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(newPosition)
                    .title("Ambulance")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_ambulance_marker));
            driverMarker = mMap.addMarker(markerOptions);
        } else {
            animateMarker(driverMarker, newPosition);
        }
    }

    private void animateMarker(final Marker marker, final LatLng toPosition) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final LatLng startLatLng = marker.getPosition();
        final long duration = 1000;
        final LinearInterpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                double lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude;
                double lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude;

                marker.setPosition(new LatLng(lat, lng));

                // Rotate marker to face movement direction
                float bearing = (float) computeHeading(startLatLng, toPosition);
                marker.setRotation(bearing);

                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    private double computeHeading(LatLng from, LatLng to) {
        double fromLat = Math.toRadians(from.latitude);
        double fromLng = Math.toRadians(from.longitude);
        double toLat = Math.toRadians(to.latitude);
        double toLng = Math.toRadians(to.longitude);
        double dLng = toLng - fromLng;

        double y = Math.sin(dLng) * Math.cos(toLat);
        double x = Math.cos(fromLat) * Math.sin(toLat) -
                Math.sin(fromLat) * Math.cos(toLat) * Math.cos(dLng);

        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    private void updateRoute(LatLng driverLocation) {
        // Get current destination based on pickup status
        final LatLng destination = isPickupComplete ? hospitalLocation : pickupLocation;

        try {
            DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                    .origin(new com.google.maps.model.LatLng(
                            driverLocation.latitude,
                            driverLocation.longitude))
                    .destination(new com.google.maps.model.LatLng(
                            destination.latitude,
                            destination.longitude))
                    .mode(TravelMode.DRIVING)
                    .await();

            if (result.routes.length > 0) {
                final List<LatLng> decodedPath =
                        PolyUtil.decode(result.routes[0].overviewPolyline.getEncodedPath());

                runOnUiThread(() -> {
                    if (routePolyline != null) {
                        routePolyline.remove();
                    }
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .addAll(decodedPath)
                            .color(getResources().getColor(R.color.route_color))
                            .width(10);
                    routePolyline = mMap.addPolyline(polylineOptions);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkDestinationReached(DriverLocation driverLocation) {
        LatLng currentDestination = isPickupComplete ? hospitalLocation : pickupLocation;
        float[] results = new float[1];
        android.location.Location.distanceBetween(
                driverLocation.getLatitude(), driverLocation.getLongitude(),
                currentDestination.latitude, currentDestination.longitude,
                results
        );

        if (results[0] <= DESTINATION_RADIUS) {
            if (!isPickupComplete) {
                isPickupComplete = true;
                showPickupCompleteDialog();
            } else {
                showJourneyCompleteDialog();
            }
        }
    }

    private void showPickupCompleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Pickup Complete")
                .setMessage("Patient picked up. Proceeding to hospital.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showJourneyCompleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Journey Complete")
                .setMessage("Arrived at hospital")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        if (geoApiContext != null) {
            geoApiContext.shutdown();
        }
    }
}