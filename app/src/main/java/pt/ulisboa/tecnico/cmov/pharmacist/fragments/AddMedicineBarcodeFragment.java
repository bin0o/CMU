package pt.ulisboa.tecnico.cmov.pharmacist.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.pharmacist.AddPharmacyActivity;
import pt.ulisboa.tecnico.cmov.pharmacist.DatabaseClasses.Medicine;
import pt.ulisboa.tecnico.cmov.pharmacist.DatabaseClasses.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.ScanBarcodeActivity;

public class AddMedicineBarcodeFragment extends DialogFragment {
    private final String TAG = "AddMedicineStockBarcode";

    private View view;

    private String pharmacyName;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    public AddMedicineBarcodeFragment() {
        // Required empty public constructor
    }

    public static AddMedicineBarcodeFragment newInstance() {
        return new AddMedicineBarcodeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_medicine_barcode, container, false);
        Bundle bundle = this.getArguments();
        pharmacyName = bundle.getString("PharmacyName");

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        Button closeButton = view.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button scanButton = view.findViewById(R.id.scan_barcode_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan();
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
        window.setLayout(size.x, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
    }

    private void scan() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Align the barcode to scan it");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(ScanBarcodeActivity.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            ConstraintLayout scanSuccessLayout = view.findViewById(R.id.scan_success_layout);
            View successView = LayoutInflater.from(getActivity()).inflate(R.layout.scan_success_dialog, scanSuccessLayout);
            View unsuccessfulView = LayoutInflater.from(getActivity()).inflate(R.layout.scan_unsuccess_dialog, scanSuccessLayout);

            String medicineName = result.getContents();
            Log.d(TAG, medicineName);
            Query queryMedicines = mDatabase.child("Medicines");
            queryMedicines.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean exists = false;
                    for (DataSnapshot medicineSnapshot : snapshot.getChildren()) {
                        String name = medicineSnapshot.getKey();
                        Log.d(TAG, "Medicine name found: " + name);

                        if (name.equalsIgnoreCase(medicineName)) {
                            exists = true;
                            break;
                        }
                    }

                    if (exists) {
                        Button successButton = successView.findViewById(R.id.done_button);

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setView(successView);

                        final AlertDialog alertDialog = builder.create();

                        successButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();
                            }
                        });

                        if (alertDialog.getWindow() != null) {
                            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                        }

                        alertDialog.show();

                        Button addMedicineButton = view.findViewById(R.id.add_medicine_button);
                        addMedicineButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                EditText quantity = view.findViewById(R.id.medicine_quantity);
                                Integer stock = Integer.parseInt(quantity.getText().toString());

                                if (TextUtils.isEmpty(quantity.getText().toString())) {
                                    Toast.makeText(getActivity(), "Please enter the quantity to add!", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                Query queryMedicinePharmacy = mDatabase.child("Medicines").child(medicineName).child("pharmacies");
                                queryMedicinePharmacy.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Map<String, Integer> pharmacies = new HashMap<>();
                                        int stockTemp = stock;

                                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                            String pharmacyNameTemp = childSnapshot.getKey();
                                            Integer quantity = childSnapshot.getValue(Integer.class);

                                            if (pharmacyNameTemp.equals(pharmacyName)) {
                                                stockTemp = quantity + stock;
                                            }
                                            pharmacies.put(pharmacyNameTemp, quantity);
                                        }

                                        Log.d(TAG, "Pharmacies: " + pharmacies);
                                        pharmacies.put(pharmacyName, stockTemp);

                                        mDatabase.child("Medicines").child(medicineName).child("pharmacies").setValue(pharmacies);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                Query queryPharmacy = mDatabase.child("PharmacyMedicines").child(pharmacyName);
                                queryPharmacy.addListenerForSingleValueEvent( new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Map<String, Integer> medicines = new HashMap<>();
                                        int stockTemp = stock;

                                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                            String medicineNameTemp = childSnapshot.getKey();
                                            Integer quantity = childSnapshot.getValue(Integer.class);

                                            if (medicineNameTemp.equals(medicineName)) {
                                                stockTemp = quantity + stock;
                                            }
                                            medicines.put(medicineNameTemp, quantity);
                                        }
                                        Log.d(TAG, "Medicines: " + medicines);
                                        medicines.put(medicineName, stockTemp);

                                        mDatabase.child("PharmacyMedicines").child(pharmacyName).setValue(medicines);

                                        Bundle result = new Bundle();

                                        result.putBoolean("added", true);

                                        getParentFragmentManager().setFragmentResult("added",result);

                                        dismiss();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                        });

                    } else {
                        Button unsuccessfulButton = unsuccessfulView.findViewById(R.id.done_button);

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setView(unsuccessfulView);

                        final AlertDialog alertDialog = builder.create();

                        unsuccessfulButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();
                                Bundle bundle = new Bundle();
                                bundle.putString("PharmacyName", pharmacyName);
                                AddMedicineManualFragment fragment = AddMedicineManualFragment.newInstance();
                                fragment.setArguments(bundle);
                                fragment.show(getActivity().getSupportFragmentManager(), "AddMedicineStock");
                                dismiss();
                            }
                        });

                        if (alertDialog.getWindow() != null) {
                            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                        }

                        alertDialog.show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    });
}
