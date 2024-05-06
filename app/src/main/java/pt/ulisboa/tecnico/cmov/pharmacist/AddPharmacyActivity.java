package pt.ulisboa.tecnico.cmov.pharmacist;

import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

public class AddPharmacyActivity extends ComponentActivity {

    private final String TAG = "AddPharmacyActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pharmacy);
    }
}
