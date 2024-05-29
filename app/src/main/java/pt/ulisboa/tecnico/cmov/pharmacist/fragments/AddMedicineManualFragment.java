package pt.ulisboa.tecnico.cmov.pharmacist.fragments;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.fragment.app.DialogFragment;

import pt.ulisboa.tecnico.cmov.pharmacist.R;

public class AddMedicineManualFragment extends DialogFragment {
    private final String TAG = "AddMedicineStockManual";

    private View view;

    private String pharmacyAddressString;

    public AddMedicineManualFragment() {
        // Required empty public constructor
    }

    public static AddMedicineManualFragment newInstance() {
        return new AddMedicineManualFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_medicine_manual, container, false);
        Bundle bundle = this.getArguments();
        pharmacyAddressString = bundle.getString("PharmacyAddress");

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        Point size = new Point();
        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);
        window.setLayout(size.x, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
    }
}
