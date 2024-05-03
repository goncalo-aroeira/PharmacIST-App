package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Login extends AppCompatActivity {

    Button btnLogin;
    TextView tvRegisterLink;
    EditText etUsername, etPassword;
    UserLocalStore userLocalStore;

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

        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        tvRegisterLink = (TextView) findViewById(R.id.tvRegisterLink);

        userLocalStore = new UserLocalStore(this);

        btnLogin.setOnClickListener(v -> {

            String username, password;
            username = etUsername.getText().toString();
            password = etUsername.getText().toString();

            User loggedInUser = new User(username, password);

            // always registers a new user, does not validate account
            userLocalStore.storeUserData(loggedInUser);
            userLocalStore.setUserLoggedIn(true);
            startActivity(new Intent(this, MainActivity.class));
        });

        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(this, Register.class));
        });
    }
}