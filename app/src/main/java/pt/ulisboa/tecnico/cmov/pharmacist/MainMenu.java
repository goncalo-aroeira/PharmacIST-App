package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.UserLocalStore;

public class MainMenu extends AppCompatActivity {

    Button btnMedicine, btnPharmacy, btnMap, btnLogout;

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

        UserLocalStore userLocalStore = new UserLocalStore(this);

        if (!userLocalStore.isUserLoggedIn()) {
            Log.d("MainMenu", "onCreate: User is not logged in yet");
            startActivity(new Intent(MainMenu.this, Login.class));
        }

        tvWelcome = (TextView) findViewById(R.id.tvWelcome);
        tvWelcome.setText(userLocalStore.getLoggedInName() + ",");


        btnMedicine = (Button) findViewById(R.id.btnMedicine);
        btnMedicine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenu.this, MedicineActivity.class);
                startActivity(intent);
            }
        });

        btnPharmacy = (Button) findViewById(R.id.btnPharmacy);
        btnPharmacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenu.this, PharmaciesMenu.class);
                startActivity(intent);
            }
        });

        btnMap = (Button) findViewById(R.id.btnMap);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenu.this, Map.class);
                startActivity(intent);
            }
        });


        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenu.this, Login.class);
                userLocalStore.clearLoginDetails();
                startActivity(intent);
            }
        });

    }

    ;

}
