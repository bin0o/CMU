package pt.ulisboa.tecnico.cmov.pharmacist;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.Manifest;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MapFragment extends Fragment {

    private final String TAG = "Map_Fragment";

    private final int FINE_PERMISSION_CODE = 1;

    View view;

    GoogleMap map;

    FusedLocationProviderClient client;

    Location currentLocation;

    public static Fragment newInstance() {
        return new MapFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_map, container, false);

        client = LocationServices.getFusedLocationProviderClient(getActivity());

        SupportMapFragment supportMapFragment = (SupportMapFragment)
            getChildFragmentManager().findFragmentById(R.id.map);


        supportMapFragment.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                map = googleMap;

                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
                    ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, FINE_PERMISSION_CODE);
                }


                map.setMyLocationEnabled(true);

                // Set up search query listener
                SearchView mapSearch = view.findViewById(R.id.mapSearch);
                mapSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        String location = mapSearch.getQuery().toString();
                        List<Address> addresses = new ArrayList<>();

                        // Use Geocoder to get location from name
                        Geocoder geocoder = new Geocoder(getActivity());
                        try {
                            addresses = geocoder.getFromLocationName(location, 1);
                        } catch (IOException e) {
                            Log.e(TAG, "Location not Found!");
                        }

                        if (!addresses.isEmpty()) {
                            Address address = addresses.get(0);

                            // Move camera to searched location
                            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                        } else {
                            Toast.makeText(getActivity(), "Location not found", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return false;
                    }
                });

                getLastLocation();
            }
        });

        return view;
    }


    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, FINE_PERMISSION_CODE);
        }

        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;

                    // Move map-related operations here
                    LatLng tagus = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(tagus, 17));
                } else {
                    // Handle case where location is null
                    Log.e(TAG, "getLastLocation: Location is null");
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case FINE_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation();
                    Log.i(TAG, "location fine permission granted");

                } else {
                    Toast.makeText(getActivity(), "No Permission to View Location. Please Allow!",Toast.LENGTH_SHORT).show();
                }
            }

        }
    }
}