package com.example.projectx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class SignInActivity extends Activity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 123;
    EditText emailInput;
    EditText passwordInput;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);


        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        Button googleSignIn = findViewById(R.id.googleSignIn);
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
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Log.d("Result", "onActivityResult: Success");
                Toast.makeText(this, "LoggedIn", Toast.LENGTH_LONG).show();
                postLogin();

                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
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
        Intent i = new Intent(SignInActivity.this, ChatActivity.class);
        startActivity(i);
    }

    // [END auth_fui_result]
}