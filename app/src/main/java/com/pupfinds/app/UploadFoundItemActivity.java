package com.pupfinds.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;


public class  UploadFoundItemActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference foundItemsRef;
    private StorageReference storageRef;
    private Uri selectedImageUri;
    private Spinner spinnerCampus, spinnerCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_found_item);

        spinnerCampus = findViewById(R.id.spinnerFoundCampus);
        spinnerCategory = findViewById(R.id.spinnerFoundCategory);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        foundItemsRef = database.getReference("found_items");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference("found_item_images");

        EditText foundName = findViewById(R.id.foundItemName);
        EditText foundDescription = findViewById(R.id.foundItemDescription);
        EditText editTextLocation = findViewById(R.id.editTextLocation);// Added this line
        EditText date = findViewById(R.id.editTextDate);
        Spinner spinnerCampus = findViewById(R.id.spinnerFoundCampus);
        Spinner spinnerBuilding = findViewById(R.id.spinnerFoundBuilding); // Added this line
        ImageView itemImage = findViewById(R.id.itemImage);

        // Set a click listener for the image to open the gallery
        itemImage.setOnClickListener(view -> openGallery());

        // Set a click listener for the upload button
        Button uploadButton = findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(view -> {
            // Get the item details from your UI elements (EditText, Spinner)
            String itemName = foundName.getText().toString().trim();
            String itemDescription = foundDescription.getText().toString().trim();
            String location = editTextLocation.getText().toString().trim();
            String campus = spinnerCampus.getSelectedItem().toString();
            String building = spinnerBuilding.getSelectedItem().toString();
            String category = spinnerCategory.getSelectedItem().toString();
            String uploadDate =  date.getText().toString().trim();

            // Check if the itemName, location, and image are not empty before uploading
            if (!itemName.isEmpty() && !location.isEmpty() && selectedImageUri != null) {
                // Generate a unique key for the new found item
                String itemId = foundItemsRef.push().getKey();

                // Upload the image to Firebase Storage and item details to Realtime Database
                uploadImage(itemId, selectedImageUri, itemName, itemDescription, location, campus, building, category, uploadDate);
            } else {
                // Handle the case where itemName, location, or image is empty
                Toast.makeText(UploadFoundItemActivity.this, "Item Name, Location, and Image are required", Toast.LENGTH_SHORT).show();
            }
        });

        // Populate the campus spinner
        ArrayAdapter<CharSequence> campusAdapter = ArrayAdapter.createFromResource(this,
                R.array.campus_array, android.R.layout.simple_spinner_item);
        campusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCampus.setAdapter(campusAdapter);

        // Populate the building spinner
        ArrayAdapter<CharSequence> buildingAdapter = ArrayAdapter.createFromResource(this,
                R.array.building_array, android.R.layout.simple_spinner_item);
        buildingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBuilding.setAdapter(buildingAdapter);

        // Populate the category spinner
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.category_array, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);


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
            ImageView itemImage = findViewById(R.id.itemImage);
            itemImage.setImageURI(selectedImageUri);
        }
    }

    private void uploadImage(String itemId, Uri imageUri, String itemName, String itemDescription, String location, String campus, String building, String category, String uploadDate) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userUid = currentUser.getUid();

            StorageReference imageRef = storageRef.child(itemId + ".jpg");

            // Upload image to Firebase Storage
            UploadTask uploadTask = imageRef.putFile(imageUri);

            // Register observers to listen for when the upload is successful or if it fails
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {



                    // FoundItem object
                    FoundItem foundItem = new FoundItem(itemName, itemDescription, location, uri.toString(), userUid, category, uploadDate);
                    foundItem.setCampus(campus);
                    foundItem.setBuilding(building);
                    foundItem.setCategory(category);


                    // Upload the found item details to the "found_items" node in the database
                    foundItemsRef.child(itemId).setValue(foundItem);

                    finish();
                }).addOnFailureListener(e -> {
                    // Handle unsuccessful image upload (getDownloadUrl)
                    Log.e("UploadFoundItemActivity", "Error getting image download URL: " + e.getMessage());
                    Toast.makeText(UploadFoundItemActivity.this, "Error getting image download URL", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                // Handle unsuccessful image upload (putFile)
                Log.e("UploadFoundItemActivity", "Error uploading image: " + e.getMessage());
                Toast.makeText(UploadFoundItemActivity.this, "Error uploading image", Toast.LENGTH_SHORT).show();
            });
        } else {
            // Handle the case where the user is not authenticated
            Toast.makeText(UploadFoundItemActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

}

