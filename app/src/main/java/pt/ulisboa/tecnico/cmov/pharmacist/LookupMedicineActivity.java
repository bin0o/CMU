package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LookupMedicineActivity extends AppCompatActivity {
    private final String TAG = "LookupMedicineActivity";

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    ArrayAdapter<String> arrayAdapter = null;

    ListView medicinesList = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lookup_medicine);

        // Sets the customized toolbar in the view
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Display available medicines
        medicinesList = findViewById(R.id.medicines_list);

        getMedicinesFromDatabase();

        // Set up the FragmentResultListener
        getSupportFragmentManager().setFragmentResultListener(
                "added",
                this,
                (requestKey, result) -> {
                    if (result.getBoolean("added", false)) {
                        // Medicine was added, refresh the list
                        Log.d(TAG, "Medicine was added");
                        getMedicinesFromDatabase();
                    }
                }
        );

        medicinesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected medicine name
                String medicineName = (String) parent.getItemAtPosition(position);

                Log.d(TAG, "Selected medicine: " + medicineName);

                // Create an intent to start the MedicineInformationPanelActivity
                Intent intent = new Intent(LookupMedicineActivity.this, MedicineInformationPanelActivity.class);

                // Pass the medicine name to the next activity
                intent.putExtra("MedicineName", medicineName);

                // Start the activity
                startActivity(intent);
            }
        });

        // Set up search query listener
        SearchView medicineSearch = findViewById(R.id.medicine_search);
        medicineSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

    private void getMedicinesFromDatabase() {
        Query queryMedicines = mDatabase.child("Medicines");
        queryMedicines.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Medicine query count: " + snapshot.getChildrenCount());
                List<String> medicineNames = new ArrayList<>();

                for (DataSnapshot medicineSnapshot : snapshot.getChildren()) {
                    medicineNames.add(medicineSnapshot.getKey());
                    Log.d(TAG, medicineSnapshot.getKey());
                }

                arrayAdapter = new ArrayAdapter<String>(LookupMedicineActivity.this,
                        R.layout.medicines_list_item_lookup,
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}
