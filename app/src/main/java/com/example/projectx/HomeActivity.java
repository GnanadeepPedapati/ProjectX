package com.example.projectx;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import lombok.NonNull;

public class HomeActivity extends AppCompatActivity {

    //Image request code
    private int PICK_IMAGE_REQUEST = 1;
    Fragment active;
    //storage permission code
    private static final int STORAGE_PERMISSION_CODE = 123;

    BottomNavigationView bottomNavigationView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        requestStoragePermission();
        Fragment homeFragment = HomeFragment.newInstance();
        Fragment requestsFragment = RequestsListFragment.newInstance("1", "2");
        Fragment incomingListFragment = IncomingListFragment.newInstance("1", "2");
        active = homeFragment;
        final FragmentManager fm = getSupportFragmentManager();

        fm.beginTransaction().add(R.id.main_container, requestsFragment, "3").hide(requestsFragment).commit();
        fm.beginTransaction().add(R.id.main_container, incomingListFragment, "2").hide(incomingListFragment).commit();
        fm.beginTransaction().add(R.id.main_container, homeFragment, "1").commit();


        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@androidx.annotation.NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.home:
                        fm.beginTransaction().hide(active).show(homeFragment).commit();
                        active = homeFragment;
                        return true;

                    case R.id.outgoing:
                        fm.beginTransaction().hide(active).show(requestsFragment).commit();
                        active = requestsFragment;
                        return true;

                    case R.id.incoming:
                        fm.beginTransaction().hide(active).show(incomingListFragment).commit();
                        active = incomingListFragment;
                        return true;

                }
                return false;


            }
        });


    }


    //Requesting permission
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }


    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
                requestStoragePermission();
            }
        }
    }


}