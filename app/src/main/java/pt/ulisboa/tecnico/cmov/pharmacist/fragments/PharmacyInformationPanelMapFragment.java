package pt.ulisboa.tecnico.cmov.pharmacist.fragments;

import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.R;

public class PharmacyInformationPanelMapFragment extends DialogFragment {
    private final String TAG = "PharmacyInfoPanelMap";

    private View view;

    private MapView mapView;

    private GoogleMap map;

    private String pharmacyAddressString;

    public PharmacyInformationPanelMapFragment() {
        // Required empty public constructor
    }

    public static PharmacyInformationPanelMapFragment newInstance() { return new PharmacyInformationPanelMapFragment(); }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_info_panel_map, container, false);
        Bundle bundle = this.getArguments();
        pharmacyAddressString = bundle.getString("PharmacyAddress");

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
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                map.getUiSettings().setScrollGesturesEnabled(false);

                List<Address> addresses = new ArrayList<>();

                Geocoder geocoder = new Geocoder(getActivity());
                try {
                    addresses = geocoder.getFromLocationName(pharmacyAddressString, 1);
                } catch (IOException err) {
                    err.printStackTrace();
                }

                Address pharmacyAddress = addresses.get(0);
                LatLng latLng = new LatLng(pharmacyAddress.getLatitude(), pharmacyAddress.getLongitude());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                MarkerOptions markerOptions = new MarkerOptions().position(latLng);
                map.addMarker(markerOptions);

            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        Point size = new Point();
        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, (int) (size.y*.5));
        window.setGravity(Gravity.CENTER);
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
