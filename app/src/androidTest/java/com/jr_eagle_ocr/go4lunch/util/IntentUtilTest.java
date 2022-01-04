package com.jr_eagle_ocr.go4lunch.util;

import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_RESTAURANT;
import static com.jr_eagle_ocr.go4lunch.AndroidTestUtils.TEST_RESTAURANT_DETAIL_VIEW_SATE;
import static org.junit.Assert.assertTrue;

import android.content.Intent;
import android.net.Uri;

import org.junit.Test;

public class IntentUtilTest {

    @Test
    public void getIntent_withActionDialString_shouldReturnExpectedIntent() {
        Intent actualIntent = IntentUtil.getIntent(IntentUtil.ACTION_DIAL_INTENT, TEST_RESTAURANT_DETAIL_VIEW_SATE);

        String phoneNumber = TEST_RESTAURANT.getPhoneNumber();
        Uri tel = Uri.fromParts("tel", phoneNumber, null);
        Intent expectedIntent = new Intent(Intent.ACTION_DIAL, tel);

        assertTrue(expectedIntent.filterEquals(actualIntent));
    }

    @Test
    public void getIntent_withActionViewString_shouldReturnExpectedIntent() {
        Intent actualIntent = IntentUtil.getIntent(IntentUtil.ACTION_VIEW_INTENT, TEST_RESTAURANT_DETAIL_VIEW_SATE);

        String websiteUrl = TEST_RESTAURANT.getWebsiteUrl();
        Uri url = Uri.parse(websiteUrl);
        Intent expectedIntent = new Intent(Intent.ACTION_VIEW, url);

        assertTrue(expectedIntent.filterEquals(actualIntent));
    }
}