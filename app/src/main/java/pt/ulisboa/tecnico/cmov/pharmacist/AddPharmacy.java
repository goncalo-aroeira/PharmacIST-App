package pt.ulisboa.tecnico.cmov.pharmacist;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.elements.BottomSheetMenuFragment;
import pt.ulisboa.tecnico.cmov.pharmacist.elements.MapPicker;
import pt.ulisboa.tecnico.cmov.pharmacist.elements.utils;

public class AddPharmacy extends AppCompatActivity implements BottomSheetMenuFragment.OnButtonClickListener {

    private static final String TAG = "AddPharmacy";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    Button btnSave, btnCancel, btnPickLocation, btnCurrentLocation;
    private FusedLocationProviderClient fusedLocationClient;

    EditText etName, etAddress;
    ImageView ivLocation;
    FirebaseDBHandler firebaseDBHandler;
    Bitmap imageBitmap;

    private ActivityResultLauncher<Intent> cameraLauncher, galleryLauncher;

    private ActivityResultLauncher<Intent> mapPickerLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_add_pharmacy);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseDBHandler = new FirebaseDBHandler();

        etName = findViewById(R.id.etName);
        etAddress = findViewById(R.id.etAddress);
        ivLocation = findViewById(R.id.ivPhoto);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnPickLocation = findViewById(R.id.btnPickLocation);
        btnCurrentLocation = findViewById(R.id.btnCurrentLocation);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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


        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                imageBitmap = (Bitmap) result.getData().getExtras().get("data");
                ivLocation.setImageBitmap(imageBitmap);
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        Uri imageUri = o.getData().getData();
                        ivLocation.setImageURI(imageUri);
                    }
                });

        etAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // update map location
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


        ivLocation.setOnClickListener(v -> {
            BottomSheetMenuFragment bottomSheetMenuFragment = BottomSheetMenuFragment.newInstance();
            bottomSheetMenuFragment.setOnButtonClickListener(this);
            bottomSheetMenuFragment.show(getSupportFragmentManager(), "bottomSheetMenuFragment");
        });

        mapPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                double latitude = result.getData().getDoubleExtra("latitude", 0);
                double longitude = result.getData().getDoubleExtra("longitude", 0);
                updateLocationAddress(latitude, longitude);
            }
        });

        btnPickLocation.setOnClickListener(v -> {
            Intent intent = new Intent(AddPharmacy.this, MapPicker.class);
            mapPickerLauncher.launch(intent);
        });
    }


    public void savePharmacy() {
        String name = etName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Check if name or address fields are empty
        if (name.isEmpty() || address.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please fill all fields before saving.", Toast.LENGTH_SHORT).show();
            return; // Return early if any field is empty
        }

        // If the image has not been set, you might also want to check that
        if (imageBitmap == null) {
            Toast.makeText(getApplicationContext(), "Please add an image for the pharmacy.", Toast.LENGTH_SHORT).show();
            return; // Return early if the image is not set
        }

        // Create a new Pharmacy object
        Pharmacy pharmacy = new Pharmacy(name, address);
        String imageByte = utils.bitmapToByteArray(imageBitmap);
        pharmacy.setImageBytes(imageByte);
        pharmacy.generateId(); // Generating an ID for the pharmacy

        // Save the pharmacy to the database
        firebaseDBHandler.addPharmacy(pharmacy, new FirebaseDBHandler.OnChangeListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(AddPharmacy.this, "Pharmacy added successfully", Toast.LENGTH_SHORT).show();
                finish(); // Finish this activity and return to the previous screen
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to add the pharmacy: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onCameraButtonClick() {
        Log.d(TAG, "onCameraButtonClick");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            startCamera();
        }
    }

    @Override
    public void onGalleryButtonClick() {
        openGallery();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    private void startCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(takePictureIntent);
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    updateLocationAddress(latitude, longitude);
                } else {
                    Toast.makeText(AddPharmacy.this, "Location not detected", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateLocationAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                etAddress.setText(address.getAddressLine(0)); // Update the EditText with the first address line.
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to get street address", Toast.LENGTH_SHORT).show();
        }
    }


    // onRequestPermissionsResult to handle the case where the user grants the permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
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
