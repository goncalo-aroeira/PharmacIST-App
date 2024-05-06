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

public class Login extends AppCompatActivity {

    EditText editTextTextEmailAddress;
    EditText editTextTextPassword;

    Button button;
    Button button2;

    String userEmail;
    String userPass;

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

        firebaseDBHandler = new FirebaseDBHandler();
        editTextTextEmailAddress = findViewById(R.id.editTextTextEmailAddress);
        editTextTextPassword = findViewById(R.id.editTextTextPassword);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userEmail = editTextTextEmailAddress.getText().toString();
                userPass = editTextTextPassword.getText().toString();
                // check password on back end
                firebaseDBHandler.getPasswordByEmail(userEmail, new FirebaseDBHandler.PasswordCallback() {
                    @Override
                    public void onPasswordRetrieved(String password) {
                        // User found, do something with the password
                        System.out.println("Password retrieved: " + password);
                        if(userPass.equals(password)){
                            Toast.makeText(Login.this,"Login Success :)",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Login.this, MainMenu.class));
                        }
                        else {

                            Toast.makeText(Login.this,"Login Fail :(",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onUserNotFound() {
                        // User not found, handle accordingly
                        Toast.makeText(Login.this,"User Not Found",Toast.LENGTH_SHORT).show();
                    }
                });
                


            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Register.class));

            }
        });
    }

}