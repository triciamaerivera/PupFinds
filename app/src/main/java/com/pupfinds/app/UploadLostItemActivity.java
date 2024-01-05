package com.pupfinds.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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
    private Spinner spinnerCampus, spinnerCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_lost_item);

        // Initialize the lost_items reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        lostItemsRef = database.getReference("lost_items");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference("lost_item_images");

        EditText lostName = findViewById(R.id.lostItemName);
        EditText lostDescription = findViewById(R.id.lostItemDescription);
        ImageView itemImage = findViewById(R.id.itemImage);
        Button uploadButton = findViewById(R.id.uploadButton);

        // Set a click listener for the image to open the gallery
        itemImage.setOnClickListener(view -> openGallery());

        // Set a click listener for the upload button
        uploadButton.setOnClickListener(view -> {
            // Get the item details from your UI elements (EditText)
            String itemName = lostName.getText().toString().trim();
            String itemDescription = lostDescription.getText().toString().trim();

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

        spinnerCampus = findViewById(R.id.spinnerLostCampus);
        spinnerCategory = findViewById(R.id.spinnerLostCategory);

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


