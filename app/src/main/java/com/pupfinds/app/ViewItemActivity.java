package com.pupfinds.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.widget.Button;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
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
            String itemCategory = extras.getString("ITEM_CATEGORY", "");
            String itemLocation = extras.getString("ITEM_LOCATION", "");
            String imageUrl = extras.getString("ITEM_IMAGE_URL", "");
            Log.i("TAG", "This is a debug message" + itemLocation);
            String userUid = extras.getString("USER_UID", "");
            String lostOrFound = extras.getString("LOST_OR_FOUND", "");
            fetchUserProfile(userUid);


            // Find views in the layout
            ImageView itemImageView = findViewById(R.id.itemImageView);
            TextView itemNameTextView = findViewById(R.id.itemNameTextView);
            TextView itemCategoryTextView = findViewById(R.id.itemCategoryTextView);
            TextView itemDescriptionTextView = findViewById(R.id.itemDescription);
            TextView itemLocationTextView = findViewById(R.id.itemLocationTextView);
            TextView viewHeader = findViewById(R.id.viewHeader);
            Button buttonClaimOrReturn = findViewById(R.id.buttonClaimOrReturn);

            itemCategoryTextView.setText(itemCategory);
            if (lostOrFound.equals("LOST")){
                viewHeader.setText("Lost Item");
                buttonClaimOrReturn.setText("Return Item");
            }

            else if (lostOrFound.equals("FOUND")){
                viewHeader.setText("Found Item");
                buttonClaimOrReturn.setText("Claim Item");
            }


            // loads the image from the URL using Picasso
            if (!imageUrl.isEmpty()) {
                Picasso.get().load(imageUrl).into(itemImageView);
            }

            // Set data to the views
            itemNameTextView.setText(itemName);
            itemDescriptionTextView.setText(itemDescription);
            itemLocationTextView.setText(itemLocation);

            buttonClaimOrReturn.setOnClickListener(view -> {
                Intent intent = new Intent(ViewItemActivity.this, ConvoActivity.class);
                intent.putExtra("current_user_uid", FirebaseAuth.getInstance().getUid()); // Pass the current user's UID
                intent.putExtra("item_owner_uid", userUid); // Pass the item owner's UID
                startActivity(intent);
                finish();
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
}




