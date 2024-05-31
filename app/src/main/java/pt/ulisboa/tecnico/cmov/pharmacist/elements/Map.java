package pt.ulisboa.tecnico.cmov.pharmacist.elements;

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
import android.widget.EditText;
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
import java.util.Locale;

import pt.ulisboa.tecnico.cmov.pharmacist.PharmacyInformationPannel;
import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.UserLocalStore;

public class Map<S, B> extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gMap;
    private ArrayList<Pharmacy> pharmacies;
    private EditText editTextSearch;
    private Button buttonSearch;
    private Marker searchedLocationMarker;
    private ArrayList<Marker> pharmacyMarkers = new ArrayList<>();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private ArrayList<String> favoritePharmacies;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // return to previous activity
            Toast.makeText(this, "Location permission is required to view pharmacy information", Toast.LENGTH_SHORT).show();
            finish();
        }

        UserLocalStore userLocalStore = new UserLocalStore(this);
        String userID = userLocalStore.getLoggedInId();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        FirebaseDBHandler dbHandler = new FirebaseDBHandler();

        dbHandler.loadPharmacies(userID, new FirebaseDBHandler.OnPharmaciesLoadedListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e("Map", "Failed to load pharmacies", e);
            }

            @Override
            public void onPharmaciesLoaded(ArrayList<Pharmacy> pharmacies) {
                Log.d("Map", "Pharmacies loaded: " + pharmacies.size());
                Map.this.pharmacies = pharmacies;
                if (gMap != null) {
                    onMapReady(gMap);
                }
            }
        });

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Initialize views for search functionality
        editTextSearch = findViewById(R.id.edit_text_search);
        buttonSearch = findViewById(R.id.button_search);

        // Set click listener for search button
        buttonSearch.setOnClickListener(v -> {
            String address = editTextSearch.getText().toString().trim();
            if (!address.isEmpty()) {
                searchAddress(address);
            } else {
                Toast.makeText(Map.this, "Please enter an address", Toast.LENGTH_SHORT).show();
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

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(location)
                        .title(pharmacy.getName())
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                pharmacy.isFavorite() ? BitmapDescriptorFactory.HUE_AZURE : BitmapDescriptorFactory.HUE_RED));

                Marker marker = googleMap.addMarker(markerOptions);
                pharmacyMarkers.add(marker);
            }
        }
        LatLng initial_location = geocodeAddress("Lisbon");

        assert initial_location != null;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initial_location, 12));

        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Check if the clicked marker is a pharmacy marker
                if (pharmacyMarkers.contains(marker)) {
                    // Retrieve the corresponding Pharmacy object
                    Pharmacy clickedPharmacy = getPharmacyFromMarker(marker);

                    // Launch detail activity or fragment and pass data
                    Intent intent = new Intent(Map.this, PharmacyInformationPannel.class);
                    intent.putExtra(("pharmacy"), clickedPharmacy);

                    LatLng location = geocodeAddress(clickedPharmacy.getAddress());
                    intent.putExtra("pharmacy_location", location);
                    // Add more data as needed
                    startActivity(intent);

                    return true;
                }

                return false;
            }
        });
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);
        }
    }

    private Pharmacy getPharmacyFromMarker(Marker marker) {
        // Iterate through pharmacies and find the matching marker
        for (Pharmacy pharmacy : pharmacies) {
            if (marker.getTitle().equals(pharmacy.getName())) {
                return pharmacy;
            }
        }
        return null;
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
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        moveCameraToLocation(currentLocation);
                    } else {
                        Toast.makeText(Map.this, "Unable to determine your location", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();  // Only call getCurrentLocation if permissions are granted
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
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