package com.life.lifelink;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TrackAmbulanceActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "TrackAmbulance";
    private static final String MAPS_API_KEY = "your_api_key_here";

    private GoogleMap mMap;
    private String bookingId;
    private Marker driverMarker;
    private WebSocketManager webSocketManager;
    private LatLng userLocation;
    private LatLng destinationLocation;
    private Polyline routePolyline;
    private TextView etaTextView;
    private TextView distanceTextView;
    private TextView statusTextView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_ambulance);

        initializeViews();
        getIntentData();
        setupMap();
        setupWebSocket();
    }

    private void initializeViews() {
        etaTextView = findViewById(R.id.etaTextView);
        distanceTextView = findViewById(R.id.distanceTextView);
        statusTextView = findViewById(R.id.statusTextView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void getIntentData() {
        bookingId = getIntent().getStringExtra("booking_id");
        userLocation = getIntent().getParcelableExtra("user_location");
        destinationLocation = getIntent().getParcelableExtra("destination");

        if (bookingId == null || userLocation == null || destinationLocation == null) {
            showError("Invalid booking details");
            finish();
        }
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupWebSocket() {
        webSocketManager = new WebSocketManager(bookingId, new WebSocketManager.LocationCallback() {
            @Override
            public void onLocationUpdate(LatLng driverLocation) {
                runOnUiThread(() -> {
                    updateDriverLocation(driverLocation);
                    updateRoute(driverLocation);
                });
            }

            @Override
            public void onConnectionEstablished() {
                runOnUiThread(() -> {
                    showToast("Connected to ambulance");
                    statusTextView.setText("Connected");
                    statusTextView.setTextColor(getColor(R.color.green));
                });
            }

            @Override
            public void onConnectionError(String error) {
                runOnUiThread(() -> {
                    showError("Connection lost: " + error);
                    statusTextView.setText("Disconnected");
                    statusTextView.setTextColor(getColor(R.color.red));
                });
            }
        });
    }

    private void updateDriverLocation(LatLng location) {
        if (mMap == null) return;

        if (driverMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(location)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_ambulance))
                    .title("Ambulance")
                    .flat(true)
                    .anchor(0.5f, 0.5f);
            driverMarker = mMap.addMarker(markerOptions);
        } else {
            animateMarker(driverMarker, location);
        }

        calculateAndShowETA(location);
    }

    private void animateMarker(final Marker marker, final LatLng toPosition) {
        final Handler handler = new Handler(Looper.getMainLooper());
        final long start = SystemClock.uptimeMillis();
        final LatLng startLatLng = marker.getPosition();
        final long duration = 1000;
        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                double lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude;
                double lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude;
                marker.setPosition(new LatLng(lat, lng));

                // Rotate marker to face movement direction
                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                } else {
                    float bearing = (float) SphericalUtil.computeHeading(startLatLng, toPosition);
                    marker.setRotation(bearing);
                }
            }
        });
    }

    private void updateRoute(LatLng driverLocation) {
        String url = getDirectionsUrl(driverLocation, userLocation);
        new FetchDirectionsTask().execute(url);
    }

    private String getDirectionsUrl(LatLng origin, LatLng destination) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + destination.latitude + "," + destination.longitude;
        String sensor = "sensor=false";
        String mode = "mode=driving";
        String key = "key=" + MAPS_API_KEY;

        return "https://maps.googleapis.com/maps/api/directions/json?" +
                str_origin + "&" + str_dest + "&" + sensor + "&" + mode + "&" + key;
    }

    private class FetchDirectionsTask extends AsyncTask<String, Void, List<LatLng>> {
        @Override
        protected List<LatLng> doInBackground(String... urls) {
            if (urls.length == 0) return null;

            try {
                String jsonData = downloadUrl(urls[0]);
                return parseDirections(jsonData);
            } catch (Exception e) {
                Log.e(TAG, "Error fetching directions", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<LatLng> route) {
            if (route != null && !route.isEmpty()) {
                drawRoute(route);
            }
        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(strUrl).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                return response.body().string();
            }
            return "";
        }
    }

    private List<LatLng> parseDirections(String jsonData) {
        List<LatLng> points = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray routes = jsonObject.getJSONArray("routes");

            if (routes.length() > 0) {
                JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
                JSONArray steps = legs.getJSONObject(0).getJSONArray("steps");

                for (int i = 0; i < steps.length(); i++) {
                    String polyline = steps.getJSONObject(i)
                            .getJSONObject("polyline")
                            .getString("points");
                    List<LatLng> decodedPoints = decodePolyline(polyline);
                    points.addAll(decodedPoints);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing directions JSON", e);
        }
        return points;
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

            double latitude = lat * 1e-5;
            double longitude = lng * 1e-5;
            poly.add(new LatLng(latitude, longitude));
        }
        return poly;
    }

    private void drawRoute(List<LatLng> points) {
        if (routePolyline != null) {
            routePolyline.remove();
        }

        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(points)
                .width(12)
                .color(ContextCompat.getColor(this, R.color.route_color))
                .geodesic(true);

        routePolyline = mMap.addPolyline(polylineOptions);
    }

    private void calculateAndShowETA(LatLng driverLocation) {
        String url = getDistanceMatrixUrl(driverLocation, userLocation);
        new FetchETATask().execute(url);
    }

    private String getDistanceMatrixUrl(LatLng origin, LatLng destination) {
        return "https://maps.googleapis.com/maps/api/distancematrix/json?" +
                "origins=" + origin.latitude + "," + origin.longitude +
                "&destinations=" + destination.latitude + "," + destination.longitude +
                "&mode=driving&key=" + MAPS_API_KEY;
    }

    private class FetchETATask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... urls) {
            try {
                String jsonData = downloadUrl(urls[0]);
                return new JSONObject(jsonData);
            } catch (Exception e) {
                Log.e(TAG, "Error fetching ETA", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (result != null) {
                try {
                    JSONArray rows = result.getJSONArray("rows");
                    JSONArray elements = rows.getJSONObject(0).getJSONArray("elements");
                    JSONObject element = elements.getJSONObject(0);

                    String duration = element.getJSONObject("duration").getString("text");
                    String distance = element.getJSONObject("distance").getString("text");

                    etaTextView.setText("ETA: " + duration);
                    distanceTextView.setText("Distance: " + distance);
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing ETA response", e);
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        // Add markers for user and destination
        mMap.addMarker(new MarkerOptions()
                .position(userLocation)
                .title("Pickup Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        mMap.addMarker(new MarkerOptions()
                .position(destinationLocation)
                .title("Hospital")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        // Show all markers
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(userLocation);
        builder.include(destinationLocation);

        // Add padding to bounds
        int padding = getResources().getDimensionPixelSize(R.dimen.map_padding);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showError(String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketManager != null) {
            webSocketManager.disconnect();
        }
    }
}