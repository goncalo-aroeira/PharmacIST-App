package pt.ulisboa.tecnico.cmov.pharmacist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.elements.MedicineListAdapter;

public class AddMedicine extends AppCompatActivity implements MedicineListAdapter.onMedicineClicked {

    private EditText searchMedicine;
    private MedicineListAdapter adapter;
    private final List<Medicine> allMedicines = new ArrayList<>();
    private FirebaseDBHandler firebaseDBHandler;
    private Pharmacy pharmacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_add_medicine);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseDBHandler = new FirebaseDBHandler();

        // get intent extras
        Intent intent = getIntent();
        String medicine_key = intent.getStringExtra("medicine_key");
        String pharmacyId = intent.getStringExtra("pharmacy_id");

        Log.d("AddMedicine", "onCreate:  pharmacyId: " + pharmacyId);
        Log.d("AddMedicine", "onCreate:  medicineKey: " + medicine_key);

        firebaseDBHandler.getPharmacyById(pharmacyId, new FirebaseDBHandler.OnPharmacyLoadedListener() {
            @Override
            public void onPharmacyLoaded(Pharmacy pharmacy) {
                AddMedicine.this.pharmacy = pharmacy;
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AddMedicine.this, "Failed to load pharmacy: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        if (medicine_key != null) {
            Log.d("AddMedicine", "onCreate: medicine_key: " + medicine_key);

            firebaseDBHandler.getMedicineById(medicine_key, new FirebaseDBHandler.OnMedicineLoadedListener() {
                @Override
                public void onMedicineLoaded(Medicine medicine) {
                    showConfirmDialog(medicine);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(AddMedicine.this, "Failed to load medicine: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setupViews();
                }
            });
        } else {
            setupViews();
        }
    }

    private void setupViews() {
        searchMedicine = findViewById(R.id.searchMedicine);
        RecyclerView medicineList = findViewById(R.id.medicineList);
        medicineList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicineListAdapter(this, allMedicines, this);
        medicineList.setAdapter(adapter);

        loadMedicines();
        setupSearchBar();
    }

    private void loadMedicines() {
        firebaseDBHandler.getAllMedicines(new FirebaseDBHandler.OnMedicinesLoadedListener() {
            @Override
            public void onMedicinesLoaded(ArrayList<Medicine> medicines) {
                allMedicines.clear();
                allMedicines.addAll(medicines);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AddMedicine.this, "Failed to load medicines: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearchBar() {
        searchMedicine.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMedicines(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used
            }
        });
    }

    private void filterMedicines(String query) {
        List<Medicine> filteredList = new ArrayList<>();
        for (Medicine medicine : allMedicines) {
            if (medicine.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(medicine);
            }
        }
        adapter.updateList(filteredList); // Ensure your adapter has a method to update its data list
    }

    private void showConfirmDialog(Medicine medicine){
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_confirm_quantity, null);
            builder.setView(dialogView);

            TextView medicineName = dialogView.findViewById(R.id.medicineName);
            TextView pharmacyNameTv = dialogView.findViewById(R.id.pharmacyName);

            Button decreaseButton = dialogView.findViewById(R.id.decreaseButton);
            Button increaseButton = dialogView.findViewById(R.id.increaseButton);
            EditText quantityInput = dialogView.findViewById(R.id.quantityInput);
            Button confirmButton = dialogView.findViewById(R.id.confirmButton);

            medicineName.setText(medicine.getName());
            pharmacyNameTv.setText(pharmacy != null ? pharmacy.getName() : "Unknown");

            decreaseButton.setOnClickListener(v -> {
                int currentQuantity = Integer.parseInt(quantityInput.getText().toString());
                if (currentQuantity > 0) {
                    quantityInput.setText(String.valueOf(currentQuantity - 1));
                }
            });

            increaseButton.setOnClickListener(v -> {
                int currentQuantity = Integer.parseInt(quantityInput.getText().toString());
                quantityInput.setText(String.valueOf(currentQuantity + 1));
            });

            AlertDialog dialog = builder.create();

            confirmButton.setOnClickListener(v -> {
                int quantity = Integer.parseInt(quantityInput.getText().toString());
                addMedicineToInventory(medicine, quantity);
                dialog.dismiss();
            });

            dialog.show();
        } catch (Exception e) {
            Log.e("AddMedicine", "Error showing confirm dialog", e);
            Toast.makeText(this, "Failed to show dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void addMedicineToInventory(Medicine medicine, int quantity) {
        firebaseDBHandler.addMedicineToPharmacyInventory(pharmacy.getId(), medicine.getId(), quantity, new FirebaseDBHandler.OnChangeListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(AddMedicine.this, "Medicine added to inventory", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AddMedicine.this, "Failed to add medicine to inventory: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLocale(String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", language);
        editor.apply();
    }

    public void loadLocale(){
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "");
        setLocale(language);
    }


    @Override
    public void onMedicineSelected(Medicine medicine) {
        Log.d("AddMedicine", "onMedicineSelected: " + medicine.getName());
        showConfirmDialog(medicine);
    }
}
