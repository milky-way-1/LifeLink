package com.life.lifelink;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextPaint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.life.lifelink.api.ApiService;
import com.life.lifelink.api.RetrofitClient;
import com.life.lifelink.model.BookingRequest;
import com.life.lifelink.model.BookingResponse;
import com.life.lifelink.model.HospitalResponse;
import com.life.lifelink.model.Location;
import com.life.lifelink.util.SessionManager;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class dashboard extends AppCompatActivity {
    private static final String TAG = "Dashboard";
    private static final int MAP_REQUEST_CODE = 100;

    private LottieAnimationView ambulanceAnimation;
    private Button callAmbulanceButton;
    private TextInputLayout searchInputLayout;
    private TextInputEditText searchInput;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private View contentView;
    private CardView statusCard;
    private TextView statusText;
    private ProgressBar statusProgress;

    private ApiService apiService;
    private SessionManager sessionManager;
    private String currentBookingId;
    private android.location.Location lastKnownLocation;

    private Handler pollingHandler;
    private AtomicBoolean pollingShouldContinue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initializeViews();
        setupServices();
        setupNavigation();
        setupAnimation();
        setupCallButton();

        if (getIntent().getBooleanExtra("LAUNCH_AMBULANCE", false)) {
            double currentLat = getIntent().getDoubleExtra("CURRENT_LAT", 0.0);
            double currentLng = getIntent().getDoubleExtra("CURRENT_LNG", 0.0);

            callAmbulanceButton = findViewById(R.id.callAmbulanceButton);
            if (callAmbulanceButton != null) {
                saveCurrentLocation(currentLat, currentLng);
                callAmbulanceButton.performClick();
            }
        }
    }
    private void saveCurrentLocation(double lat, double lng) {
        lastKnownLocation.setLatitude(lat);
        lastKnownLocation.setLongitude(lng);
    }


    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressText = address.getAddressLine(0);
                searchInput.setText(addressText);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting address: " + e.getMessage());
            searchInput.setText(String.format("%.6f, %.6f", latitude, longitude));
        }
    }

    private void initializeViews() {
        ambulanceAnimation = findViewById(R.id.ambulanceAnimation);
        callAmbulanceButton = findViewById(R.id.callAmbulanceButton);
        searchInput = findViewById(R.id.searchInput);
        searchInputLayout = findViewById(R.id.searchInputLayout);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        contentView = findViewById(R.id.mainContent);
        statusCard = findViewById(R.id.statusCard);
        statusText = findViewById(R.id.statusText);
        statusProgress = findViewById(R.id.statusProgress);

        // Setup gradient for emergency text
        TextView emergencyText = findViewById(R.id.emergencyText);
        TextPaint paint = emergencyText.getPaint();
        float width = paint.measureText("Emergency");
        Shader textShader = new LinearGradient(0, 0, width, 0,
                new int[]{
                        Color.parseColor("#8E2DE2"),
                        Color.parseColor("#4A00E0")
                }, null, Shader.TileMode.CLAMP);
        emergencyText.getPaint().setShader(textShader);
    }

    private void setupServices() {
        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getInstance().getApiService();

        String userId = sessionManager.getUserId();
        if (userId == null) {

            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void setupNavigation() {
        ImageButton menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        setupDrawerLayout();
        setupNavigationView();
        setupSearchInput();
    }

    private void setupDrawerLayout() {
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            private static final float END_SCALE = 0.85f;

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                final float diffScaledOffset = slideOffset * (1 - END_SCALE);
                final float offsetScale = 1 - diffScaledOffset;
                contentView.setScaleX(offsetScale);
                contentView.setScaleY(offsetScale);

                final float xOffset = drawerView.getWidth() * slideOffset;
                final float xOffsetDiff = contentView.getWidth() * diffScaledOffset / 2;
                final float xTranslation = xOffset - xOffsetDiff;
                contentView.setTranslationX(xTranslation);
            }
        });
    }

    private void setupNavigationView() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_health_records) {
                startActivity(new Intent(this, Health_record.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else if (itemId == R.id.nav_insurance) {
                startActivity(new Intent(this, Insurance.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void setupSearchInput() {
        View.OnClickListener searchClickListener = v -> {
            Intent intent = new Intent(dashboard.this, MapActivity.class);
            startActivityForResult(intent, MAP_REQUEST_CODE);
        };

        searchInput.setOnClickListener(searchClickListener);
        searchInputLayout.setStartIconOnClickListener(searchClickListener);
    }

    private void setupAnimation() {
        ambulanceAnimation.setAnimation(R.raw.ambulance);
        ambulanceAnimation.setRepeatCount(LottieDrawable.INFINITE);
        ambulanceAnimation.playAnimation();
    }

    public void perfomClick(){
        callAmbulanceButton.setOnClickListener(v -> {
            if (currentBookingId != null) {
                return;
            }

            if (lastKnownLocation == null) {
                return;
            }

            // Disable button and show loading state
            callAmbulanceButton.setEnabled(false);
            callAmbulanceButton.setText("Requesting...");
            showLoadingState();

            // First find nearest hospital
            String token = "Bearer " + sessionManager.getToken();
            showBookingStatus("Finding nearest hospital...");

            RetrofitClient.getInstance().getApiService()
                    .findNearestHospital(
                            token,
                            lastKnownLocation.getLatitude(),
                            lastKnownLocation.getLongitude()
                    ).enqueue(new Callback<HospitalResponse>() {
                        @Override
                        public void onResponse(Call<HospitalResponse> call, Response<HospitalResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                HospitalResponse hospital = response.body();
                                createBookingWithHospital(hospital);
                            } else {
                                hideLoadingState();
                                resetButtonState();
                                showError("Failed to find nearest hospital");
                            }
                        }

                        @Override
                        public void onFailure(Call<HospitalResponse> call, Throwable t) {
                            hideLoadingState();
                            resetButtonState();
                            showError("Network error: " + t.getMessage());
                        }
                    });
        });
    }

    private void setupCallButton() {
        perfomClick();
    }

    private void createBookingWithHospital(HospitalResponse hospital) {
        showBookingStatus("Creating booking request...");

        // Create booking request
        BookingRequest request = new BookingRequest();
        request.setUserId(sessionManager.getUserId());

        // Set pickup location
        Location pickupLocation = new Location(
                lastKnownLocation.getLatitude(),
                lastKnownLocation.getLongitude()
        );
        request.setPickupLocation(pickupLocation);

        // Set hospital as destination
        Location destinationLocation = new Location(
                hospital.getLatitude(),
                hospital.getLongitude()
        );
        request.setDestinationLocation(destinationLocation);

        String token = "Bearer " + sessionManager.getToken();

        // Make API call to request ambulance
        RetrofitClient.getInstance().getApiService()
                .requestAmbulance(token, request)
                .enqueue(new Callback<BookingResponse>() {
                    @Override
                    public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
                        hideLoadingState();

                        if (response.isSuccessful() && response.body() != null) {
                            BookingResponse bookingResponse = response.body();
                            Log.d(TAG, "Booking Response: " + bookingResponse.toString());

                            if ("ASSIGNED".equals(bookingResponse.getStatus())) {
                                try {
                                    // Validate required data
                                    if (bookingResponse.getBookingId() == null ||
                                            bookingResponse.getDriverId() == null) {
                                        throw new IllegalArgumentException("Invalid booking response data");

                                    }

                                    // Create intent for tracking activity
                                    Intent trackingIntent = new Intent(dashboard.this, VehicleTrackingActivity.class);

                                    // Add booking details
                                    trackingIntent.putExtra("booking_id", bookingResponse.getBookingId());
                                    trackingIntent.putExtra("driver_id", bookingResponse.getDriverId());

                                    // Add pickup location
                                    if (lastKnownLocation != null) {
                                        trackingIntent.putExtra("pickup_lat", lastKnownLocation.getLatitude());
                                        trackingIntent.putExtra("pickup_lng", lastKnownLocation.getLongitude());
                                    } else {
                                        throw new IllegalStateException("Pickup location is null");
                                    }

                                    // Add destination (hospital) location
                                    if (hospital != null) {
                                        trackingIntent.putExtra("dest_lat", hospital.getLatitude());
                                        trackingIntent.putExtra("dest_lng", hospital.getLongitude());
                                        trackingIntent.putExtra("hospital_name",
                                                hospital.getHospitalName() != null ?
                                                        hospital.getHospitalName() : "Hospital");
                                    } else {
                                        throw new IllegalStateException("Hospital data is null");
                                    }

                                    // Log the data being passed
                                    Log.d(TAG, String.format(
                                            "Starting tracking with: Booking: %s, Driver: %s, " +
                                                    "Pickup: %f,%f, Dest: %f,%f",
                                            bookingResponse.getBookingId(),
                                            bookingResponse.getDriverId(),
                                            lastKnownLocation.getLatitude(),
                                            lastKnownLocation.getLongitude(),
                                            hospital.getLatitude(),
                                            hospital.getLongitude()
                                    ));

                                    // Start tracking activity
                                    startActivity(trackingIntent);

                                } catch (Exception e) {
                                    Log.e(TAG, "Error starting tracking: " + e.getMessage());
                                    showError("Failed to start tracking: " + e.getMessage());
                                    resetButtonState();
                                }
                            } else {
                                resetButtonState();
                                showError(bookingResponse.getMessage() != null ?
                                        bookingResponse.getMessage() : "No drivers available");
                            }
                        } else {
                            resetButtonState();
                            showError("Failed to process booking");
                        }
                    }

                    @Override
                    public void onFailure(Call<BookingResponse> call, Throwable t) {
                        Log.e(TAG, "Network Error", t);
                        hideLoadingState();
                        resetButtonState();
                        showError("Network error: " + t.getMessage());
                    }
                });
    }

    private void showError(final String message) {
        runOnUiThread(() -> {
            statusProgress.setVisibility(View.GONE);
            statusText.setText(message);
        });
    }

    private void resetButtonState() {
        runOnUiThread(() -> {
            callAmbulanceButton.setEnabled(true);
            callAmbulanceButton.setText("Call Ambulance");
            statusCard.setVisibility(View.GONE);
        });
    }




    private void showLoadingState() {
        statusCard.setVisibility(View.VISIBLE);
        statusProgress.setVisibility(View.VISIBLE);
        statusText.setText("Requesting ambulance...");
    }

    private void hideLoadingState() {
        statusProgress.setVisibility(View.GONE);
    }

    private void showBookingStatus(String message) {
        statusText.setText(message);
        statusText.setVisibility(View.VISIBLE);
    }

    private void stopPolling() {
        if (pollingShouldContinue != null) {
            pollingShouldContinue.set(false);
        }
        if (pollingHandler != null) {
            pollingHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            String address = data.getStringExtra("address");
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);

            searchInput.setText(address + " " + latitude + " " + longitude);

            lastKnownLocation = new android.location.Location("");
            lastKnownLocation.setLatitude(latitude);
            lastKnownLocation.setLongitude(longitude);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    @Override
    protected void onDestroy() {
        stopPolling();
        super.onDestroy();
    }
}