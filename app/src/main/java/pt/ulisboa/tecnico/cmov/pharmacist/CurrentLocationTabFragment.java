package pt.ulisboa.tecnico.cmov.pharmacist;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CurrentLocationTabFragment extends Fragment {

    public CurrentLocationTabFragment() {
        // Required empty public constructor
    }

    public static CurrentLocationTabFragment newInstance() { return new CurrentLocationTabFragment(); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_current_location_tab, container, false);
    }
}