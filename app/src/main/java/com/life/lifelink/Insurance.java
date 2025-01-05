package com.life.lifelink;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.life.lifelink.api.ApiService;
import com.life.lifelink.api.RetrofitClient;
import com.life.lifelink.model.InsuranceRequest;
import com.life.lifelink.model.InsuranceResponse;
import com.life.lifelink.util.SessionManager;


import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Insurance extends AppCompatActivity implements InsuranceAdapter.OnInsuranceClickListener {
    private static final String TAG = "Insurance";

    private RecyclerView insuranceRecyclerView;
    private View emptyStateLayout;
    private InsuranceAdapter adapter;
    private List<InsuranceDTO> insuranceList = new ArrayList<>();

    // Update field names to match XML IDs
    private AutoCompleteTextView insuranceTypeDropdown;
    private AutoCompleteTextView relationshipDropdown;
    private AutoCompleteTextView planTypeDropdown;
    private TextInputEditText providerNameInput;
    private TextInputEditText policyNumberInput;
    private TextInputEditText groupNumberInput;
    private TextInputEditText policyholderNameInput; // Updated to match XML
    private TextInputEditText startDateInput;
    private TextInputEditText endDateInput;
    private SwitchMaterial emergencyServicesSwitch; // Changed to SwitchMaterial
    private SwitchMaterial ambulanceServicesSwitch; // Changed to SwitchMaterial
    private ExtendedFloatingActionButton saveFab;

    private SessionManager sessionManager;
    private ApiService apiService;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insurance);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getInstance().getApiService();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        setupToolbar();
        initializeViews();
        setupRecyclerView();
        setupDropdowns();
        setupDatePickers();
        setupButtons();
        loadInsuranceData();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void initializeViews() {
        insuranceRecyclerView = findViewById(R.id.insuranceRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);

        // Update view bindings to match XML IDs
        insuranceTypeDropdown = findViewById(R.id.insuranceTypeDropdown);
        relationshipDropdown = findViewById(R.id.relationshipDropdown);
        planTypeDropdown = findViewById(R.id.planTypeDropdown);
        providerNameInput = findViewById(R.id.providerNameInput);
        policyNumberInput = findViewById(R.id.policyNumberInput);
        groupNumberInput = findViewById(R.id.groupNumberInput);
        policyholderNameInput = findViewById(R.id.policyholderNameInput);
        startDateInput = findViewById(R.id.startDateInput);
        endDateInput = findViewById(R.id.endDateInput);
        emergencyServicesSwitch = findViewById(R.id.emergencyServicesSwitch);
        ambulanceServicesSwitch = findViewById(R.id.ambulanceServicesSwitch);
        saveFab = findViewById(R.id.saveFab);
    }

    private void setupRecyclerView() {
        adapter = new InsuranceAdapter(insuranceList, this);
        insuranceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        insuranceRecyclerView.setAdapter(adapter);
    }

    private void setupDropdowns() {
        String[] insuranceTypes = {"HEALTH", "ACCIDENT", "TRAVEL", "LIFE", "DENTAL", "VISION"};
        ArrayAdapter<String> insuranceTypeAdapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, insuranceTypes);
        insuranceTypeDropdown.setAdapter(insuranceTypeAdapter);

        String[] relationships = {"SELF", "SPOUSE", "CHILD", "PARENT", "OTHER"};
        ArrayAdapter<String> relationshipAdapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, relationships);
        relationshipDropdown.setAdapter(relationshipAdapter);

        String[] planTypes = {"HMO", "PPO", "EPO", "POS", "HDHP"};
        ArrayAdapter<String> planTypeAdapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, planTypes);
        planTypeDropdown.setAdapter(planTypeAdapter);
    }

    private void setupDatePickers() {
        View.OnClickListener dateClickListener = v -> {
            TextInputEditText dateInput = (TextInputEditText) v;
            Calendar calendar = Calendar.getInstance();

            new DatePickerDialog(this, (view, year, month, day) -> {
                calendar.set(year, month, day);
                dateInput.setText(dateFormat.format(calendar.getTime()));
            },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        };

        startDateInput.setOnClickListener(dateClickListener);
        endDateInput.setOnClickListener(dateClickListener);
    }

    private void setupButtons() {
        saveFab.setOnClickListener(v -> saveInsurance());
    }

    private void loadInsuranceData() {
        String token = sessionManager.getToken();
        if (token == null) {
            showSnackbar("Session expired. Please login again");
            navigateToLogin();
            return;
        }

        apiService.getAllInsurance("Bearer " + token)
                .enqueue(new Callback<List<InsuranceResponse>>() {
                    @Override
                    public void onResponse(Call<List<InsuranceResponse>> call, Response<List<InsuranceResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            insuranceList.clear();
                            insuranceList.addAll(mapToInsuranceDTO(response.body()));
                            updateUIState();
                        } else {
                            handleError(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<InsuranceResponse>> call, Throwable t) {
                        Log.e(TAG, "Network error", t);
                        showSnackbar("Network error: " + t.getMessage());
                    }
                });
    }

    private void saveInsurance() {
        if (!validateForm()) {
            showSnackbar("Please fill in all required fields");
            return;
        }

        String token = sessionManager.getToken();
        if (token == null) {
            showSnackbar("Session expired. Please login again");
            navigateToLogin();
            return;
        }

        saveFab.setEnabled(false);
        saveFab.setText("Saving...");

        InsuranceRequest request = createInsuranceRequest();

        apiService.createInsurance("Bearer " + token, request)
                .enqueue(new Callback<InsuranceResponse>() {
                    @Override
                    public void onResponse(Call<InsuranceResponse> call, Response<InsuranceResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            showSnackbar("Insurance information saved successfully");
                            clearForm();
                            loadInsuranceData();
                        } else {
                            handleError(response);
                        }
                        resetSaveButton();
                    }

                    @Override
                    public void onFailure(Call<InsuranceResponse> call, Throwable t) {
                        Log.e(TAG, "Network error", t);
                        showSnackbar("Network error: " + t.getMessage());
                        resetSaveButton();
                    }
                });
    }

    private InsuranceRequest createInsuranceRequest() {
        InsuranceRequest request = new InsuranceRequest();
        request.setInsuranceProviderName(providerNameInput.getText().toString().trim());
        request.setPolicyNumber(policyNumberInput.getText().toString().trim());
        request.setGroupNumber(groupNumberInput.getText().toString().trim());
        request.setInsuranceType(insuranceTypeDropdown.getText().toString().trim());
        request.setPolicyHolderName(policyholderNameInput.getText().toString().trim());
        request.setRelationshipToPolicyHolder(relationshipDropdown.getText().toString().trim());
        request.setStartDate(startDateInput.getText().toString().trim());
        request.setEndDate(endDateInput.getText().toString().trim());
        request.setPlanType(planTypeDropdown.getText().toString().trim());
        request.setCoversEmergencyService(emergencyServicesSwitch.isChecked());
        request.setCoversAmbulanceService(ambulanceServicesSwitch.isChecked());
        return request;
    }

    private void updateUIState() {
        if (insuranceList.isEmpty()) {
            insuranceRecyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            insuranceRecyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            adapter.updateInsuranceList(insuranceList);
        }
    }

    private boolean validateForm() {
        return !providerNameInput.getText().toString().trim().isEmpty() &&
                !policyNumberInput.getText().toString().trim().isEmpty() &&
                !policyholderNameInput.getText().toString().trim().isEmpty() &&
                !insuranceTypeDropdown.getText().toString().trim().isEmpty() &&
                !relationshipDropdown.getText().toString().trim().isEmpty() &&
                !planTypeDropdown.getText().toString().trim().isEmpty() &&
                !startDateInput.getText().toString().trim().isEmpty();
    }

    private void handleError(Response<?> response) {
        if (response.code() == 401) {
            sessionManager.clearSession();
            showSnackbar("Session expired. Please login again");
            navigateToLogin();
        } else {
            try {
                JSONObject errorBody = new JSONObject(response.errorBody().string());
                showSnackbar(errorBody.getString("message"));
            } catch (Exception e) {
                showSnackbar("Failed to process insurance information");
            }
        }
    }

    private void resetSaveButton() {
        saveFab.setEnabled(true);
        saveFab.setText("Save Insurance");
    }

    private void clearForm() {
        providerNameInput.setText("");
        policyNumberInput.setText("");
        groupNumberInput.setText("");
        insuranceTypeDropdown.setText("");
        policyholderNameInput.setText("");
        relationshipDropdown.setText("");
        startDateInput.setText("");
        endDateInput.setText("");
        planTypeDropdown.setText("");
        emergencyServicesSwitch.setChecked(false);
        ambulanceServicesSwitch.setChecked(false);
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onInsuranceClick(InsuranceDTO insurance) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private List<InsuranceDTO> mapToInsuranceDTO(List<InsuranceResponse> responses) {
        List<InsuranceDTO> dtos = new ArrayList<>();
        for (InsuranceResponse response : responses) {
            InsuranceDTO dto = new InsuranceDTO();
            dto.setId(response.getId());
            dto.setProviderName(response.getInsuranceProviderName());
            dto.setPolicyNumber(response.getPolicyNumber());
            dto.setInsuranceType(response.getInsuranceType());
            dto.setPolicyHolderName(response.getPolicyHolderName());
            dto.setStartDate(response.getStartDate());
            dto.setEndDate(response.getEndDate());
            dtos.add(dto);
        }
        return dtos;
    }
}