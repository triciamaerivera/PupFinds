package com.pupfinds.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import android.util.Log;

public class UploadLostItemActivity extends AppCompatActivity {

    private DatabaseReference lostItemsRef;
    private StorageReference storageRef;
    private Uri selectedImageUri;
    private Spinner spinnerCampus, spinnerBuilding, spinnerCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_lost_item);

        spinnerCampus = findViewById(R.id.spinnerLostCampus);
        spinnerBuilding = findViewById(R.id.spinnerLostBuilding);
        spinnerCategory = findViewById(R.id.spinnerLostCategory);
        
        // Initialize the lost_items reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        lostItemsRef = database.getReference("lost_items");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference("lost_item_images");

        EditText LostName = findViewById(R.id.LostItemName);
        EditText LostDescription = findViewById(R.id.LostItemDescription);
        EditText editTextLocation = findViewById(R.id.editTextLocation); // Added this line
        Spinner spinnerCampus = findViewById(R.id.spinnerLostCampus);
        Spinner spinnerBuilding = findViewById(R.id.spinnerLostBuilding); // Added this line
        ImageView itemImage = findViewById(R.id.itemImage);

        // Set a click listener for the image to open the gallery
        itemImage.setOnClickListener(view -> openGallery());

        // Set a click listener for the upload button
        Button uploadButton = findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(view -> {
            // Get the item details from UI elements (EditText, Spinner)
            String itemName = LostName.getText().toString().trim();
            String itemDescription = LostDescription.getText().toString().trim();
            String location = editTextLocation.getText().toString().trim();
            String campus = spinnerCampus.getSelectedItem().toString();
            String building = spinnerBuilding.getSelectedItem().toString();

            // Check if the itemName and image are not empty before uploading
            if (!itemName.isEmpty() && selectedImageUri != null) {
                // Generate a unique key for the new lost item
                String itemId = lostItemsRef.push().getKey();

                // Upload the image to Firebase Storage
                uploadImage(itemId, selectedImageUri, itemName, itemDescription);
            } else {
                // Handle the case where itemName or image is empty
                Toast.makeText(UploadLostItemActivity.this, "Item Name and Image are required", Toast.LENGTH_SHORT).show();
            }
        });
        

        // Populate the campus spinner
        ArrayAdapter<CharSequence> campusAdapter = ArrayAdapter.createFromResource(this,
                R.array.campus_array, android.R.layout.simple_spinner_item);
        campusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCampus.setAdapter(campusAdapter);

        // Set the onItemSelectedListener for the campus spinner
        spinnerCampus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get the selected campus
                String selectedCampus = parentView.getItemAtPosition(position).toString();

                // Dynamically populate the building spinner based on the selected campus
                populateBuildingSpinner(selectedCampus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing if nothing is selected
            }
        });

        // Populate the category spinner
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.category_array, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void populateBuildingSpinner(String selectedCampus) {
        int buildingArrayResourceId;

        // Determine the appropriate building array based on the selected campus
        switch (selectedCampus) {
            case "Mabini (Main)":
                buildingArrayResourceId = R.array.building_mabini;
                break;
            case "NDC Compound":
                buildingArrayResourceId = R.array.building_ndc;
                break;
            case "M.H. Del Pilar":
                buildingArrayResourceId = R.array.building_mhdp;
                break;
            default:
                // Handle the case where no campus is selected
                buildingArrayResourceId = R.array.empty_array;
        }

        // Populate the building spinner with the appropriate array
        ArrayAdapter<CharSequence> buildingAdapter = ArrayAdapter.createFromResource(this,
                buildingArrayResourceId, android.R.layout.simple_spinner_item);
        buildingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBuilding.setAdapter(buildingAdapter);
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

    private void uploadImage(String itemId, Uri imageUri, String itemName, String itemDescription) {
        StorageReference imageRef = storageRef.child(itemId + ".jpg");

        // Upload image to Firebase Storage
        UploadTask uploadTask = imageRef.putFile(imageUri);

        // Register observers to listen for when the upload is successful or if it fails
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // uri contains the download URL

                // Create a LostItem object with the details including the image URL
                LostItem lostItem = new LostItem(itemName, itemDescription, uri.toString());

                // Upload the lost item details to the "lost_items" node in the database
                lostItemsRef.child(itemId).setValue(lostItem);

                finish();
            }).addOnFailureListener(e -> {
                // Handle unsuccessful image upload (getDownloadUrl)
                Log.e("UploadLostItemActivity", "Error getting image download URL: " + e.getMessage());
                Toast.makeText(UploadLostItemActivity.this, "Error getting image download URL", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            // Handle unsuccessful image upload (putFile)
            Log.e("UploadLostItemActivity", "Error uploading image: " + e.getMessage());
            Toast.makeText(UploadLostItemActivity.this, "Error uploading image", Toast.LENGTH_SHORT).show();
        });
    }


}


