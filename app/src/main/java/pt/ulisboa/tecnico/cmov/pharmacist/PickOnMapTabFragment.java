package pt.ulisboa.tecnico.cmov.pharmacist;

import static pt.ulisboa.tecnico.cmov.pharmacist.MapFragment.currentLocation;
import static pt.ulisboa.tecnico.cmov.pharmacist.MapFragment.tagusAddress;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class PickOnMapTabFragment extends Fragment {

    private View view;

    private MapView mapView;

    private GoogleMap map;

    public static String pickOnMapAddress;

    public PickOnMapTabFragment() {
        // Required empty public constructor
    }

    public static PickOnMapTabFragment newInstance() { return new PickOnMapTabFragment(); }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_pick_on_map_tab, container, false);

        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        mapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                map.getUiSettings().setZoomControlsEnabled(true);

                // If permission was granted, got to current location
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // Move map-related operations here
                    LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                }

                // Set a default position on map
                else {
                    // Move map-related operations here
                    LatLng latLng = new LatLng(tagusAddress.getLatitude(), tagusAddress.getLongitude());
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                }
                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {

                        // Do something when map is clicked
                        MarkerOptions markerOptions = new MarkerOptions();

                        markerOptions.position(latLng);

                        map.clear();

                        Geocoder geocoder;
                        List<Address> addresses;
                        geocoder = new Geocoder(getActivity());

                        try {
                            addresses = geocoder.getFromLocation(markerOptions.getPosition().latitude, markerOptions.getPosition().longitude, 1);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        pickOnMapAddress = addresses.get(0).getAddressLine(0);

                        map.addMarker(markerOptions);
                }
            });
        }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}