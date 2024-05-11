package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
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

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.MedicineAdapter;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.UserLocalStore;

public class PharmacyInformationPannel extends AppCompatActivity {

    private static final int REQUEST_ADD_MEDICINE = 1;

    private FirebaseDBHandler dbHandler;

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

            dbHandler = new FirebaseDBHandler();

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

            ImageButton addToFavoritesButton = findViewById(R.id.imageButton_favorite);
            addToFavoritesButton.setOnClickListener(view -> {
                UserLocalStore userLocalStore = new UserLocalStore(this);
                String userEmail = userLocalStore.getLoggedInEmail();
                dbHandler.toggleFavoriteStatus(userEmail, pharmacy.getAddress(), new FirebaseDBHandler.OnChangeListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(PharmacyInformationPannel.this, "Favorite status toggled successfully", Toast.LENGTH_SHORT).show();
                        // Optionally update the button icon here based on new status
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(PharmacyInformationPannel.this, "Failed to toggle favorite status", Toast.LENGTH_SHORT).show();
                    }
                });
            });



            // Initialize the RecyclerView for displaying medicines
            RecyclerView recyclerViewMedicines = findViewById(R.id.recyclerViewMedicines);
            recyclerViewMedicines.setLayoutManager(new LinearLayoutManager(this));
            List<Medicine> medicines = new ArrayList<>(pharmacyInventory.keySet());
            MedicineAdapter adapter = new MedicineAdapter(this, medicines, pharmacyInventory);
            recyclerViewMedicines.setAdapter(adapter);

            Button addMedicineButton = findViewById(R.id.button_add_medicine);
            addMedicineButton.setOnClickListener(view -> {
                // Launch activity or dialog to capture medicine details
                Intent add_medicine_intent = new Intent(PharmacyInformationPannel.this, AddMedicine.class);
                add_medicine_intent.putExtra("pharmacy", pharmacy);
                startActivityForResult(add_medicine_intent, REQUEST_ADD_MEDICINE);
            });

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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_MEDICINE && resultCode == RESULT_OK && data != null) {
            Medicine newMedicine = data.getParcelableExtra("new_medicine");
            // Update your app's data structure and notify the RecyclerView adapter
            // Add newMedicine to your list of medicines
            // Notify RecyclerView adapter to update UI
        }
    }

}