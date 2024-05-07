package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class Login extends AppCompatActivity {

    EditText etEmail, etPassword;

    UserLocalStore userLocalStore;

    Button btnLogin, btnGoToRegister;

    String userEmail, userPass;

    FirebaseDBHandler firebaseDBHandler;

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

        userLocalStore = new UserLocalStore(this);

        firebaseDBHandler = new FirebaseDBHandler();
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userEmail = etEmail.getText().toString();
                userPass = etPassword.getText().toString();
                // check password on back end
                firebaseDBHandler.performLogin(userEmail, userPass, new FirebaseDBHandler.PasswordCallback() {
                    @Override
                    public void onUserNotFound() {
                        Toast.makeText(Login.this, "User Not Found", Toast.LENGTH_SHORT).show();
                        cleanUserData();
                    }

                    @Override
                    public void onSucessfullLogin(User user) {
                        Log.d("Login Page", "onSucessfullLogin: User data: " + user.getName() + " " + user.getEmail() + " " + user.getPassword());
                        userLocalStore.saveLoginDetails(user.getName(), user.getEmail(), user.getPassword());
                        Toast.makeText(Login.this, "Login Success", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Login.this, MainMenu.class));
                    }

                    @Override
                    public void onWrongPassword() {
                        Toast.makeText(Login.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                        cleanUserData();
                    }

                });
            }
        });


        btnGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Register.class));

            }
        });
    }

    public void cleanUserData() {
        etEmail.setText("");
        etPassword.setText("");
    }



}