package pt.ulisboa.tecnico.cmov.pharmacist;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CreateMedicine extends AppCompatActivity {

    EditText medicineName, usage;
    ImageView boxPhoto;
    Button btnSave, btnCancel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_medicine);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        medicineName = findViewById(R.id.medicineName);
        usage = findViewById(R.id.medicineDescription);
        boxPhoto = findViewById(R.id.ivBoxPhoto);
        btnSave = findViewById(R.id.btnCreateMedicine);
        btnCancel = findViewById(R.id.btnCancelMedicine);


    }
}