package com.pupfinds.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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

public class ViewReturnedItemActivity extends AppCompatActivity {
    private ImageView itemImageViewReturned;
    private TextView itemNameTextViewReturned;
    private TextView itemLocationTextViewReturned;

    private String itemName;
    private String itemLocation;
    private String userUid;
    private String imageUrl;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_returned);

        // Find views in the layout
        itemImageViewReturned = findViewById(R.id.itemImageViewReturned);
        itemNameTextViewReturned = findViewById(R.id.itemNameTextViewReturned);
        itemLocationTextViewReturned = findViewById(R.id.itemLocationTextViewReturned);

        // Retrieve data passed from DashboardActivity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            itemName = extras.getString("ITEM_NAME", "");
            itemLocation = extras.getString("ITEM_LOCATION", "");
            imageUrl = extras.getString("ITEM_IMAGE_URL", "");
            userUid = extras.getString("USER_UID", "");

            fetchUserInformation(imageUrl);

            // loads the image from the URL using Picasso
            if (!imageUrl.isEmpty()) {
                Picasso.get().load(imageUrl).into(itemImageViewReturned);
            }

            // Set data to the views
            itemNameTextViewReturned.setText(itemName);
            itemLocationTextViewReturned.setText(itemLocation);
        }

        // for debugging purposes
        if (extras != null) {
            String userUid = extras.getString("USER_UID", "");
            Log.d("UserUidDebug", "User UID: " + userUid);
        }
    }



    private void fetchUserInformation(String imageUrl) {
        TextView userDisplayNameTextViewOwner = findViewById(R.id.DisplayNameTextViewOwner);
        TextView userDisplayNameTextViewFinder = findViewById(R.id.DisplayNameTextViewFinder);


        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("returned_items");
        Query query = usersRef.orderByChild("imageUrl").equalTo(imageUrl);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("Debug", "DataSnapshot: " + dataSnapshot.toString());

                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    // Assuming each itemSnapshot has children finderName and ownerName
                    String ownerName = itemSnapshot.child("ownerName").getValue(String.class);
                    String finderName = itemSnapshot.child("finderName").getValue(String.class);

                    userDisplayNameTextViewOwner.setText(ownerName);
                    userDisplayNameTextViewFinder.setText(finderName);

                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

}