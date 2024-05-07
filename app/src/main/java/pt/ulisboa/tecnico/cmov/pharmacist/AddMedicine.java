package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Pharmacy;

public class AddMedicine extends AppCompatActivity {

    private EditText editTextMedicineName;
    private EditText editTextMedicineQuantity;
    private EditText editTextMedicinePurpose;
    private ActivityResultLauncher<Intent> scanBarcodeLauncher;
    private ActivityResultLauncher<Intent> enterQuantityLauncher;
    private FirebaseDBHandler firebaseDBHandler;
    private Pharmacy pharmacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        firebaseDBHandler = new FirebaseDBHandler();

        editTextMedicineName = findViewById(R.id.editTextMedicineName);
        editTextMedicineQuantity = findViewById(R.id.editTextMedicineQuantity);
        editTextMedicinePurpose = findViewById(R.id.editTextMedicinePurpose);

        Button buttonAddMedicine = findViewById(R.id.buttonAddMedicine);
        buttonAddMedicine.setOnClickListener(view -> addMedicine());

        Button buttonScanBarcode = findViewById(R.id.buttonScanBarcode);
        buttonScanBarcode.setOnClickListener(view -> scanBarcode());

        Intent intent = getIntent();
        if (intent != null) {
            pharmacy = (Pharmacy) intent.getSerializableExtra("pharmacy");
        }

        // Initialize ActivityResultLauncher for scanning barcode
        scanBarcodeLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        IntentResult scanResult = IntentIntegrator.parseActivityResult(result.getResultCode(), result.getData());
                        if (scanResult != null && scanResult.getContents() != null) {
                            // Handle barcode scan result
                            String barcode = scanResult.getContents();
                            editTextMedicineName.setText(barcode); // Set the scanned barcode in EditText

                            // Launch EnterQuantity Activity with the scanned barcode data
                            Intent quantityIntent = new Intent(this, EnterQuantity.class);
                            quantityIntent.putExtra("BARCODE_DATA", barcode);
                            enterQuantityLauncher.launch(quantityIntent);
                        }
                    }
                });

        // Initialize ActivityResultLauncher for entering quantity
        enterQuantityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        int quantity = result.getData().getIntExtra("QUANTITY", 0);
                        editTextMedicineQuantity.setText(String.valueOf(quantity));
                        // Add the logic to update stock quantity with the scanned barcode and quantity
                    }
                });
    }

    private void scanBarcode() {
        IntentIntegrator integrator = new IntentIntegrator(AddMedicine.this);
        integrator.setPrompt("Scan a barcode");
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
        Toast.makeText(this, "Failed to scan barcode", Toast.LENGTH_SHORT).show();
    }

    private void addMedicine() {
        String name = editTextMedicineName.getText().toString();
        String quantity = editTextMedicineQuantity.getText().toString();
        String purpose = editTextMedicinePurpose.getText().toString();
        
    }
}
