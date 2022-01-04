package com.jr_eagle_ocr.go4lunch.util;

import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.jr_eagle_ocr.go4lunch.data.models.Restaurant;
import com.jr_eagle_ocr.go4lunch.ui.restaurant_detail.RestaurantDetailViewSate;

public abstract class IntentUtil {
    public static final String ACTION_DIAL_INTENT = "ACTION_DIAL_INTENT";
    public static final String ACTION_VIEW_INTENT = "ACTION_VIEW_INTENT";

    public static Intent getIntent(@NonNull String intentString, @NonNull RestaurantDetailViewSate restaurant) {
        switch (intentString) {
            case ACTION_DIAL_INTENT:
                String phoneNumber = restaurant.getPhoneNumber();
                Uri tel = Uri.fromParts("tel", phoneNumber, null);
                return new Intent(Intent.ACTION_DIAL, tel);
            case ACTION_VIEW_INTENT:
                String websiteUrl = restaurant.getWebsiteUrl();
                Uri url = Uri.parse(websiteUrl);
                return new Intent(Intent.ACTION_VIEW, url);
            default:
                return null;
        }
    }
}
