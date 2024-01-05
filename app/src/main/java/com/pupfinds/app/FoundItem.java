package com.pupfinds.app;

public class FoundItem {
    private String name;
    private String description;
    private String location;
    private String imageUrl;
    private String campus;
    private String building;

    // Fields for user profile information
    private String profileImageUrl;
    private String firstName;
    private String lastName;
    private String program;
    private String userUid;
    private String userDisplayName;


    public FoundItem() {
        // required by Firebase
    }
    public FoundItem(String name, String description, String location, String imageUrl, String userUid) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.imageUrl = imageUrl;
        this.userUid = userUid;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getCampus() { return campus; }

    public void setCampus(String campus) { this.campus = campus; }

    public String getBuilding() { return building; }

    public void setBuilding(String building) { this.building = building; }

    public String getLocation() { return location; }

    public void setLocation(String location) { this.location = location; }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public void setUserDisplayName(String userDisplayName) { this.userDisplayName = userDisplayName; }
}






