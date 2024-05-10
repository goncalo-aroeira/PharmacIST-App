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

    ArrayList<Pharmacy> pharmacies;
    Button btnAddPharmacy;

    // UI
    SearchView searchBarValue;
    ListView lvPharmacies;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pharmacies_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    FirebaseDBHandler dbHandler = new FirebaseDBHandler();
    lvPharmacies = (ListView) findViewById(R.id.lvPharmacies);
    searchBarValue = (SearchView) findViewById(R.id.searchBarValue);
    btnAddPharmacy = (Button) findViewById(R.id.btnAddPharmacy);


        dbHandler.getAllPharmacies(new FirebaseDBHandler.OnPharmaciesLoadedListener() {
            @Override
            public void onPharmaciesLoaded(ArrayList<Pharmacy> pharmacies) {
                Log.d("Medicine Activity Page", "Pharmacies: " + pharmacies.size() + " pharmacies loaded.");
                PharmaciesMenu.this.pharmacies = pharmacies;
                PharmacyAdapter pharmacyAdapter = new PharmacyAdapter(PharmaciesMenu.this, pharmacies);
                lvPharmacies.setAdapter(pharmacyAdapter);

            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Error", "MedicineActivity: Failed to load pharmacies", e);
            }
        });
        btnAddPharmacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PharmaciesMenu.this, AddPharmacy.class);
                startActivity(intent);
            }
        });

        // Connect List of pharmacies to information page


        lvPharmacies.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Pharmacy pharmacy = (Pharmacy) parent.getItemAtPosition(position);
                Intent intent = new Intent(PharmaciesMenu.this, PharmacyInformationPannel.class);
                intent.putExtra("pharmacy", pharmacy);
                LatLng location = geocodeAddress(pharmacy.getAddress());
                intent.putExtra("pharmacy_location", location);
                startActivity(intent);
            }
        });

    // Search
        searchBarValue.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            ArrayList<Pharmacy> filteredPharmacies = new ArrayList<>();
            for (Pharmacy pharmacy : pharmacies) {
                for (Medicine medicine : pharmacy.getInventory().keySet()) {
                    if (medicine.getName().toLowerCase().contains(query.toLowerCase())) {
                        filteredPharmacies.add(pharmacy);
                    }
                }
            }
            PharmacyAdapter pharmacyAdapter = new PharmacyAdapter(PharmaciesMenu.this, filteredPharmacies);
            lvPharmacies.setAdapter(pharmacyAdapter);
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            ArrayList<Pharmacy> filteredPharmacies = new ArrayList<>();
            for (Pharmacy pharmacy : pharmacies) {
                for (Medicine medicine : pharmacy.getInventory().keySet()) {
                    if (medicine.getName().toLowerCase().contains(newText.toLowerCase())) {
                        filteredPharmacies.add(pharmacy);
                    }
                }
            }
            PharmacyAdapter pharmacyAdapter = new PharmacyAdapter(PharmaciesMenu.this, filteredPharmacies);
            lvPharmacies.setAdapter(pharmacyAdapter);
            return true;
        }

        


    });

    }

    private LatLng geocodeAddress(String address) {
        Log.d("Map", "Geocoding address: " + address);
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(address, 1);
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