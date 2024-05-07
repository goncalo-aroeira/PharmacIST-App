package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class EnterQuantity extends AppCompatActivity {

    private EditText editTextQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_quantity);

        editTextQuantity = findViewById(R.id.editTextQuantity);
        Button buttonAddQuantity = findViewById(R.id.buttonAddQuantity);

        buttonAddQuantity.setOnClickListener(view -> addQuantity());
    }

    private void addQuantity() {
        String quantityString = editTextQuantity.getText().toString();
        int quantity = Integer.parseInt(quantityString);

        Intent intent = new Intent();
        intent.putExtra("QUANTITY", quantity);
        setResult(RESULT_OK, intent);
        finish();
    }
}
