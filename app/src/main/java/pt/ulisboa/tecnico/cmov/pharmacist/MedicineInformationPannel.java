package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
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
import pt.ulisboa.tecnico.cmov.pharmacist.domain.UserLocalStore;
import pt.ulisboa.tecnico.cmov.pharmacist.elements.PharmacyAdapter;
import pt.ulisboa.tecnico.cmov.pharmacist.elements.utils;

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
    private ImageButton toggleNotificatioButton;

    private Medicine medicine;

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
        medicinePurposeTextView = findViewById(R.id.textView_medicine_usage);
        medicineImageView = findViewById(R.id.ivMedicinePhoto);
        toggleNotificatioButton = findViewById(R.id.imageButton_notification);


        // Retrieve and display medicine data
        String medicine_id = (String) getIntent().getStringExtra("medicine_id");

        dbHandler.getMedicineById(medicine_id, new FirebaseDBHandler.OnMedicineLoadedListener() {
            @Override
            public void onMedicineLoaded(Medicine medicine) {
                MedicineInformationPannel.this.medicine = medicine;
                medicineNameTextView.setText(medicine.getName());
                medicinePurposeTextView.setText(medicine.getUsage());
                Bitmap image = utils.convertCompressedByteArrayToBitmap(medicine.getImageBytes());
                medicineImageView.setImageBitmap(image);
                toggleNotification(medicine);
                fetchAllPharmacies(medicine);

            }

            @Override
            public void onFailure(Exception e) {
                Log.e("MedicineInformationPannel", "Failed to load medicine", e);
            }
        });

        pharmaciesListView = findViewById(R.id.recyclerViewPharmacies);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
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


    private void toggleNotification(Medicine medicine) {
        UserLocalStore userLocalStore = new UserLocalStore(this);
        if (userLocalStore.getLoggedInId() != null) {
            dbHandler.checkNotificationExists(medicine.getId(), userLocalStore.getLoggedInId(), new FirebaseDBHandler.OnCheckNotificationExists() {
                @Override
                public void onExists(boolean exists) {
                    if (exists) {
                        medicine.setHasNotification(true);
                        toggleNotificatioButton.setImageResource(R.drawable.ic_notification_active);
                    } else {
                        medicine.setHasNotification(false);
                        toggleNotificatioButton.setImageResource(R.drawable.ic_notifications);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("MedicineInformationPannel", "Failed to check notification", e);
                }
            });
        }

        toggleNotificatioButton.setOnClickListener(v -> {
            if (medicine.getHasNotification()) {
                removeNotification(medicine.getId());
                medicine.setHasNotification(false);
                toggleNotificatioButton.setImageResource(R.drawable.ic_notifications);
            } else {
                createNotification(medicine.getId());
                medicine.setHasNotification(true);
                toggleNotificatioButton.setImageResource(R.drawable.ic_notification_active);
            }
        });
    }

    private void fetchAllPharmacies(Medicine medicine) {
        UserLocalStore userLocalStore = new UserLocalStore(this);
        dbHandler.loadPharmacies(userLocalStore.getLoggedInId(), new FirebaseDBHandler.OnPharmaciesLoadedListener() {
            @Override
            public void onPharmaciesLoaded(ArrayList<Pharmacy> pharmacies) {
                allPharmacies = pharmacies;
                filterPharmaciesWithMedicine(medicine);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("MedicineInformationPannel", "Failed to load pharmacies", e);
            }
        });

        UserLocalStore user = new UserLocalStore(this);

        String userId = user.getLoggedInId();

        dbHandler.loadPharmacies(userId, new FirebaseDBHandler.OnPharmaciesLoadedListener() {
            @Override
            public void onPharmaciesLoaded(ArrayList<Pharmacy> pharmacies) {
                Log.d("PharmaciesMenu", "Pharmacies loaded: " + pharmacies.size());
                MedicineInformationPannel.this.allPharmacies = pharmacies;
                filterPharmaciesWithMedicine(medicine);
                calculateDistancesAndUpdateList();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("PharmaciesMenu", "Failed to load pharmacies", e);
            }
        });
    }

    private void filterPharmaciesWithMedicine(Medicine medicine) {
        if (allPharmacies == null) {
            Log.e("MedicineInformationPannel", "Pharmacies data is not available.");
            return;
        }
        ArrayList<Pharmacy> filteredPharmacies = new ArrayList<>();
        dbHandler.getInventoryForMedicine(medicine, new FirebaseDBHandler.OnGetInventory() {
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


    private void createNotification(String medicine_id) {

        String userId = new UserLocalStore(this).getLoggedInId();

        dbHandler.addNotification(medicine_id, userId, new FirebaseDBHandler.onCreateNotification() {

            @Override
            public void onAlreadyExists() {
                Toast.makeText(MedicineInformationPannel.this, "Notification already exists", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                Toast.makeText(MedicineInformationPannel.this, "Notification created successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MedicineInformationPannel.this, "Failed to create notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeNotification(String medicine_id) {

        String userId = new UserLocalStore(this).getLoggedInEmail();
        dbHandler.removeNotification(medicine_id, userId, new FirebaseDBHandler.OnChangeListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MedicineInformationPannel.this, "Notification removed successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MedicineInformationPannel.this, "Failed to removed notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
