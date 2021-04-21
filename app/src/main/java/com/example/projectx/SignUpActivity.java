package com.example.projectx;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.projectx.model.UserDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.ImmutableMap;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.ContentValues.TAG;

public class SignUpActivity extends Activity {
    EditText emailInput, passwordInput, confirmPasswordInput, nameInput;
    private FirebaseAuth auth;

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        emailInput = findViewById(R.id.signUpEmail);
        passwordInput = findViewById(R.id.signUpPassword);
        confirmPasswordInput = findViewById(R.id.confirmPassword);
        nameInput = findViewById(R.id.name_Input);
        auth = FirebaseAuth.getInstance();


        Button signUpButton = findViewById(R.id.createAccount);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUserWithEmailPassword();
            }
        });

    }

    private void insertToFirebase(FirebaseUser user, String name) {

        String email = user.getEmail();
        String uid = user.getUid();


        UserDetails userDetails = new UserDetails();
        userDetails.setEmail(email);
        userDetails.setUid(uid);
        userDetails.setDisplayName(name);
        saveToFireStore(userDetails);

    }

    public void saveToFireStore(UserDetails userDetails) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("UserDetails")
                .document(userDetails.getUid())
                .set(userDetails, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Insert", "DocumentSnapshot successfully written!");
                        updateToken(userDetails.getUid());
                        insertUidToCollection(userDetails.getUid());

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

    }

    private void insertUidToCollection(String uid) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference tagRef = db.collection("CollectionData").document("AllUsers");

        tagRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        tagRef.update("Users", FieldValue.arrayUnion(uid));

                    } else {
                        tagRef.set(ImmutableMap.of("Users", Collections.singletonList(uid)));
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


    }

    private void updateToken(String uid) {

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        if (uid != null) {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            Map<String, Object> userDetails = new HashMap<>();
                            userDetails.put("token", token);
                            db.collection("UserDetails")
                                    .document(UserDetailsUtil.getUID())
                                    .set(userDetails, SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("Insert", "DocumentSnapshot successfully written!");

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error writing document", e);
                                        }
                                    });

                        }

                    }
                });


    }

    private void createUserWithEmailPassword() {

        String name = nameInput.getText().toString();
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();
        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(email) || TextUtils.isEmpty(name))
            displayToast("Required Fields missing");

        else if (!isEmailValid(email))
            displayToast("Please provide valid Email address");
        else if (!password.equals(confirmPassword))
            displayToast("Passwords don't match");

        else {

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this, "ERROR" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                Log.e("Err", task.getException().getMessage());

                            } else {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                updateUserWithDisplayName(user, name);
                                insertToFirebase(user, name);
                                Toast.makeText(SignUpActivity.this, "Success" + user.getDisplayName(), Toast.LENGTH_LONG).show();
                                goToBusinessActivity();
                            }
                        }
                    });
        }
    }

    private void goToBusinessActivity() {
        Intent intent = new Intent(getApplicationContext(), BusinessActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateUserWithDisplayName(FirebaseUser user, String name) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                        }
                    }
                });
    }

    private void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}