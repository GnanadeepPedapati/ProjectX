package com.example.projectx.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.example.projectx.MainActivity;
import com.example.projectx.R;
import com.example.projectx.Restarter;
import com.example.projectx.UserDetailsUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

public class NotificationService extends Service {

    int MINUTES = 1; // The delay in minutes
    private String previousMessageNotification;
    private String previousRequestNotification;
    private Timer timer;
    private TimerTask timerTask;


    public NotificationService() {

    }

    public static boolean isAppRunning(final Context context, final String packageName) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        if (procInfos != null) {
            for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {


                if (processInfo.processName.equals(packageName) && processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    public void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                createAndNotify();
            }
        };
        timer.schedule(timerTask, 1000, 1000 * 5 * MINUTES); //
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onCreate() {
        startTimer();
        Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
    }

    private void createAndNotify() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        NotificationService notificationService = this;
        String uid = UserDetailsUtil.getUID();
        if (uid != null) {
            DocumentReference docRef = db.collection("Notifications").document(uid);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> data = document.getData();
                            String requestNotification = null;
                            Log.d("Inert", "DocumentSnapshot data: " + document.getData());

                            if (data.containsKey("requestNotification") && data.get("requestNotification") != null) {
                                requestNotification = data.get("requestNotification").toString();
                            }
                            String messageNotification = null;

                            if (data.containsKey("messageNotification") && data.get("messageNotification") != null) {

                                messageNotification = data.get("messageNotification").toString();
                            }

                            boolean appRunning = isAppRunning(notificationService, "com.example.projectx");

                            Log.d("App running", Boolean.toString(appRunning));

                            if (appRunning) {
                                showRequestNotification(notificationService, requestNotification, null);

                            } else {
                                if (requestNotification != null || messageNotification != null) {


                                    boolean isSameNotification = sameNotification(requestNotification, messageNotification);
                                    if (!isSameNotification) {
                                        Notification notification = buildNotification(requestNotification, messageNotification);
                                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(notificationService);
                                        notificationManager.notify(0, notification);
                                        notificationService.previousMessageNotification = messageNotification;
                                        notificationService.previousRequestNotification = requestNotification;
                                    }

                                }
                            }
                        } else {
                            Log.d("Inert", "No such document");
                        }
                    } else {
                        Log.d("Inert", "get failed with ", task.getException());
                    }
                }
            });
        }

    }

    private void showRequestNotification(NotificationService service, String requestNotification, String messageNotification) {
        if (requestNotification != null) {
            Notification notification = buildRequesNotification(requestNotification, messageNotification);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(service);
            notificationManager.notify(0, notification);
        }
        resetNotificationRequest(UserDetailsUtil.getUID());
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

    private boolean sameNotification(String requestNotification, String messageNotification) {
        boolean reqEquals, messageEquals;

        if (requestNotification != null) {
            if (requestNotification.equals(previousRequestNotification) == false) {
                return false;
            }
//            else{
//                reqEquals = true;
//            }
        } else {
            if (previousRequestNotification != null) {
                return false;
            }
//            else{
//                reqEquals = true;
//            }
        }

        if (messageNotification != null) {
            return messageNotification.equals(previousMessageNotification) != false;
//            else{
//                messageEquals = true;
//            }
        } else {
            return previousMessageNotification == null;
//            else{
//                messageEquals = true;
//            }
        }
    }

    private android.app.Notification buildNotification(String requestNotification, String messageNotification) {

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
                .setContentTitle("You have new notifications. Tap to view")
                .setPriority(NotificationCompat.DEFAULT_VIBRATE)
                .setStyle(buildInboxStyle(requestNotification, messageNotification))
                .setContentIntent(resultPendingIntent)
                // Set the intent that will fire when the user taps the notification
                .setAutoCancel(true);
        return builder.build();


    }

    private android.app.Notification buildRequesNotification(String requestNotification, String messageNotification) {

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
                .setContentTitle("You have new request. Tap to view")
                .setPriority(NotificationCompat.DEFAULT_VIBRATE)
                .setContentIntent(resultPendingIntent)
                // Set the intent that will fire when the user taps the notification
                .setAutoCancel(true);
        return builder.build();

    }

    private NotificationCompat.Style buildInboxStyle(String requestNotification, String messageNotification) {


        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        if (requestNotification != null)
            inboxStyle.addLine(requestNotification);
        if (messageNotification != null)
            inboxStyle.addLine(messageNotification);
        return inboxStyle;
    }

    @Override
    public int onStartCommand(final Intent intent,
                              final int flags,
                              final int startId) {
        return START_STICKY;

        //your code
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stoptimertask();
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show();
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
    }
}