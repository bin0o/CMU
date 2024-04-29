package pt.ulisboa.tecnico.cmov.pharmacist;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap map;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        String tagusLocation = "Instituto Superior Técnico - Taguspark";
        List<Address> addresses = new ArrayList<>();

        Geocoder geocoder = new Geocoder(MainActivity.this);
        try {
            addresses = geocoder.getFromLocationName(tagusLocation, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Address tagusAddress = addresses.get(0);

        LatLng tagus = new LatLng(tagusAddress.getLatitude(), tagusAddress.getLongitude());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(tagus, 17));
    }
}
