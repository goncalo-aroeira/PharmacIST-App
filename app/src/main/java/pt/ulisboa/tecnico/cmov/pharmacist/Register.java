package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

        btnRegister.setOnClickListener(view -> {
            User newUser = new User(etUsername.getText().toString(), etEmail.getText().toString(), etPassword.getText().toString());

            firebaseDBHandler.registerUser(newUser, new FirebaseDBHandler.OnRegistrationListener() {
                @Override
                public void onRegistrationSuccess() {
                    Toast.makeText(Register.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Register.this, MainMenu.class));
                }

                @Override
                public void onRegistrationFailure(Exception e) {
                    Toast.makeText(Register.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onEmailExists() {
                    Toast.makeText(Register.this, "Email already exists", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onUsernameExists() {
                    Toast.makeText(Register.this, "Username already exists", Toast.LENGTH_LONG).show();
                }
            });
        });

    }
    private boolean isValidInput(String email, String password) {
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            return false;
        }
        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return false;
        }
        return true;
    }

}