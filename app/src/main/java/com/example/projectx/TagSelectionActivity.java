package com.example.projectx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.projectx.model.ResponseOverview;
import com.example.projectx.model.UserDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.common.collect.ImmutableMap;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static android.content.ContentValues.TAG;

public class TagSelectionActivity extends AppCompatActivity {

    EditText tagsFilter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private List<String> selectedTags = new ArrayList<>();
    private List<String> sourceTags = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_selection);
        setSourceTags(sourceTags);

        getTags();
        tagsFilter = findViewById(R.id.filterTags);
        Button tagSelectionContinue = findViewById(R.id.tagSelectionContinue);

        tagSelectionContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assignTagsToUser();
            }
        });
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

    private void getTags() {

        db.collection("Tags")

                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                sourceTags.add(document.getId());
                                addTagToSourceGroup(document.getId());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }


    private void assignTagsToUser() {
        String uid = UserDetailsUtil.getUID();


        DocumentReference documentReference = db.collection("UserDetails").document(uid);
        for (String tag : selectedTags) {
            assignUserToTag(tag, uid);
        }

        documentReference.update("tags", selectedTags).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Intent intent = new Intent(TagSelectionActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("firestore", "Error writing document", e);
                    }
                });

    }

    private void assignUserToTag(String tag, String uid) {
        DocumentReference tagRef = db.collection("Tags").document(tag);

        tagRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        tagRef.update("users", FieldValue.arrayUnion(uid));

                    } else {
                        tagRef.set(ImmutableMap.of("users", Collections.singletonList(uid)));
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
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

}