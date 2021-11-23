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
}
