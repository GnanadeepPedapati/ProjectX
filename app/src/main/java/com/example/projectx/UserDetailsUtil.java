package com.example.projectx;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class UserDetailsUtil {

    static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public static String getUID() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        return currentUser.getUid();
    }

    public static boolean isUserLoggedIn() {
        return Objects.nonNull(firebaseAuth.getCurrentUser());
    }
}
