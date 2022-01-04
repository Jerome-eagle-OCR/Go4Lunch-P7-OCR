package com.jr_eagle_ocr.go4lunch.ui.settings;

/**
 * @author jrigault
 */
public class SettingsDialogViewState {
    private final boolean isSwitchNotificationChecked;
    private final String userName;
    private final String userURLpicture;

    public SettingsDialogViewState(
            boolean isSwitchNotificationChecked,
            String userName,
            String userURLpicture
    ) {
        this.isSwitchNotificationChecked = isSwitchNotificationChecked;
        this.userName = userName;
        this.userURLpicture = userURLpicture;
    }

    public boolean isSwitchNotificationChecked() {
        return isSwitchNotificationChecked;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserURLpicture() {
        return userURLpicture;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SettingsDialogViewState that = (SettingsDialogViewState) o;

        if (isSwitchNotificationChecked != that.isSwitchNotificationChecked) return false;
        if (userName != null ? !userName.equals(that.userName) : that.userName != null)
            return false;
        return userURLpicture != null ? userURLpicture.equals(that.userURLpicture) : that.userURLpicture == null;
    }

    @Override
    public int hashCode() {
        int result = (isSwitchNotificationChecked ? 1 : 0);
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (userURLpicture != null ? userURLpicture.hashCode() : 0);
        return result;
    }
}
