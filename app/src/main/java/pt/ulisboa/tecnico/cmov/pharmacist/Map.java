package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.PharmacyManager;

public class Map extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gMap;
    ArrayList<Pharmacy> pharmacies;
    PharmacyManager pharmacyManager;
    private EditText editTextSearch;
    private Button buttonSearch;
    private Marker searchedLocationMarker;
    private ArrayList<Marker> pharmacyMarkers = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        pharmacyManager = new PharmacyManager(new FirebaseDBHandler());
        pharmacyManager.loadPharmacies(new PharmacyManager.OnPharmaciesLoadedListener() {
            @Override
            public void onPharmaciesLoaded(ArrayList<Pharmacy> pharmacies) {
                Log.d("Map", "Loaded pharmacies");
                Map.this.pharmacies = pharmacies;
                Log.d("Map", "Pharmacies: " + pharmacies.size() + " pharmacies loaded.");

                // Call onMapReady here because pharmacies are loaded now
                if (gMap != null) {
                    onMapReady(gMap);
                }
            }

            @Override
            public void onPharmaciesLoadFailed(Exception e) {
                Log.e("Error", "Map: Failed to load pharmacies", e);
            }
        });

        // Initialize views for search functionality
        editTextSearch = findViewById(R.id.edit_text_search);
        buttonSearch = findViewById(R.id.button_search);

        // Set click listener for search button
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = editTextSearch.getText().toString().trim();
                if (!address.isEmpty()) {
                    searchAddress(address);
                } else {
                    Toast.makeText(Map.this, "Please enter an address", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap; // Assign the map instance to the global variable

        if (pharmacies == null) {
            Log.d("Map", "Pharmacies not loaded yet.");
            return;
        }


        for (Pharmacy pharmacy : pharmacies) {
            Log.d("Map", "Adding marker for pharmacy: " + pharmacy.getName() + " at address: " + pharmacy.getAddress());
            LatLng location = geocodeAddress(pharmacy.getAddress());

            if (location != null) {
                googleMap.addMarker(new MarkerOptions().position(location).title(pharmacy.getName()));
            }
        }
        LatLng initial_location = geocodeAddress("Lisbon");
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initial_location, 12));
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

    // Add this method to your activity to move the camera to a specific location
    private void moveCameraToLocation(LatLng location) {
        if (location != null && gMap != null) {
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12));
        }
    }

    // Add this method to your activity to handle address search
    private void searchAddress(String address) {
        LatLng location = geocodeAddress(address);
        if (location != null) {
            // Move the camera to the location
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));

            if (searchedLocationMarker != null) {
                searchedLocationMarker.remove();
            }

            // Add a marker at the location
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(location)
                    .title("Searched Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

            searchedLocationMarker = gMap.addMarker(markerOptions);
        } else {
            Toast.makeText(this, "Could not find the address", Toast.LENGTH_SHORT).show();
        }
    }


    // Add this method to your activity to handle getting the current location
    private void getCurrentLocation() {
        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Get the last known location
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        moveCameraToLocation(currentLocation);
                    }
                }
            });
        }
    }

// Call getCurrentLocation() when you want to center the map on the current location

// Call searchAddress(address) when you want to center the map on a specific address

}