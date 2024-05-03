package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import pt.ulisboa.tecnico.cmov.pharmacist.farmacies.Farmacies;
import pt.ulisboa.tecnico.cmov.pharmacist.medicine.Medicine;

public class MainActivity extends AppCompatActivity {

    Button button_medicine, button_far, button_logout;
    UserLocalStore userLocalStore;
    TextView tvWelcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvWelcomeText = (TextView) findViewById(R.id.tvWelcomeText);

        userLocalStore = new UserLocalStore(this);

        tvWelcomeText.setText("Welcome");

        button_medicine = (Button) findViewById(R.id.btnMedicine);
        button_medicine.setOnClickListener(v ->  {
                Intent intent = new Intent(MainActivity.this, Medicine.class);
                startActivity(intent);
        });


        button_far = (Button) findViewById(R.id.btnFarmacy);
        button_far.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Farmacies.class);
            startActivity(intent);
        });

        button_logout = (Button) findViewById(R.id.btnLogout);
        button_far.setOnClickListener(v -> {
            userLocalStore.cleanUserData();
            userLocalStore.setUserLoggedIn(false);

            startActivity(new Intent(this, Login.class));
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        if (authenticate()) {
            displayUserDetails();
        } else {
            startActivity(new Intent(this, Login.class));
        }
    }

    private boolean authenticate() {
        // check if theres a logged in user yet
        return userLocalStore.getUserLoggedIn();

    }

    private void displayUserDetails() {
        User user = userLocalStore.getLoggedInUser();
        tvWelcomeText.setText("Welcome " + user.name + ",");
    }
}