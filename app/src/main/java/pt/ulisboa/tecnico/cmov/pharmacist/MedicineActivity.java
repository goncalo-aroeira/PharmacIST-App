package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SearchView;
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

import pt.ulisboa.tecnico.cmov.pharmacist.databinding.ActivityAddMedicineBinding;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Pharmacy;



public class MedicineActivity extends AppCompatActivity {
    // Domain
    ArrayList<Pharmacy> pharmacies;

    final static String CAMERA_OPTION = "Scan barcode";
    final static String ADD_MANUALLY_OPTION = "Insert manually";
    MaterialButton btnMenu;

    // UI
    SearchView searchBarValue;
    ListView lvPharmacies;
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
                    setResult(result.getContents());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medicine);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseDBHandler dbHandler = new FirebaseDBHandler();
        lvPharmacies = (ListView) findViewById(R.id.lvPharmacies);
        searchBarValue = (SearchView) findViewById(R.id.searchBarValue);
        btnMenu = (MaterialButton) findViewById(R.id.btnOpenPopupMenu);
        btnMenu.setOnClickListener(this::showMenu);


        // import list of pharmacies
        dbHandler.getAllPharmacies(new FirebaseDBHandler.OnPharmaciesLoadedListener() {
            @Override
            public void onPharmaciesLoaded(ArrayList<Pharmacy> pharmacies) {
                Log.d("Medicine Activity Page", "Pharmacies: " + pharmacies.size() + " pharmacies loaded.");
                MedicineActivity.this.pharmacies = pharmacies;
                PharmacyAdapter pharmacyAdapter = new PharmacyAdapter(MedicineActivity.this, pharmacies);
                lvPharmacies.setAdapter(pharmacyAdapter);

            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Error", "MedicineActivity: Failed to load pharmacies", e);
            }
        });

        // Search
        searchBarValue.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ArrayList<Pharmacy> filteredPharmacies = new ArrayList<>();
                for (Pharmacy pharmacy : pharmacies) {
                    for (Medicine medicine : pharmacy.getInventory().keySet()) {
                        if (medicine.getName().toLowerCase().contains(query.toLowerCase())) {
                            filteredPharmacies.add(pharmacy);
                        }
                    }
                }
                PharmacyAdapter pharmacyAdapter = new PharmacyAdapter(MedicineActivity.this, filteredPharmacies);
                lvPharmacies.setAdapter(pharmacyAdapter);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<Pharmacy> filteredPharmacies = new ArrayList<>();
                for (Pharmacy pharmacy : pharmacies) {
                    for (Medicine medicine : pharmacy.getInventory().keySet()) {
                        if (medicine.getName().toLowerCase().contains(newText.toLowerCase())) {
                            filteredPharmacies.add(pharmacy);
                        }
                    }
                }
                PharmacyAdapter pharmacyAdapter = new PharmacyAdapter(MedicineActivity.this, filteredPharmacies);
                lvPharmacies.setAdapter(pharmacyAdapter);
                return true;
            }
        });

    }


    private void showMenu(View v) {

        Context context = getApplicationContext();

        PopupMenu popup = new PopupMenu(getApplicationContext(), v);

        // Inflate the menu from xml
        popup.getMenuInflater().inflate(R.menu.menu_popup, popup.getMenu());


        MenuItem scanItem = popup.getMenu().findItem(R.id.item_scan).setVisible(true);
        scanItem.getItemId();
        MenuItem addItem = popup.getMenu().findItem(R.id.item_add).setVisible(true);
        MenuItem editItem = popup.getMenu().findItem(R.id.item_edit).setVisible(false);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Log.d("Clicked Event", "onMenuItemClick: " + menuItem.getTitle() + " clicked");
                menuItem.getItemId();
                if (menuItem.getItemId() == scanItem.getItemId()) {
                    // Start scan barcode activity
                    scanBarcode();
                } else if (menuItem.getItemId() == addItem.getItemId()) {
                    // Start add manually activity
                    Intent intent = new Intent(context, CreateMedicine.class);
                    startActivity(intent);
                    Log.d("Clicked Event", "onMenuItemClick: " + addItem.getItemId());
                } else if (menuItem.getItemId() == editItem.getItemId()) {
                    // Start add manually activity
                    Log.d("Clicked Event", "onMenuItemClick: " + editItem.getItemId());

                }
                return true; // Return true if the click event is handled.
            }
        });

        // Set the dismiss listener for the popup menu
        popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                // Respond to popup being dismissed.
            }
        });

        // Show the popup menu.
        popup.show();
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
        if (ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED ) {
            scanBarcode();
        }else if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)){
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
}