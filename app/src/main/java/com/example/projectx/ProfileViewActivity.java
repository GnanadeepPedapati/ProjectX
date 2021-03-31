package com.example.projectx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileViewActivity extends AppCompatActivity {


    Button changePassword, myTags, signOut;

    TextView profileName, textEmail, textPhone;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        profileName = findViewById(R.id.profileName);
        textEmail = findViewById(R.id.profileEmail);
        textPhone = findViewById(R.id.profilePhone);

        loadDetails();


        changePassword = findViewById(R.id.changePassword);
        myTags = findViewById(R.id.profileTagsButton);
        signOut = findViewById(R.id.signOut);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                Intent intent = new Intent(ProfileViewActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });
        myTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileViewActivity.this, EditProfileActivity.class);
                startActivity(intent);
            }
        });


        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = buildChangePasswordDialog();
                alertDialog.show();

            }
        });

    }

    private void loadDetails() {

        FirebaseUser user = UserDetailsUtil.getUser();
        profileName.setText(user.getDisplayName());
        textEmail.setText(user.getEmail());
        textPhone.setText(user.getPhoneNumber());

    }


    private AlertDialog buildChangePasswordDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = LayoutInflater.from(this);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View dialogView = inflater.
                inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        EditText oldPassword = dialogView.findViewById(R.id.oldPassword);
                        String oldPasswordString = oldPassword.getText().toString();

                        EditText newPassword = dialogView.findViewById(R.id.newPassword);
                        String newPasswordString = newPassword.getText().toString();

                        EditText confirmPassword = dialogView.findViewById(R.id.newPassword);
                        String confirmPasswordString = confirmPassword.getText().toString();


                        if (newPasswordString.equals(confirmPasswordString)) {
                            FirebaseUser user = UserDetailsUtil.getUser();
                            AuthCredential credential = EmailAuthProvider
                                    .getCredential(user.getEmail(), oldPasswordString);
                            user.reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                user.updatePassword(newPasswordString).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(ProfileViewActivity.this, "Password updated", Toast.LENGTH_LONG).show();
                                                        } else {
                                                            Toast.makeText(ProfileViewActivity.this, "Password  not updated", Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                            } else {
                                                buildChangePasswordDialog().show();
                                                Toast.makeText(ProfileViewActivity.this, "Your current password is incorrect", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });

                        } else {
                            Toast.makeText(ProfileViewActivity.this, "Passwords don't match", Toast.LENGTH_LONG).show();

                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.dismiss();
                    }
                });


        return builder.create();
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadDetails();
    }
}