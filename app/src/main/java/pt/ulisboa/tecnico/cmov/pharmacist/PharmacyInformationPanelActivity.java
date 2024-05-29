package pt.ulisboa.tecnico.cmov.pharmacist;

import pt.ulisboa.tecnico.cmov.pharmacist.DatabaseClasses.*;
import pt.ulisboa.tecnico.cmov.pharmacist.fragments.AddMedicineManualFragment;
import pt.ulisboa.tecnico.cmov.pharmacist.fragments.PharmacyInformationPanelMapFragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class PharmacyInformationPanelActivity extends AppCompatActivity {

    private final String TAG = "PharmacyInformationPanelActivity";

    private FirebaseAuth mAuth;

    private FirebaseStorage mStorageRef;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    ArrayAdapter<String> arrayAdapter = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharmacy_information_panel);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Gets the storage
        mStorageRef = FirebaseStorage.getInstance();

        // Display available medicines
        ListView medicinesList = findViewById(R.id.medicines_list);

        String userId = mAuth.getCurrentUser().getUid();

        DatabaseReference favsRef = mDatabase.child("UsersFavoritePharmacies").child(userId).child("favs");

        Bundle extras = getIntent().getExtras();
        String pharmacyName = extras.getString("PharmacyName");

        // Sets the customized toolbar in the view
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

                ImageView photoView = findViewById(R.id.pharmacy_image);

                StorageReference photoRef = mStorageRef.getReferenceFromUrl(pharmacy.getImageUrl());

                // Set the maximum size to download in bytes (e.g., 1024 * 1024 for 1MB)
                long ONE_MEGABYTE = 1024 * 1024;
                photoRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // Convert the byte array to a Bitmap
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        // Set the Bitmap to the ImageView
                        photoView.setImageBitmap(bitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        exception.printStackTrace();
                        Toast.makeText(PharmacyInformationPanelActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                });

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


                Query queryMedicines = mDatabase.child("PharmacyMedicines").child(pharmacyName);
                queryMedicines.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> medicineNames = new ArrayList<>();

                        for (DataSnapshot medicineSnapshot : snapshot.getChildren()) {
                            medicineNames.add(medicineSnapshot.getKey());
                            Log.d(TAG, medicineSnapshot.getKey());
                        }

                        arrayAdapter = new ArrayAdapter<String>(PharmacyInformationPanelActivity.this,
                                R.layout.medicines_list_item,
                                R.id.medicine_name,
                                medicineNames);

                        medicinesList.setAdapter(arrayAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                // Adds stock to an existing medicine or adds a new medicine
                Button addMedicineButton = findViewById(R.id.add_medicine_button);
                addMedicineButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString("PharmacyAddress", address.getText().toString());
                        AddMedicineManualFragment fragment = AddMedicineManualFragment.newInstance();
                        fragment.setArguments(bundle);
                        fragment.show(getSupportFragmentManager(), "InfoPanelMapFragment");
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        medicinesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected medicine name
                String medicineName = (String) parent.getItemAtPosition(position);

                Log.d(TAG, "Selected medicine: " + medicineName);

                // Create an intent to start the MedicineInformationPanelActivity
                Intent intent = new Intent(PharmacyInformationPanelActivity.this, MedicineInformationPanelActivity.class);

                // Pass the medicine name to the next activity
                intent.putExtra("MedicineName", medicineName);

                // Start the activity
                startActivity(intent);
            }
        });

        // Set up search query listener
        SearchView mapSearch = findViewById(R.id.medicine_search);
        mapSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public boolean onQueryTextSubmit(String query) {
                if (query.isEmpty()) {
                    arrayAdapter.getFilter().filter("");
                } else {
                    arrayAdapter.getFilter().filter(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    arrayAdapter.getFilter().filter("");
                } else {
                    arrayAdapter.getFilter().filter(newText);
                }
                return true;
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}