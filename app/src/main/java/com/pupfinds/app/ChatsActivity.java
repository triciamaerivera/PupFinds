package com.pupfinds.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ChatsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        // add bottom navigation menu
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_chat);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_dashboard) {
                startActivity(new Intent(getApplicationContext(), DashboardActivityFound.class));
                overridePendingTransition(R.anim.fade, R.anim.fade);
                finish();
                return true;
            } else if (item.getItemId() == R.id.bottom_returned) {
                // Handle Returned menu item click
                startActivity(new Intent(getApplicationContext(), DashboardActivityFound.class));
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
