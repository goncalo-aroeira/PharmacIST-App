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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.PharmacyManager;

public class AddPharmacy extends AppCompatActivity {

    Button btnSave, btnCancel;

    EditText etName, etAddress;

    FloatingActionButton fabNavigate;

    ImageView ivLocation;

    ImageButton isFavorite;

    private static final String PHARMACIES_NODE = "pharmacy";
    ActivityResultLauncher<Intent> resultLauncher;



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

        // Take photo or select from gallery
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 100);

        }

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

        ivLocation.setOnClickListener(v -> {
            // wait for users photo to be received
            BottomSheetMenuFragment bottomSheetMenuFragment = BottomSheetMenuFragment.newInstance();
            bottomSheetMenuFragment.setOnPhotoSelectedListener(photoUri -> {
                String base64Image = convertImageToBase64(photoUri);
                // save image in Firebase
                FirebaseDBHandler firebaseDBHandler = new FirebaseDBHandler();
                firebaseDBHandler.uploadImage(base64Image, PHARMACIES_NODE, new FirebaseDBHandler.OnImageSavedListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onImageSaved() {
                        ivLocation.setImageURI(photoUri);
                        Toast.makeText(getApplicationContext(), "Image saved successfully", Toast.LENGTH_SHORT).show();

                    }
                });
            });
            bottomSheetMenuFragment.show(getSupportFragmentManager(), BottomSheetMenuFragment.TAG);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Bitmap bitmap = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
            ivLocation.setImageBitmap(bitmap);
        }
    }

    private String convertImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            byte[] bytes = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}