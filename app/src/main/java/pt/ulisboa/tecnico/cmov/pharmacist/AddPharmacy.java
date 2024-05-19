package pt.ulisboa.tecnico.cmov.pharmacist;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Pharmacy;

public class AddPharmacy extends AppCompatActivity implements BottomSheetMenuFragment.OnButtonClickListener {

    private static final String TAG = "AddPharmacy";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    Button btnSave, btnCancel, btnPickLocation, btnCurrentLocation;
    private FusedLocationProviderClient fusedLocationClient;

    EditText etName, etAddress;
    ImageView ivLocation;
    FirebaseDBHandler firebaseDBHandler;
    Uri imageUri;


    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    ActivityResultLauncher<Intent> takePictureResultLauncher;
    private ActivityResultLauncher<Intent> mapPickerLauncher;


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
                Bitmap imageBitmap = (Bitmap) result.getData().getExtras().get("data");
                ivLocation.setImageBitmap(imageBitmap);
                imageUri = saveImageToGallery(imageBitmap);
            }
        });

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
        String name = etName.getText().toString();
        String address = etAddress.getText().toString();

        if (name.isEmpty() || address.isEmpty()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Please fill all the fields", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }


        firebaseDBHandler.uploadImageToStorage(imageUri, address, new FirebaseDBHandler.OnImageSavedListener() {
            @Override
            public void onImageSaved(String imageURL) {
                Log.d(TAG, "onImageSaved: Image saved successfully : " + imageURL);
            }


            @Override
            public void onFailure(Exception e) {
                Toast toast = Toast.makeText(getApplicationContext(), "System failed to upload the image", Toast.LENGTH_SHORT);
                toast.show();
                e.printStackTrace();
            }
        });


        Pharmacy pharmacy = new Pharmacy(name, address);
        firebaseDBHandler.addPharmacy(pharmacy, new FirebaseDBHandler.OnChangeListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(AddPharmacy.this, "Pharmacy added successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast toast = Toast.makeText(getApplicationContext(), "System failed to add the pharmacy ", Toast.LENGTH_SHORT);
                toast.show();
                e.printStackTrace();
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

    private Uri saveImageToGallery(Bitmap imageBitmap) {
        // Get the content resolver
        Log.d(TAG, "saveImageToGallery");
        ContentResolver resolver = getContentResolver();

        // Define the path and filename for the image
        String imageName = "image_" + System.currentTimeMillis() + ".jpg";
        Log.d(TAG, "saveImageToGallery: " + imageName);
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, imageName);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        // Insert the image into the MediaStore
        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Log.d(TAG, "saveImageToGallery: " + imageUri.toString());

        try {
            // Open an OutputStream to write the image data
            if (imageUri != null) {
                OutputStream outputStream = resolver.openOutputStream(imageUri);
                if (outputStream != null) {
                    // Compress and write the Bitmap to the OutputStream
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                    // Close the OutputStream
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "saveImageToGallery: Image saved to gallery successfully");
        return imageUri;
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
}
