package com.example.projectx;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class UserDetailsUtil {

    static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public static String getUID() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null)
            return null;
        return currentUser.getUid();
    }


    public static FirebaseUser getUser() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        return currentUser;
    }

    public static boolean isUserLoggedIn() {
        return Objects.nonNull(firebaseAuth.getCurrentUser());
    }

    public static String generateChatId(String loggedInUser, String otherUser) {
        int i = loggedInUser.compareTo(otherUser);
        if (i > 0)
            return loggedInUser.concat(otherUser);
        else
            return otherUser.concat(loggedInUser);
    }
}
