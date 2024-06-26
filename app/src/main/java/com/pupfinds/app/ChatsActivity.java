package com.pupfinds.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ChatsActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReferenceUsers;

    private DatabaseReference databaseReferenceChats;

    private RecyclerView recyclerView;
    private Toolbar toolbarChat;

    private String program;

    private List chatManager;

    private ChatsUserAdapter chatsUserAdapter;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_chats);

            toolbarChat = findViewById(R.id.toolbarChat);
            setSupportActionBar(toolbarChat);

            mAuth = FirebaseAuth.getInstance();
            chatsUserAdapter = new ChatsUserAdapter(this);
            recyclerView = findViewById(R.id.recyclerViewChat);

            recyclerView.setAdapter(chatsUserAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            databaseReferenceChats = FirebaseDatabase.getInstance().getReference("chats");
            databaseReferenceChats.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    chatManager = new ArrayList<>();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String uid = dataSnapshot.getKey();
                        int maxLength = Math.min(uid.length(), 28);
                        String senderUid = uid.substring(0, maxLength);

                        String receiverUid = uid.substring(28);
                        if(uid.contains(mAuth.getUid())){
                            chatManager.add(senderUid);
                            chatManager.add(receiverUid);
                        }

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            databaseReferenceUsers = FirebaseDatabase.getInstance().getReference("users");
            databaseReferenceUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<UserModel> userModelList = new ArrayList<>();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String uid = dataSnapshot.getKey();
                        UserModel userModel = dataSnapshot.getValue(UserModel.class);
                        userModel.setUserUid(uid);

                        // Add to userModelList only if the userModel.getUserUid() is in the chatManager list
                        if (userModel != null && chatManager.contains(userModel.getUserUid()) && !userModel.getUserUid().equals(mAuth.getUid())) {
                            userModelList.add(userModel);
                        }
                    }

                    chatsUserAdapter.clear();
                    chatsUserAdapter.addAll(userModelList);
                    chatsUserAdapter.sortByLastMessageTimestamp();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });

        // add bottom navigation menu
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_chat);

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

}

