package pt.ulisboa.tecnico.cmov.pharmacist.fragments;

import static pt.ulisboa.tecnico.cmov.pharmacist.fragments.MapFragment.currentLocation;

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
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.R;

public class CurrentLocationTabFragment extends Fragment {

    public CurrentLocationTabFragment() {
        // Required empty public constructor
    }

    public static CurrentLocationTabFragment newInstance() { return new CurrentLocationTabFragment(); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_current_location_tab, container, false);

        // If permission was granted, got to current location
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Move map-related operations here
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(getActivity());

            try {
                addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String address = addresses.get(0).getAddressLine(0);

            TextView addressTextView = view.findViewById(R.id.current_location_text);
            addressTextView.setText(address);
        }
        return view;
    }
}