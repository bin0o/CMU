package pt.ulisboa.tecnico.cmov.pharmacist;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import pt.ulisboa.tecnico.cmov.pharmacist.fragments.MapFragment;

public class HomePageActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    private final int FINE_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        // Check for location permissions
        if (checkLocationPermission()) {
            loadMapFragment();
        } else {
            requestLocationPermission();
        }

        // Sets the customized toolbar in the view
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Call map even before permissions (app must work regardless of current location)
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.map_frame_layout, MapFragment.newInstance())
                .commit();

        // Call add pharmacy
        Button addPharmacyButton = findViewById(R.id.add_pharmacy_button);
        addPharmacyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, AddPharmacyActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, FINE_PERMISSION_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    private void loadMapFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.map_frame_layout, MapFragment.newInstance())
                .commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadMapFragment();
            } else {
                Toast.makeText(HomePageActivity.this, "No Permission to View Location. Please Allow!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
