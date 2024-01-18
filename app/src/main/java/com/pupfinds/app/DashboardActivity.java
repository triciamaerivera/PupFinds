package com.pupfinds.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class DashboardActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FoundItemAdapter foundItemAdapter;
    private List<FoundItem> foundItemList;
    private DatabaseReference foundItemsRef;
    private TextInputEditText searchEditText;
    private Spinner searchCategorySpinner;
    private ArrayAdapter<String> categoryAdapter;
    private String selectedCategory = "";
    private String[] categoryArray;
    private ImageView searchButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);

        categoryArray = getResources().getStringArray(R.array.category_array);
        List<String> categoryList = new ArrayList<>(Arrays.asList(categoryArray));
        categoryList.add(0, "Categories");
        categoryArray = categoryList.toArray(new String[0]);

        searchCategorySpinner = findViewById(R.id.searchCategorySpinner);
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryArray);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchCategorySpinner.setAdapter(categoryAdapter);
        selectedCategory = "";



        // Check if the user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        searchEditText = findViewById(R.id.editText_SearchField);

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

        searchEditText = findViewById(R.id.editText_SearchField);
        searchEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    performSearch();
                    return true;
                }
                return false;
            }


        });

        searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSearchButtonClick(view);
            }
        });

        searchCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                selectedCategory = categoryArray[position];
                performSearch();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // TODO: Create element when nothing is selected
            }
        });
    }
    public void onSearchButtonClick(View view) {
        selectedCategory = searchCategorySpinner.getSelectedItem().toString();
        performSearch();
    }
    private void performSearch() {
        String searchQuery = searchEditText.getText().toString().trim().toLowerCase();

        if (selectedCategory == null) {
            selectedCategory = ""; // Ensure selectedCategory is not null
        }

        if (searchQuery.isEmpty() && selectedCategory.equals("Categories")) {
            foundItemAdapter.setFoundItems(foundItemList);
        } else {
            List<FoundItem> filteredItems = new ArrayList<>();
            for (FoundItem item : foundItemList) {
                boolean matchesSearch = searchQuery.isEmpty() ||
                        item.getName().toLowerCase().contains(searchQuery) ||
                        item.getLocation().toLowerCase().contains(searchQuery) ||
                        item.getBuilding().toLowerCase().contains(searchQuery) ||
                        item.getCampus().toLowerCase().contains(searchQuery);

                boolean matchesCategory = selectedCategory.isEmpty() ||
                        (selectedCategory.equals("Categories") && item.getCategory() != null && !item.getCategory().isEmpty()) ||
                        (item.getCategory() != null && item.getCategory().equalsIgnoreCase(selectedCategory));

                if (matchesSearch && matchesCategory) {
                    filteredItems.add(item);
                }
            }
            foundItemAdapter.setFoundItems(filteredItems);
        }
    }

    private void loadFoundItems() {
        foundItemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<FoundItem> foundItems = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FoundItem foundItem = snapshot.getValue(FoundItem.class);
                    if (foundItem != null) {
                        String firstName = snapshot.child("firstName").getValue(String.class);

                        foundItem.setUserDisplayName(firstName);

                        foundItems.add(foundItem);
                    }
                }
                // Update the foundItemList and notify the adapter
                foundItemList = foundItems;
                foundItemAdapter.setFoundItems(foundItems);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DashboardActivity", "Error loading found items: " + databaseError.getMessage());
            }
        });
    }
}














