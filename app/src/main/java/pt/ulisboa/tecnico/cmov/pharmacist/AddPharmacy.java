package pt.ulisboa.tecnico.cmov.pharmacist;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Pharmacy;

public class AddPharmacy extends AppCompatActivity {

    EditText etName, etAddress;
    ImageView ivLocation;
    Button btnSave, btnCancel, btnPickLocation, btnCurrentLocation;
    FirebaseDBHandler firebaseDBHandler;

    ActivityResultLauncher<Intent> takePictureResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pharmacy);

        firebaseDBHandler = new FirebaseDBHandler();

        etName = findViewById(R.id.etName);
        etAddress = findViewById(R.id.etAddress);
        ivLocation = findViewById(R.id.ivPhoto);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnPickLocation = findViewById(R.id.btnPickLocation);
        btnCurrentLocation = findViewById(R.id.btnCurrentLocation);

        takePictureResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap imageBitmap = (Bitmap) result.getData().getExtras().get("data");
                        ivLocation.setImageBitmap(imageBitmap);
                    }
                }
        );

        ivLocation.setOnClickListener(v -> takePicture());

        btnPickLocation.setOnClickListener(v -> {
            // Implement logic to pick location on the map
        });

        btnCurrentLocation.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                getCurrentLocation();
            }
        });

        btnSave.setOnClickListener(v -> savePharmacy());

        btnCancel.setOnClickListener(v -> finish());
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            takePictureResultLauncher.launch(takePictureIntent);
        }
    }

    private void getCurrentLocation() {
        // Use Location Services to get the current location
    }

    public void savePharmacy() {
        String name = etName.getText().toString();
        String address = etAddress.getText().toString();
        Pharmacy pharmacy = new Pharmacy(name, address);
        firebaseDBHandler.addPharmacy(pharmacy, new FirebaseDBHandler.OnChangeListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(AddPharmacy.this, "Pharmacy added successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AddPharmacy.this, "Failed to add pharmacy: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
