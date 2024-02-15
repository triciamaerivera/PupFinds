package com.pupfinds.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.google.firebase.storage.StorageException;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;
    private StorageReference profilePicStorageRef;

    private Uri previousImageUri;
    private String userUid;
    private CircleImageView profilePictureImageView;
    private CircleImageView ButtonEditProfilePic;
    private TextView displayNameTextView;
    private TextView programTextView;

    private Button buttonApply;

    private Button buttonDiscard;

    private Button buttonLogOut;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference(); // Add this line to initialize storageReference
        profilePicStorageRef = storageReference.child("profile_images"); // Initialize profilePicStorageRef


        // Retrieve data
        mAuth = FirebaseAuth.getInstance();
        String userUid = mAuth.getUid();
        fetchUserProfile(userUid);

        profilePictureImageView = findViewById(R.id.imageViewProfilePic);
        ButtonEditProfilePic = findViewById(R.id.ButtonEditProfilePic);
        ButtonEditProfilePic.setOnClickListener(v -> openFileChooser());

        buttonApply = findViewById(R.id.buttonApply);
        buttonDiscard = findViewById(R.id.buttonDiscard);
        buttonLogOut = findViewById(R.id.buttonLogOut);

        buttonLogOut.setOnClickListener(v -> logoutAccount());

        // add bottom navigation menu
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_dashboard) {
                startActivity(new Intent(getApplicationContext(), DashboardActivityLost.class));
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
                startActivity(new Intent(getApplicationContext(), ChatsActivity.class));
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
}


    private void fetchUserProfile(String userUid) {
        DatabaseReference userProfileRef = FirebaseDatabase.getInstance().getReference("users").child(userUid);

        userProfileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("UserProfile", "onDataChange triggered");
                if (dataSnapshot.exists()) {
                    UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                    if (userProfile != null) {
                        // Add the displayName field to the UserProfile object
                        userProfile.setDisplayName(userProfile.getFirstName() + " " + userProfile.getLastName());
                        // Update the user profile information in the views
                        updateUserProfileViews(userProfile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // To-do: error handling
            }
        });
    }

    private void updateUserProfileViews(UserProfile userProfile) {
        // Find views for user profile information
        ImageView userProfilePhotoImageView = findViewById(R.id.imageViewProfilePic);
        TextView userProgramTextView = findViewById(R.id.ProgramTextView);
        TextView userDisplayNameTextView = findViewById(R.id.NameTextView);

        // save the current profile picture, for discard button
        if (previousImageUri != null){
            previousImageUri = Uri.parse(userProfile.getProfileImageUrl());
        }

        if (userProfile.getProfileImageUrl() != null && !userProfile.getProfileImageUrl().isEmpty()) {
            Picasso.get().load(userProfile.getProfileImageUrl()).into(userProfilePhotoImageView);
        }
        userProgramTextView.setText(userProfile.getProgram());
        userDisplayNameTextView.setText(userProfile.getDisplayName());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profilePictureImageView.setImageURI(imageUri);
        }

        buttonApply.setVisibility(View.VISIBLE);
        buttonDiscard.setVisibility(View.VISIBLE);

        buttonApply.setOnClickListener(v -> uploadImageToFirebase());
        buttonDiscard.setOnClickListener(v -> discardChanges());
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            // Storage reference
            String userId = mAuth.getCurrentUser().getUid();
            StorageReference fileRef = profilePicStorageRef.child(userId + "_profile.jpg"); // Corrected this line

            // Upload image to Firebase Storage
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the download URL of the uploaded image
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Update the user profile in the Realtime Database with the new image URL
                            updateUserProfileImage(uri.toString());
                        }).addOnFailureListener(e -> {
                            // Handle failure to get the download URL
                            Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure to upload the image
                        Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

            buttonApply.setVisibility(View.GONE);
            buttonDiscard.setVisibility(View.GONE);
        }
    }

    private void updateUserProfileImage(String imageUrl) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

            // Update the user's profile image URL
            userRef.child("profileImageUrl").setValue(imageUrl)
                    .addOnSuccessListener(aVoid -> {
                        // Image URL updated successfully
                        Toast.makeText(UserProfileActivity.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure to update the image URL
                        Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void discardChanges() {
        // Check if there is a previous image URI
        if (previousImageUri != null) {
            // Revert the image in the profilePictureImageView using Picasso
            Picasso.get().load(previousImageUri).into(profilePictureImageView);
            // Clear the current image URI and hide the buttons
            imageUri = null;

        }
        else{
            profilePictureImageView.setImageResource(R.drawable.reg_profile);
        }
        buttonApply.setVisibility(View.GONE);
        buttonDiscard.setVisibility(View.GONE);

    }

    private void logoutAccount() {
        // Log out the current user
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(UserProfileActivity.this, LoginActivity.class));
        finish();
    }

}

