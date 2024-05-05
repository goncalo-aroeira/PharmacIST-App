package pt.ulisboa.tecnico.cmov.pharmacist;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.PharmacyManager;

;


public class MedicineActivity extends AppCompatActivity {
    // Domain
    PharmacyManager pharmacyManager;
    ArrayList<Pharmacy> pharmacies;

    // UI
    SearchView searchBarValue;
    ListView lvPharmacies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medicine);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        pharmacyManager = new PharmacyManager(new FirebaseDBHandler());

        pharmacyManager.loadPharmacies(new PharmacyManager.OnPharmaciesLoadedListener() {
            @Override
            public void onPharmaciesLoaded(ArrayList<Pharmacy> pharmacies) {
                Log.d("Medicine Ativity Page", "Pharmacies: " + pharmacies.size() + " pharmacies loaded.");
                MedicineActivity.this.pharmacies = pharmacies;
                PharmacyAdapter pharmacyAdapter = new PharmacyAdapter(MedicineActivity.this, pharmacies);
                lvPharmacies.setAdapter(pharmacyAdapter);

            }

            @Override
            public void onPharmaciesLoadFailed(Exception e) {
                Log.e("Error", "MedicineActivity: Failed to load pharmacies", e);
            }
        });

        // UI
        searchBarValue = (SearchView) findViewById(R.id.searchBarValue);
        lvPharmacies = (ListView) findViewById(R.id.lvPharmacies);


        // Search
        searchBarValue.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ArrayList<Pharmacy> filteredPharmacies = new ArrayList<>();
                for (Pharmacy pharmacy : pharmacies) {
                    if (pharmacy.getName().toLowerCase().contains(query.toLowerCase()) || pharmacy.getAddress().toLowerCase().contains(query.toLowerCase())) {
                        filteredPharmacies.add(pharmacy);
                    }
                }
                PharmacyAdapter pharmacyAdapter = new PharmacyAdapter(MedicineActivity.this, filteredPharmacies);
                lvPharmacies.setAdapter(pharmacyAdapter);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<Pharmacy> filteredPharmacies = new ArrayList<>();
                for (Pharmacy pharmacy : pharmacies) {
                    if (pharmacy.getName().toLowerCase().contains(newText.toLowerCase())) {
                        filteredPharmacies.add(pharmacy);
                    }
                }
                PharmacyAdapter pharmacyAdapter = new PharmacyAdapter(MedicineActivity.this, filteredPharmacies);
                lvPharmacies.setAdapter(pharmacyAdapter);
                return true;
            }
        });

    }
}