package pt.ulisboa.tecnico.cmov.pharmacist;

import pt.ulisboa.tecnico.cmov.pharmacist.DatabaseClasses.*;
import pt.ulisboa.tecnico.cmov.pharmacist.adapter.MedicineAdapter;
import pt.ulisboa.tecnico.cmov.pharmacist.fragments.AddMedicineBarcodeFragment;
import pt.ulisboa.tecnico.cmov.pharmacist.fragments.PharmacyInformationPanelMapFragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

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

public class PharmacyInformationPanelActivity extends AppCompatActivity implements MedicineAdapter.OnAddMedicineClickListener {

    private final String TAG = "PharmacyInformationPanelActivity";

    private FirebaseAuth mAuth;

    private FirebaseStorage mStorageRef;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    MedicineAdapter medicineAdapter = null;

    ListView medicinesList = null;

    String pharmacyName = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharmacy_information_panel);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Gets the storage
        mStorageRef = FirebaseStorage.getInstance();

        // Display available medicines
        medicinesList = findViewById(R.id.medicines_list);

        String userId = mAuth.getCurrentUser().getUid();

        Bundle extras = getIntent().getExtras();
        pharmacyName = extras.getString("PharmacyName");

        // Set up the FragmentResultListener
        getSupportFragmentManager().setFragmentResultListener(
                "added",
                this,
                (requestKey, result) -> {
                    if (result.getBoolean("added", false)) {
                        // Medicine was added, refresh the list
                        Log.d(TAG, "Medicine was added");
                        getMedicinesFromDatabase(pharmacyName);
                    }
                }
        );

        // Sets the customized toolbar in the view
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ToggleButton favorite = findViewById(R.id.favorite);

        getMedicinesFromDatabase(pharmacyName);

        DatabaseReference favsRef = mDatabase.child("UsersFavoritePharmacies").child(userId).child("favs");

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

                TextView addressView = findViewById(R.id.pharmacy_address);
                String pharmacyAddress = pharmacy.getAddress();
                addressView.setText(pharmacyAddress);

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
                        bundle.putString("PharmacyAddress", pharmacyAddress);
                        PharmacyInformationPanelMapFragment fragment = PharmacyInformationPanelMapFragment.newInstance();
                        fragment.setArguments(bundle);
                        fragment.show(getSupportFragmentManager(), "InfoPanelMapFragment");
                    }
                });

                Button directionsButton = findViewById(R.id.directions_button);

                directionsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(PharmacyInformationPanelActivity.this, HomePageActivity.class);
                        intent.putExtra("pharmacy_address", pharmacyAddress);
                        startActivity(intent);
                    }
                });

                // Adds stock to an existing medicine or adds a new medicine
                Button addMedicineButton = findViewById(R.id.add_medicine_button);
                addMedicineButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString("PharmacyName", pharmacyName);
                        AddMedicineBarcodeFragment fragment = AddMedicineBarcodeFragment.newInstance();
                        fragment.setArguments(bundle);
                        fragment.show(getSupportFragmentManager(), "AddMedicineStock");
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
                    medicineAdapter.getFilter().filter("");
                } else {
                    medicineAdapter.getFilter().filter(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    medicineAdapter.getFilter().filter("");
                } else {
                    medicineAdapter.getFilter().filter(newText);
                }
                return true;
            }
        });


    }

    private void getMedicinesFromDatabase(String pharmacyName) {
        Query queryMedicines = mDatabase.child("PharmacyMedicines").child(pharmacyName);
        queryMedicines.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Medicine query count: " + snapshot.getChildrenCount());
                List<String> medicineNames = new ArrayList<>();

                for (DataSnapshot medicineSnapshot : snapshot.getChildren()) {
                    medicineNames.add(medicineSnapshot.getKey());
                    Log.d(TAG, medicineSnapshot.getKey());
                }

                medicineAdapter = new MedicineAdapter(PharmacyInformationPanelActivity.this, medicineNames, PharmacyInformationPanelActivity.this);
                medicinesList.setAdapter(medicineAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onAddMedicineClick(String medicineName) {
        // Add your logic to handle the add medicine button click event
        ConstraintLayout purchaseLayout = findViewById(R.id.purchase_layout);
        View viewPurchase = LayoutInflater.from(PharmacyInformationPanelActivity.this).inflate(R.layout.purchase_medicine_dialog, purchaseLayout);

        TextView purchaseMedicine = viewPurchase.findViewById(R.id.purchase_text);
        String purchaseText = "Purchase " + medicineName;
        purchaseMedicine.setText(purchaseText);

        AlertDialog.Builder builder = new AlertDialog.Builder(PharmacyInformationPanelActivity.this);
        builder.setView(viewPurchase);
        final AlertDialog alertDialog = builder.create();

        Button purchaseButton = viewPurchase.findViewById(R.id.purchase_button);
        purchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText quantity = viewPurchase.findViewById(R.id.medicine_quantity);

                if (TextUtils.isEmpty(quantity.getText().toString())) {
                    Toast.makeText(PharmacyInformationPanelActivity.this, "Please enter the quantity to purchase!", Toast.LENGTH_LONG).show();
                    return;
                }

                Integer purchaseQuantity = Integer.parseInt(quantity.getText().toString());

                // Check if there's enough medicine quantity
                Query queryPharmacy = mDatabase.child("PharmacyMedicines").child(pharmacyName).child(medicineName);
                queryPharmacy.addListenerForSingleValueEvent( new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            int currentStock = snapshot.getValue(Integer.class);

                            Log.d(TAG, "Stock: " + currentStock);

                            if (currentStock - purchaseQuantity < 0) {
                                Toast.makeText(PharmacyInformationPanelActivity.this, "Not enough stock", Toast.LENGTH_LONG).show();
                            } else {
                                int updatedStock = currentStock - purchaseQuantity;

                                if (updatedStock == 0) {
                                    mDatabase.child("PharmacyMedicines").child(pharmacyName).child(medicineName).removeValue();

                                    Query query = mDatabase.child("Medicines").child(medicineName).child("pharmacies");

                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            int position = -1;

                                            for (DataSnapshot pharmacySnapshot : snapshot.getChildren()) {
                                                if (pharmacySnapshot.getValue(String.class).equals(pharmacyName)) {
                                                    position = Integer.parseInt(pharmacySnapshot.getKey());
                                                }
                                            }

                                            mDatabase.child("Medicines").child(medicineName).child("pharmacies").child(String.valueOf(position)).removeValue();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                                else {
                                    mDatabase.child("PharmacyMedicines").child(pharmacyName).child(medicineName).setValue(updatedStock);
                                }

                                Bundle result = new Bundle();

                                result.putBoolean("added", true);

                                getSupportFragmentManager().setFragmentResult("added",result);

                                Toast.makeText(PharmacyInformationPanelActivity.this, "Purchased " + purchaseQuantity + " " + medicineName, Toast.LENGTH_LONG).show();

                                alertDialog.dismiss();
                            }

                        } else {
                            Log.e(TAG, "Error retrieving medicine stock");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}