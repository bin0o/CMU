package pt.ulisboa.tecnico.cmov.pharmacist;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import pt.ulisboa.tecnico.cmov.pharmacist.fragments.MapFragment;

public class HomePageActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final String TAG = "MainActivity";

    private DrawerLayout drawerLayout;

    private ActionBarDrawerToggle toggle;

    private final int FINE_PERMISSION_CODE = 1;

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationIcon(R.drawable.baseline_menu_24);

        // Set Navigation Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.map_frame_layout, new MapFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        // Call map even before permissions (app must work regardless of current location)
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.map_frame_layout, MapFragment.newInstance())
                .commit();

    }

    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, FINE_PERMISSION_CODE);
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

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
       if (menuItem.getItemId() == R.id.nav_home) {
           getSupportFragmentManager().beginTransaction().replace(R.id.map_frame_layout, new MapFragment()).commit();
       }
       else if (menuItem.getItemId() == R.id.nav_add_pharmacy) {
           Intent intent = new Intent(HomePageActivity.this, AddPharmacyActivity.class);
           startActivity(intent);
       }
       else if (menuItem.getItemId() == R.id.nav_lookup_med) {
           Toast.makeText(this, "Lookup Medicines!", Toast.LENGTH_SHORT).show();
       }
       else if (menuItem.getItemId() ==  R.id.nav_logout) {
           Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show();
           mAuth.signOut();
           Intent intent = new Intent(HomePageActivity.this, WelcomeActivity.class);
           startActivity(intent);
       }

       drawerLayout.close();
       return true;
    }

}
