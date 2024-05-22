package pt.ulisboa.tecnico.cmov.pharmacist;

import pt.ulisboa.tecnico.cmov.pharmacist.DatabaseClasses.*;
import pt.ulisboa.tecnico.cmov.pharmacist.fragments.PharmacyInformationPanelMapFragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.nfc.Tag;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.compose.runtime.snapshots.Snapshot;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class PharmacyInformationPanelActivity extends AppCompatActivity {

    private final String TAG ="PharmacyInformationPanelActivity";

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharmacy_information_panel);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        String userId = mAuth.getCurrentUser().getUid();

        DatabaseReference favsRef = mDatabase.child("UsersFavoritePharmacies").child(userId).child("favs");

        Bundle extras = getIntent().getExtras();
        String pharmacyName = extras.getString("PharmacyName");

        // Sets the customized toolbar in the view
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ToggleButton favorite = findViewById(R.id.favorite);

        // Initialize the ToggleButton state based on the database
        favsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> favs = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        favs.add(snapshot.getValue(String.class));
                    }
                }

                // Set the initial state of the ToggleButton
                favorite.setChecked(favs.contains(pharmacyName));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error reading favorites", databaseError.toException());
            }
        });

        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<String> favs = new ArrayList<>();
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                favs.add(snapshot.getValue(String.class));
                            }
                        }

                        if (favorite.isChecked()) {
                            if (!favs.contains(pharmacyName)) {
                                favs.add(pharmacyName);
                                favsRef.setValue(favs);
                                Toast.makeText(PharmacyInformationPanelActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (favs.contains(pharmacyName)) {
                                favs.remove(pharmacyName);
                                favsRef.setValue(favs);
                                Toast.makeText(PharmacyInformationPanelActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("Firebase", "Error reading favorites", databaseError.toException());
                    }
                });
            }
        });

        // Go back to Home page
        Button back = findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Going back to Home Page");
                Intent intent = new Intent(PharmacyInformationPanelActivity.this, HomePageActivity.class);
                startActivity(intent);
            }
        });

        // Gets the database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Query query = mDatabase.child("Pharmacy").orderByChild("name").equalTo(pharmacyName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Pharmacy query count: " + snapshot.getChildrenCount());
                List<Pharmacy> pharmacies = new ArrayList<>();

                for (DataSnapshot pharmacySnapshot : snapshot.getChildren()) {
                    Pharmacy pharmacy = pharmacySnapshot.getValue(Pharmacy.class);
                    Log.d(TAG, "Retrieved pharmacy: " + pharmacy);
                    pharmacies.add(pharmacy);
                }

                Pharmacy pharmacy = pharmacies.get(0);

                TextView name = findViewById(R.id.pharmacy_name);
                name.setText(pharmacy.getName());

                TextView address = findViewById(R.id.pharmacy_address);
                address.setText(pharmacy.getAddress());

                ImageView photo = findViewById(R.id.pharmacy_image);
                byte [] encodeByte = Base64.decode(pharmacy.getImageBase64(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                photo.setImageBitmap(bitmap);

                // Call map
                Button openMapButton = findViewById(R.id.open_map_button);
                openMapButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString("PharmacyAddress", address.getText().toString());
                        PharmacyInformationPanelMapFragment fragment = PharmacyInformationPanelMapFragment.newInstance();
                        fragment.setArguments(bundle);
                        fragment.show(getSupportFragmentManager(), "InfoPanelMapFragment");
                    }
                });

                // Display available medicines
                ListView medicinesList = findViewById(R.id.medicines_list);

                Query queryMedicines = mDatabase.child("PharmacyMedicines").child(pharmacyName);
                queryMedicines.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> medicineNames = new ArrayList<>();

                        for (DataSnapshot medicineSnapshot : snapshot.getChildren()) {
                            medicineNames.add(medicineSnapshot.getKey());
                            Log.d(TAG, medicineSnapshot.getKey());
                        }

                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(PharmacyInformationPanelActivity.this,
                                R.layout.medicines_list_item,
                                R.id.medicine_name,
                                medicineNames);

                        medicinesList.setAdapter(arrayAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Adds stock to an existing medicine or adds a new medicine
//        Button addMedicineButton = findViewById(R.id.add_medicine_button);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }
}
