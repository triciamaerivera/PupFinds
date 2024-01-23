package com.pupfinds.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UploadItemActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference foundItemsRef;
    private DatabaseReference lostItemsRef;
    private StorageReference storageRef;
    private Uri selectedImageUri;
    private Spinner spinnerCampus, spinnerCategory, spinnerBuilding;
    private EditText Name, Description, editTextLocation, date;
    private ImageView itemImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_item);

        // Initialize UI elements
        spinnerCampus = findViewById(R.id.spinnerCampus);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerBuilding = findViewById(R.id.spinnerBuilding); // Add spinnerBuilding

        Name = findViewById(R.id.ItemName);
        Description = findViewById(R.id.ItemDescription);
        editTextLocation = findViewById(R.id.editTextLocation);
        date = findViewById(R.id.editTextDate);
        itemImage = findViewById(R.id.itemImage);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        foundItemsRef = database.getReference("found_items");
        lostItemsRef = database.getReference("lost_items");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference lostItemsStorageRef = storage.getReference("lost_item_images");
        StorageReference foundItemsStorageRef = storage.getReference("found_item_images");


        // Add bottom navigation menu
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_add);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_dashboard) {
                startActivity(new Intent(getApplicationContext(), DashboardActivityFound.class));
                overridePendingTransition(R.anim.fade, R.anim.fade);
                finish();
                return true;

            } else if (item.getItemId() == R.id.bottom_returned) {
                // Handle Returned menu item click
                startActivity(new Intent(getApplicationContext(), ReturnedItemsActivity.class));
                overridePendingTransition(R.anim.fade, R.anim.fade);
                finish();
                return true;
            } else if (item.getItemId() == R.id.bottom_add) {
                // Handle Add menu item click
                startActivity(new Intent(getApplicationContext(), UploadItemActivity.class));
                overridePendingTransition(R.anim.fade, R.anim.fade);
                finish();
                return true;
            } else if (item.getItemId() == R.id.bottom_chat) {
                // Handle Chat menu item click
                startActivity(new Intent(getApplicationContext(), UserProfileActivity.class));
                overridePendingTransition(R.anim.fade, R.anim.fade);
                finish();
                return true;
            } else if (item.getItemId() == R.id.bottom_profile) {
                // Handle Profile menu item click
                startActivity(new Intent(getApplicationContext(), UserProfileActivity.class));
                overridePendingTransition(R.anim.fade, R.anim.fade);
                finish();
                return true;
            }

            return false;
        });

        // Set a click listener for the image to open the gallery
        itemImage.setOnClickListener(view -> openGallery());

        // Set a click listener for the upload button
        Button uploadItemButton = findViewById(R.id.uploadItemButton);
        uploadItemButton.setOnClickListener(view -> {
            String itemName = Name.getText().toString().trim();
            String itemDescription = Description.getText().toString().trim();
            String location = editTextLocation.getText().toString().trim();
            String campus = spinnerCampus.getSelectedItem().toString();
            String building = spinnerBuilding.getSelectedItem().toString(); // Update to use spinnerBuilding
            String category = spinnerCategory.getSelectedItem().toString();
            String uploadDate = date.getText().toString().trim();

            // Get the selected radio button ID
            int selectedRadioButtonId = ((RadioGroup) findViewById(R.id.radioGroup)).getCheckedRadioButtonId();

            if (!itemName.isEmpty() && !location.isEmpty() && selectedImageUri != null && selectedRadioButtonId != -1) {
                String itemId;

                if (selectedRadioButtonId == R.id.radioButtonLost) {
                    itemId = lostItemsRef.push().getKey();  // Use the reference for lost items
                    uploadImage(itemId, selectedImageUri, itemName, itemDescription, location, campus, building, category, uploadDate, lostItemsRef, lostItemsStorageRef);
                    Log.d("UploadItemActivity", "Item Uploaded in Lost Items");
                } else if (selectedRadioButtonId == R.id.radioButtonFound) {
                    itemId = foundItemsRef.push().getKey(); // Use the reference for found items
                    uploadImage(itemId, selectedImageUri, itemName, itemDescription, location, campus, building, category, uploadDate, foundItemsRef, foundItemsStorageRef);
                    Log.d("UploadItemActivity", "Item Uploaded in Found Items");
                }
            } else {
                Toast.makeText(UploadItemActivity.this, "Item Name, Location, and Image are required", Toast.LENGTH_SHORT).show();
            }
        });

        // Populate the campus spinner
        ArrayAdapter<CharSequence> campusAdapter = ArrayAdapter.createFromResource(this,
                R.array.campus_array, android.R.layout.simple_spinner_item);
        campusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCampus.setAdapter(campusAdapter);

        // Populate the category spinner
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.category_array, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Set an OnItemSelectedListener for the campus spinner
        spinnerCampus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                // Update the building spinner choices based on the selected campus
                updateBuildingSpinner(adapterView.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing here for now
            }
        });
    }

    // Method to update the building spinner choices based on the selected campus
    private void updateBuildingSpinner(String selectedCampus) {
        // Retrieve the appropriate building array based on the selected campus
        int buildingArrayResourceId = getBuildingArrayResourceId(selectedCampus);
        String[] buildingArray = getResources().getStringArray(buildingArrayResourceId);

        // Create and set the adapter for the building spinner
        ArrayAdapter<String> buildingAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, buildingArray);
        buildingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBuilding.setAdapter(buildingAdapter);
    }

    // Method to get the appropriate building array resource ID based on the selected campus
    private int getBuildingArrayResourceId(String selectedCampus) {
        switch (selectedCampus) {
            case "Mabini (Main)":
                return R.array.building_mabini;
            case "NDC Compound":
                return R.array.building_ndc;
            case "M.H. Del Pilar":
                return R.array.building_mhdp;
            default:
                return R.array.empty_array;
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            itemImage.setImageURI(selectedImageUri);
        }
    }

    private void uploadImage(String itemId, Uri imageUri, String itemName, String itemDescription, String location, String campus, String building, String category, String uploadDate, DatabaseReference itemsRef, StorageReference itemsStorageRef) {
        Log.d("UploadItemActivity", "uploadImage: Starting image upload");
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userUid = currentUser.getUid();

            StorageReference imageRef = itemsStorageRef.child(itemId + ".jpg");

            // Upload image to Firebase Storage
            UploadTask uploadTask = imageRef.putFile(imageUri);

            // Register observers to listen for when the upload is successful or if it fails
            uploadTask.addOnSuccessListener(taskSnapshot -> {

                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    if (itemsRef == foundItemsRef) {
                        Item foundItem = new Item(itemName, itemDescription, location, uri.toString(), userUid, category, uploadDate);
                        foundItem.setCampus(campus);
                        foundItem.setBuilding(building);
                        foundItem.setCategory(category);
                        // Upload the found item details to the "found_items" node in the database
                        itemsRef.child(itemId).setValue(foundItem);

                    } else {
                        Item lostItem = new Item(itemName, itemDescription, location, uri.toString(), userUid, category, uploadDate);
                        lostItem.setCampus(campus);
                        lostItem.setBuilding(building);
                        lostItem.setCategory(category);
                        // Upload the lost item details to the "lost_items" node in the database
                        itemsRef.child(itemId).setValue(lostItem);
                    }
                    // Navigate to DashboardActivity after successful upload
                    startActivity(new Intent(UploadItemActivity.this, DashboardActivityFound.class));
                    finish();

                    Log.d("UploadItemActivity", "uploadImage: Image uploaded successfully");

                }).addOnFailureListener(e -> {
                    Log.e("UploadItemActivity", "Error getting image download URL: " + e.getMessage());
                    Toast.makeText(UploadItemActivity.this, "Error getting image download URL", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Log.e("UploadItemActivity", "Error uploading image: " + e.getMessage());
                Toast.makeText(UploadItemActivity.this, "Error uploading image", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(UploadItemActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
}
