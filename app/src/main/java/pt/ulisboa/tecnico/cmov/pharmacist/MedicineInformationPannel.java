package pt.ulisboa.tecnico.cmov.pharmacist;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Pharmacy;

public class MedicineInformationPannel extends AppCompatActivity {

    private TextView medicineNameTextView;
    private TextView medicinePurposeTextView;
    private ImageView medicineImageView;
    private ListView pharmaciesListView;
    private PharmacyAdapter pharmacyAdapter;
    private ArrayList<Pharmacy> allPharmacies;
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
        medicineNameTextView = findViewById(R.id.medicineNameTextView);
        //medicineImageView = findViewById(R.id.medicineImageView);

        // Retrieve and display medicine data
        Medicine medicine = (Medicine) getIntent().getSerializableExtra("medicine");
        if (medicine != null) {
            medicineNameTextView.setText(medicine.getName());

            // If you are using an image loader library like Glide or Picasso, load the image
            // Example with Picasso:
            // Picasso.get().load(medicine.getImageUrl()).into(medicineImageView);
        }

        pharmaciesListView = findViewById(R.id.pharmaciesListView);

        medicine = (Medicine) getIntent().getSerializableExtra("medicine");

        fetchAllPharmacies(medicine);
    }

    private void fetchAllPharmacies(Medicine medicine) {
        FirebaseDBHandler dbHandler = new FirebaseDBHandler();
        dbHandler.getAllPharmacies(new FirebaseDBHandler.OnPharmaciesLoadedListener() {
            @Override
            public void onPharmaciesLoaded(ArrayList<Pharmacy> pharmacies) {
                allPharmacies = pharmacies;

                //print all pharmacies
                for (Pharmacy pharmacy : allPharmacies) {
                    Log.d("MedicineInformationPannel", "Pharmacy: " + pharmacy.getName() + " Quantity: " + pharmacy.getInventory().get(medicine));
                }

                filterPharmaciesWithMedicine(medicine);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Error", "Failed to load pharmacies", e);
            }
        });
    }

    private void filterPharmaciesWithMedicine(Medicine medicine) {
        ArrayList<Pharmacy> filteredPharmacies = new ArrayList<>();

        for (Pharmacy pharmacy : allPharmacies) {
            if (pharmacy.getInventory().get(medicine) != null && pharmacy.getInventory().containsKey(medicine) && pharmacy.getInventory().get(medicine) > 0) {
                //print added pharmacies
                filteredPharmacies.add(pharmacy);
            }
        }
        updatePharmacyList(filteredPharmacies);
    }

    private void updatePharmacyList(ArrayList<Pharmacy> pharmacies) {
        pharmacyAdapter = new PharmacyAdapter(this, pharmacies);
        pharmaciesListView.setAdapter(pharmacyAdapter);
    }
}
