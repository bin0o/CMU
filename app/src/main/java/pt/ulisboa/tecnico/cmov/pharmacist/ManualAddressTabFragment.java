package pt.ulisboa.tecnico.cmov.pharmacist;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;

public class ManualAddressTabFragment extends Fragment {

    public ManualAddressTabFragment() {
        // Required empty public constructor
    }

    public static ManualAddressTabFragment newInstance() { return new ManualAddressTabFragment(); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manual_address_tab, container, false);
    }
}