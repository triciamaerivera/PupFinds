package com.pupfinds.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
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


public class DashboardActivityLost extends AppCompatActivity {
    private RecyclerView recyclerView;
    private LostItemAdapter lostItemAdapter;
    private List<LostItem> lostItemList;
    private DatabaseReference lostItemsRef;
    private TextInputEditText searchEditText;
    private Spinner searchCategorySpinner;
    private ArrayAdapter<String> categoryAdapter;
    private String selectedCategory = "";
    private String[] categoryArray;
    private ImageView searchButton;
    private MenuItem menuItemDashboard;
    private MenuItem menuItemReturned;
    private MenuItem menuItemAdd;
    private MenuItem menuItemChat;
    private MenuItem menuItemProfile;


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

        Button lostButton = findViewById(R.id.lostButton);
        Button foundButton = findViewById(R.id.foundButton);

        // Change background color
        lostButton.setBackgroundResource((R.drawable.dashboard_clicked));
        foundButton.setBackgroundResource(R.drawable.dashboard_not_clicked);

        // Change text color
        lostButton.setTextColor(getResources().getColor(R.color.white));
        foundButton.setTextColor(getResources().getColor(R.color.LightRed));
        // Set click listener for Lost button
        lostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to DashboardActivityLost
                Intent intent = new Intent(DashboardActivityLost.this, DashboardActivityLost.class);
                startActivity(intent);
                finish();
            }
        });

        // Set click listener for Found button
        foundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to DashboardActivityFound
                Intent intent = new Intent(DashboardActivityLost.this, DashboardActivityFound.class);
                startActivity(intent);
                finish();
            }
        });


        // add bottom navigation menu
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_dashboard);

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

        // Check if the user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        searchEditText = findViewById(R.id.editText_SearchField);

        // Initialize RecyclerView and Adapter
        recyclerView = findViewById(R.id.recyclerView);
        lostItemList = new ArrayList<>();
        lostItemAdapter = new LostItemAdapter(lostItemList, item -> {
            // onItemClick logic
            Intent viewItemIntent = new Intent(DashboardActivityLost.this, ViewItemActivity.class);
            viewItemIntent.putExtra("ITEM_NAME", item.getName());
            viewItemIntent.putExtra("ITEM_CATEGORY", item.getCategory());
            viewItemIntent.putExtra("ITEM_DESCRIPTION", item.getDescription());
            viewItemIntent.putExtra("ITEM_LOCATION", item.getFullLocation());
            viewItemIntent.putExtra("ITEM_IMAGE_URL", item.getImageUrl());
            viewItemIntent.putExtra("LOST_OR_FOUND", "LOST");

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
//        GridLayout itemsGrid = findViewById(R.id.itemsGrid);

        // Initialize Firebase Database reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        lostItemsRef = database.getReference("lost_items");

        // Set up RecyclerView with the adapter
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(lostItemAdapter);

        // Add spacing between items
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing); // Adjust as needed
        recyclerView.addItemDecoration(new DashboardSpace(spacingInPixels));

        loadLostItems();

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
            lostItemAdapter.setLostItems(lostItemList);
        } else {
            List<LostItem> filteredItems = new ArrayList<>();
            for (LostItem item : lostItemList) {
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
            lostItemAdapter.setLostItems(filteredItems);
        }
    }

    private void loadLostItems() {
        lostItemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<LostItem> lostItems = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LostItem lostItem = snapshot.getValue(LostItem.class);
                    if (lostItem != null) {
                        String firstName = snapshot.child("firstName").getValue(String.class);

                        lostItem.setUserDisplayName(firstName);

                        lostItems.add(lostItem);
                    }
                }
                // Update the lostItemList and notify the adapter
                lostItemList = lostItems;
                lostItemAdapter.setLostItems(lostItems);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DashboardActivity", "Error loading lost items: " + databaseError.getMessage());
            }
        });
    }
}