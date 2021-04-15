package com.example.projectx;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import static android.content.ContentValues.TAG;

public class EditProfileActivity extends AppCompatActivity {
    EditText email, phoneNo, userName;
    Button updateProfileBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        updateProfileBtn = findViewById(R.id.editProfileUpdate);
        email = findViewById(R.id.editProfileEmail);
        userName = findViewById(R.id.editProfileName);
        phoneNo = findViewById(R.id.editProfilePhone);

        FirebaseUser user = UserDetailsUtil.getUser();
        email.setText(user.getEmail());
        userName.setText(user.getDisplayName());
        phoneNo.setText(user.getPhoneNumber());

        updateProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(user);
            }
        });
    }

    private void updateProfile(FirebaseUser user) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(userName.getText().toString())
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditProfileActivity.this, "User profile updated.", Toast.LENGTH_LONG).show();
                        }
                    }
                });

        user.updateEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "User profile updated.");
                }
            }
        });


//        user.updatePhoneNumber(phoneNo.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()) {
//                    Log.d(TAG, "User profile updated.");
//                }
//            }
//        });
    }
}