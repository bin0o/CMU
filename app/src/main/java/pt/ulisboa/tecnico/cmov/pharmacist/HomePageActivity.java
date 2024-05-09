package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import pt.ulisboa.tecnico.cmov.pharmacist.fragments.MapFragment;

public class HomePageActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    private final int FINE_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }
}
