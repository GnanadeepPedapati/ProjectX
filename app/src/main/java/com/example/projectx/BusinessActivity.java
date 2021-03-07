package com.example.projectx;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

public class BusinessActivity extends AppCompatActivity {

    boolean isBusiness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business);
    }


    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_yes:
                if (checked)
                    isBusiness = true;
                // Pirates are the best
                break;
            case R.id.radio_no:
                if (checked)
                    // Ninjas rule
                    isBusiness = false;
                break;
        }
    }
}