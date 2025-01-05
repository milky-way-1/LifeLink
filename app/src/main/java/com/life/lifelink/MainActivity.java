package com.life.lifelink;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import com.airbnb.lottie.LottieAnimationView;
import android.animation.Animator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.airbnb.lottie.RenderMode;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.life.lifelink.api.RetrofitClient;
import com.life.lifelink.model.JwtResponse;
import com.life.lifelink.model.LoginRequest;
import com.life.lifelink.util.SessionManager;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private Button loginButton;
    private Button signUpButton;

    private MaterialTextView messageText;

    private LottieAnimationView animationView;

    private SessionManager sessionManager;

    private FusedLocationProviderClient fusedLocationClient;

    private ViewGroup mainLayout;
    private ViewGroup splashLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setStatusBarColor(getResources().getColor(R.color.secondary_purple, getTheme()));

        // Set the splash screen layout first
        setContentView(R.layout.splash_screen);
        splashLayout = findViewById(R.id.splashLayout);
        animationView = findViewById(R.id.animationView);

        TextView titleText = findViewById(R.id.titleText);

        sessionManager = new SessionManager(this);

        setContentView(R.layout.splash_screen);
        setupSplashScreen();

        // Create a handler to delay next screen
        new Handler().postDelayed(() -> {
            // Check login status after splash screen
            if (sessionManager.getToken() != null) {
                navigateToDashboard();
                finish();
            } else {
                // Show login screen if not logged in
                setContentView(R.layout.activity_main);
                initializeViews();
                setupLoginButton();
                setupSignUpButton();
                setupWindowInsets();
            }
        }, 4000);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        Uri data = intent.getData();

        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            if (data.getPath().contains("/ambulance")) {
                getCurrentLocationAndLaunchDashboard();
            }
        }
    }

    private void getCurrentLocationAndLaunchDashboard() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            launchDashboard(location.getLatitude(), location.getLongitude());
                        } else {
                            // If location is null, launch without location
                            launchDashboard(0.0, 0.0);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Launch without location if there's an error
                        launchDashboard(0.0, 0.0);
                    });
        } else {
            // Launch without location if permission not granted
            launchDashboard(0.0, 0.0);
        }
    }

    private void launchDashboard(double latitude, double longitude) {
        Intent dashboardIntent = new Intent(this, dashboard.class);
        dashboardIntent.putExtra("LAUNCH_AMBULANCE", true);
        dashboardIntent.putExtra("CURRENT_LAT", latitude);
        dashboardIntent.putExtra("CURRENT_LNG", longitude);
        startActivity(dashboardIntent);
    }

    private void setupSplashScreen() {
        splashLayout = findViewById(R.id.splashLayout);
        animationView = findViewById(R.id.animationView);
        TextView titleText = findViewById(R.id.titleText);

        // Create gradient shader
        Shader textShader = new LinearGradient(
                0, 0,
                titleText.getPaint().measureText("LifeLink"), 0,
                new int[]{
                        Color.parseColor("#8E2DE2"),
                        Color.parseColor("#4A00E0")
                },
                null,
                Shader.TileMode.CLAMP
        );

        titleText.getPaint().setShader(textShader);
        titleText.invalidate();
        setupAnimation();
    }

    private void setupAnimation() {
        if (animationView != null) {
            animationView.setVisibility(View.VISIBLE);
            animationView.playAnimation();

            animationView.addAnimatorListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    Log.d("Animation", "Started");
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    Log.d("Animation", "Ended");
                }

                @Override
                public void onAnimationCancel(Animator animation) {}

                @Override
                public void onAnimationRepeat(Animator animation) {}
            });

            // Add failure listener
            animationView.setFailureListener(result -> {
                Log.e("Animation", "Failed to load animation: " + result.getMessage());
            });
        }
    }
    private void initializeViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);
        messageText = findViewById(R.id.messageText);
    }

    private void setupLoginButton() {
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (validateInput(email, password)) {
                attemptLogin(email, password);
            }
        });
    }

    private void setupSignUpButton() {
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUp.class);
            startActivity(intent);
        });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email");
            return false;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            return false;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            return false;
        }

        return true;
    }
    private void attemptLogin(String email, String password) {
        showLoading(true);
        showMessage("", false); // Clear any previous messages

        LoginRequest request = new LoginRequest(email, password);

        RetrofitClient.getInstance()
                .getApiService()
                .login(request)
                .enqueue(new Callback<JwtResponse>() {
                    @Override
                    public void onResponse(Call<JwtResponse> call, Response<JwtResponse> response) {
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            sessionManager.saveAuthToken(response.body());
                            showMessage("Login successful!", false);

                            new Handler().postDelayed(() -> {
                                navigateToDashboard();
                                finish();
                            }, 1000);
                        } else {
                            try {
                                JSONObject errorBody = new JSONObject(response.errorBody().string());
                                showMessage(errorBody.getString("message"), true);
                            } catch (Exception e) {
                                showMessage("Login failed. Please try again.", true);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JwtResponse> call, Throwable t) {
                        showLoading(false);
                        showMessage("Network error. Please check your connection.", true);
                    }
                });
    }

    private void showLoading(boolean isLoading) {
        loginButton.setEnabled(!isLoading);
        loginButton.setText(isLoading ? "Loading..." : "Login");
        emailInput.setEnabled(!isLoading);
        passwordInput.setEnabled(!isLoading);
        signUpButton.setEnabled(!isLoading);
    }

    private void testAppActions() {
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setData(Uri.parse("emergency://"));
        testIntent.putExtra("from_assistant", true);
        testIntent.putExtra("latitude", 8.352345346);
        testIntent.putExtra("longitude", 76.8535993989);
        startActivity(testIntent);
    }

    private void showMessage(String message, boolean isError) {
        if (messageText != null) {
            messageText.setText(message);
            messageText.setVisibility(message.isEmpty() ? View.GONE : View.VISIBLE);

            if (isError) {
                messageText.setTextColor(getColor(R.color.error_color));
                messageText.setBackgroundResource(R.drawable.error_background);
            } else {
                messageText.setTextColor(getColor(R.color.success_color));
                messageText.setBackgroundResource(R.drawable.success_background);
            }

            // Auto hide after 3 seconds
            new Handler().postDelayed(() -> {
                if (messageText != null) {
                    messageText.setVisibility(View.GONE);
                }
            }, 3000);
        }
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(MainActivity.this, dashboard.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void setupWindowInsets() {
        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}