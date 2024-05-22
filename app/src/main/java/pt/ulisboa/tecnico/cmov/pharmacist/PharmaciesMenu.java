package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Pharmacy;

public class PharmaciesMenu extends AppCompatActivity {

    private ArrayList<Pharmacy> pharmacies;
    private Button btnAddPharmacy;
    private SearchView searchBarValue;
    private ListView lvPharmacies;
    private FirebaseDBHandler dbHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pharmacies_menu);
        initializeViewsAndFirebase();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadPharmacies();
    }


    private void loadPharmacies() {
        dbHandler.getAllPharmacies(new FirebaseDBHandler.OnPharmaciesLoadedListener() {
            @Override
            public void onPharmaciesLoaded(ArrayList<Pharmacy> pharmacies) {
                Log.d("PharmaciesMenu", "Pharmacies loaded: " + pharmacies.size());
                PharmaciesMenu.this.pharmacies = pharmacies;
                PharmacyAdapter pharmacyAdapter = new PharmacyAdapter(PharmaciesMenu.this, pharmacies);
                lvPharmacies.setAdapter(pharmacyAdapter);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("PharmaciesMenu", "Failed to load pharmacies", e);
            }
        });
    }

    private void initializeViewsAndFirebase() {
        lvPharmacies = findViewById(R.id.lvPharmacies);
        searchBarValue = findViewById(R.id.searchBarValue);
        btnAddPharmacy = findViewById(R.id.btnAddPharmacy);

        dbHandler = new FirebaseDBHandler();

        btnAddPharmacy.setOnClickListener(v -> {
            Intent intent = new Intent(PharmaciesMenu.this, AddPharmacy.class);
            startActivity(intent);
        });

        lvPharmacies.setOnItemClickListener((parent, view, position, id) -> {
            Pharmacy pharmacy = pharmacies.get(position);
            Intent intent = new Intent(PharmaciesMenu.this, PharmacyInformationPannel.class);
            intent.putExtra("pharmacy", pharmacy);
            LatLng location = geocodeAddress(pharmacy.getAddress());
            intent.putExtra("pharmacy_location", location);
            startActivity(intent);
        });

        searchBarValue.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterPharmacies(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterPharmacies(newText);
                return true;
            }
        });

    }


    private void filterPharmacies(String query) {
        ArrayList<Pharmacy> filteredPharmacies = new ArrayList<>();
        if (pharmacies != null) {
            for (Pharmacy pharmacy : pharmacies) {
                if (pharmacy.getName().toLowerCase().contains(query.toLowerCase()) ||
                        pharmacy.getAddress().toLowerCase().contains(query.toLowerCase())) {
                    filteredPharmacies.add(pharmacy);
                }
            }
        }

        PharmacyAdapter pharmacyAdapter = new PharmacyAdapter(PharmaciesMenu.this, filteredPharmacies);
        lvPharmacies.setAdapter(pharmacyAdapter);
    }

    private LatLng geocodeAddress(String address) {
        Log.d("Map", "Geocoding address: " + address);
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                double latitude = addresses.get(0).getLatitude();
                double longitude = addresses.get(0).getLongitude();
                return new LatLng(latitude, longitude);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}