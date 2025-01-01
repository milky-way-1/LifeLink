package com.life.lifelink;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.life.lifelink.api.ApiService;
import com.life.lifelink.api.RetrofitClient;
import com.life.lifelink.util.SessionManager;
import com.life.lifelink.util.WebSocketService;

public class dashboard extends AppCompatActivity implements WebSocketService.WebSocketCallback {
    private LottieAnimationView ambulanceAnimation;
    private Button callAmbulanceButton;
    private TextInputLayout searchInputLayout;

    private static final int MAP_REQUEST_CODE = 100;

    private TextInputEditText searchInput;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private ActionBarDrawerToggle drawerToggle;
    private View contentView;

    private WebSocketService webSocketService;
    private String currentBookingId;
    private ApiService apiService;
    private SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sessionManager = new SessionManager(this);

        // Initialize API service
        apiService = RetrofitClient.getInstance().getApiService();

        // Initialize WebSocket service with user ID from SessionManager
        String userId = sessionManager.getUserId(); // Add this getter to SessionManager
        if (userId != null) {
            webSocketService = new WebSocketService(userId, this);
        } else {
            // Handle case where user is not logged in
            Toast.makeText(this, "Please log in again", Toast.LENGTH_LONG).show();
            // Redirect to login
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        ambulanceAnimation = findViewById(R.id.ambulanceAnimation);
        callAmbulanceButton = findViewById(R.id.callAmbulanceButton);
        TextView emergencyText = findViewById(R.id.emergencyText);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        ImageButton menuButton = findViewById(R.id.menuButton);
        contentView = findViewById(R.id.mainContent);

        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            private static final float END_SCALE = 0.85f;

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                // Scale the View based on current slide offset
                final float diffScaledOffset = slideOffset * (1 - END_SCALE);
                final float offsetScale = 1 - diffScaledOffset;
                contentView.setScaleX(offsetScale);
                contentView.setScaleY(offsetScale);

                // Translate the View, accounting for the scaled width
                final float xOffset = drawerView.getWidth() * slideOffset;
                final float xOffsetDiff = contentView.getWidth() * diffScaledOffset / 2;
                final float xTranslation = xOffset - xOffsetDiff;
                contentView.setTranslationX(xTranslation);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                // Reset any custom animations here if needed
            }
        });


        // Create gradient for emergency text
        TextPaint paint = emergencyText.getPaint();
        float width = paint.measureText("Emergency");

        searchInput = findViewById(R.id.searchInput);
        searchInputLayout = findViewById(R.id.searchInputLayout);

        // Setup click listener for both the input and its layout
        View.OnClickListener searchClickListener = v -> {
            Intent intent = new Intent(dashboard.this, MapActivity.class);
            startActivityForResult(intent, MAP_REQUEST_CODE);
        };

        searchInput.setOnClickListener(searchClickListener);
        searchInputLayout.setStartIconOnClickListener(searchClickListener);

        navigationView.setNavigationItemSelectedListener(item -> {
                    int itemId = item.getItemId();

                    if (itemId == R.id.nav_health_records) {
                        Intent intent = new Intent(this, Health_record.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                    else if (itemId == R.id.nav_insurance) {
                        Intent intent = new Intent(this, Insurance.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
//            } else if (itemId == R.id.nav_hospitals) {
//                // Handle hospitals
//                startActivity(new Intent(this, HospitalsActivity.class));
//            } else if (itemId == R.id.nav_ambulance) {
//                // Handle ambulance
//                startActivity(new Intent(this, AmbulanceActivity.class));
//            } else if (itemId == R.id.nav_blood_banks) {
//                // Handle blood banks
//                startActivity(new Intent(this, BloodBanksActivity.class));
//            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Optional: Customize navigation drawer
        navigationView.setItemIconTintList(null);

        // Optional: Add touch feedback
        searchInput.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                searchClickListener.onClick(v);
                return true;
            }
            return false;
        });

        Shader textShader = new LinearGradient(0, 0, width, 0,
                new int[] {
                        Color.parseColor("#8E2DE2"),  // Start color
                        Color.parseColor("#4A00E0")   // End color
                }, null, Shader.TileMode.CLAMP);
        emergencyText.getPaint().setShader(textShader);

        // Setup animation
        setupAnimation();

        // Setup button click
        setupCallButton();
    }

    private void setupAnimation() {
        ambulanceAnimation.setAnimation(R.raw.ambulance);
        ambulanceAnimation.setRepeatCount(LottieDrawable.INFINITE);
        ambulanceAnimation.playAnimation();
    }

    private void setupCallButton() {
        callAmbulanceButton.setOnClickListener(v -> {
            String location = searchInput.getText().toString().trim();
            if (location.isEmpty()) {
                Toast.makeText(this, "Please enter your location", Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: Implement ambulance calling logic
            Toast.makeText(this, "Calling ambulance to: " + location, Toast.LENGTH_LONG).show();
        });


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawerLayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            // If drawer is open, close it
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // If drawer is closed, proceed with normal back button behavior
            super.onBackPressed();
            // Optional: Add reverse transition animation
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            String address = data.getStringExtra("address");
            searchInput.setText(address);
        }
    }

    @Override
    public void onBookingAccepted(String driverId, String bookingId) {
        runOnUiThread(() -> {
            showBookingStatus("Driver accepted! They're on their way.");
            startActivity(new Intent(this, TrackAmbulanceActivity.class)
                    .putExtra("booking_id", bookingId)
                    .putExtra("driver_id", driverId));
        });
    }

    @Override
    public void onStatusUpdate(String bookingId, String status) {
        runOnUiThread(() -> {
            if (status.equals("CANCELLED")) {
                currentBookingId = null;
                showError("Booking cancelled: No drivers available");
            }
            showBookingStatus("Booking status: " + status);
        });
    }

    @Override
    public void onDriverLocation(String driverId, double lat, double lng) {
        runOnUiThread(() -> {
            // Update driver location on map or UI
            updateDriverLocation(driverId, lat, lng);
        });
    }

    @Override
    public void onError(String message) {
        runOnUiThread(() -> showError(message));
    }

    private void showBookingStatus(String message) {
        // Implement UI update for booking status
        // For example:
        TextView statusText = findViewById(R.id.statusText); // Add this TextView to your layout
        if (statusText != null) {
            statusText.setText(message);
            statusText.setVisibility(View.VISIBLE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateDriverLocation(String driverId, double lat, double lng) {
        // Implement driver location update logic
        // This will be used when tracking the ambulance
    }
}