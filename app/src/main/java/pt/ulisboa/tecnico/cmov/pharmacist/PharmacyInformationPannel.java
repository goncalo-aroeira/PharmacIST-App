package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

    private ActivityResultLauncher<Intent> addMedicineLauncher;
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

        // Initialize the launcher for AddMedicine activity result
        addMedicineLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleAddMedicineResult(result.getData());
                    }
                });

        Intent intent = getIntent();
        if (intent != null) {
            dbHandler = new FirebaseDBHandler();

            Pharmacy pharmacy = (Pharmacy) intent.getSerializableExtra("pharmacy");
            LatLng pharmacyLocation = intent.getParcelableExtra("pharmacy_location");

            if (pharmacy == null || pharmacyLocation == null) {
                Toast.makeText(this, "Failed to load pharmacy information", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            populateDetailView(pharmacy);
            setupMap(pharmacyLocation, pharmacy.getName());
            setupButtons(pharmacy, pharmacyLocation);
            setupRecyclerView(pharmacy.getInventory());
        }
    }


    private void populateDetailView(Pharmacy pharmacy) {
        // Find TextViews in the layout
        TextView textViewName = findViewById(R.id.textView_pharmacy_name);
        TextView textViewAddress = findViewById(R.id.textView_pharmacy_address);

        // Set text to display in the TextViews
        textViewName.setText(pharmacy.getName());
        textViewAddress.setText(pharmacy.getAddress());

        // Convert and set the pharmacy image if available
        String imageBytes = pharmacy.getImageBytes();
        Bitmap imageBitmap = utils.convertCompressedByteArrayToBitmap(imageBytes);
        // Assuming there's an ImageView to set the bitmap
        ImageView pharmacyImageView = findViewById(R.id.ivPhoto);
        pharmacyImageView.setImageBitmap(imageBitmap);
    }

    private void setupMap(LatLng pharmacyLocation, String pharmacyName) {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(googleMap -> {
            // Move the camera to focus on the pharmacy's location
            if (pharmacyLocation != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pharmacyLocation, 15));
                // Add a marker to indicate the pharmacy's location
                googleMap.addMarker(new MarkerOptions().position(pharmacyLocation).title(pharmacyName));
            }
        });
    }

    private void setupButtons(Pharmacy pharmacy, LatLng pharmacyLocation) {
        setupNavigateButton(pharmacyLocation);
        setupAddToFavoritesButton(pharmacy);
        setupAddMedicineButton(pharmacy);
    }

    private void setupNavigateButton(LatLng location) {
        Button navigateButton = findViewById(R.id.button_navigate_to_pharmacy);
        navigateButton.setOnClickListener(view -> navigateToPharmacy(location));
    }

    private void setupAddToFavoritesButton(Pharmacy pharmacy) {
        ImageButton addToFavoritesButton = findViewById(R.id.imageButton_favorite);
        addToFavoritesButton.setOnClickListener(view -> {
            UserLocalStore userLocalStore = new UserLocalStore(this);
            String userEmail = userLocalStore.getLoggedInEmail();
            dbHandler.toggleFavoriteStatus(userEmail, pharmacy.getAddress(), new FirebaseDBHandler.OnFavoriteToggleListener() {
                @Override
                public void onAddedToFavorite() {
                    Toast.makeText(PharmacyInformationPannel.this, "Pharmacy added to favorites", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRemovedFromFavorite() {
                    Toast.makeText(PharmacyInformationPannel.this, "Pharmacy removed from favorites", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(PharmacyInformationPannel.this, "Failed to update favorites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void setupAddMedicineButton(Pharmacy pharmacy) {
        Button addMedicineButton = findViewById(R.id.button_add_medicine);
        addMedicineButton.setOnClickListener(view -> {
            // Launch activity to capture medicine details
            Intent addMedicineIntent = new Intent(PharmacyInformationPannel.this, AddMedicine.class);
            addMedicineIntent.putExtra("pharmacy", pharmacy);
            addMedicineLauncher.launch(addMedicineIntent);
        });
    }

    private void handleAddMedicineResult(Intent data) {
        Medicine newMedicine = data.getParcelableExtra("new_medicine");
        // Update your app's data structure and notify the RecyclerView adapter
        // Add newMedicine to your list of medicines
        // Notify RecyclerView adapter to update UI
    }

    private void setupRecyclerView(HashMap<Medicine, Integer> pharmacyInventory) {
        RecyclerView recyclerViewMedicines = findViewById(R.id.recyclerViewMedicines);
        recyclerViewMedicines.setLayoutManager(new LinearLayoutManager(this));
        List<Medicine> medicines = new ArrayList<>(pharmacyInventory.keySet());
        MedicineAdapter adapter = new MedicineAdapter(this, medicines, pharmacyInventory);
        recyclerViewMedicines.setAdapter(adapter);
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