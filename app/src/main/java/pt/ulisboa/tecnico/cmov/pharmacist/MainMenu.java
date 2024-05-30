package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.Manifest.*;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Objects;
import java.util.Locale;
import android.app.Activity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.UserLocalStore;
import pt.ulisboa.tecnico.cmov.pharmacist.elements.Map;

public class MainMenu extends AppCompatActivity {

    Button btnMedicine, btnPharmacy, btnMap, btnLogout, btnUpgradeAccount, btnChangeLanguage;

    TextView tvWelcome;

    UserLocalStore userLocalStore;

    private static final String TAG = "MainMenu";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Check for permissions and get location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permissions
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        initializeViewsAndFirebase();

    }


    private void initializeViewsAndFirebase() {
        tvWelcome = findViewById(R.id.tvWelcome);
        btnMedicine = findViewById(R.id.btnMedicine);
        btnPharmacy = findViewById(R.id.btnPharmacy);
        btnMap = findViewById(R.id.btnMap);
        btnLogout = findViewById(R.id.btnLogout);
        btnUpgradeAccount = findViewById(R.id.btnUpgradeAccount);
        userLocalStore = new UserLocalStore(this);
        btnChangeLanguage = findViewById(R.id.btnChangeLanguage);

        String greeting = userLocalStore.getLoggedInName() + ",";
        tvWelcome.setText(greeting);

        btnMedicine.setOnClickListener(v -> startActivity(new Intent(MainMenu.this, MedicineActivity.class)));
        btnPharmacy.setOnClickListener(v -> startActivity(new Intent(MainMenu.this, PharmaciesMenu.class)));
        btnMap.setOnClickListener(v -> startActivity(new Intent(MainMenu.this, Map.class)));

        btnChangeLanguage.setOnClickListener(v -> {
            showChangeLanguageDialog();
        });
        if (Objects.equals(userLocalStore.getLoggedInName(), "Guest")){
            btnUpgradeAccount.setVisibility(View.VISIBLE);
            btnUpgradeAccount.setOnClickListener(v -> {
                Intent intent = new Intent(MainMenu.this, Register.class);
                intent.putExtra("id", userLocalStore.getLoggedInId());
                startActivity(intent);
            });
        }

        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        userLocalStore.clearLoginDetails();
        startActivity(new Intent(MainMenu.this, Login.class));
        finish();
    }

    private void showChangeLanguageDialog() {
        final String [] listItems = {"English", "Portuguese"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainMenu.this);
        mBuilder.setTitle("Choose Language");
        mBuilder.setSingleChoiceItems(listItems, -1, (dialogInterface, i) -> {
            if (i == 0){
                setLocale("en");
                recreate();
            } else if (i == 1){
                setLocale("pt");
                recreate();
            }
            dialogInterface.dismiss();
        });
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
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
