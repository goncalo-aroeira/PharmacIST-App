package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Register extends AppCompatActivity {

    EditText etName, etUsername, etPassword, etPasswordConfirmed;
    Button btnRegister;

    UserLocalStore userLocalStore;


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

        userLocalStore = new UserLocalStore(this);

        etName = (EditText) findViewById(R.id.etName);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etPasswordConfirmed = (EditText) findViewById(R.id.etPasswordConfirmed);

        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> {

            String name = etName.getText().toString();
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            User registerData = new User(name, username, password);

            userLocalStore.storeUserData(registerData);
            userLocalStore.setUserLoggedIn(true);

            Intent intent = new Intent(Register.this, MainActivity.class);
            startActivity(intent);
        });

    }


}