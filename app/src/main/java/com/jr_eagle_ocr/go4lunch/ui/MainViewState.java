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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MainViewState that = (MainViewState) o;

        if (currentUserName != null ? !currentUserName.equals(that.currentUserName) : that.currentUserName != null)
            return false;
        if (currentUserUrlPicture != null ? !currentUserUrlPicture.equals(that.currentUserUrlPicture) : that.currentUserUrlPicture != null)
            return false;
        if (currentUserEmail != null ? !currentUserEmail.equals(that.currentUserEmail) : that.currentUserEmail != null)
            return false;
        if (currentUserChosenRestaurantId != null ? !currentUserChosenRestaurantId.equals(that.currentUserChosenRestaurantId) : that.currentUserChosenRestaurantId != null)
            return false;
        return action != null ? action.equals(that.action) : that.action == null;
    }

    @Override
    public int hashCode() {
        int result = currentUserName != null ? currentUserName.hashCode() : 0;
        result = 31 * result + (currentUserUrlPicture != null ? currentUserUrlPicture.hashCode() : 0);
        result = 31 * result + (currentUserEmail != null ? currentUserEmail.hashCode() : 0);
        result = 31 * result + (currentUserChosenRestaurantId != null ? currentUserChosenRestaurantId.hashCode() : 0);
        result = 31 * result + (action != null ? action.hashCode() : 0);
        return result;
    }
}
