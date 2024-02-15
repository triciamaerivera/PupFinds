package com.pupfinds.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ViewItemActivity extends AppCompatActivity {
    private ImageView itemImageView;
    private TextView itemNameTextView;
    private TextView itemCategoryTextView;
    private TextView itemDescriptionTextView;
    private TextView itemLocationTextView;
    private TextView viewHeader;
    private Button buttonMessage;
    private Button buttonClaimedOrReturned;
    private String lostOrFound;
    private String message;

    private String itemName;
    private String itemDescription;
    private String itemCategory;
    private String itemLocation;
    private String userUid;
    private String imageUrl;
    
    private EditText editTextName;

    private String path;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_item);
        
        // Find views in the layout
        itemImageView = findViewById(R.id.itemImageView);
        itemNameTextView = findViewById(R.id.itemNameTextView);
        itemCategoryTextView = findViewById(R.id.itemCategoryTextView);
        itemDescriptionTextView = findViewById(R.id.itemDescription);
        itemLocationTextView = findViewById(R.id.itemLocationTextView);
        viewHeader = findViewById(R.id.viewHeader);
        buttonMessage = findViewById(R.id.buttonMessagePost);
        buttonClaimedOrReturned = findViewById(R.id.buttonClaimedOrReturned);

        // Retrieve data passed from DashboardActivity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            itemName = extras.getString("ITEM_NAME", "");
            itemDescription = extras.getString("ITEM_DESCRIPTION", "");
            itemCategory = extras.getString("ITEM_CATEGORY", "");
            itemLocation = extras.getString("ITEM_LOCATION", "");
            imageUrl = extras.getString("ITEM_IMAGE_URL", "");
            Log.i("TAG", "This is a debug message" + itemLocation);
            userUid = extras.getString("USER_UID", "");

            lostOrFound = extras.getString("LOST_OR_FOUND", "");


            itemCategoryTextView.setText(itemCategory);

            if (lostOrFound.equals("LOST")){
                viewHeader.setText("Lost Item");
                buttonMessage.setText("Message Owner");
                buttonClaimedOrReturned.setText("Item Received");
            }

            else if (lostOrFound.equals("FOUND")){
                viewHeader.setText("Found Item");
                buttonMessage.setText("Message Finder");
                buttonClaimedOrReturned.setText("Returned to Owner");
            }
            fetchUserProfile(userUid);

            // loads the image from the URL using Picasso
            if (!imageUrl.isEmpty()) {
                Picasso.get().load(imageUrl).into(itemImageView);
            }

            // Set data to the views
            itemNameTextView.setText(itemName);
            itemDescriptionTextView.setText(itemDescription);
            itemLocationTextView.setText(itemLocation);

            buttonMessage.setOnClickListener(view -> {
                Intent intent = new Intent(ViewItemActivity.this, ConvoActivity.class);
                intent.putExtra("current_user_uid", FirebaseAuth.getInstance().getUid());
                intent.putExtra("receiver_uid", userUid);
                startActivity(intent);
                finish();
            });

            buttonClaimedOrReturned.setOnClickListener(view -> {
                showConfirmationDialog();
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

        if(userUid.equals(FirebaseAuth.getInstance().getUid())){
            buttonMessage.setVisibility(View.GONE);
            buttonClaimedOrReturned.setVisibility(View.VISIBLE);
        }

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


    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewItemActivity.this);

        if (lostOrFound.equals("LOST")) {
            message = "Thank you for your commitment to honesty and integrity. In order to proceed, please provide the name of the FINDER of the item. We appreciate your cooperation in making our app a reliable platform for the PUPian Community.";
        } else if (lostOrFound.equals("FOUND")) {
            message = "Thank you for your commitment to honesty and integrity. In order to proceed, please provide the name of the OWNER of the item. We appreciate your cooperation in making our app a reliable platform for the PUPian Community.";
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirmation, null);
        builder.setView(dialogView);

        TextView textViewMessage = dialogView.findViewById(R.id.textViewMessage);
        textViewMessage.setText(message);

        editTextName = dialogView.findViewById(R.id.editTextName);
        CheckBox checkBoxPledge = dialogView.findViewById(R.id.checkBoxPledge);

        // Set the positive button with a listener
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = editTextName.getText().toString().trim();
                if (!name.isEmpty() && checkBoxPledge.isChecked()) {
                    updateItemStatus(imageUrl, name);
                }
                else if (name.isEmpty() && checkBoxPledge.isChecked()){
                    Toast.makeText(ViewItemActivity.this, "Please enter a name", Toast.LENGTH_SHORT).show();
                }
                else if (!name.isEmpty() && !checkBoxPledge.isChecked()){
                    Toast.makeText(ViewItemActivity.this, "Please tick the checkbox", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(ViewItemActivity.this, "Please enter a name and tick the checkbox", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set the negative button with a listener
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Show the dialog
        builder.show();
    }


    private void updateItemStatus(String imageUrl, String EnteredName){
        if (lostOrFound.equals("LOST")){
            path = "lost_items";
        }

        else if (lostOrFound.equals("FOUND")){
            path = "found_items";
        }

        DatabaseReference lostItemsRef = FirebaseDatabase.getInstance().getReference(path);
        Query query = lostItemsRef.orderByChild("imageUrl").equalTo(imageUrl);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    // Retrieve data from the matched item
                    String itemId = itemSnapshot.getKey();
                    Log.d("Debug", "debug ID  " + itemId);
                    String buildingReturned = itemSnapshot.child("building").getValue(String.class);
                    String campusReturned = itemSnapshot.child("campus").getValue(String.class);
                    String categoryReturned = itemSnapshot.child("category").getValue(String.class);
                    String descriptionReturned = itemSnapshot.child("description").getValue(String.class);
                    String imageUrlReturned = itemSnapshot.child("imageUrl").getValue(String.class);
                    String locationReturned = itemSnapshot.child("location").getValue(String.class);
                    String nameReturned = itemSnapshot.child("name").getValue(String.class);
                    String uploadDateReturned = itemSnapshot.child("uploadDate").getValue(String.class);
                    String userUidReturned = itemSnapshot.child("userUid").getValue(String.class);

                    // Get a reference to the "returned" root
                    DatabaseReference returnedItemsRef = FirebaseDatabase.getInstance().getReference("returned_items").child(itemId);

                    // Copy the information to the "returned" root
                    returnedItemsRef.child("building").setValue(buildingReturned);
                    returnedItemsRef.child("campus").setValue(campusReturned);
                    returnedItemsRef.child("category").setValue(categoryReturned);
                    returnedItemsRef.child("description").setValue(descriptionReturned);
                    returnedItemsRef.child("imageUrl").setValue(imageUrlReturned);
                    returnedItemsRef.child("location").setValue(locationReturned);
                    returnedItemsRef.child("name").setValue(nameReturned);
                    returnedItemsRef.child("uploadDate").setValue(uploadDateReturned);
                    returnedItemsRef.child("finderName").setValue(uploadDateReturned);

                    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

                    usersRef.child(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String FirstName = dataSnapshot.child("firstName").getValue(String.class);
                                String LastName = dataSnapshot.child("lastName").getValue(String.class);
                                String Name = FirstName + " " + LastName;

                                if (lostOrFound.equals("LOST")){
                                    returnedItemsRef.child("ownerUid").setValue(FirebaseAuth.getInstance().getUid());
                                    returnedItemsRef.child("ownerName").setValue(Name);
                                    returnedItemsRef.child("finderName").setValue(EnteredName);
                                    returnedItemsRef.child("finderUID").setValue("NA");
                                }

                                else if (lostOrFound.equals("FOUND")){
                                    returnedItemsRef.child("finderUID").setValue(FirebaseAuth.getInstance().getUid());
                                    returnedItemsRef.child("finderName").setValue(Name);
                                    returnedItemsRef.child("ownerName").setValue(EnteredName);
                                    returnedItemsRef.child("ownerUid").setValue("NA");
                                }
                            } else {
                                Log.d("FirebaseData", "User does not exist");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle errors
                        }
                    });


                    // Delete the item from the "lost-items" root
                    itemSnapshot.getRef().removeValue();

                    finish();
                    Intent intent = new Intent(ViewItemActivity.this, ReturnedItemsActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }
}




