package pt.ulisboa.tecnico.cmov.pharmacist.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import pt.ulisboa.tecnico.cmov.pharmacist.DatabaseClasses.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.DatabaseClasses.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.PharmacyInformationPanelActivity;
import pt.ulisboa.tecnico.cmov.pharmacist.R;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private final String TAG = "Map_Fragment";

    private View view;

    private FirebaseAuth mAuth;

    private GoogleMap map;

    public FusedLocationProviderClient client;

    public static Location currentLocation;

    private String destinationAddress="";

    public static Address tagusAddress;

    private final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    private List<MarkerOptions> markers = new ArrayList<>();

    // Names of the favorite pharmacies of the logged in user
    private HashSet<String> favoritePharmacies = new HashSet<String>();

    private static final String KEY_CAMERA_POSITION = "camera_position";

    private CameraPosition mCameraPosition;

    private boolean isCameraPositionSaved = false;

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

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        client = LocationServices.getFusedLocationProviderClient(getActivity());

        if (savedInstanceState != null) {
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        SupportMapFragment supportMapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);

        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(this);
        }

        // Retrieve the destination address from arguments
        Log.d(TAG, "Arguments"+ String.valueOf(getArguments()));
        if (getArguments() != null) {
            String destinationAddress = getArguments().getString("destination_address");
            if (destinationAddress != null) {
                Log.d(TAG,"destination"+destinationAddress);
                this.destinationAddress = destinationAddress;
            }
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        map.getUiSettings().setZoomControlsEnabled(true);

        if (mCameraPosition != null) {
            map.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
            isCameraPositionSaved = true;
        } else {
            onLocationPermissionGranted();
            loadFavoritePharmacies();

            if (!destinationAddress.isEmpty()){
                try {
                    displayRoute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        setupMapInteraction();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) {
            mCameraPosition = map.getCameraPosition();
            isCameraPositionSaved = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavoritePharmacies();
        if (map != null && mCameraPosition != null) {
            map.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
            isCameraPositionSaved = true;
        }
    }

    public void setupMapInteraction() {
        // Set up clicking on markers
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                Intent intent = new Intent(getActivity(), PharmacyInformationPanelActivity.class);
                intent.putExtra("PharmacyName", marker.getTitle());
                startActivity(intent);
                return false;
            }
        });

        // Set up search query listener
        SearchView mapSearch = view.findViewById(R.id.mapSearch);
        mapSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    public void search(String query) {
        final String[] queryAddress = {query};

        Query queryMedicine = mDatabase.child("Medicines");
        queryMedicine.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot medicineSnapshot : dataSnapshot.getChildren()) {
                    String key = medicineSnapshot.getKey();
                    if (key != null && key.toLowerCase().contains(queryAddress[0].toLowerCase())) {
                        Map<String, Integer> pharmacies = new HashMap<>();
                        for (DataSnapshot pharmacySnapshot : medicineSnapshot.child("pharmacies").getChildren()) {
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
                Log.e(TAG, "Failed to retrieve medicine data: " + databaseError.getMessage());
            }
        });
    }

    public void loadFavoritePharmacies() {
        // Retrieve favorite pharmacies for the authenticated user
        Query query = mDatabase.child("UsersFavoritePharmacies").child(mAuth.getCurrentUser().getUid()).child("favs");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear previous favoritePharmacies
                favoritePharmacies.clear();

                // Iterate through the favorite pharmacies and add markers
                for (DataSnapshot favSnapshot : dataSnapshot.getChildren()) {
                    String favPharmacy = favSnapshot.getValue(String.class);
                    if (favPharmacy != null) {
                        favoritePharmacies.add(favPharmacy);
                    }
                }
                loadPharmaciesFromDatabase();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to retrieve favorite pharmacies: " + databaseError.getMessage());
            }
        });

    }

    public void loadPharmaciesFromDatabase() {
        // Retrieve pharmacy data from Firebase Realtime Database
        Query queryPharmacies = mDatabase.child("Pharmacy");
        queryPharmacies.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot pharmacySnapshot : dataSnapshot.getChildren()) {
                    Pharmacy pharmacy = pharmacySnapshot.getValue(Pharmacy.class);
                    if (pharmacy != null) {
                        String address = pharmacy.getAddress();
                        String name = pharmacy.getName();
                        LatLng latLng = geocodeAddress(address);

                        // Check if the pharmacy name is in the favorite pharmacies set
                        MarkerOptions markerOptions;
                        if (favoritePharmacies.contains(name)) {
                            // set marker color to golden
                            markerOptions = new MarkerOptions().position(latLng).title(name).icon(getMarkerIcon("#FFD700"));
                        } else {
                            // Otherwise, use default marker color
                            markerOptions = new MarkerOptions().position(latLng).title(name);
                        }
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

    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext.setQueryRateLimit(3).setApiKey(getString(R.string.api_key))
                .setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS);
    }

    private void displayRoute() throws IOException, InterruptedException, ApiException {

        LatLng start = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        LatLng destination = geocodeAddress(destinationAddress);

        new FetchDirectionsTask().execute(start, destination);
    }

    private class FetchDirectionsTask extends AsyncTask<LatLng, Void, DirectionsResult> {
        @Override
        protected DirectionsResult doInBackground(LatLng... params) {
            LatLng start = params[0];
            LatLng destination = params[1];

            GeoApiContext geoApiContext = getGeoContext();

            try {
                return DirectionsApi.newRequest(geoApiContext)
                        .origin(new com.google.maps.model.LatLng(start.latitude, start.longitude))
                        .destination(new com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                        .mode(TravelMode.DRIVING) // Specify travel mode (DRIVING, WALKING, BICYCLING)
                        .await();
            } catch (Exception e) {
                Log.e(TAG, "Failed to get directions: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(DirectionsResult result) {
            if (result != null && result.routes != null && result.routes.length > 0) {
                com.google.maps.model.LatLng[] path = result.routes[0].overviewPolyline.decodePath().toArray(new com.google.maps.model.LatLng[0]);
                PolylineOptions polylineOptions = new PolylineOptions();
                for (com.google.maps.model.LatLng latLng : path) {
                    polylineOptions.add(new LatLng(latLng.lat, latLng.lng));
                }
                polylineOptions.width(10).color(Color.BLUE).geodesic(true);
                map.addPolyline(polylineOptions);
            }
        }
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
            return;
        }

        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        customizeLocationButton();

        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null && !isCameraPositionSaved) {
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

    private void customizeLocationButton() {
        // Customize position of current location button
        View locationButton = ((View) view.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 0, 300);
    }

    private BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }
}