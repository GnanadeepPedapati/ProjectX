package com.example.projectx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.DrawableCompat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import java.util.Random;

public class ProfileViewActivity extends AppCompatActivity {


    Button changePassword, myTags, signOut,helpCenter;
    TextView profileTitleLetter;

    TextView profileName, textEmail, textPhone;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        profileName = findViewById(R.id.profileName);
        textEmail = findViewById(R.id.profileEmail);
        textPhone = findViewById(R.id.profilePhone);
        profileTitleLetter = findViewById(R.id.profileTitleLetter);


        loadDetails();

        changePassword = findViewById(R.id.changePassword);
        myTags = findViewById(R.id.profileTagsButton);
        helpCenter = findViewById(R.id.profileHelp);
        signOut = findViewById(R.id.signOut);
        ImageView editprofile = findViewById(R.id.editprofile);
        editprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileViewActivity.this, EditProfileActivity.class);
                startActivity(intent);
            }
        });
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
                Intent intent = new Intent(ProfileViewActivity.this, TagSelectionActivity.class);
                startActivity(intent);
            }
        });
        helpCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "projectx@gmail.com", null));
                startActivity(Intent.createChooser(emailIntent, null));
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
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

        View iconContainer = findViewById(R.id.icon_container);
        Drawable background = iconContainer.getBackground();
        DrawableCompat.setTint(background, color);

        if (user.getDisplayName() != null && user.getDisplayName().length() > 1)
            profileTitleLetter.setText(user.getDisplayName().substring(0, 1).toUpperCase());
        textEmail.setText(user.getEmail());
        textPhone.setText(user.getPhoneNumber());

    }


    private AlertDialog buildChangePasswordDialog() {

//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.dialog_change_password, null);

        final AlertDialog alertD = new AlertDialog.Builder(this).create();

//        EditText userInput = (EditText) promptView.findViewById(R.id.display_always);

        Button btnAdd1 = (Button) promptView.findViewById(R.id.confirm_request);

        Button btnAdd2 = (Button) promptView.findViewById(R.id.cancel_request);

        btnAdd1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText oldPassword = promptView.findViewById(R.id.oldPassword);
                String oldPasswordString = oldPassword.getText().toString();

                EditText newPassword = promptView.findViewById(R.id.newPassword);
                String newPasswordString = newPassword.getText().toString();

                EditText confirmPassword = promptView.findViewById(R.id.newPassword);
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
        });

        btnAdd2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                alertD.dismiss();

            }
        });

        alertD.setView(promptView);

        alertD.show();

        return alertD;

//        View dialogView = inflater.
//                inflate(R.layout.dialog_change_password, null);
//        builder.setView(dialogView)
//                // Add action buttons
//                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        EditText oldPassword = dialogView.findViewById(R.id.oldPassword);
//                        String oldPasswordString = oldPassword.getText().toString();
//
//                        EditText newPassword = dialogView.findViewById(R.id.newPassword);
//                        String newPasswordString = newPassword.getText().toString();
//
//                        EditText confirmPassword = dialogView.findViewById(R.id.newPassword);
//                        String confirmPasswordString = confirmPassword.getText().toString();
//
//
//                        if (newPasswordString.equals(confirmPasswordString)) {
//                            FirebaseUser user = UserDetailsUtil.getUser();
//                            AuthCredential credential = EmailAuthProvider
//                                    .getCredential(user.getEmail(), oldPasswordString);
//                            user.reauthenticate(credential)
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if (task.isSuccessful()) {
//                                                user.updatePassword(newPasswordString).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                    @Override
//                                                    public void onComplete(@NonNull Task<Void> task) {
//                                                        if (task.isSuccessful()) {
//                                                            Toast.makeText(ProfileViewActivity.this, "Password updated", Toast.LENGTH_LONG).show();
//                                                        } else {
//                                                            Toast.makeText(ProfileViewActivity.this, "Password  not updated", Toast.LENGTH_LONG).show();
//                                                        }
//                                                    }
//                                                });
//                                            } else {
//                                                buildChangePasswordDialog().show();
//                                                Toast.makeText(ProfileViewActivity.this, "Your current password is incorrect", Toast.LENGTH_LONG).show();
//                                            }
//                                        }
//                                    });
//
//                        } else {
//                            Toast.makeText(ProfileViewActivity.this, "Passwords don't match", Toast.LENGTH_LONG).show();
//
//                        }
//
//                    }
//                })
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//
//                        dialog.dismiss();
//                    }
//                });


//        return builder.create();
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadDetails();
    }
}