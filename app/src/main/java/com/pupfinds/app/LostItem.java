package com.pupfinds.app;

public class LostItem {
    private String itemName;
    private String itemDescription;
    private String imageUrl;

    public LostItem() {
        // required for calls to DataSnapshot.getValue(LostItem.class)
    }

    public LostItem(String itemName, String itemDescription, String imageUrl) {
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.imageUrl = imageUrl;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
