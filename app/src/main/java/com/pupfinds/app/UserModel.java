package com.pupfinds.app;

public class UserModel {
    private String userUid;
    private String profileImageUrl;
    private String firstName;
    private String program;
    private String lastName;
    private String displayName;

    private String lastMessage;
    private long lastMessageTimestamp;
    public UserModel() {
    }

    public UserModel(String userUid, String profileImageUrl, String firstName, String lastName, String program, String displayName) {
        this.userUid = userUid;
        this.profileImageUrl = profileImageUrl;
        this.firstName = firstName;
        this.program = program;
        this.lastName = lastName;
        this.displayName = displayName;
    }


    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProgram() {
        return program;
    }
    public void setProgram(String program) {
        this.program = program;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }
}


