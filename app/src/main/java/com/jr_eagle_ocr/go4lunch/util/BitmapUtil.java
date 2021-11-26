package com.jr_eagle_ocr.go4lunch.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;

/**
 * @author Found on the web
 * Bitmap to String, and vice-versa, conversion utils to allow restaurant photo storage
 * in a Restaurant Document in Firestore db
 */
public abstract class BitmapUtil {

    /**
     * Convert Bitmap to String
     *
     * @param image the Bitmap to convert
     * @return the base64-encoded Bitmap String
     */
    @Nullable
    public static String encodeToBase64(@Nullable Bitmap image) {
        String imageString = null;
        if (image != null) {
            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOS);
            imageString = Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
        }
        return imageString;
    }

    /**
     * Convert String to Bitmap
     *
     * @param imageString the base64-encoded Bitmap String
     * @return the base64-decoded String Bitmap
     */
    @Nullable
    public static Bitmap decodeBase64(@Nullable String imageString) {
        Bitmap stringImage = null;
        if (imageString != null) {
            byte[] decodedBytes = Base64.decode(imageString, 0);
            stringImage = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        }
        return stringImage;
    }
}
