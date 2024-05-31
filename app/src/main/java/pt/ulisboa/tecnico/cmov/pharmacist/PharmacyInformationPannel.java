package pt.ulisboa.tecnico.cmov.pharmacist;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.UserLocalStore;
import pt.ulisboa.tecnico.cmov.pharmacist.elements.MedicineAdapter;
import pt.ulisboa.tecnico.cmov.pharmacist.elements.utils;

public class PharmacyInformationPannel extends AppCompatActivity{

    private FirebaseDBHandler dbHandler;

    private String pharmacyId;
    private String pharmacyName;

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


    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    scanBarcode();
                } else {
                    Toast.makeText(this, "Permission denied to access camera", Toast.LENGTH_SHORT).show();
                }
            });



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pharmacy_information_pannel);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        pharmacyId = (String) intent.getStringExtra("pharmacy_id");
        Log.d("PharmacyInformationPannel", "Pharmacy ID: " + pharmacyId);

        if (intent != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // return to previous activity
                Toast.makeText(this, "Location permission is required to view pharmacy information", Toast.LENGTH_SHORT).show();
                finish();
            }

            dbHandler = new FirebaseDBHandler();

            dbHandler.getPharmacyById(pharmacyId, new FirebaseDBHandler.OnPharmacyLoadedListener() {
                @Override
                public void onPharmacyLoaded(Pharmacy pharmacy) {
                    if (pharmacy == null) {
                        Toast.makeText(PharmacyInformationPannel.this, "Failed to load pharmacy information", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    pharmacyName = pharmacy.getName();
                    LatLng pharmacyLocation = geocodeAddress(pharmacy.getAddress());
                    populateDetailView(pharmacy);
                    setupMap(pharmacyLocation, pharmacy.getName());
                    setupButtons(pharmacy, pharmacyLocation);
                    setupRecyclerView();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(PharmacyInformationPannel.this, "Failed to load pharmacy information", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    }


    private void populateDetailView(Pharmacy pharmacy) {
        // Find TextViews in the layout
        TextView textViewName = findViewById(R.id.textView_pharmacy_name);
        TextView textViewAddress = findViewById(R.id.textView_pharmacy_address);

        // Set text to display in the TextViews
        textViewName.setText(pharmacy.getName());
        textViewAddress.setText(pharmacy.getAddress());

        // Convert and set the pharmacy image if available
        String imageBytes = pharmacy.getImageBytes();
        Bitmap imageBitmap = utils.convertCompressedByteArrayToBitmap(imageBytes);
        // Assuming there's an ImageView to set the bitmap
        ImageView pharmacyImageView = findViewById(R.id.ivPhoto);
        pharmacyImageView.setImageBitmap(imageBitmap);

    }


    private void setupMap(LatLng pharmacyLocation, String pharmacyName) {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(googleMap -> {
            // Move the camera to focus on the pharmacy's location
            if (pharmacyLocation != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pharmacyLocation, 15));
                // Add a marker to indicate the pharmacy's location
                googleMap.addMarker(new MarkerOptions().position(pharmacyLocation).title(pharmacyName));
            }
        });
    }

    private void setupButtons(Pharmacy pharmacy, LatLng pharmacyLocation) {
        setupNavigateButton(pharmacyLocation);
        setupAddToFavoritesButton(pharmacy);
        ImageButton flagButton = findViewById(R.id.imageButton_flag);
        flagButton.setOnClickListener(v -> toggleFlag(pharmacy, flagButton));
        setupAddMedicineButton(pharmacy);
    }

    private void setupNavigateButton(LatLng location) {
        Button navigateButton = findViewById(R.id.button_navigate_to_pharmacy);
        navigateButton.setOnClickListener(view -> navigateToPharmacy(location));
    }

    private void setupAddToFavoritesButton(Pharmacy pharmacy) {
        ImageButton addToFavoritesButton;
        addToFavoritesButton = findViewById(R.id.imageButton_favorite);
        if (pharmacy.isFavorite()) {
            addToFavoritesButton.setImageResource(R.drawable.ic_favorite_full);
        } else {
            addToFavoritesButton.setImageResource(R.drawable.ic_favorite_outline);
        }
        addToFavoritesButton.setOnClickListener(view -> {
            Log.d("PharmacyInformationPannel", "Add to favorites button clicked");
            UserLocalStore userLocalStore = new UserLocalStore(this);
            String userId = userLocalStore.getLoggedInId();
            dbHandler.toggleFavoriteStatus(userId, pharmacy.getId(), new FirebaseDBHandler.OnFavoriteToggleListener() {
                @Override
                public void onAddedToFavorite() {
                    Toast.makeText(PharmacyInformationPannel.this, "Pharmacy added to favorites", Toast.LENGTH_SHORT).show();
                    addToFavoritesButton.setImageResource(R.drawable.ic_favorite_full);
                    pharmacy.setFavorite(true);
                }

                @Override
                public void onRemovedFromFavorite() {
                    Toast.makeText(PharmacyInformationPannel.this, "Pharmacy removed from favorites", Toast.LENGTH_SHORT).show();
                    addToFavoritesButton.setImageResource(R.drawable.ic_favorite_outline);
                    pharmacy.setFavorite(false);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(PharmacyInformationPannel.this, "Failed to update favorites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void toggleFlag(Pharmacy pharmacy, ImageButton button) {
        UserLocalStore userLocalStore = new UserLocalStore(this);
        String userId = userLocalStore.getLoggedInId();
        dbHandler.flagPharmacy(userId, pharmacy.getId(), new FirebaseDBHandler.OnFlaggedPharmacy() {
            @Override
            public void onSuccess() {
                Toast.makeText(PharmacyInformationPannel.this, "Pharmacy flagged successfully", Toast.LENGTH_SHORT).show();
                pharmacy.setFlagged(true);
                button.setImageResource(R.drawable.ic_favorite_full);
                Intent intent = new Intent(PharmacyInformationPannel.this, MainMenu.class);
                startActivity(intent);
            }

            @Override
            public void onPharmacySuspended() {
                Toast.makeText(PharmacyInformationPannel.this, "Pharmacy suspended due to flags", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAccountSuspended() {
                Toast.makeText(PharmacyInformationPannel.this, "Your account is suspended due to excessive flagging", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(PharmacyInformationPannel.this, Login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(PharmacyInformationPannel.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void setupAddMedicineButton(Pharmacy pharmacy) {
        Button addMedicineButton = findViewById(R.id.button_add_medicine);
        addMedicineButton.setOnClickListener(this::showMenu);

    }private void showMenu(View v) {
        PopupMenu popup = new PopupMenu(getApplicationContext(), v);
        popup.getMenuInflater().inflate(R.menu.menu_popup, popup.getMenu());

        MenuItem scanItem = popup.getMenu().findItem(R.id.item_scan).setVisible(true);
        MenuItem addItem = popup.getMenu().findItem(R.id.item_add).setVisible(true);

        popup.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == scanItem.getItemId()) {
                checkPermissionAndShowActivity(this);
            } else if (menuItem.getItemId() == addItem.getItemId()) {
                Intent intent = new Intent(getApplicationContext(), AddMedicine.class);
                intent.putExtra("pharmacy_id", pharmacyId);
                startActivity(intent);
            }
            return true;
        });

        popup.show();
    }

    private void setupRecyclerView() {

        RecyclerView recyclerViewMedicines = findViewById(R.id.recyclerViewMedicines);
        recyclerViewMedicines.setLayoutManager(new LinearLayoutManager(PharmacyInformationPannel.this));
        List<Medicine> medicines = new ArrayList<>();
        dbHandler.getInventoryForPharmacy(pharmacyId, new FirebaseDBHandler.OnGetInventory() {
            @Override
            public void onInventoryLoaded(HashMap<String, Integer> inventory) {
                List<String> medicines_id = new ArrayList<>(inventory.keySet());
                Log.d(TAG, "onInventoryLoaded: medicines_id: " + medicines_id);
                List<Task<Void>> tasks = new ArrayList<>();
                for (String id : medicines_id) {
                    TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
                    tasks.add(taskCompletionSource.getTask());

                    dbHandler.getMedicineById(id, new FirebaseDBHandler.OnMedicineLoadedListener() {
                        @Override
                        public void onMedicineLoaded(Medicine medicine) {
                            Log.d(TAG, "onMedicineLoaded: medicine: " + medicine);
                            medicines.add(medicine);
                            taskCompletionSource.setResult(null);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            taskCompletionSource.setException(e);
                            Log.e("PharmacyInformationPannel", "Failed to load medicine", e);
                        }
                    });

                }

                Tasks.whenAll(tasks).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        MedicineAdapter adapter = new MedicineAdapter(PharmacyInformationPannel.this, medicines, inventory, new MedicineAdapter.OnMedicineItemClickListener() {
                            @Override
                            public void onPurchaseClick(Medicine medicine) {
                                Log.d(TAG, "onPurchaseClick: clicked on purchase button for medicine: " + medicine);
                                showSellDialog(medicine);
                            }

                            @Override
                            public void onMedicineClick(Medicine medicine) {
                                Log.d(TAG, "onMedicineClick: clicked on medicine: " + medicine);
                                Intent intent = new Intent(PharmacyInformationPannel.this, MedicineInformationPannel.class);
                                intent.putExtra("medicine_id", medicine.getId());
                                startActivity(intent);
                            }

                        });
                        recyclerViewMedicines.setAdapter(adapter);
                    } else {
                        Log.e("PharmacyInformationPannel", "Failed to load all medicines");
                    }
                });

            }


            @Override
            public void onFailure(Exception e) {
                Log.e("PharmacyInformationPannel", "Failed to load pharmacy inventory", e);
            }
        });

    }

    private void navigateToPharmacy(LatLng location) {
        // Create an intent to launch Google Maps with the pharmacy's location
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + location.latitude + "," + location.longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        // Verify that Google Maps is installed and launch the intent
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
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

    private void setResult(String contents) {
        if (contents != null) {
            Intent intent = new Intent(this, AddMedicine.class);
            intent.putExtra("medicine_key", contents);
            intent.putExtra("pharmacy_id", pharmacyId);
            startActivity(intent);
        }
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



    private void purchaseMedicine(String medicine_id, int quantity) {
        dbHandler.purchaseMedicineFromPharmacy(pharmacyId, medicine_id, quantity,  new FirebaseDBHandler.OnPurchaseMedicineListener() {
            @Override
            public void onNotEnoughStock() {
                Toast.makeText(PharmacyInformationPannel.this, "Not enough stock to purchase medicine", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                Toast.makeText(PharmacyInformationPannel.this, "Medicine purchased successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(PharmacyInformationPannel.this, "Failed to purchase medicine: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void showSellDialog(Medicine medicine){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_sell_quantity, null);
        builder.setView(dialogView);

        TextView medicineName = dialogView.findViewById(R.id.medicineName);
        TextView pharmacyNameTv = dialogView.findViewById(R.id.pharmacyName);

        Button decreaseButton = dialogView.findViewById(R.id.decreaseButton);
        Button increaseButton = dialogView.findViewById(R.id.increaseButton);
        EditText quantityInput = dialogView.findViewById(R.id.quantityInput);
        Button confirmButton = dialogView.findViewById(R.id.confirmButton);

        medicineName.setText(medicine.getName());
        pharmacyNameTv.setText(pharmacyName);
        quantityInput.setText("0");

        // SALE
        decreaseButton.setOnClickListener(v -> {
            int currentQuantity = Integer.parseInt(quantityInput.getText().toString());
            if (currentQuantity > 0) {
                quantityInput.setText(String.valueOf(currentQuantity - 1));
            } else {
                decreaseButton.setEnabled(false);
            }
        });

        increaseButton.setOnClickListener(v -> {
            int currentQuantity = Integer.parseInt(quantityInput.getText().toString());
            if (currentQuantity > 0) {
                decreaseButton.setEnabled(true);
            }
            quantityInput.setText(String.valueOf(currentQuantity + 1));
        });

        AlertDialog dialog = builder.create();

        confirmButton.setOnClickListener(v -> {
            int quantity = Integer.parseInt(quantityInput.getText().toString());
            purchaseMedicine(medicine.getId(), quantity);
            dialog.dismiss();
        });

        dialog.show();
    }

    private LatLng geocodeAddress(String address) {
        Log.d("PharmacyInformationPannel", "Geocoding address: " + address);
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                double latitude = addresses.get(0).getLatitude();
                double longitude = addresses.get(0).getLongitude();
                return new LatLng(latitude, longitude);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}