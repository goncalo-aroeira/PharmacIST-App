package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Objects;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.UserLocalStore;
import pt.ulisboa.tecnico.cmov.pharmacist.elements.Map;

public class MainMenu extends AppCompatActivity {

    Button btnMedicine, btnPharmacy, btnMap, btnLogout, btnUpgradeAccount;

    TextView tvWelcome;

    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


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

        String greeting = userLocalStore.getLoggedInName() + ",";
        tvWelcome.setText(greeting);

        btnMedicine.setOnClickListener(v -> startActivity(new Intent(MainMenu.this, MedicineActivity.class)));
        btnPharmacy.setOnClickListener(v -> startActivity(new Intent(MainMenu.this, PharmaciesMenu.class)));
        btnMap.setOnClickListener(v -> startActivity(new Intent(MainMenu.this, Map.class)));

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
}
