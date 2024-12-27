package com.life.lifelink;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Insurance extends AppCompatActivity implements InsuranceAdapter.OnInsuranceClickListener {
    private RecyclerView insuranceRecyclerView;
    private View emptyStateLayout;
    private InsuranceAdapter adapter;
    private List<InsuranceDTO> insuranceList = new ArrayList<>();

    // UI Components for the form
    private AutoCompleteTextView insuranceTypeDropdown;
    private AutoCompleteTextView relationshipDropdown;
    private AutoCompleteTextView planTypeDropdown;
    private TextInputEditText startDateInput;
    private TextInputEditText endDateInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insurance);

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void initializeViews() {
        insuranceRecyclerView = findViewById(R.id.insuranceRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);

        // Initialize form components
        insuranceTypeDropdown = findViewById(R.id.insuranceTypeDropdown);
        relationshipDropdown = findViewById(R.id.relationshipDropdown);
        planTypeDropdown = findViewById(R.id.planTypeDropdown);
        startDateInput = findViewById(R.id.startDateInput);
        endDateInput = findViewById(R.id.endDateInput);
    }

    private void setupRecyclerView() {
        adapter = new InsuranceAdapter(insuranceList, this);
        insuranceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        insuranceRecyclerView.setAdapter(adapter);
    }

    private void setupDropdowns() {
        // Insurance Type Dropdown
        String[] insuranceTypes = {"Health", "Accident", "Travel", "Life", "Dental", "Vision"};
        ArrayAdapter<String> insuranceTypeAdapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, insuranceTypes);
        insuranceTypeDropdown.setAdapter(insuranceTypeAdapter);

        // Relationship Dropdown
        String[] relationships = {"Self", "Spouse", "Child", "Parent", "Other"};
        ArrayAdapter<String> relationshipAdapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, relationships);
        relationshipDropdown.setAdapter(relationshipAdapter);

        // Plan Type Dropdown
        String[] planTypes = {"HMO", "PPO", "EPO", "POS", "HDHP"};
        ArrayAdapter<String> planTypeAdapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, planTypes);
        planTypeDropdown.setAdapter(planTypeAdapter);
    }

    private void setupDatePickers() {
        DatePickerDialog.OnDateSetListener startDateListener = (view, year, month, day) -> {
            String date = String.format(Locale.US, "%02d/%02d/%d", month + 1, day, year);
            startDateInput.setText(date);
        };

        DatePickerDialog.OnDateSetListener endDateListener = (view, year, month, day) -> {
            String date = String.format(Locale.US, "%02d/%02d/%d", month + 1, day, year);
            endDateInput.setText(date);
        };

        startDateInput.setOnClickListener(v -> showDatePicker(startDateListener));
        endDateInput.setOnClickListener(v -> showDatePicker(endDateListener));
    }

    private void showDatePicker(DatePickerDialog.OnDateSetListener listener) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, listener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupButtons() {
        // Upload buttons
        MaterialButton uploadFrontButton = findViewById(R.id.uploadFrontButton);
        MaterialButton uploadBackButton = findViewById(R.id.uploadBackButton);

        uploadFrontButton.setOnClickListener(v -> handleImageUpload(true));
        uploadBackButton.setOnClickListener(v -> handleImageUpload(false));

        // Save FAB
        ExtendedFloatingActionButton saveFab = findViewById(R.id.saveFab);
        saveFab.setOnClickListener(v -> saveInsurance());
    }

    private void handleImageUpload(boolean isFront) {
        // TODO: Implement image upload functionality
        Toast.makeText(this, "Image upload not implemented yet", Toast.LENGTH_SHORT).show();
    }

    private void loadInsuranceData() {
        // TODO: Load insurance data from backend
        // For demo, let's add some dummy data
        insuranceList.clear();

        updateUIState();
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

    private void saveInsurance() {
        if (!validateForm()) {
            showSnackbar("Please fill in all required fields");
            return;
        }

        // Show loading state
        ExtendedFloatingActionButton saveFab = findViewById(R.id.saveFab);
        saveFab.setEnabled(false);
        saveFab.setText("Saving...");

        // TODO: Implement actual saving logic
        new Handler().postDelayed(() -> {
            showSnackbar("Insurance information saved successfully");
            saveFab.setEnabled(true);
            saveFab.setText("Save Insurance");

            // Refresh insurance list
            loadInsuranceData();
        }, 2000);
    }

    private boolean validateForm() {
        // Add your validation logic here
        return true;
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onInsuranceClick(InsuranceDTO insurance) {
        // TODO: Handle insurance card click (show details)
        Toast.makeText(this, "Viewing details for " + insurance.getProviderName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}