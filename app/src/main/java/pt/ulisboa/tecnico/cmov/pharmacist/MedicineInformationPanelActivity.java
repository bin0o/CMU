package pt.ulisboa.tecnico.cmov.pharmacist;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.DatabaseClasses.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.adapter.PharmacyAdapter;
import pt.ulisboa.tecnico.cmov.pharmacist.helper.PharmacyHelper;

public class MedicineInformationPanelActivity extends AppCompatActivity {

    private static final String TAG = "MedicineInfoActivity";
    private ListView pharmaciesList;
    private PharmacyAdapter pharmacyAdapter;
    private List<PharmacyHelper> pharmaciesHelper;
    public FusedLocationProviderClient client;
    public static Location currentLocation;
    private List<Pharmacy> pharmacies;

    private FirebaseStorage mStorageRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_information_panel);

        // Gets the storage
        mStorageRef = FirebaseStorage.getInstance();

        // Sets the customized toolbar in the view
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        client = LocationServices.getFusedLocationProviderClient(this);

        // Get the medicine name from the intent
        String medicineName = getIntent().getStringExtra("MedicineName");

        onLocationPermissionGranted();

        // Log the medicine name to verify the intent data
        Log.d(TAG, "Received medicine name: " + medicineName);

        // Display the medicine name
        TextView medicineNameTextView = findViewById(R.id.medicine_name_panel);
        medicineNameTextView.setText(medicineName);

        pharmaciesList = findViewById(R.id.pharmacies_list);
        pharmaciesHelper = new ArrayList<>();
        pharmacies = new ArrayList<>();

        pharmacyAdapter = new PharmacyAdapter(this, pharmaciesHelper);
        pharmaciesList.setAdapter(pharmacyAdapter);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Medicines");
        Query query = databaseReference.child(medicineName);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pharmacies.clear();
                pharmaciesHelper.clear();

                ImageView photoView = findViewById(R.id.medicine_image);

                StorageReference photoRef = mStorageRef.getReferenceFromUrl(dataSnapshot.child("imageUrl").getValue(String.class));

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
                        Toast.makeText(MedicineInformationPanelActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                });

                for (DataSnapshot snapshot : dataSnapshot.child("pharmacies").getChildren()) {
                    String pharmacyName = snapshot.getKey();
                    Log.d(TAG, "Pharmacy name: " + pharmacyName);
                    DatabaseReference pharmacyRef = FirebaseDatabase.getInstance().getReference("Pharmacy").child(pharmacyName);
                    pharmacyRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Pharmacy pharmacy = snapshot.getValue(Pharmacy.class);
                            if (pharmacy != null) {
                                PharmacyHelper pharmacyHelper = new PharmacyHelper();
                                pharmacyHelper.setPharmacy(pharmacy);
                                pharmaciesHelper.add(pharmacyHelper);

                                sortPharmaciesByDistance(pharmaciesHelper);
                                pharmacyAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Failed to read pharmacy data", error.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read medicine data", error.toException());
            }
        });

        pharmaciesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected PharmacyHelper object
                PharmacyHelper pharmacyHelper = (PharmacyHelper) parent.getItemAtPosition(position);

                // Extract the pharmacy name from the PharmacyHelper object
                String pharmacyName = pharmacyHelper.getName();

                Log.d(TAG, "Selected pharmacy: " + pharmacyName);

                // Create an intent to start the MedicineInformationPanelActivity
                Intent intent = new Intent(MedicineInformationPanelActivity.this, PharmacyInformationPanelActivity.class);

                // Pass the medicine name to the next activity
                intent.putExtra("PharmacyName", pharmacyName);

                // Start the activity
                startActivity(intent);
            }
        });

        // Set up search query listener
        SearchView pharmacySearch = findViewById(R.id.pharmacy_search);
        pharmacySearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public boolean onQueryTextSubmit(String query) {
                if (query.isEmpty()) {
                    pharmacyAdapter.getFilter().filter("");
                } else {
                    pharmacyAdapter.getFilter().filter(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    pharmacyAdapter.getFilter().filter("");
                } else {
                    pharmacyAdapter.getFilter().filter(newText);
                }
                return true;
            }
        });

    }

    private void sortPharmaciesByDistance(List<PharmacyHelper> pharmacies) {
        Location userLocation = currentLocation;

        for (PharmacyHelper pharmacyHelper : pharmacies) {
            float[] results = new float[1];
            LatLng latLng = geocodeAddress(pharmacyHelper.getAddress());
            Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(), latLng.latitude, latLng.longitude, results);
            pharmacyHelper.setDistance(results[0]);
        }

        Collections.sort(pharmaciesHelper, new Comparator<PharmacyHelper>() {
            @Override
            public int compare(PharmacyHelper p1, PharmacyHelper p2) {
                return Float.compare(p1.getDistance(), p2.getDistance());
            }
        });
    }

    private LatLng geocodeAddress(String address) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (!addresses.isEmpty()) {
                Address location = addresses.get(0);
                return new LatLng(location.getLatitude(), location.getLongitude());
            } else {
                Log.e(TAG, "Address not found: " + address);
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoding failed: " + e.getMessage());
        }
        return null;
    }

    private void onLocationPermissionGranted() {
        // If permission was not granted, place a default position on map
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            currentLocation = new Location("Instituto Superior Técnico - Taguspark");
            return;
        }

        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;

                } else {
                    Log.e(TAG, "getLastLocation: Location is null");
                }
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