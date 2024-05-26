package pt.ulisboa.tecnico.cmov.pharmacist;

import static androidx.core.location.LocationManagerCompat.getCurrentLocation;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.elements.PharmacyAdapter;

public class MedicineInformationPannel extends AppCompatActivity {

    private TextView medicineNameTextView;
    private TextView medicinePurposeTextView;
    private ImageView medicineImageView;
    private ListView pharmaciesListView;
    private PharmacyAdapter pharmacyAdapter;
    private ArrayList<Pharmacy> allPharmacies;
    private LatLng currentUserLocation;
    private static final int LOCATION_REQUEST_CODE = 101;
    private HashMap<String, LatLng> addressCache = new HashMap<>();

    private FirebaseDBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_information_pannel);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        dbHandler = new FirebaseDBHandler();
        medicineNameTextView = findViewById(R.id.textView_medicine_name);


        // Retrieve and display medicine data
        Medicine medicine = (Medicine) getIntent().getSerializableExtra("medicine");
        if (medicine != null) {
            medicineNameTextView.setText(medicine.getName());
        }


        pharmaciesListView = findViewById(R.id.recyclerViewPharmacies);

        medicine = (Medicine) getIntent().getSerializableExtra("medicine");


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
        fetchAllPharmacies(medicine);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchAllPharmacies(Medicine medicine) {
        allPharmacies = dbHandler.getPharmacies();
        filterPharmaciesWithMedicine(medicine);
        calculateDistancesAndUpdateList();
    }

    private void filterPharmaciesWithMedicine(Medicine medicine) {
        if (allPharmacies == null) {
            Log.e("MedicineInformationPannel", "Pharmacies data is not available.");
            return;
        }
        ArrayList<Pharmacy> filteredPharmacies = new ArrayList<>();
        dbHandler.getInventoryForMedicine(medicine.getId(), new FirebaseDBHandler.OnGetInventory() {
            @Override
            public void onInventoryLoaded(HashMap<String, Integer> inventory) {
                for (Pharmacy pharmacy : allPharmacies) {
                    if (inventory.containsKey(pharmacy.getId())) {
                        filteredPharmacies.add(pharmacy);
                    }
                }
                updatePharmacyList(filteredPharmacies);
            }

            @Override
            public void onFailure(Exception e) {

            };
        });
    }

    private void updatePharmacyList(ArrayList<Pharmacy> pharmacies) {
        pharmacyAdapter = new PharmacyAdapter(this, pharmacies);
        pharmaciesListView.setAdapter(pharmacyAdapter);
    }

    private void sortPharmaciesByDistance() {
        if (allPharmacies == null) {
            Log.e("MedicineInformationPannel", "Pharmacies data is not available.");
            return;
        }
        Collections.sort(allPharmacies, Comparator.comparingDouble(Pharmacy::getDistance)
        );
        updatePharmacyList(allPharmacies);
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
                    for (Pharmacy pharmacy : allPharmacies) {
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
