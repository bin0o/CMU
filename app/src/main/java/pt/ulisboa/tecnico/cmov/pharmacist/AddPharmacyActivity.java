package pt.ulisboa.tecnico.cmov.pharmacist;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddPharmacyActivity extends AppCompatActivity {

    public static final int CAMERA_PERMISSION_CODE = 101;
    private static final int REQUEST_IMAGE_CAPTURE_CODE = 102;
    private DatabaseReference mDatabase;

    private TabLayout addressTabLayout;

    private ViewPager2 viewPager2;

    private ImageView photoView;

    private final String TAG = "AddPharmacyActivity";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pharmacy);

        // Sets the customized toolbar in the view
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Sets the tabs for the different options to add an address
        addressTabLayout = findViewById(R.id.address_options_tab);
        viewPager2 = findViewById(R.id.address_options_viewpager);
        ViewPagerAdpater viewPagerAdpater = new ViewPagerAdpater(this);

        viewPager2.setAdapter(viewPagerAdpater);
        addressTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                addressTabLayout.getTabAt(position).select();
            }
        });

        // Gets the database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Go back to Home page
        Button back = findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Going back to Home Page");
                Intent intent = new Intent(AddPharmacyActivity.this, HomePageActivity.class);
                startActivity(intent);
            }
        });

        // Add a photo
        Button photoButton = findViewById(R.id.take_photo_button);
        photoView = findViewById(R.id.photo);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askCameraPermissions();
            }
        });


        // Adding a new Pharmacy
        Button addPharmacyButton = findViewById(R.id.add_pharmacy_button);
        addPharmacyButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 EditText pharmacyNameEditText = findViewById(R.id.name);
                 String pharmacyName = pharmacyNameEditText.getText().toString();

                 if (TextUtils.isEmpty(pharmacyName)){
                     Toast.makeText(AddPharmacyActivity.this, "Please enter the name of the pharmacy!", Toast.LENGTH_SHORT).show();
                     return;
                 }

                 String address = "";

                 int tabSelected = addressTabLayout.getSelectedTabPosition();

                 if (tabSelected == 0) {
                    address = PickOnMapTabFragment.pickOnMapAddress;
                 }
                 else if (tabSelected == 1) {
                    TextView pharmacyAddressTextView = findViewById(R.id.current_location_text);
                    address = pharmacyAddressTextView.getText().toString();
                 }
                 else if (tabSelected == 2) {
                     EditText pharmacyAddressEditText = findViewById(R.id.manual_address);
                     address = pharmacyAddressEditText.getText().toString();
                 }

                 if (TextUtils.isEmpty(address)){
                     Toast.makeText(AddPharmacyActivity.this, "Please enter the address of the pharmacy!", Toast.LENGTH_SHORT).show();
                     return;
                 }

                 // Get the drawable from the photoView
                 Drawable drawable = photoView.getDrawable();
                 if (drawable == null) {
                     Toast.makeText(AddPharmacyActivity.this, "Please capture a photo first!", Toast.LENGTH_SHORT).show();
                     return;
                 }

                 // Convert the drawable to a bitmap
                 BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                 Bitmap bitmap = bitmapDrawable.getBitmap();

                // Convert the bitmap to a byte array
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                 byte[] imageData = baos.toByteArray();
                 String imageBase64 = Base64.encodeToString(imageData, Base64.DEFAULT);

                 // Create a new Pharmacy object
                 Pharmacy pharmacy = new Pharmacy(pharmacyName, address, imageBase64);

                // Push the Pharmacy object to the "Pharmacy" node
                 mDatabase.child("Pharmacy").push().setValue(pharmacy)
                         .addOnCompleteListener(new OnCompleteListener<Void>() {
                             @Override
                             public void onComplete(@NonNull Task<Void> task) {
                                 if (task.isSuccessful()) {
                                     Log.d(TAG, "addPharmacy: Pharmacy added successfully");
                                 } else {
                                     Log.e(TAG, "addPharmacy: Failed to add pharmacy", task.getException());
                                 }
                             }
                         });

                 Toast.makeText(AddPharmacyActivity.this, "Pharmacy added successfully", Toast.LENGTH_SHORT).show();

                 Log.i(TAG, "Going back to Home Page");
                 Intent intent = new Intent(AddPharmacyActivity.this, HomePageActivity.class);
                 startActivity(intent);
             }
         });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    private void askCameraPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},  CAMERA_PERMISSION_CODE);
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
        if (requestCode ==  CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to take photo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle image capture result
        if (requestCode == REQUEST_IMAGE_CAPTURE_CODE && resultCode == RESULT_OK && data != null) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            photoView.setImageBitmap(image);
        }
    }

}
