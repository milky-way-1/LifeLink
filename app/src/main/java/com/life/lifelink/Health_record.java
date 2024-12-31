package com.life.lifelink;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.life.lifelink.api.RetrofitClient;
import com.life.lifelink.model.BloodType;
import com.life.lifelink.model.EmergencyContactDto;
import com.life.lifelink.model.Gender;
import com.life.lifelink.model.MedicationDto;
import com.life.lifelink.model.PastSurgeryDto;
import com.life.lifelink.model.PatientRequest;
import com.life.lifelink.model.PatientResponse;
import com.life.lifelink.util.SessionManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Health_record extends AppCompatActivity {
    private static final int MAX_EMERGENCY_CONTACTS = 3;
    private AutoCompleteTextView genderDropdown;
    private AutoCompleteTextView bloodTypeDropdown;
    private ChipGroup emergencyContactsChipGroup;
    private ChipGroup medicalConditionsChipGroup;
    private ChipGroup surgeriesChipGroup;
    private ChipGroup medicationsChipGroup;
    private ChipGroup allergiesChipGroup;
    private ChipGroup dietaryRestrictionsChipGroup;
    private ChipGroup religiousConsiderationsChipGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_record);

        setupToolbar();
        initializeViews();
        setupDropdowns();
        setupButtons();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void initializeViews() {
        // Initialize all ChipGroups
        emergencyContactsChipGroup = findViewById(R.id.emergencyContactsChipGroup);
        medicalConditionsChipGroup = findViewById(R.id.medicalConditionsChipGroup);
        surgeriesChipGroup = findViewById(R.id.surgeriesChipGroup);
        medicationsChipGroup = findViewById(R.id.medicationsChipGroup);
        allergiesChipGroup = findViewById(R.id.allergiesChipGroup);
        dietaryRestrictionsChipGroup = findViewById(R.id.dietaryRestrictionsChipGroup);
        religiousConsiderationsChipGroup = findViewById(R.id.religiousConsiderationsChipGroup);

        // Initialize dropdowns
        genderDropdown = findViewById(R.id.genderDropdown);
        bloodTypeDropdown = findViewById(R.id.bloodTypeDropdown);
    }

    private void setupDropdowns() {
        // Setup Gender Dropdown
        String[] genders = new String[]{"MALE", "FEMALE", "OTHER"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, genders);
        genderDropdown.setAdapter(genderAdapter);

        // Setup Blood Type Dropdown
        String[] bloodTypes = new String[]{"A_POSITIVE", "A_NEGATIVE", "B_POSITIVE", "B_NEGATIVE", "AB_POSITIVE", "AB_NEGATIVE", "O_POSITIVE", "O_NEGATIVE"};
        ArrayAdapter<String> bloodTypeAdapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, bloodTypes);
        bloodTypeDropdown.setAdapter(bloodTypeAdapter);
    }

    private void setupButtons() {
        // Emergency Contact Button
        MaterialButton addEmergencyContactButton = findViewById(R.id.addEmergencyContactButton);
        addEmergencyContactButton.setOnClickListener(v -> addEmergencyContact());

        // Medical Condition Button
        MaterialButton addMedicalConditionButton = findViewById(R.id.addMedicalConditionButton);
        addMedicalConditionButton.setOnClickListener(v -> addMedicalCondition());

        // Surgery Button
        MaterialButton addSurgeryButton = findViewById(R.id.addSurgeryButton);
        addSurgeryButton.setOnClickListener(v -> addSurgery());

        // Medication Button
        MaterialButton addMedicationButton = findViewById(R.id.addMedicationButton);
        addMedicationButton.setOnClickListener(v -> addMedication());

        // Allergy Button
        MaterialButton addAllergyButton = findViewById(R.id.addAllergyButton);
        addAllergyButton.setOnClickListener(v -> addAllergy());

        // Dietary Restriction Button
        MaterialButton addDietaryRestrictionButton = findViewById(R.id.addDietaryRestrictionButton);
        addDietaryRestrictionButton.setOnClickListener(v -> addDietaryRestriction());

        // Religious Consideration Button
        MaterialButton addReligiousConsiderationButton = findViewById(R.id.addReligiousConsiderationButton);
        addReligiousConsiderationButton.setOnClickListener(v -> addReligiousConsideration());

        // Save FAB
        ExtendedFloatingActionButton saveFab = findViewById(R.id.saveFab);
        saveFab.setOnClickListener(v -> saveHealthRecords());
    }

    private void addEmergencyContact() {
        if (emergencyContactsChipGroup.getChildCount() >= MAX_EMERGENCY_CONTACTS) {
            showSnackbar("Maximum " + MAX_EMERGENCY_CONTACTS + " emergency contacts allowed");
            return;
        }

        TextInputEditText nameInput = findViewById(R.id.emergencyContactNameInput);
        TextInputEditText phoneInput = findViewById(R.id.emergencyContactPhoneInput);

        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            showSnackbar("Please enter both name and phone number");
            return;
        }

        addChip(emergencyContactsChipGroup, name + "\n" + phone);
        nameInput.setText("");
        phoneInput.setText("");
    }

    private void addMedicalCondition() {
        TextInputEditText input = findViewById(R.id.medicalConditionInput);
        String condition = input.getText().toString().trim();
        if (!condition.isEmpty()) {
            addChip(medicalConditionsChipGroup, condition);
            input.setText("");
        }
    }

    private void addSurgery() {
        TextInputEditText typeInput = findViewById(R.id.surgeryTypeInput);
        TextInputEditText dateInput = findViewById(R.id.surgeryDateInput);

        String type = typeInput.getText().toString().trim();
        String date = dateInput.getText().toString().trim();

        if (!type.isEmpty() && !date.isEmpty()) {
            addChip(surgeriesChipGroup, type + " (" + date + ")");
            typeInput.setText("");
            dateInput.setText("");
        }
    }

    private void addMedication() {
        TextInputEditText nameInput = findViewById(R.id.medicationNameInput);
        TextInputEditText dosageInput = findViewById(R.id.medicationDosageInput);

        String name = nameInput.getText().toString().trim();
        String dosage = dosageInput.getText().toString().trim();

        if (!name.isEmpty() && !dosage.isEmpty()) {
            addChip(medicationsChipGroup, name + "\n" + dosage);
            nameInput.setText("");
            dosageInput.setText("");
        }
    }

    private void addAllergy() {
        TextInputEditText input = findViewById(R.id.allergyInput);
        String allergy = input.getText().toString().trim();
        if (!allergy.isEmpty()) {
            addChip(allergiesChipGroup, allergy);
            input.setText("");
        }
    }

    private void addDietaryRestriction() {
        TextInputEditText input = findViewById(R.id.dietaryRestrictionInput);
        String restriction = input.getText().toString().trim();
        if (!restriction.isEmpty()) {
            addChip(dietaryRestrictionsChipGroup, restriction);
            input.setText("");
        }
    }

    private void addReligiousConsideration() {
        TextInputEditText input = findViewById(R.id.religiousConsiderationInput);
        String consideration = input.getText().toString().trim();
        if (!consideration.isEmpty()) {
            addChip(religiousConsiderationsChipGroup, consideration);
            input.setText("");
        }
    }

    private void addChip(ChipGroup chipGroup, String text) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setClickable(true);
        chip.setCheckable(false);

        // Apply custom styling
        chip.setChipBackgroundColorResource(R.color.purple_500);
        chip.setTextColor(getResources().getColor(android.R.color.white));
        chip.setCloseIconTint(ColorStateList.valueOf(getResources().getColor(android.R.color.white)));

        // Add close icon click listener
        chip.setOnCloseIconClickListener(v -> chipGroup.removeView(chip));

        // Add animation
        chip.setAlpha(0f);
        chip.animate().alpha(1f).setDuration(300).start();

        chipGroup.addView(chip);
    }

    private void saveHealthRecords() {
        if (!validateForm()) {
            showSnackbar("Please fill in all required fields");
            return;
        }

        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();

        if (token == null) {
            showSnackbar("Session expired. Please login again");
            navigateToLogin();
            return;
        }


        // Show loading state
        ExtendedFloatingActionButton saveFab = findViewById(R.id.saveFab);
        saveFab.setEnabled(false);
        saveFab.setText("Saving...");

        // Create PatientRequest object
        PatientRequest request = new PatientRequest();

        // Get basic information
        TextInputEditText nameInput = findViewById(R.id.fullNameInput);
        TextInputEditText ageInput = findViewById(R.id.ageInput);
        TextInputEditText weightInput = findViewById(R.id.weightInput);
        TextInputEditText heightInput = findViewById(R.id.heightInput);

        request.setFullName(nameInput.getText().toString().trim());
        request.setAge(Integer.parseInt(ageInput.getText().toString().trim()));
        request.setGender(Gender.valueOf(genderDropdown.getText().toString().toUpperCase()));
        request.setBloodType(BloodType.valueOf(bloodTypeDropdown.getText().toString().replace("+", "_POSITIVE").replace("-", "_NEGATIVE")));
        request.setWeight(Double.parseDouble(weightInput.getText().toString().trim()));
        request.setHeight(Double.parseDouble(heightInput.getText().toString().trim()));

        // Get emergency contacts
        List<EmergencyContactDto> emergencyContacts = new ArrayList<>();
        for (int i = 0; i < emergencyContactsChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) emergencyContactsChipGroup.getChildAt(i);
            String[] parts = chip.getText().toString().split("\n");
            emergencyContacts.add(new EmergencyContactDto(parts[0], parts[1]));
        }
        request.setEmergencyContacts(emergencyContacts);

        // Get medical conditions
        List<String> medicalHistory = getChipTexts(medicalConditionsChipGroup);
        request.setMedicalHistory(medicalHistory);

        // Get surgeries
        List<PastSurgeryDto> surgeries = new ArrayList<>();
        for (int i = 0; i < surgeriesChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) surgeriesChipGroup.getChildAt(i);
            String text = chip.getText().toString();
            String type = text.substring(0, text.lastIndexOf("(")).trim();
            String date = text.substring(text.lastIndexOf("(") + 1, text.length() - 1);
            surgeries.add(new PastSurgeryDto(type, date));
        }
        request.setPastSurgeries(surgeries);

        // Get medications
        List<MedicationDto> medications = new ArrayList<>();
        for (int i = 0; i < medicationsChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) medicationsChipGroup.getChildAt(i);
            String[] parts = chip.getText().toString().split("\n");
            medications.add(new MedicationDto(parts[0], parts[1]));
        }
        request.setCurrentMedications(medications);

        // Get other information
        request.setAllergies(getChipTexts(allergiesChipGroup));
        request.setDietaryRestrictions(getChipTexts(dietaryRestrictionsChipGroup));
        request.setCulturalConsiderations(getChipTexts(religiousConsiderationsChipGroup));

        // Make API call
        RetrofitClient.getInstance()
                .getApiService()
                .createPatientProfile("Bearer " + token, request)
                .enqueue(new Callback<PatientResponse>() {
                    @Override
                    public void onResponse(Call<PatientResponse> call, Response<PatientResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            showSnackbar("Health records saved successfully");

                            // Store patient data in SharedPreferences if needed
                            savePatientData(response.body());

                            // Navigate to dashboard
                            new Handler().postDelayed(() -> {
                                Intent intent = new Intent(Health_record.this, dashboard.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }, 1000);
                        } else {
                            saveFab.setEnabled(true);
                            saveFab.setText("Save Records");
                            try {
                                JSONObject errorBody = new JSONObject(response.errorBody().string());
                                showSnackbar(errorBody.getString("message"));
                            } catch (Exception e) {
                                showSnackbar("Failed to save health records" + token);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<PatientResponse> call, Throwable t) {
                        saveFab.setEnabled(true);
                        saveFab.setText("Save Records");
                        showSnackbar("Network error. Please try again." + token);
                    }
                });
    }

    private List<String> getChipTexts(ChipGroup chipGroup) {
        List<String> texts = new ArrayList<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            texts.add(chip.getText().toString());
        }
        return texts;
    }

    private void savePatientData(PatientResponse response) {
        SharedPreferences prefs = getSharedPreferences("PatientData", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("patientId", response.getId());
        editor.putString("fullName", response.getFullName());
        // Add other fields as needed
        editor.apply();
    }

    private boolean validateForm() {
        TextInputEditText nameInput = findViewById(R.id.fullNameInput);
        TextInputEditText ageInput = findViewById(R.id.ageInput);

        return !nameInput.getText().toString().trim().isEmpty() &&
                !ageInput.getText().toString().trim().isEmpty() &&
                !genderDropdown.getText().toString().trim().isEmpty() &&
                emergencyContactsChipGroup.getChildCount() > 0;
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void resetSaveButton(ExtendedFloatingActionButton saveFab) {
        saveFab.setEnabled(true);
        saveFab.setText("Save Records");
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, dashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}