package com.jr_eagle_ocr.go4lunch.di;

import android.app.Application;

import com.jr_eagle_ocr.go4lunch.BuildConfig;

/**
 * @author jrigault
 */
public final class Go4LunchApplication extends Application {
    private static Go4LunchDependencyContainer sDependencyContainer;

    @Override
    public void onCreate() {
        super.onCreate();

        // For testing purpose to work on "*_test" collection in Firestore and mock place autocomplete response
        // If AndroidJunit4 test class is found from its name valorize boolean to true, to false if not
        try {
            Class.forName("androidx.test.ext.junit.runners.AndroidJUnit4");
            BuildConfig.IS_TESTING.set(true);
        } catch (ClassNotFoundException e) {
            BuildConfig.IS_TESTING.set(false);
        }

        if (sDependencyContainer == null) {
            sDependencyContainer = new Go4LunchDependencyContainer(this);
        }
    }

    public static Go4LunchDependencyContainer getDependencyContainer() {
        return sDependencyContainer;
    }
}
