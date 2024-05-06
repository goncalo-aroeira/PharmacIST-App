package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.User;

public class Register extends AppCompatActivity {
    EditText editTextText;
    EditText editTextTextEmailAddress2;
    EditText editTextTextPassword2;

    Button button3;

    String userName;
    String userEmail;
    String userPass;

    FirebaseDBHandler firebaseDBHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        firebaseDBHandler = new FirebaseDBHandler();

        editTextText = findViewById(R.id.editTextText);
        editTextTextEmailAddress2 = findViewById(R.id.editTextTextEmailAddress2);
        editTextTextPassword2 = findViewById(R.id.editTextTextPassword2);
        button3 = findViewById(R.id.button3);



        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userName = editTextText.getText().toString();
                userEmail = editTextTextEmailAddress2.getText().toString();
                userPass = editTextTextPassword2.getText().toString();
                firebaseDBHandler.addUser(new User(userName,userEmail,userPass));
                startActivity(new Intent(Register.this, MainMenu.class));
                // check password on back end

            }
        });

    }
}