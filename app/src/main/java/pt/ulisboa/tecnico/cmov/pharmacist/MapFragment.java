package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.Manifest;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    private final String TAG = "Map_Fragment";

    private final int FINE_PERMISSION_CODE = 1;

    private View view;

    private GoogleMap map;

    public FusedLocationProviderClient client;

    public static Location currentLocation;

    public static Address tagusAddress;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    public MapFragment() {
        // Required empty public constructor
    }

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


        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(new OnMapReadyCallback() {

                @Override
                public void onMapReady(@NonNull GoogleMap googleMap) {
                    // Set map and zoom controls
                    map = googleMap;
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    map.getUiSettings().setZoomControlsEnabled(true);

                    // Get permission for current location
                    onLocationPermissionGranted();

                    // Retrieve pharmacy data from Firebase Realtime Database
                    Query query = mDatabase.child("Pharmacy");
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot pharmacySnapshot : dataSnapshot.getChildren()) {
                                Pharmacy pharmacy = pharmacySnapshot.getValue(Pharmacy.class);
                                if (pharmacy != null) {
                                    String address = pharmacy.getAddress();
                                    geocodeAddress(address);
                                    Log.d(TAG, "Pharmacy: " + pharmacy.getName());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Failed to retrieve pharmacy data: " + databaseError.getMessage());
                        }
                    });

                    // If permission was not granted, place a default position on map
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        String tagusLocation = "Instituto Superior Técnico - Taguspark";
                        List<Address> addresses = new ArrayList<>();

                        Geocoder geocoder = new Geocoder(getActivity());
                        try {
                            addresses = geocoder.getFromLocationName(tagusLocation, 1);
                        } catch (IOException err) {
                            err.printStackTrace();
                        }

                        tagusAddress = addresses.get(0);

                        LatLng tagus = new LatLng(tagusAddress.getLatitude(), tagusAddress.getLongitude());
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(tagus, 17));
                    }

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

                            if (addresses != null) {
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
                }
            });
        }

        return view;
    }

    private void geocodeAddress(String address) {
        Geocoder geocoder = new Geocoder(getActivity());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (!addresses.isEmpty()) {
                Address location = addresses.get(0);
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                map.addMarker(new MarkerOptions().position(latLng).title(address));
            } else {
                Log.e(TAG, "Address not found: " + address);
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoding failed: " + e.getMessage());
        }
    }

    private void onLocationPermissionGranted() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }

        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        // Customize position of current location button
        View locationButton = ((View) view.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 0, 300);

        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;

                    // Move map-related operations here
                    LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                } else {
                    Log.e(TAG, "getLastLocation: Location is null");
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onLocationPermissionGranted();
                Log.i(TAG, "Location fine permission granted");

            } else {
                Toast.makeText(getActivity(), "No Permission to View Location. Please Allow!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}