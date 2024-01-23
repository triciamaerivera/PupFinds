package com.pupfinds.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import com.pupfinds.app.FoundItem; // replace with the actual package of your FoundItem class


public class DashboardActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FoundItemAdapter foundItemAdapter;
    private List<FoundItem> foundItemList;
    private DatabaseReference foundItemsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);


        // Check if the user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // User is not logged in, redirect to LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        // Initialize RecyclerView and Adapter
        recyclerView = findViewById(R.id.recyclerView);
        foundItemList = new ArrayList<>();
        foundItemAdapter = new FoundItemAdapter(foundItemList, item -> {
            // onItemClick logic
            Intent viewItemIntent = new Intent(DashboardActivity.this, ViewItemActivity.class);
            viewItemIntent.putExtra("ITEM_NAME", item.getName());
            viewItemIntent.putExtra("ITEM_DESCRIPTION", item.getDescription());
            viewItemIntent.putExtra("ITEM_LOCATION", item.getLocation());
            viewItemIntent.putExtra("ITEM_IMAGE_URL", item.getImageUrl());

            // Add user profile information to the intent
            viewItemIntent.putExtra("USER_UID", item.getUserUid());
            viewItemIntent.putExtra("USER_PROFILE_PHOTO_URL", item.getProfileImageUrl());
            viewItemIntent.putExtra("USER_NAME", item.getFirstName());
            viewItemIntent.putExtra("USER_LASTNAME", item.getLastName());
            viewItemIntent.putExtra("USER_PROGRAM", item.getProgram());

            // Pass the combined display name to ViewItemActivity
            String displayName = item.getFirstName() + " " + item.getLastName();
            viewItemIntent.putExtra("USER_DISPLAY_NAME", displayName);

            startActivity(viewItemIntent);
        });

        // Get references to UI elements
        TextView welcomeText = findViewById(R.id.welcomeText);
        Button uploadLostItemButton = findViewById(R.id.UploadLostItemButton);
        Button uploadFoundItemButton = findViewById(R.id.UploadFoundItemButton);
        GridLayout itemsGrid = findViewById(R.id.itemsGrid);

        // Initialize Firebase Database reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        foundItemsRef = database.getReference("found_items");

        // Set up RecyclerView with the adapter
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(foundItemAdapter);

        // Set click listener for Upload Lost Item button
        uploadLostItemButton.setOnClickListener(view -> {
            // Open activity to upload a lost item
            Intent intent = new Intent(DashboardActivity.this, UploadLostItemActivity.class);
            startActivity(intent);
        });

        // Set click listener for Upload Found Item button
        uploadFoundItemButton.setOnClickListener(view -> {
            // Open activity to upload a found item
            Intent intent = new Intent(DashboardActivity.this, UploadFoundItemActivity.class);
            startActivity(intent);
        });
        loadFoundItems();
    }
    
    private void loadFoundItems() {
        foundItemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<FoundItem> foundItems = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FoundItem foundItem = snapshot.getValue(FoundItem.class);
                    if (foundItem != null) {
                        String firstName= snapshot.child("firstName").getValue(String.class);

                        foundItem.setUserDisplayName(firstName);

                        foundItems.add(foundItem);
                        foundItemAdapter.setFoundItems(foundItems);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DashboardActivity", "Error loading found items: " + databaseError.getMessage());
            }
        });
    }

}














