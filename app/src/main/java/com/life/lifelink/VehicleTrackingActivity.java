package com.life.lifelink;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import com.life.lifelink.api.RetrofitClient;
import com.life.lifelink.model.DriverLocation;
import com.life.lifelink.util.SessionManager;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehicleTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String driverId;
    private Handler locationUpdateHandler;
    private static final int UPDATE_INTERVAL = 5000;
    private Marker driverMarker;
    private Polyline routePolyline;
    private SessionManager sessionManager;
    private LatLng userLocation;
    private GeoApiContext geoApiContext;
    private static final float DESTINATION_RADIUS = 100; // meters

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_tracking);

        sessionManager = new SessionManager(this);

        // Get locations from intent
        driverId = getIntent().getStringExtra("driverId");
        userLocation = new LatLng(
                getIntent().getDoubleExtra("userLat", 0),
                getIntent().getDoubleExtra("userLng", 0)
        );

        // Get Maps API key from manifest
        try {
            ApplicationInfo ai = getPackageManager()
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            String apiKey = ai.metaData.getString("com.google.android.geo.API_KEY");

            // Initialize Google Maps Directions API context
            geoApiContext = new GeoApiContext.Builder()
                    .apiKey(apiKey)
                    .build();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationUpdateHandler = new Handler();
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
                            updateMapWithNewLocation(location);
                            updateRoute(new LatLng(location.getLatitude(), location.getLongitude()));

                            // Check if driver has reached the patient
                            if (hasReachedDestination(location)) {
                                findNearestHospital(new LatLng(location.getLatitude(), location.getLongitude()));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<DriverLocation> call, Throwable t) {
                        Toast.makeText(VehicleTrackingActivity.this,
                                "Failed to get driver location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateMapWithNewLocation(DriverLocation location) {
        LatLng driverLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (driverMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(driverLatLng)
                    .title("Ambulance")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_ambulance_marker));
            driverMarker = mMap.addMarker(markerOptions);

            // Move camera to show both driver and user
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(driverLatLng);
            builder.include(userLocation);
            LatLngBounds bounds = builder.build();
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } else {
            driverMarker.setPosition(driverLatLng);
        }
    }

    private void updateRoute(LatLng driverLocation) {
        try {
            DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                    .origin(new com.google.maps.model.LatLng(driverLocation.latitude, driverLocation.longitude))
                    .destination(new com.google.maps.model.LatLng(userLocation.latitude, userLocation.longitude))
                    .mode(TravelMode.DRIVING)
                    .await();

            if (result.routes.length > 0) {
                List<LatLng> decodedPath = PolyUtil.decode(result.routes[0].overviewPolyline.getEncodedPath());

                runOnUiThread(() -> {
                    if (routePolyline != null) {
                        routePolyline.remove();
                    }
                    routePolyline = mMap.addPolyline(new PolylineOptions()
                            .addAll(decodedPath)
                            .color(getResources().getColor(R.color.route_color))
                            .width(10));
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean hasReachedDestination(DriverLocation driverLocation) {
        float[] results = new float[1];
        android.location.Location.distanceBetween(
                driverLocation.getLatitude(), driverLocation.getLongitude(),
                userLocation.latitude, userLocation.longitude,
                results
        );
        return results[0] <= DESTINATION_RADIUS;
    }

    private void findNearestHospital(LatLng currentLocation) {
        String token = "Bearer " + sessionManager.getToken();

        RetrofitClient.getInstance()
                .getApiService()
                .findNearestHospital(token, currentLocation.latitude, currentLocation.longitude)
                .enqueue(new Callback<Hospital>() {
                    @Override
                    public void onResponse(Call<Hospital> call, Response<Hospital> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Hospital hospital = response.body();
                            showHospitalFoundDialog(hospital);
                        }
                    }

                    @Override
                    public void onFailure(Call<Hospital> call, Throwable t) {
                        Toast.makeText(VehicleTrackingActivity.this,
                                "Failed to find nearest hospital", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showHospitalFoundDialog(Hospital hospital) {
        new AlertDialog.Builder(this)
                .setTitle("Nearest Hospital Found")
                .setMessage("Found " + hospital.getName() + "\nDistance: " + hospital.getDistance() + "km")
                .setPositiveButton("Navigate", (dialog, which) -> {
                    // Start navigation to hospital
                    userLocation = new LatLng(hospital.getLatitude(), hospital.getLongitude());
                    updateRoute(new LatLng(
                            driverMarker.getPosition().latitude,
                            driverMarker.getPosition().longitude
                    ));
                })
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

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

    }
}