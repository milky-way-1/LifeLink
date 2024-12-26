package com.life.lifelink;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
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
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private Button loginButton;
    private Button signUpButton;

    private LottieAnimationView animationView;
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

        // Create gradient shader
        Shader textShader = new LinearGradient(
                0, 0,
                titleText.getPaint().measureText("LifeLink"), 0,
                new int[]{
                        Color.parseColor("#8E2DE2"),  // Start color
                        Color.parseColor("#4A00E0")   // End color
                },
                null,
                Shader.TileMode.CLAMP
        );

        // Apply the shader to the TextView's paint
        titleText.getPaint().setShader(textShader);

        // Force redraw
        titleText.invalidate();

        // Setup animation
        setupAnimation();

        // Create a handler to delay showing the login interface
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Switch to main layout after delay
                setContentView(R.layout.activity_main);

                // Initialize views
                initializeViews();

                // Set up buttons
                setupLoginButton();
                setupSignUpButton();

                // Set up window insets for the root layout
                View rootView = findViewById(android.R.id.content);
                ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });
            }
        }, 4000);
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
    }

    private void setupLoginButton() {
        loginButton.setOnClickListener(v -> {
            // Show loading state
            loginButton.setEnabled(false);
            loginButton.setText("Loading...");

            // Simulate loading (remove this in production)
            new Handler().postDelayed(() -> {
                // Navigate to Dashboard
                Intent intent = new Intent(MainActivity.this, dashboard.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                // Optional: finish MainActivity
                // finish();
            }, 500); // Half second delay for demo
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
        // TODO: Implement your authentication logic here
    }
}