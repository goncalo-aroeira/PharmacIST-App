package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Pharmacy;

public class PharmacyInformationPannel extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pharmacy_information_pannel);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Retrieve data passed from the map activity
        Intent intent = getIntent();
        if (intent != null) {

            Pharmacy pharmacy = (Pharmacy) intent.getSerializableExtra("pharmacy");

            String pharmacyName = pharmacy.getName();
            String pharmacyAddress = pharmacy.getAddress();
            HashMap<Medicine, Integer> pharmacyInventory = pharmacy.getInventory();

            LatLng pharmacyLocation = intent.getParcelableExtra("pharmacy_location");
            //HashMap<String, Integer> pharmacyInventory = (HashMap<String, Integer>) intent.getSerializableExtra("pharmacy_inventory");

            populateDetailView(pharmacyName, pharmacyAddress);
            // Initialize the map fragment
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(googleMap -> {
                // Move the camera to focus on the pharmacy's location
                if (pharmacyLocation != null) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pharmacyLocation, 15));
                    // Add a marker to indicate the pharmacy's location
                    googleMap.addMarker(new MarkerOptions().position(pharmacyLocation).title(pharmacyName));
                }
            });

            Button navigateButton = findViewById(R.id.button_navigate_to_pharmacy);
            navigateButton.setOnClickListener(view -> navigateToPharmacy(pharmacyLocation));
        }
    }

    private void populateDetailView(String name, String address) {
        // Find TextViews in the layout
        TextView textViewName = findViewById(R.id.textView_pharmacy_name);
        TextView textViewAddress = findViewById(R.id.textView_pharmacy_address);
        // Find more TextViews as needed

        // Set text to display in the TextViews
        textViewName.setText(name);
        textViewAddress.setText(address);
        // Set more data to other TextViews as needed
    }

    private void navigateToPharmacy(LatLng location) {
        // Create an intent to launch Google Maps with the pharmacy's location
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + location.latitude + "," + location.longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        // Verify that Google Maps is installed and launch the intent
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }
}