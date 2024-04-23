package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

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
        Button button_medicine, button_far;

        button_medicine = (Button) findViewById(R.id.button_menu1);
        button_medicine.setOnClickListener(v ->  {
                Intent intent = new Intent(MainActivity.this, Medicine.class);
                startActivity(intent);
        });


        button_far = (Button) findViewById(R.id.button_menu2);
        button_far.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Farmacies.class);
            startActivity(intent);
        });

    }
}