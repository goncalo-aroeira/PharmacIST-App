package pt.ulisboa.tecnico.cmov.pharmacist;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.MedicineListAdapter;

public class AddMedicine extends AppCompatActivity {

    private EditText searchMedicine;
    private RecyclerView medicineList;
    private MedicineListAdapter adapter;
    private List<Medicine> allMedicines = new ArrayList<>();
    private FirebaseDBHandler firebaseDBHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        firebaseDBHandler = new FirebaseDBHandler();

        searchMedicine = findViewById(R.id.searchMedicine);
        medicineList = findViewById(R.id.medicineList);
        medicineList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicineListAdapter(this, allMedicines);
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
                adapter.notifyDataSetChanged(); // Ensure adapter is updated on data load
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
}
