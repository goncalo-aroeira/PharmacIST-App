package pt.ulisboa.tecnico.cmov.pharmacist;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.elements.PharmacyStockAdapter;


public class MedicineActivity extends AppCompatActivity {

    // UI Components
    private MaterialButton btnMenu;

    // Domain
    private HashMap<Pharmacy, Integer> pharmacies = new HashMap<>();
    private AutoCompleteTextView searchInput;
    private PharmacyStockAdapter pharmacyAdapter;
    private ListView pharmacyListView;
    private FirebaseDBHandler firebaseDBHandler = new FirebaseDBHandler();
    List<Map.Entry<Pharmacy, Integer>> pharmacyList = new ArrayList<>();
    HashMap<String, String> medicineNamesAndIdsclass = new HashMap<>();

    // Activity Result Launchers
    private final ActivityResultLauncher<String> requestPermissionLauncher =
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
                    Log.d("AddMedicine", "qrCodeScannerLauncher: barcode result" + result.getContents());
                    Toast.makeText(this, "Scanned barcode: " + result.getContents(), Toast.LENGTH_SHORT).show();
                    setResult(result.getContents());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.d(TAG, "onCreate: Loading Medicine Activity...");
        btnMenu = findViewById(R.id.btnOpenPopupMenu);
        btnMenu.setOnClickListener(this::showMenu);

        searchInput = findViewById(R.id.searchInput);
        pharmacyListView = findViewById(R.id.pharmacyListView);
        pharmacyAdapter = new PharmacyStockAdapter(this, pharmacyList);
        pharmacyListView.setAdapter(pharmacyAdapter);

        setupSearchBar();

        // Set item click listener for the ListView
        pharmacyListView.setOnItemClickListener((parent, view, position, id) -> {
            Map.Entry<Pharmacy, Integer> entry = pharmacyList.get(position);
            Pharmacy selectedPharmacy = entry.getKey();
            navigateToPharmacyInformationPanel(selectedPharmacy.getId());
        });

        loadLocale();
    }

    private void showMenu(View v) {
        PopupMenu popup = new PopupMenu(getApplicationContext(), v);
        popup.getMenuInflater().inflate(R.menu.menu_popup, popup.getMenu());
        loadLocale();

        MenuItem scanItem = popup.getMenu().findItem(R.id.item_scan).setVisible(true);
        MenuItem addItem = popup.getMenu().findItem(R.id.item_add).setVisible(true);

        popup.setOnMenuItemClickListener(menuItem -> {
            Log.d("Clicked Event", "onMenuItemClick: " + menuItem.getTitle() + " clicked");
            if (menuItem.getItemId() == scanItem.getItemId()) {
                checkPermissionAndShowActivity(this);
            } else if (menuItem.getItemId() == addItem.getItemId()) {
                startActivity(new Intent(getApplicationContext(), CreateMedicine.class));
            }
            return true;
        });

        popup.show();
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

    private void checkPermissionAndShowActivity(Context context) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            scanBarcode();
        } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
            Toast.makeText(context, "Camera permission is needed to scan barcode", Toast.LENGTH_SHORT).show();
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA);
        }
    }

    private void setResult(String contents) {
        Intent intent = new Intent(this, CreateMedicine.class);
        intent.putExtra("medicine_key", contents);
        Log.d("MedicineActivity", "setResult: contents: " + contents);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            String barcode = result.getContents();
            Log.d("Scan Barcode", "onActivityResult: " + barcode);
            // Start activity to show medicine details
        } else {
            super.onActivityResult(requestCode, resultCode, data);
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

    private void setupSearchBar() {
        firebaseDBHandler.getMedicineNames(new FirebaseDBHandler.OnMedicineNamesAndIdsLoaded() {
            @Override
            public void onLoaded(HashMap<String, String> medicineNamesAndIds) {
                medicineNamesAndIdsclass.putAll(medicineNamesAndIds);
                ArrayList<String> medicineNames = new ArrayList<>(medicineNamesAndIds.keySet());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MedicineActivity.this,
                        android.R.layout.simple_dropdown_item_1line, medicineNames);
                searchInput.setAdapter(adapter);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MedicineActivity.this, "Error loading medicine names: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        searchInput.setOnItemClickListener((parent, view, position, id) -> {
            String selectedMedicine = (String) parent.getItemAtPosition(position);
            medicineNamesAndIdsclass.get(selectedMedicine);
            fetchPharmaciesThatStock(medicineNamesAndIdsclass.get(selectedMedicine));
        });
    }

    private void fetchPharmaciesThatStock(String medicine) {
        firebaseDBHandler.getPharmaciesWithMedicine(medicine, new FirebaseDBHandler.OnPharmaciesWithMedicineLoaded() {
            @Override
            public void onLoaded(HashMap<Pharmacy, Integer> loadedPharmacies) {

                runOnUiThread(() -> {
                    pharmacies.clear();
                    pharmacies.putAll(loadedPharmacies);
                    pharmacyList.clear();
                    pharmacyList.addAll(pharmacies.entrySet());
                    pharmacyAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Adapter notified of data set changed.");
                });
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MedicineActivity.this, "Error loading pharmacies: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToPharmacyInformationPanel(String pharmacyId) {
        Intent intent = new Intent(MedicineActivity.this, PharmacyInformationPannel.class);
        intent.putExtra("pharmacy_id", pharmacyId);
        startActivity(intent);
    }
}
