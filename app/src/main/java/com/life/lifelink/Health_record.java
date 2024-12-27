package com.life.lifelink;

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
        String[] genders = new String[]{"Male", "Female", "Other"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, genders);
        genderDropdown.setAdapter(genderAdapter);

        // Setup Blood Type Dropdown
        String[] bloodTypes = new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
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
        // Validate required fields
        if (!validateForm()) {
            showSnackbar("Please fill in all required fields");
            return;
        }

        // Show loading state
        ExtendedFloatingActionButton saveFab = findViewById(R.id.saveFab);
        saveFab.setEnabled(false);
        saveFab.setText("Saving...");

        // TODO: Implement actual saving logic here
        // For demo, we'll just show a success message after a delay
        new Handler().postDelayed(() -> {
            showSnackbar("Health records saved successfully");
            saveFab.setEnabled(true);
            saveFab.setText("Save Records");
            finish();
        }, 2000);
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
}