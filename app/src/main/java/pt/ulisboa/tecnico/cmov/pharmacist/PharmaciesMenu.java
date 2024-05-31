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
import android.widget.ImageButton;
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
import java.util.HashMap;
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
    private LatLng currentUserLocation;
    private static final int LOCATION_REQUEST_CODE = 101;
    private HashMap<String, LatLng> addressCache = new HashMap<>();
    PharmacyAdapter pharmacyAdapter;


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

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }

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
                calculateDistancesAndUpdateList();
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
        ImageButton btnBack = findViewById(R.id.btnBack);  // Get the ImageButton
        btnBack.setOnClickListener(v -> finish());  // Set the back button to finish the activity

        dbHandler = new FirebaseDBHandler();

        btnAddPharmacy.setOnClickListener(v -> {
            Intent intent = new Intent(PharmaciesMenu.this, AddPharmacy.class);
            startActivity(intent);
        });

        lvPharmacies.setOnItemClickListener((parent, view, position, id) -> {
            Pharmacy pharmacy = pharmacies.get(position);
            Intent intent = new Intent(PharmaciesMenu.this, PharmacyInformationPannel.class);
            intent.putExtra("pharmacy_id", pharmacy.getId());
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



    private void updatePharmacyList(ArrayList<Pharmacy> pharmacies) {
        pharmacyAdapter = new PharmacyAdapter(this, pharmacies);
        lvPharmacies.setAdapter(pharmacyAdapter);
    }

    private void sortPharmaciesByDistance() {
        if (pharmacies == null) {
            Log.e("MedicineInformationPannel", "Pharmacies data is not available.");
            return;
        }
        Collections.sort(pharmacies, Comparator.comparingDouble(Pharmacy::getDistance)
        );
        updatePharmacyList(pharmacies);
    }



    private double calculateDistance(LatLng start, LatLng end) {
        float[] results = new float[1];
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results);
        Log.d("MedicineInformationPannel", "Distance: " + results[0] / 1000 + " km");
        return results[0] / 1000; // Convert meters to kilometers
    }

    private void getCurrentLocation() {
        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    sortPharmaciesByDistance();  // Call sorting right after location is obtained
                } else {
                    Log.e("MedicineInformationPannel", "Location is null");
                }
            }).addOnFailureListener(e -> Log.e("MedicineInformationPannel", "Failed to get location", e));
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
    }

    private LatLng geocodeAddress(String address) {
        if (addressCache.containsKey(address)) {
            return addressCache.get(address);
        }
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                double latitude = addresses.get(0).getLatitude();
                double longitude = addresses.get(0).getLongitude();
                LatLng latLng = new LatLng(latitude, longitude);
                addressCache.put(address, latLng);
                return latLng;
            }
        } catch (IOException e) {
            Log.e("Geocode", "Failed to geocode address", e);
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

    private void calculateDistancesAndUpdateList() {
        if (currentUserLocation == null) {
            Log.e("MedicineInformationPannel", "Current location is not available.");
            return;
        }

        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    for (Pharmacy pharmacy : pharmacies) {
                        LatLng pharmacyLocation = geocodeAddress(pharmacy.getAddress());
                        if (pharmacyLocation != null) {
                            double distance = calculateDistance(currentUserLocation, pharmacyLocation);
                            pharmacy.setDistance(distance);
                        } else {
                            pharmacy.setDistance(Double.MAX_VALUE);  // Set a high value if location is not found
                        }
                    }
                    sortPharmaciesByDistance();
                } else {
                    Log.e("MedicineInformationPannel", "Location is null");
                }
            }).addOnFailureListener(e -> Log.e("MedicineInformationPannel", "Failed to get location", e));
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
    }

}