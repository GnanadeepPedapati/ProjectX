package com.example.projectx;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class BusinessActivity extends Activity {

    boolean isBusiness;
    CardView businessCard;
    Button continueBtn;
    TextView skipNow;
    EditText businessName, businessAddress, businessPinCode, businessPhoneNo, businessCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business);
        businessCard = findViewById(R.id.businessCard);
        continueBtn = findViewById(R.id.continueBtn);
        businessName = findViewById(R.id.businessName);

        businessAddress = findViewById(R.id.businessAdresss);

        businessPinCode = findViewById(R.id.businessPinCode);

        businessPhoneNo = findViewById(R.id.businessPhone);
        businessCity = findViewById(R.id.businessCity);

        skipNow = findViewById(R.id.skip_now);

        continueBtn.setEnabled(false);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBusiness) {
                    submitBussinessData();
                } else
                    updateUserAsNonBusiness();

            }
        });


        skipNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserAsNonBusiness();
            }
        });


    }

    private void submitBussinessData() {

        boolean isValidated = isRequired(businessName);

        isValidated = isValidated && isRequired(businessAddress);
        isValidated = isValidated && isRequired(businessCity);
        isValidated = isValidated && isRequired(businessPinCode);
        isValidated = isValidated && isRequired(businessPhoneNo);
        isValidated = isValidated && minLength(businessPhoneNo, 10);
        isValidated = isValidated && minLength(businessPinCode, 6);
        if (isValidated) {
            insertBusinesssData();
        }


    }

    private boolean minLength(EditText editText, int i) {
        boolean b = editText.getText().toString().length() != i;
        if (b)
            editText.setError("Length must be " + i);
        return !b;

    }

    private boolean isRequired(EditText editText) {
        if (TextUtils.isEmpty(editText.getText())) {
            editText.setError("Field Required");
        }

        return !TextUtils.isEmpty(editText.getText());
    }

    private void updateUserAsNonBusiness() {

        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("isBusiness", false);

        saveToFireStore(hashMap);

    }


    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        continueBtn.setEnabled(true);

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_yes:
                if (checked)
                    isBusiness = true;
                businessCard.setVisibility(View.VISIBLE);

                // Pirates are the best
                break;
            case R.id.radio_no:
                if (checked)
                    // Ninjas rule
                    isBusiness = false;
                businessCard.setVisibility(View.INVISIBLE);

                break;
        }
    }


    private void insertBusinesssData() {
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("address", getText(businessAddress) + "  ^ " + getText(businessCity));
        hashMap.put("businessPhoneNumber", getText(businessPhoneNo));
        hashMap.put("businessName", getText(businessName));
        hashMap.put("pinCode", getText(businessPinCode));
        hashMap.put("isBusiness", true);


        saveToFireStore(hashMap);

    }

    private String getText(EditText editText) {
        return editText.getText().toString();
    }


    public void saveToFireStore(Map<String, Object> userDetails) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("UserDetails")
                .document(UserDetailsUtil.getUID())
                .set(userDetails, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Insert", "DocumentSnapshot successfully written!");
                        goToHomeActivity();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void goToHomeActivity() {
        if (isBusiness == true) {
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getApplicationContext(), TagSelectionActivity.class);
            startActivity(intent);
        }
    }


}