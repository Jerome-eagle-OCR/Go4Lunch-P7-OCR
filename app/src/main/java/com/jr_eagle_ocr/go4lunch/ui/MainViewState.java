package com.jr_eagle_ocr.go4lunch.ui;

public class MainViewState {
    private final String currentUserName;
    private final String currentUserUrlPicture;
    private final String currentUserEmail;
    private final String currentUserChosenRestaurantId;
    private final String action;


    public MainViewState(
            String currentUserName,
            String currentUserUrlPicture,
            String currentUserEmail,
            String currentUserChosenRestaurantId,
            String action
    ) {
        this.currentUserName = currentUserName;
        this.currentUserUrlPicture = currentUserUrlPicture;
        this.currentUserEmail = currentUserEmail;
        this.currentUserChosenRestaurantId = currentUserChosenRestaurantId;
        this.action = action;
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

    public String getAction() {
        return action;
    }
}
