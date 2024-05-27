package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.UUID;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.User;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.UserLocalStore;

public class Login extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoToRegister;

    private TextView tvLoginAsGuest;
    private String userEmail, userPass;
    private FirebaseDBHandler firebaseDBHandler;
    private UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViewsAndFirebase();

    }

    private void initializeViewsAndFirebase() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnRegister);
        tvLoginAsGuest = findViewById(R.id.tvLoginAsGuest);
        firebaseDBHandler = new FirebaseDBHandler();
        userLocalStore = new UserLocalStore(this);

        btnLogin.setOnClickListener(this::onLoginButtonClick);
        btnGoToRegister.setOnClickListener(this::onRegisterButtonClick);

        tvLoginAsGuest.setOnClickListener(this::onLoginAsGuestClick);
    }

    private void onLoginButtonClick(View view) {
        userEmail = etEmail.getText().toString();
        userPass = etPassword.getText().toString();
        authenticateLogin();
    }

    private void onRegisterButtonClick(View view) {
        startActivity(new Intent(Login.this, Register.class));
    }

    private void onLoginAsGuestClick(View view) {

        Log.d("LOGIN", "onLoginAsGuestClick: Guest");

        String name = "Guest";
        String email = UUID.randomUUID().toString() + "@guest.com";
        String password = name + "_pwd";


        User guest = new User(name, email, password);
        guest.generateId();
        userLocalStore.saveLoginDetails(guest.getId(), guest.getName(), guest.getEmail(), guest.getPassword());


        firebaseDBHandler.registerUser(guest, new FirebaseDBHandler.OnRegistrationListener() {
            @Override
            public void onRegistrationSuccess() {
                Toast.makeText(Login.this, "Entered as guest", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Login.this, MainMenu.class));
            }

            @Override
            public void onRegistrationFailure(Exception e) {

            }

            @Override
            public void onEmailExists() {

            }

            @Override
            public void onUsernameExists() {

            }
        });

    }

    private void authenticateLogin() {

        firebaseDBHandler.performLogin(userEmail, userPass, new FirebaseDBHandler.PasswordCallback() {
            @Override
            public void onUserNotFound() {
                Toast.makeText(Login.this, "User Not Found", Toast.LENGTH_SHORT).show();
                clearInputFields();
            }

            @Override
            public void onSuccessfulLogin(User user) {
                Log.d("Login Page", "onSucessfullLogin: User data: " + user.getName() + " " + user.getEmail() + " " + user.getPassword());
                userLocalStore.saveLoginDetails(user.getId(), user.getName(), user.getEmail(), user.getPassword());
                Toast.makeText(Login.this, "Login Success", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Login.this, MainMenu.class));
            }

            @Override
            public void onWrongPassword() {
                Toast.makeText(Login.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                clearInputFields();
            }
        });
    }

    private void clearInputFields() {
        etEmail.setText("");
        etPassword.setText("");
    }
}
