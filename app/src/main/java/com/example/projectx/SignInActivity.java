package com.example.projectx;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.projectx.model.UserDetails;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.ImmutableMap;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;


public class SignInActivity extends Activity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 123;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText emailInput;
    private EditText passwordInput;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        SignInButton googleSignIn = findViewById(R.id.googleSignIn);
        Button emailSignIn = findViewById(R.id.emailSignIn);
        googleSignIn.setOnClickListener(this);
        emailSignIn.setOnClickListener(this);
        TextView signUpTextView = findViewById(R.id.signUpLink);

        auth = FirebaseAuth.getInstance();

        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(i);
            }
        });

    }


    public void createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
        // [END auth_fui_create_intent]
    }


    // [START auth_fui_result]
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                insertToFirebase(user);
                Log.d("Result", "onActivityResult: Success");
                Toast.makeText(this, "LoggedIn", Toast.LENGTH_LONG).show();

                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    private void insertToFirebase(FirebaseUser user) {

        String displayName = user.getDisplayName();
        String email = user.getEmail();
        String uid = user.getUid();

        UserDetails userDetails = new UserDetails();
        userDetails.setEmail(email);
        userDetails.setUid(uid);
        userDetails.setDisplayName(displayName);
        DocumentReference documentReference = db.collection("UserDetails")
                .document(userDetails.getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot result = task.getResult();
                    if (!result.exists()) { // User Doesnt exists - first time logged in
                        saveToFireStore(userDetails);
                        insertUidToCollection(uid);
                    } else
                        postLogin();

                }
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

    public void saveToFireStore(UserDetails userDetails) {

        db.collection("UserDetails")
                .document(userDetails.getUid())
                .set(userDetails, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Insert", "DocumentSnapshot successfully written!");
                        postLogin();


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

    }


    @Override
    public void onClick(View v) {
        if (v.getId() == (R.id.googleSignIn)) {
            createSignInIntent();
        } else if (R.id.emailSignIn == v.getId()) {
            signInWithEmailPassword();
        }
    }

    private void signInWithEmailPassword() {

        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                    public void onComplete(@NonNull Task<AuthResult> task) {


                        if (!task.isSuccessful()) {
                            Toast.makeText(SignInActivity.this, "ERROR" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("Err", task.getException().getMessage());

                        } else {
                            Toast.makeText(SignInActivity.this, "Success", Toast.LENGTH_LONG).show();
                            postLogin();
                        }
                    }
                });
    }


    private void postLogin() {
        updateToken(UserDetailsUtil.getUID());
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("UserDetails").document(UserDetailsUtil.getUID());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        UserDetails userDetails = document.toObject(UserDetails.class);
                        Boolean isBusiness = userDetails.getIsBusiness();

                        if (isBusiness == null) {
                            goToBusinessActivity();
                        } else {
                            Intent i = new Intent(SignInActivity.this, HomeActivity.class);
                            startActivity(i);
                            finish();
                        }
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
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


    private void goToBusinessActivity() {
        Intent intent = new Intent(getApplicationContext(), BusinessActivity.class);
        startActivity(intent);
        finish();
    }
}