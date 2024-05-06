package pt.ulisboa.tecnico.cmov.pharmacist;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.PharmacyManager;

public class AddPharmacy extends AppCompatActivity {

    Button btnSave, btnCancel;

    EditText etName, etAddress;

    FloatingActionButton fabNavigate;

    ImageView ivLocation;

    ImageButton isFavorite;


    // Missing MAP

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_pharmacy);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        etName = findViewById(R.id.etName);
        etAddress = findViewById(R.id.etAddress);
        ivLocation = findViewById(R.id.ivPhoto);
        fabNavigate = findViewById(R.id.fabNavigate);
        isFavorite = findViewById(R.id.isFavorite);

        etAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Change map location
            }

            @Override
            public void afterTextChanged(Editable s) {
                // update map camera
            }
        });

        // save pharmacy
        btnSave.setOnClickListener(v -> {
            savePharmacy();
        });

        // cancel pharmacy
        btnCancel.setOnClickListener(v -> {
            finish();
        });

        // add to favorites
        isFavorite.setOnClickListener(v -> {
            // add to favorites in Users
        });

        fabNavigate.setOnClickListener(v -> {
            // navigate to pharmacy
        });
    }

    public void savePharmacy(){
        String name = etName.getText().toString();
        String address = etAddress.getText().toString();
        // Missing MAP
        Pharmacy pharmacy = new Pharmacy(name, address);

        PharmacyManager pharmacyManager = new PharmacyManager(new FirebaseDBHandler());
        pharmacyManager.addPharmacy(pharmacy, new PharmacyManager.OnPharmaciesAddListener() {
            @Override
            public void onPharmaciesAdd() {
                Toast toast = Toast.makeText(getApplicationContext(), "Pharmacy added successfully", Toast.LENGTH_SHORT);
                toast.show();
            }

            @Override
            public void onPharmaciesAddFailed(Exception e) {
                Toast toast = Toast.makeText(getApplicationContext(), "System failed to add the pharmacy ", Toast.LENGTH_SHORT);
                toast.show();
                e.printStackTrace();
            }
        });
    }
}