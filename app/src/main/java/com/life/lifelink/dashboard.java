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

    // Polling related fields
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

        if (getIntent().getBooleanExtra("from_assistant", false)) {
            handleAssistantLaunch();
        }
    }

    private void handleAssistantLaunch() {
        double latitude = getIntent().getDoubleExtra("latitude", 0);
        double longitude = getIntent().getDoubleExtra("longitude", 0);

        if (latitude != 0 && longitude != 0) {
            android.location.Location location = new android.location.Location("");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            lastKnownLocation = location;

            getAddressFromLocation(latitude, longitude);

            new Handler().postDelayed(() -> {
                if (callAmbulanceButton != null) {
                    callAmbulanceButton.performClick();
                }
            }, 1000);
        }
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
            Toast.makeText(this, "Please log in again", Toast.LENGTH_LONG).show();
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

    private void setupCallButton() {
        callAmbulanceButton.setOnClickListener(v -> {
            if (currentBookingId != null) {
                Toast.makeText(this, "You already have an active booking",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (lastKnownLocation == null) {
                Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show();
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
                                // Driver found and assigned immediately
                                statusProgress.setVisibility(View.GONE);
                                statusText.setText("Driver assigned! Starting journey tracking...");

                                // Add null checks and validations
                                if (bookingResponse.getDriverId() == null) {
                                    showError("Invalid driver assignment");
                                    resetButtonState();
                                    return;
                                }

                                Intent trackingIntent = new Intent(dashboard.this, VehicleTrackingActivity.class);
                                trackingIntent.putExtra("booking_id", bookingResponse.getBookingId());
                                trackingIntent.putExtra("driver_id", bookingResponse.getDriverId());

                                // Add hospital information
                                trackingIntent.putExtra("hospital_name", hospital.getHospitalName());
                                trackingIntent.putExtra("dest_lat", hospital.getLatitude());
                                trackingIntent.putExtra("dest_lng", hospital.getLongitude());

                                if (lastKnownLocation != null) {
                                    trackingIntent.putExtra("pickup_lat", lastKnownLocation.getLatitude());
                                    trackingIntent.putExtra("pickup_lng", lastKnownLocation.getLongitude());
                                } else {
                                    showError("Invalid pickup location");
                                    resetButtonState();
                                    return;
                                }

                                try {
                                    startActivity(trackingIntent);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error starting tracking activity", e);
                                    showError("Failed to start tracking");
                                    resetButtonState();
                                }
                            } else {
                                resetButtonState();
                                showError(bookingResponse.getMessage() != null ?
                                        bookingResponse.getMessage() : "No drivers available");
                            }
                        } else {
                            resetButtonState();
                            showError("Failed to request ambulance");
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

    private void startStatusPolling(String bookingId, HospitalResponse hospital) {
        Handler handler = new Handler();
        AtomicBoolean shouldContinue = new AtomicBoolean(true);
        AtomicReference<Runnable> statusCheckerRef = new AtomicReference<>();

        Runnable statusChecker = new Runnable() {
            @Override
            public void run() {
                if (!shouldContinue.get()) {
                    return;
                }

                RetrofitClient.getInstance().getApiService()
                        .getBookingStatus(bookingId)
                        .enqueue(new Callback<BookingResponse>() {
                            @Override
                            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
                                if (!shouldContinue.get()) {
                                    return;
                                }

                                if (response.isSuccessful() && response.body() != null) {
                                    BookingResponse statusResponse = response.body();

                                    if (statusResponse.isAssigned()) {
                                        shouldContinue.set(false);
                                        onBookingAccepted(statusResponse.getDriverId(), hospital.getHospitalId());
                                    } else if (statusResponse.isCancelled()) {
                                        shouldContinue.set(false);
                                        resetButtonState();
                                        showError("No drivers available");
                                    } else {
                                        // Continue polling
                                        if (shouldContinue.get() && statusCheckerRef.get() != null) {
                                            handler.postDelayed(statusCheckerRef.get(), 5000);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<BookingResponse> call, Throwable t) {
                                if (shouldContinue.get() && statusCheckerRef.get() != null) {
                                    handler.postDelayed(statusCheckerRef.get(), 5000);
                                }
                            }
                        });
            }
        };

        statusCheckerRef.set(statusChecker);
        handler.post(statusChecker);

        this.pollingHandler = handler;
        this.pollingShouldContinue = shouldContinue;
    }

    private void startStatusPolling(String bookingId) {
        Handler handler = new Handler();
        AtomicBoolean shouldContinue = new AtomicBoolean(true);
        AtomicReference<Runnable> statusCheckerRef = new AtomicReference<>();

        Runnable statusChecker = new Runnable() {
            @Override
            public void run() {
                if (!shouldContinue.get()) {
                    return;
                }

                apiService.getBookingStatus(bookingId).enqueue(new Callback<BookingResponse>() {
                    @Override
                    public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
                        if (!shouldContinue.get()) {
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            BookingResponse statusResponse = response.body();

                            switch (statusResponse.getStatus()) {
                                case "ASSIGNED":
                                    shouldContinue.set(false);
                                    onBookingAccepted(statusResponse.getDriverId(), bookingId);
                                    break;
                                case "CANCELLED":
                                    shouldContinue.set(false);
                                    resetButtonState();
                                    showError("No drivers available");
                                    break;
                                case "SEARCHING":
                                    // Continue polling
                                    if (shouldContinue.get() && statusCheckerRef.get() != null) {
                                        handler.postDelayed(statusCheckerRef.get(), 5000);
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            if (shouldContinue.get() && statusCheckerRef.get() != null) {
                                handler.postDelayed(statusCheckerRef.get(), 5000);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<BookingResponse> call, Throwable t) {
                        if (!shouldContinue.get()) {
                            return;
                        }

                        Log.e(TAG, "Failed to get booking status", t);

                        if (shouldContinue.get() && statusCheckerRef.get() != null) {
                            handler.postDelayed(statusCheckerRef.get(), 5000);
                        }
                    }
                });
            }
        };

        statusCheckerRef.set(statusChecker);
        handler.post(statusChecker);

        this.pollingHandler = handler;
        this.pollingShouldContinue = shouldContinue;
    }

    private void onBookingAccepted(String driverId, String bookingId) {
        runOnUiThread(() -> {
            statusProgress.setVisibility(View.GONE);
            statusText.setText("Driver assigned! Starting journey tracking...");

            currentBookingId = bookingId;

            Intent trackingIntent = new Intent(this, VehicleTrackingActivity.class);
            trackingIntent.putExtra("booking_id", bookingId);
            trackingIntent.putExtra("driver_id", driverId);

            if (lastKnownLocation != null) {
                trackingIntent.putExtra("pickup_lat", lastKnownLocation.getLatitude());
                trackingIntent.putExtra("pickup_lng", lastKnownLocation.getLongitude());
            }

            trackingIntent.putExtra("pickup_address", searchInput.getText().toString());
            startActivity(trackingIntent);
        });
    }

    private void showError(final String message) {
        runOnUiThread(() -> {
            statusProgress.setVisibility(View.GONE);
            statusText.setText(message);
            Toast.makeText(dashboard.this, message, Toast.LENGTH_LONG).show();
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