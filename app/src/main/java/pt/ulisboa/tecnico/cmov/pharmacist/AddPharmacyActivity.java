package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddPharmacyActivity extends ComponentActivity {

    private DatabaseReference mDatabase;

    private final String TAG = "AddPharmacyActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pharmacy);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Go back to Home page
        Button back = findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Going back to Home Page");
                Intent intent = new Intent(AddPharmacyActivity.this, HomePageActivity.class);
                startActivity(intent);
            }
        });

        Button addPharmacyButton = findViewById(R.id.add_pharmacy_button);
        addPharmacyButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 EditText pharmacyNameEditText = findViewById(R.id.name);
                 String pharmacyName = pharmacyNameEditText.getText().toString();

                 EditText pharmacyAddressEditText = findViewById(R.id.location);
                 String pharmacyAddress = pharmacyAddressEditText.getText().toString();

                 Pharmacy pharmacy = new Pharmacy(pharmacyName, pharmacyAddress);

                 mDatabase.child("Pharmacy").child("1").child("name").setValue(pharmacyName);
                 mDatabase.child("Pharmacy").child("1").child("address").setValue(pharmacyAddress);

                 Log.i(TAG, "Going back to Home Page");
                 Intent intent = new Intent(AddPharmacyActivity.this, HomePageActivity.class);
                 startActivity(intent);
             }
         });
    }
}
