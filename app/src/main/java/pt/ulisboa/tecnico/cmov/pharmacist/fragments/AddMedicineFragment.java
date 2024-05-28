package pt.ulisboa.tecnico.cmov.pharmacist.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import pt.ulisboa.tecnico.cmov.pharmacist.R;

public class AddMedicineFragment extends DialogFragment {
    private final String TAG = "AddMedicineStock";

    private View view;

    public AddMedicineFragment() {
        // Required empty public constructor
    }

    public static AddMedicineFragment newInstance() {
        return new AddMedicineFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_medicine, container, false);

        return view;
    }
}
