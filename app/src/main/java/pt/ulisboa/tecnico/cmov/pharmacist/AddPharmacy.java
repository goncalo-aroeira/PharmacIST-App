package pt.ulisboa.tecnico.cmov.pharmacist;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Pharmacy;

public class AddPharmacy extends AppCompatActivity implements BottomSheetMenuFragment.OnButtonClickListener {

    private static final String TAG = "AddPharmacy";
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    Button btnSave, btnCancel;

    EditText etName, etAddress;
    ImageView ivLocation;
    Button btnSave, btnCancel, btnPickLocation, btnCurrentLocation;
    FirebaseDBHandler firebaseDBHandler;
    Uri imageUri;


    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    ActivityResultLauncher<Intent> takePictureResultLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pharmacy);

        firebaseDBHandler = new FirebaseDBHandler();

        etName = findViewById(R.id.etName);
        etAddress = findViewById(R.id.etAddress);
        ivLocation = findViewById(R.id.ivPhoto);



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
    }


    public void savePharmacy() {
        String name = etName.getText().toString();
        String address = etAddress.getText().toString();

        /*if (name.isEmpty() || address.isEmpty()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Please fill all the fields", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }*/


        Log.d(TAG, "savePharmacy: imageUri : " + imageUri);
        firebaseDBHandler.uploadImageToStorage(imageUri, address, new FirebaseDBHandler.OnImageSavedListener() {
            @Override
            public void onImageSaved(String imageName) {
                Log.d(TAG, "onImageSaved: Image saved successfully : " + imageName);
                Toast toast = Toast.makeText(getApplicationContext(), "Image saved successfully : " + imageName, Toast.LENGTH_SHORT);
                toast.show();
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

}
