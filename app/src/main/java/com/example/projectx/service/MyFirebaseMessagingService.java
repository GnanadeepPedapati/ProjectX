package com.example.projectx.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.example.projectx.MainActivity;
import com.example.projectx.R;
import com.example.projectx.UserDetailsUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";


    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {

        String uid = UserDetailsUtil.getUID();
        if (uid != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("token", token);
            db.collection("UserDetails")
                    .document(UserDetailsUtil.getUID())
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

        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {


        Notification notification = buildNotification(remoteMessage.getData().get("message"));
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, notification);

    }


    private android.app.Notification buildNotification(String requestNotification) {

        // Create an Intent for the activity you want to start
        Intent resultIntent = new Intent(this, MainActivity.class);
// Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
// Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Project_X")
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle(requestNotification)
                .setPriority(NotificationCompat.DEFAULT_VIBRATE)
                .setContentIntent(resultPendingIntent)
                .setOnlyAlertOnce(true)
                // Set the intent that will fire when the user taps the notification
                .setAutoCancel(true);
        return builder.build();


    }
}
