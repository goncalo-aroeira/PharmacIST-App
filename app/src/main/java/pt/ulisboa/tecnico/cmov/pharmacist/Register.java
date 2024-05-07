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

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.User;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.UserLocalStore;

public class Register extends AppCompatActivity {
    EditText etUsername, etEmail, etPassword;
    Button btnRegister;
    String userName, userEmail, userPass;
    FirebaseDBHandler firebaseDBHandler;

    UserLocalStore userLocalStore = new UserLocalStore(this);

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

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmailRegister);
        etPassword = findViewById(R.id.etPasswordRegister);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userName = etUsername.getText().toString();
                userEmail = etEmail.getText().toString();
                userPass = etPassword.getText().toString();
                firebaseDBHandler.addUser(new User(userName,userEmail,userPass));
                userLocalStore.saveLoginDetails(userName, userEmail, userPass);
                startActivity(new Intent(Register.this, MainMenu.class));
            }
        });
    }
}