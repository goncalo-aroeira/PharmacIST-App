package pt.ulisboa.tecnico.cmov.pharmacist;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.FirebaseDBHandler;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.User;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.UserLocalStore;
import pt.ulisboa.tecnico.cmov.pharmacist.elements.utils;

public class Login extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoToRegister, btnChangeLanguage;

    private TextView tvLoginAsGuest;
    private String userEmail, userPass;
    private FirebaseDBHandler firebaseDBHandler;
    private UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        loadLocale();
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

        userLocalStore = new UserLocalStore(this);

        // Check if user is already logged in
        if (userLocalStore.isUserLoggedIn()) {
            startActivity(new Intent(Login.this, MainMenu.class));
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnRegister);
        tvLoginAsGuest = findViewById(R.id.tvLoginAsGuest);
        firebaseDBHandler = new FirebaseDBHandler();
        btnChangeLanguage = findViewById(R.id.btnChangeLanguage);
        btnLogin.setOnClickListener(this::onLoginButtonClick);
        btnGoToRegister.setOnClickListener(this::onRegisterButtonClick);

        tvLoginAsGuest.setOnClickListener(this::onLoginAsGuestClick);
        btnChangeLanguage.setOnClickListener(v -> {
            showChangeLanguageDialog();
        });
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
        String email = utils.generateRandomId(10) + "@guest.com";
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
                if (user.isSuspended()) {
                    Toast.makeText(Login.this, "Your account is suspended. Please contact support.", Toast.LENGTH_LONG).show();
                    return;
                }
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

    private void showChangeLanguageDialog() {
        final String [] listItems = {"English", "Portuguese"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(Login.this);
        mBuilder.setTitle("Choose Language");
        mBuilder.setSingleChoiceItems(listItems, -1, (dialogInterface, i) -> {
            if (i == 0){
                setLocale("en");
                recreate();
            } else if (i == 1){
                setLocale("pt");
                recreate();
            }
            dialogInterface.dismiss();
        });
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }
    private void setLocale(String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", language);
        editor.apply();
    }

    public void loadLocale(){
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "");
        setLocale(language);
    }

}
