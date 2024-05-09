package pt.ulisboa.tecnico.cmov.pharmacist.fragments;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.pharmacist.DatabaseClasses.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.DatabaseClasses.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.R;

public class MapFragment extends Fragment {

    private final String TAG = "Map_Fragment";

    private final int FINE_PERMISSION_CODE = 1;

    private View view;

    private GoogleMap map;

    public FusedLocationProviderClient client;

    public static Location currentLocation;

    public static Address tagusAddress;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    private List<MarkerOptions> markers = new ArrayList<>();

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
                    map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
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
                                    String name = pharmacy.getName();
                                    LatLng latLng = geocodeAddress(address);

                                    MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(name);
                                    markers.add(markerOptions);
                                    map.addMarker(markerOptions);
                                    Log.d(TAG, "Pharmacy: " + name + ", Address: " + address);
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

                            final String[] queryAddress = {query};

                            Query queryMedicine = mDatabase.child("Medicines");
                            queryMedicine.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    for (DataSnapshot medicineSnapshot : dataSnapshot.getChildren()) {
                                        String key = medicineSnapshot.getKey();
                                        if (key != null && key.contains(queryAddress[0])) {
                                            Map<String, Integer> pharmacies = new HashMap<>();
                                            for (DataSnapshot pharmacySnapshot : medicineSnapshot.getChildren()) {
                                                String pharmacyName = pharmacySnapshot.getKey();
                                                Integer quantity = pharmacySnapshot.getValue(Integer.class);
                                                if (pharmacyName != null && quantity != null) {
                                                    pharmacies.put(pharmacyName, quantity);
                                                }
                                            }
                                            Medicine medicine = new Medicine(pharmacies);
                                            Log.d(TAG, "Medicine: " + key + ", Pharmacies: " + medicine.getPharmacyNames());

                                            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                                    ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                // Get the first pharmacy name because the user has not granted permission for location
                                                queryAddress[0] = medicine.getPharmacyNames().get(0);
                                                Log.d(TAG, "First Farmacy: " + queryAddress[0]);
                                            }
                                            else {
                                                queryAddress[0] = findClosestAddressToCurrentLocation(medicine.getPharmacyNames());
                                                Log.d(TAG, "Closest Pharmacy: " + queryAddress[0]);
                                            }
                                        }
                                    }

                                    // Iterate through markers and check if their names match the search query
                                    for (MarkerOptions marker : markers) {
                                        if (marker.getTitle().toLowerCase().contains(queryAddress[0].toLowerCase())) {
                                            // Move camera to the marker's position
                                            map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                                            return;
                                        }
                                    }

                                    LatLng latLng = geocodeAddress(query);
                                    if (latLng != null) {
                                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                                    }

                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e(TAG, "Failed to retrieve pharmacy data: " + databaseError.getMessage());
                                }
                            });

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

    private LatLng geocodeAddress(String address) {
        Geocoder geocoder = new Geocoder(getActivity());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (!addresses.isEmpty()) {
                Address location = addresses.get(0);
                return new LatLng(location.getLatitude(), location.getLongitude());
            } else {
                Log.e(TAG, "Address not found: " + address);
                Toast.makeText(getActivity(), "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoding failed: " + e.getMessage());
        }
        return null;
    }

    private String findClosestAddressToCurrentLocation(List<String> addressList) {

        double minDistance = Double.MAX_VALUE;
        String closestAddress = "";

        for (String address : addressList) {
            for (MarkerOptions marker : markers) {
                if (marker.getTitle().toLowerCase().contains(address.toLowerCase())) {
                    Location addressLocation = new Location("");
                    addressLocation.setLatitude(marker.getPosition().latitude);
                    addressLocation.setLongitude(marker.getPosition().longitude);

                    float distance = currentLocation.distanceTo(addressLocation);
                    Log.d(TAG, "Distance to " + address + ": " + distance);

                    // Check if this address is closer than the previous closest address
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestAddress = address;
                    }
                }
            }


        }
        return closestAddress;
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