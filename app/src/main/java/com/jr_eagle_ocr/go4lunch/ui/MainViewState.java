package com.jr_eagle_ocr.go4lunch.ui;

public class MainViewState {
    private final String currentUserName;
    private final String currentUserUrlPicture;
    private final String currentUserEmail;
    private final String currentUserChosenRestaurantId;

    public MainViewState(
            String currentUserName,
            String currentUserUrlPicture,
            String currentUserEmail,
            String currentUserChosenRestaurantId
    ) {
        this.currentUserName = currentUserName;
        this.currentUserUrlPicture = currentUserUrlPicture;
        this.currentUserEmail = currentUserEmail;
        this.currentUserChosenRestaurantId = currentUserChosenRestaurantId;
    }

    public String getUserName() {
        return currentUserName;
    }

    public String getUserUrlPicture() {
        return currentUserUrlPicture;
    }

    public String getUserEmail() {
        return currentUserEmail;
    }

    public String getCurrentUserChosenRestaurantId() {
        return currentUserChosenRestaurantId;
    }
}
