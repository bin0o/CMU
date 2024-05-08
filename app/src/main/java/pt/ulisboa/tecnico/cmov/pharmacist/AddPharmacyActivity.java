package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddPharmacyActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    private TabLayout addressTabLayout;

    private ViewPager2 viewPager2;

    private final String TAG = "AddPharmacyActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pharmacy);

        // Sets the customized toolbar in the view
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Sets the tabs for the different options to add an address
        addressTabLayout = findViewById(R.id.address_options_tab);
        viewPager2 = findViewById(R.id.address_options_viewpager);
        ViewPagerAdpater viewPagerAdpater = new ViewPagerAdpater(this);

        viewPager2.setAdapter(viewPagerAdpater);
        addressTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                addressTabLayout.getTabAt(position).select();
            }
        });

        // Gets the database
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

        // Adding a new Pharmacy
        Button addPharmacyButton = findViewById(R.id.add_pharmacy_button);
        addPharmacyButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
//                 EditText pharmacyNameEditText = findViewById(R.id.name);
//                 String pharmacyName = pharmacyNameEditText.getText().toString();
//
//                 EditText pharmacyAddressEditText = findViewById(R.id.location);
//                 String pharmacyAddress = pharmacyAddressEditText.getText().toString();
//
//                 // Create a new Pharmacy object
//                 Pharmacy pharmacy = new Pharmacy(pharmacyName, pharmacyAddress);
//
//                 // Push the Pharmacy object to the "Pharmacy" node
//                 mDatabase.child("Pharmacy").push().setValue(pharmacy)
//                         .addOnCompleteListener(new OnCompleteListener<Void>() {
//                             @Override
//                             public void onComplete(@NonNull Task<Void> task) {
//                                 if (task.isSuccessful()) {
//                                     Log.d(TAG, "addPharmacy: Pharmacy added successfully");
//                                 } else {
//                                     Log.e(TAG, "addPharmacy: Failed to add pharmacy", task.getException());
//                                 }
//                             }
//                         });

                 Log.i(TAG, "Going back to Home Page");
                 Intent intent = new Intent(AddPharmacyActivity.this, HomePageActivity.class);
                 startActivity(intent);
             }
         });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }
}
