package com.life.lifelink;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.life.lifelink.api.RetrofitClient;
import com.life.lifelink.model.MessageResponse;
import com.life.lifelink.model.SignupRequest;

import org.json.JSONObject;

import java.net.SocketTimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUp extends AppCompatActivity {

    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initializeViews();
        setupSignUpButton();
    }

    private void initializeViews() {
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        signUpButton = findViewById(R.id.signUpButton);
    }

    private void setupSignUpButton() {
        signUpButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            if (validateInput(name, email, password, confirmPassword)) {
                attemptSignUp(name, email, password);
            }
        });
    }

    private boolean validateInput(String name, String email, String password, String confirmPassword) {
        if (name.isEmpty()) {
            nameInput.setError("Name is required");
            return false;
        }

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

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            return false;
        }

        return true;
    }

    private void attemptSignUp(String name, String email, String password) {
        showLoading();
        showMessage("", false); // Clear previous messages

        // Create signup request
        SignupRequest request = new SignupRequest(name, email, password, "PATIENT");

        RetrofitClient.getInstance()
                .getApiService()
                .signup(request)
                .enqueue(new Callback<MessageResponse>() {
                    @Override
                    public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                        showLoading();

                        if (response.isSuccessful() && response.body() != null) {
                            showMessage("Account created successfully!", false);
                            new Handler().postDelayed(() -> {
                                finish(); // Go back to login
                            }, 1500);
                        } else {
                            try {
                                // Try to get error message from response
                                if (response.errorBody() != null) {
                                    String errorBody = response.errorBody().string();
                                    Log.e("SignUp", "Error body: " + errorBody);
                                    JSONObject errorJson = new JSONObject(errorBody);
                                    String errorMessage = errorJson.optString("message", "Signup failed");
                                    showMessage(errorMessage, true);
                                } else {
                                    showMessage("Signup failed. Please try again.", true);
                                }
                            } catch (Exception e) {
                                Log.e("SignUp", "Error parsing error response", e);
                                showMessage("Signup failed. Please try again.", true);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<MessageResponse> call, Throwable t) {
                        hideLoading();
                        Log.e("SignUp", "Network error", t);

                        String errorMessage;
                        if (!isNetworkAvailable()) {
                            errorMessage = "No internet connection. Please check your network.";
                        } else if (t instanceof SocketTimeoutException) {
                            errorMessage = "Request timed out. Please try again.";
                        } else {
                            errorMessage = "Network error: " + t.getMessage();
                        }
                        showMessage(errorMessage, true);
                    }
                });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }
        return false;
    }
    private void showLoading() {
        signUpButton.setEnabled(false);
    }

    private void hideLoading() {
        signUpButton.setEnabled(true);
    }

    private void showMessage(String message, boolean isError) {
        MaterialTextView messageText = findViewById(R.id.messageText);
        messageText.setText(message);
        messageText.setVisibility(View.VISIBLE);

        if (isError) {
            messageText.setTextColor(getColor(R.color.error_color));
            messageText.setBackgroundResource(R.drawable.error_background);
        } else {
            messageText.setTextColor(getColor(R.color.success_color));
            messageText.setBackgroundResource(R.drawable.success_background);
        }

        // Auto hide after 3 seconds
        new Handler().postDelayed(() -> {
            messageText.setVisibility(View.GONE);
        }, 3000);
    }
}
