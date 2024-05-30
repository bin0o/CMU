package pt.ulisboa.tecnico.cmov.pharmacist.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.pharmacist.AddPharmacyActivity;
import pt.ulisboa.tecnico.cmov.pharmacist.R;

public class AddMedicineManualFragment extends DialogFragment {
    private final String TAG = "AddMedicineManualFragment";

    public static final int CAMERA_PERMISSION_CODE = 101;
    private static final int REQUEST_IMAGE_CAPTURE_CODE = 102;

    private View view;

    private String pharmacyNameString;

    private DatabaseReference mDatabase;

    private StorageReference mStorageRef;

    private ImageView photoView;

    Map<String, Integer> pharmacies = new HashMap<>();

    Map<String, Integer> medicines = new HashMap<>();

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
        pharmacyNameString = bundle.getString("PharmacyName");

        Button closeButton = view.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        // Gets the database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Gets the storage
        mStorageRef = FirebaseStorage.getInstance().getReference();

        // Add a photo
        Button photoButton = view.findViewById(R.id.camera_photo);
        photoView = view.findViewById(R.id.box_photo);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askCameraPermissions();
            }
        });

        Button addMedicineButton = view.findViewById(R.id.add_medicine_button);

        addMedicineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView name = view.findViewById(R.id.medicine_name);
                String medicineName = name.getText().toString();

                TextView quantity = view.findViewById(R.id.medicine_quantity);
                String medicineQuantity = quantity.getText().toString();

                TextView purpose = view.findViewById(R.id.medicine_purpose);
                String medicinePurpose = purpose.getText().toString();

                Log.d(TAG, "Medicine quantity: " + medicineQuantity);

                if (TextUtils.isEmpty(medicineName)){
                    Toast.makeText(getActivity(), "Please enter the name of the medicine!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(medicineQuantity)){
                    Toast.makeText(getActivity(), "Please enter the quantity of the medicine!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(medicinePurpose)){
                    Toast.makeText(getActivity(), "Please enter the purpose of the medicine!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get the drawable from the photoView
                Drawable drawable = photoView.getDrawable();
                if (drawable == null) {
                    Toast.makeText(getActivity(), "Please capture a photo first!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Convert the drawable to a bitmap
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                Bitmap bitmap = bitmapDrawable.getBitmap();

                // Convert the bitmap to a byte array
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageData = baos.toByteArray();

                // Upload image to Firebase Storage
                // it also saves the pharmacy to database
                uploadImageToStorage(medicineName, Integer.parseInt(medicineQuantity), medicinePurpose, imageData);
            }

        });
        return view;
    }

    private void uploadImageToStorage(String medicineName, Integer quantity, String purpose, byte[] imageData) {
        final StorageReference imageRef = mStorageRef.child("medicine_images/" + medicineName + ".jpg");
        UploadTask uploadTask = imageRef.putBytes(imageData);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            saveMedicineToDatabase(medicineName, quantity, purpose, pharmacyNameString, downloadUri.toString());
                        } else {
                            Log.e(TAG, "Failed to get download URL", task.getException());
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to upload image", e);
            }
        });
    }

    private void saveMedicineToDatabase(String medicineName, Integer quantity, String purpose, String pharmacyName, String imageUrl) {
        mDatabase.child("Medicines").child(medicineName).child("imageUrl").setValue(imageUrl);
        mDatabase.child("Medicines").child(medicineName).child("purpose").setValue(purpose);

        Query query = mDatabase.child("Medicines").child(medicineName).child("pharmacies");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pharmacies.clear();

                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String pharmacyName = childSnapshot.getKey();
                    Integer quantity = childSnapshot.getValue(Integer.class);

                    pharmacies.put(pharmacyName, quantity);
                }

                Log.d(TAG, "Pharmacies: " + pharmacies);
                pharmacies.put(pharmacyName, quantity);

                mDatabase.child("Medicines").child(medicineName).child("pharmacies").setValue(pharmacies);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Query query1 = mDatabase.child("PharmacyMedicines").child(pharmacyName);
        query1.addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                medicines.clear();

                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String medicineName = childSnapshot.getKey();
                    Integer quantity = childSnapshot.getValue(Integer.class);

                    medicines.put(medicineName, quantity);
                }
                Log.d(TAG, "Medicines: " + medicines);
                medicines.put(medicineName, quantity);

                mDatabase.child("PharmacyMedicines").child(pharmacyName).setValue(medicines);

                Bundle result = new Bundle();

                result.putBoolean("added", true);

                getParentFragmentManager().setFragmentResult("added",result);

                // Dismiss the dialog
                dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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

    private void askCameraPermissions() {
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CAMERA},  CAMERA_PERMISSION_CODE);
        }else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode ==  CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getActivity(), "Camera permission is required to take photo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle image capture result
        if (requestCode == REQUEST_IMAGE_CAPTURE_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            photoView.setImageBitmap(image);
        }
    }
}
