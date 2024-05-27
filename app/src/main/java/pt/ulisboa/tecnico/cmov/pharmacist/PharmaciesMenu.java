package pt.ulisboa.tecnico.cmov.pharmacist;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.UserLocalStore;
import pt.ulisboa.tecnico.cmov.pharmacist.elements.PharmacyAdapter;

public class PharmaciesMenu extends AppCompatActivity {

    private ArrayList<Pharmacy> pharmacies;
    private Button btnAddPharmacy;
    private SearchView searchBarValue;
    private ListView lvPharmacies;
    private FirebaseDBHandler dbHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pharmacies_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViewsAndFirebase();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadPharmacies();
    }


    private void loadPharmacies() {

        UserLocalStore user = new UserLocalStore(this);
        if (user.getLoggedInName().equals("Guest")) {
            btnAddPharmacy.setEnabled(false);
        }

        String userId = user.getLoggedInId();

        dbHandler.loadPharmacies(userId, new FirebaseDBHandler.OnPharmaciesLoadedListener() {
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

        Collections.sort(filteredPharmacies, Comparator.comparingDouble(Pharmacy::getDistance)
        );

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

}