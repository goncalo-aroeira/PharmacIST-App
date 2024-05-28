package pt.ulisboa.tecnico.cmov.pharmacist;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.elements.BottomSheetMenuFragment;
import pt.ulisboa.tecnico.cmov.pharmacist.elements.utils;

public class CreateMedicine extends AppCompatActivity {
    private static final String TAG = "CreateMedicine";

    private EditText medicineName, usage;
    private ImageView boxPhoto;
    private Button btnSave, btnCancel;

    private Bitmap imageBitmap;

    private FirebaseDBHandler firebaseDBHandler;

    private ActivityResultLauncher<Intent> cameraLauncher, openGalleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_medicine);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupView();
    }

    private void setupView() {
        // Initialize UI elements
        firebaseDBHandler = new FirebaseDBHandler();

        medicineName = findViewById(R.id.medicineName);
        usage = findViewById(R.id.medicineDescription);
        boxPhoto = findViewById(R.id.ivBoxPhoto);
        btnSave = findViewById(R.id.btnCreateMedicine);
        btnCancel = findViewById(R.id.btnCancelMedicine);

        btnSave.setOnClickListener(v -> saveMedicine());

        btnCancel.setOnClickListener(v -> navigateToMedicineActivity());

        boxPhoto.setOnClickListener(v -> {
            BottomSheetMenuFragment bottomSheetMenuFragment = BottomSheetMenuFragment.newInstance();
            bottomSheetMenuFragment.setOnButtonClickListener(
                    new BottomSheetMenuFragment.OnButtonClickListener() {
                        @Override
                        public void onCameraButtonClick() {
                            Log.d(TAG, "onCameraButtonClick");
                            if (ContextCompat.checkSelfPermission(CreateMedicine.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(CreateMedicine.this, new String[]{Manifest.permission.CAMERA}, 100);
                            } else {
                                startCamera();
                            }
                        }

                        @Override
                        public void onGalleryButtonClick() {
                            openGallery();
                        }
                    });
            bottomSheetMenuFragment.show(getSupportFragmentManager(), "bottomSheetMenuFragment");
        });


        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                imageBitmap = (Bitmap) result.getData().getExtras().get("data");
                boxPhoto.setImageBitmap(imageBitmap);
            }
        });

        openGalleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        Uri imageUri = o.getData().getData();
                        boxPhoto.setImageURI(imageUri);
                    }
                });


    }

    private void saveMedicine() {
        // Save medicine in the database and return to the previous activity
        String name = medicineName.getText().toString();
        String description = usage.getText().toString();

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(CreateMedicine.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Medicine medicine = new Medicine(name, description);
        medicine.generateId();

        String imageByte = utils.bitmapToByteArray(imageBitmap);
        medicine.setImageBytes(imageByte);

        firebaseDBHandler.addMedicine(medicine, new FirebaseDBHandler.OnChangeListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(CreateMedicine.this, medicine.getName() + " added successfully", Toast.LENGTH_SHORT).show();
                navigateToMedicineActivity();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(CreateMedicine.this, "System failed to add the medicine", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }

    private void navigateToMedicineActivity() {
        // Navigate to MedicineActivity
        Intent intent = new Intent(CreateMedicine.this, MedicineActivity.class);
        startActivity(intent);
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

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        openGalleryLauncher.launch(intent);
    }

}
