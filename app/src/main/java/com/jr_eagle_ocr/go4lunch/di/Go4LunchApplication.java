package com.jr_eagle_ocr.go4lunch.di;

import android.app.Application;

/**
 * @author jrigault
 */
public class Go4LunchApplication extends Application {

    private static Go4LunchDependencyContainer sDependencyContainer;

    @Override
    public void onCreate() {
        super.onCreate();
        sDependencyContainer = new Go4LunchDependencyContainer(this);
    }

    public static Go4LunchDependencyContainer getDependencyContainer() {
        return sDependencyContainer;
    }
}
