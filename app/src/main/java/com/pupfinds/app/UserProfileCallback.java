package com.pupfinds.app;

public interface UserProfileCallback {
    void onUserProfileReceived(UserProfile userProfile);

    void onProfileFetchError(String errorMessage);
}
