package com.example.projectx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.projectx.model.UserDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static android.content.ContentValues.TAG;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    EditText emailInput, passwordInput, confirmPasswordInput;
    EditText tagsFilter;

    private List<String> selectedTags = new ArrayList<>();
    private List<String> sourceTags = new ArrayList<>(
            Arrays.asList("Apple", "Orange", "Bat", "Buffalo", "Pig", "Peacock", "Pigeon", "Parrot", "Ox", "Owl", "Tiger", "Lion", "Frog"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        emailInput = findViewById(R.id.signUpEmail);
        passwordInput = findViewById(R.id.signUpPassword);
        confirmPasswordInput = findViewById(R.id.confirmPassword);
        auth = FirebaseAuth.getInstance();

        setSourceTags(sourceTags);

        Button signUpButton = findViewById(R.id.createAccount);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayToast(selectedTags.toString());
                //createUserWithEmailPassword();
            }
        });

        tagsFilter = findViewById(R.id.filterTags);
        tagsFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<String> collect = sourceTags.stream().filter(sourceTag -> sourceTag.toLowerCase().contains(s.toString().toLowerCase())).collect(Collectors.toList());
                final ChipGroup chipGroup = findViewById(R.id.tag_group);
                chipGroup.removeAllViews();
                setSourceTags(collect);
            }
        });
    }


    private void addTagToSelection(String tagName) {

        final ChipGroup chipGroup = findViewById(R.id.selected_tags);

        final Chip chip = new Chip(this);
        int paddingDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10,
                getResources().getDisplayMetrics()
        );
        chip.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);
        chip.setText(tagName);
        chip.setChecked(true);
        chip.setCloseIconVisible(true);
        chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.teal_200)));

        //Added click listener on close icon to remove tag from ChipGroup
        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chipGroup.removeView(chip);
                selectedTags.remove(tagName);
                sourceTags.add(tagName);
                addTagToSourceGroup(tagName);
            }
        });

        chipGroup.addView(chip);

    }

    private void addTagToSourceGroup(String tagName) {
        final ChipGroup chipGroup = findViewById(R.id.tag_group);
        final Chip chip = new Chip(this);
        int paddingDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10,
                getResources().getDisplayMetrics()
        );
        chip.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);
        chip.setText(tagName);
        chip.setChecked(true);
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        chip.setChipBackgroundColor(ColorStateList.valueOf(color));
        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = ((Chip) v).getText().toString();
                selectedTags.add(text);
                sourceTags.remove(text);
                addTagToSelection(text);
                chipGroup.removeView(chip);
            }
        });


        chip.setCloseIconVisible(false);
        chipGroup.addView(chip);
    }


    private void setSourceTags(final List<String> tagList) {

        for (int index = 0; index < tagList.size(); index++) {
            final String tagName = tagList.get(index);
            addTagToSourceGroup(tagName);
        }
    }


    private void insertToFirebase(FirebaseUser user) {

        String displayName = user.getDisplayName();
        String email = user.getEmail();
        String uid = user.getUid();


        UserDetails userDetails = new UserDetails(uid, displayName, email);
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

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
        ;

    }

    private void createUserWithEmailPassword() {

        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();
        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(email))
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

                                insertToFirebase(user);
                                Toast.makeText(SignUpActivity.this, "Success"+user.getDisplayName(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    private void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

    }


    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}