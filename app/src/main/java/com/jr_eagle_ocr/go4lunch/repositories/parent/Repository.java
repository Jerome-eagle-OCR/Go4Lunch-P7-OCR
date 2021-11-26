package com.jr_eagle_ocr.go4lunch.repositories.parent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * @author jrigault
 */
public abstract class Repository {
    protected final String TAG = getClass().getSimpleName();
    protected final FirebaseFirestore db;
    protected final FirebaseAuth auth;

    public Repository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }
}
