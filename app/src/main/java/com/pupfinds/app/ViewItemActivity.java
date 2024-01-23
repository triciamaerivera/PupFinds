package com.pupfinds.app;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ViewItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_item);

        // Retrieve data passed from DashboardActivity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String itemName = extras.getString("ITEM_NAME", "");
            String itemDescription = extras.getString("ITEM_DESCRIPTION", "");
            String itemLocation = extras.getString("ITEM_LOCATION", "");
            String imageUrl = extras.getString("ITEM_IMAGE_URL", "");

            String userUid = extras.getString("USER_UID", "");
            fetchUserProfile(userUid);




            // Find views in the layout
            ImageView itemImageView = findViewById(R.id.itemImageView);
            TextView itemNameTextView = findViewById(R.id.itemNameTextView);
            TextView itemDescriptionTextView = findViewById(R.id.itemDescription);
            TextView itemLocationTextView = findViewById(R.id.itemLocationTextView);

            // loads the image from the URL using Picasso
            if (!imageUrl.isEmpty()) {
                Picasso.get().load(imageUrl).into(itemImageView);
            }

            // Set data to the views
            itemNameTextView.setText(itemName);
            itemDescriptionTextView.setText(itemDescription);
            itemLocationTextView.setText(itemLocation);


            Button claimButton = findViewById(R.id.buttonClaim);
            claimButton.setOnClickListener(view -> {
                //  To-do: code for the claim button
            });
        }

        // for debugging purposes
        if (extras != null) {
            String userUid = extras.getString("USER_UID", "");
            Log.d("UserUidDebug", "User UID: " + userUid);
        }
    }


    private void fetchUserProfile(String userUid) {
        DatabaseReference userProfileRef = FirebaseDatabase.getInstance().getReference("users").child(userUid);

        userProfileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
        ImageView userProfilePhotoImageView = findViewById(R.id.imageViewProfilePicture);
        TextView userProgramTextView = findViewById(R.id.ProgramTextView);
        TextView userDisplayNameTextView = findViewById(R.id.DisplayNameTextView);

        if (userProfile.getProfileImageUrl() != null && !userProfile.getProfileImageUrl().isEmpty()) {
            Picasso.get().load(userProfile.getProfileImageUrl()).into(userProfilePhotoImageView);
        }
        userProgramTextView.setText(userProfile.getProgram());
        userDisplayNameTextView.setText(userProfile.getDisplayName());
    }

    public static class UserProfileActivity extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_user_profile); // Use your layout XML file
        }
    }
}




