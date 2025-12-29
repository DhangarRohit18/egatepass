package com.example.egatepass;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseHelper {
    public static FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }
    public static FirebaseFirestore getFirestore() {
        return FirebaseFirestore.getInstance();
    }
    public static final String COLLECTION_USERS = "users";
}
