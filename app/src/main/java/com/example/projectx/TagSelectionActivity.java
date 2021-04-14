package com.example.projectx;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static android.content.ContentValues.TAG;

public class TagSelectionActivity extends AppCompatActivity {

    EditText tagsFilter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView skipNow;
    private ProgressBar spinner;
    private List<String> selectedTags = new ArrayList<>();
    private List<String> sourceTags = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_selection);
        getSourceTags();
        tagsFilter = findViewById(R.id.filterTags);
        Button tagSelectionContinue = findViewById(R.id.tagSelectionContinue);
        spinner = (ProgressBar) findViewById(R.id.progressBar1);

        onCoachMark();

        tagSelectionContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!selectedTags.isEmpty())
                    assignTagsToUser();
            }
        });
        skipNow = findViewById(R.id.skip_now);

        if (getCallingActivity() != null && "com.example.projectx.ProfileViewActivity".equals(getCallingActivity().getClassName())) {
            TextView tagHeading = findViewById(R.id.tag_act_heading);
            tagHeading.setText("Your Tags.You can select more from below list");
            tagSelectionContinue.setText("Update");
            skipNow.setVisibility(View.GONE);
        }


        skipNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedTags.clear();
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

    private void getSourceTags() {
        db.collection("Tags")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                sourceTags.add(document.getId());
//                                addTagToSourceGroup(document.getId());
                            }
                            loadUserExistingTags();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


    public void onCoachMark(){

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.coach_mark);
        dialog.setCanceledOnTouchOutside(true);
        //for dismissing anywhere you touch
        View masterView = dialog.findViewById(R.id.coach_mark_master_view);
        masterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    private void loadUserExistingTags() {
        String uid = UserDetailsUtil.getUID();
        DocumentReference documentReference = db.collection("UserDetails").document(uid);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    selectedTags = (List<String>) task.getResult().getData().get("tags");
                    if (selectedTags == null)
                        selectedTags = new ArrayList<>();
                    loadExistingTags();
                } else {
                    selectedTags = new ArrayList<>();
                    loadExistingTags();
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

                if (getCallingActivity() != null && "com.example.projectx.ProfileViewActivity".equals(getCallingActivity().getClassName())) {
                    Toast.makeText(getApplicationContext(), "Sucessfully Updated!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(TagSelectionActivity.this, ProfileViewActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(TagSelectionActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
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


    private void loadExistingTags() {
        for (String tagName : selectedTags) {
            addTagToSelection(tagName);
            sourceTags.remove(tagName);
        }
        final ChipGroup chipGroup = findViewById(R.id.tag_group);
        chipGroup.removeAllViews();
        setSourceTags(sourceTags);


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
        spinner.setVisibility(View.GONE);

    }

}