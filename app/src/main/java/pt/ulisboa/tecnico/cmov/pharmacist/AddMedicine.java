package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import pt.ulisboa.tecnico.cmov.pharmacist.databinding.ActivityAddMedicineBinding;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Pharmacy;

public class AddMedicine extends AppCompatActivity {

    private EditText editTextMedicineQuantity, editTextMedicinePurpose;
    private AutoCompleteTextView etMedicineNameAutocomplete;
    private FirebaseDBHandler firebaseDBHandler;
    private Pharmacy pharmacy;
    private ActivityAddMedicineBinding binding;
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    scanBarcode();
                } else {
                    Toast.makeText(this, "Permission denied to access camera", Toast.LENGTH_SHORT).show();
                }
            });
    private final ActivityResultLauncher<ScanOptions> qrCodeScannerLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() == null) {
                    Toast.makeText(this, "No barcode scanned", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("AddMedicine", "qrCodeScannerLauncher: barcde result" + result.getContents());
                    Toast.makeText(this, "Scanned barcode: " + result.getContents(), Toast.LENGTH_SHORT).show();
                    loadResults(result.getContents());
                }
            });



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);
        initBinding();
        initViews();


        firebaseDBHandler = new FirebaseDBHandler();
        etMedicineNameAutocomplete = findViewById(R.id.etMedicineNameAutocomplete);
        editTextMedicineQuantity = findViewById(R.id.editTextMedicineQuantity);
        editTextMedicinePurpose = findViewById(R.id.editTextMedicinePurpose);

        Intent intent = getIntent();
        if (intent != null) {
            pharmacy = (Pharmacy) intent.getSerializableExtra("pharmacy");
            String medicine_key = (String) intent.getSerializableExtra("medicine_key");

            if (medicine_key != null) {
                loadResults(medicine_key);
            } else {

                // or add it manually or go to create medicine activity
            }
        }


        Button buttonAddMedicine = findViewById(R.id.buttonAddMedicine);
        buttonAddMedicine.setOnClickListener(view -> addMedicine());

    }

    private void scanBarcode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan a barcode");
        options.setCameraId(0);
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(true);
        options.setOrientationLocked(true);

        qrCodeScannerLauncher.launch(options);
    }

    private void addMedicine() {
        String name = etMedicineNameAutocomplete.getText().toString();
        int quantity = Integer.parseInt(editTextMedicineQuantity.getText().toString());
        String purpose = editTextMedicinePurpose.getText().toString();

        if (name.isEmpty() || purpose.isEmpty() || quantity <= 0) {
            Toast.makeText(this, "Please fill all fields correctly.", Toast.LENGTH_SHORT).show();
            return;
        }

        Medicine newMedicine = new Medicine(name, purpose);

        // Check and add medicine to the global list if it doesn't exist
        firebaseDBHandler.addNewMedicineIfNotExists(newMedicine, new FirebaseDBHandler.OnChangeListener() {
            @Override
            public void onSuccess() {
                // Add the medicine to the pharmacy's inventory
                firebaseDBHandler.addMedicineToPharmacy(pharmacy.getName(), name, quantity, new FirebaseDBHandler.OnChangeListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(AddMedicine.this, "Medicine added successfully!", Toast.LENGTH_SHORT).show();
                        clearFields();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(AddMedicine.this, "Failed to add medicine to pharmacy: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                if (e.getMessage().equals("Medicine already exists in the list")) {
                    Toast.makeText(AddMedicine.this, "This medicine already exists in the global list.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddMedicine.this, "Failed to add new medicine: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }



    private void clearFields() {
        etMedicineNameAutocomplete.setText("");
        editTextMedicineQuantity.setText("");
        editTextMedicinePurpose.setText("");
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        Log.d("AddMedicine", "onActivityResult: scanResult: " + scanResult.getContents());
        if (scanResult != null && scanResult.getContents() != null) {
            // Handle barcode scan result

            String barcode = scanResult.getContents();
            editTextMedicineName.setText(barcode); // Set the scanned barcode in EditText

            // Launch EnterQuantity Activity with the scanned barcode data
            Intent quantityIntent = new Intent(this, EnterQuantity.class);
            quantityIntent.putExtra("BARCODE_DATA", barcode);
            enterQuantityLauncher.launch(quantityIntent);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }*/

    private void initBinding(){
        binding = ActivityAddMedicineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    private void initViews(){
        binding.buttonScanBarcode.setOnClickListener( view -> {
            checkPermissionAndShowActivity(this);
        });
    }

    private void checkPermissionAndShowActivity(Context context) {
        if (ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED ) {
            scanBarcode();
        }else if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)){
            Toast.makeText(context, "Camera permission is needed to scan barcode", Toast.LENGTH_SHORT).show();
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA);
        }
    }

    private void loadResults(String medicine_key) {
        firebaseDBHandler.getMedicineById(medicine_key, new FirebaseDBHandler.OnMedicineLoadedListener() {
            @Override
            public void onMedicineLoaded(Medicine medicine) {
                binding.etMedicineNameAutocomplete.setText(medicine.getName());
                binding.editTextMedicinePurpose.setText(medicine.getUsage());

            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AddMedicine.this, "Failed to load medicine: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
