package com.example.projectx;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.projectx.model.UserDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {
    Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String uid = UserDetailsUtil.getUID();
//        if (uid != null)
//            resetNotificationRequest(uid);

        createNotificationChannel();

//        boolean myServiceRunning = isMyServiceRunning(NotificationService.class);
//
//        if (!myServiceRunning) {
//            intent = new Intent(this, NotificationService.class);
//            startService(intent);
//        }


        new Handler().postDelayed(new Runnable() {


            @Override
            public void run() {
                // This method will be executed once the timer is over
                boolean userLoggedIn = UserDetailsUtil.isUserLoggedIn();
                if (userLoggedIn) {
                    boolean myServiceRunning = isMyServiceRunning(NotificationService.class);

                    if (!myServiceRunning) {
                        Intent intent = new Intent(MainActivity.this, LocationFetchService.class);
                        startService(intent);
                    }
                    postLogin();
                } else {

                    Intent i = new Intent(MainActivity.this, SignInActivity.class);
                    startActivity(i);
                    finish();
                }


            }
        }, 500);
    }


    private void postLogin() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("UserDetails").document(UserDetailsUtil.getUID());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        UserDetails userDetails = document.toObject(UserDetails.class);
                        Boolean isBusiness = userDetails.getIsBusiness();

                        if (isBusiness == null) {
                            goToBusinessActivity();
                        } else {
                            Intent i = new Intent(getApplicationContext(), HomeActivity.class);
                            startActivity(i);
                            finish();
                        }
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


    }


    private void goToBusinessActivity() {
        Intent intent = new Intent(getApplicationContext(), BusinessActivity.class);
        startActivity(intent);
        finish();
    }

    private void resetNotificationRequest(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, String> notificationMap = new HashMap<>();

        notificationMap.put("requestNotification", null);
        notificationMap.put("messageNotification", null);


        db.collection("Notifications")
                .document(uid)
                .set(notificationMap)
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


    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Chnalle";
            String description = "Description";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel("Project_X", name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


//    @Override
//    protected void onDestroy() {
////        stopService(intent);
////        Intent broadcastIntent = new Intent();
////        broadcastIntent.setAction("restartservice");
////        broadcastIntent.setClass(this, Restarter.class);
////        this.sendBroadcast(broadcastIntent);
////        super.onDestroy();
//   }

    // [END auth_fui_result]
}