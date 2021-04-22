package com.example.projectx;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.ImmutableMap;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class JobWorker extends Worker {
    Context context;
    FusedLocationProviderClient mFusedLocationClient;
    private final LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();

            updateLastSeenLocationToUser(mLastLocation);
            //latitudeTextView.setText("Latitude: " + mLastLocation.getLatitude() + "");
            //longitTextView.setText("Longitude: " + mLastLocation.getLongitude() + "");
        }
    };

    public JobWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @Override
    public Result doWork() {

        // Do the work here--in this case, upload the images.
        Log.i("in timer", "in timer ++++  ");
        sendNotification();

        updateLocationToUser();


        WorkRequest saveRequest = new OneTimeWorkRequest.Builder(JobWorker.class)
                .setInitialDelay(20, TimeUnit.MINUTES)
                .addTag("TAG_OUTPUT")
                .build();
        WorkManager
                .getInstance(getApplicationContext())
                .enqueue(saveRequest);

        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }

    private void sendNotification() {


        Notification notification = buildNotification("Hello Work Scheduler");
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(0, notification);

    }


    private android.app.Notification buildNotification(String requestNotification) {

        // Create an Intent for the activity you want to start
        Intent resultIntent = new Intent(context, MainActivity.class);
// Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
// Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Project_X")
                .setSmallIcon(R.drawable.text_logo)
                .setContentTitle(requestNotification)
                .setContentText( new Date().toString())
                .setPriority(NotificationCompat.DEFAULT_VIBRATE)
                .setContentIntent(resultPendingIntent)
                .setOnlyAlertOnce(true)
                // Set the intent that will fire when the user taps the notification
                .setAutoCancel(true);
        return builder.build();


    }

    private boolean checkPermissions() {

        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to check
    // if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @SuppressLint("MissingPermission")
    private void updateLocationToUser() {
        // check if permissions are given
        if (checkPermissions()) {

            Log.i("in timer", "Enabled+ permis");

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last
                // location from
                // FusedLocationClient
                // object
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

                Log.i("in timer", "Enabled");
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();

                        } else {
                            // requestNewLocationData();
                            updateLastSeenLocationToUser(location);
                        }
                    }
                });
            }
        }

    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(5);
//        mLocationRequest.setFastestInterval(0);
//        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private void updateLastSeenLocationToUser(Location mLastLocation) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("UserDetails").document(UserDetailsUtil.getUID());
        docRef.update(ImmutableMap.of("latitude", mLastLocation.getLatitude(), "longitude", mLastLocation.getLongitude()));
        Log.i("in timer", "Updated");
        Toast.makeText(context, "Location Updated: I am here", Toast.LENGTH_LONG).show();

    }
}
