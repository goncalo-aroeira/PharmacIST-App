package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Medicine;

public class CreateMedicine extends AppCompatActivity {

    EditText medicineName, usage;
    ImageView boxPhoto;
    Button btnSave, btnCancel;

    FirebaseDBHandler firebaseDBHandler;


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

        // verify if medicine name already exists in database
        //return error

        firebaseDBHandler = new FirebaseDBHandler();

        btnSave.setOnClickListener(v -> {
            saveMedicine();
        });

        btnCancel.setOnClickListener(v -> {
            Intent intent = new Intent(CreateMedicine.this, MedicineActivity.class);
            startActivity(intent);
        });

    }

    private void saveMedicine() {
        // save medicine in database
        // return to previous activity
        String name = medicineName.getText().toString();
        String description = usage.getText().toString();
        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(CreateMedicine.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Medicine medicine = new Medicine(name, description);
        medicine.generateId();

        firebaseDBHandler.addMedicine(medicine, new FirebaseDBHandler.OnChangeListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(CreateMedicine.this, medicine.getName() + " added successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(CreateMedicine.this, "System failed to add the medicine ", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });

        // replace for navigation
        Intent intent = new Intent(CreateMedicine.this, MedicineActivity.class);
        startActivity(intent);
    }


}