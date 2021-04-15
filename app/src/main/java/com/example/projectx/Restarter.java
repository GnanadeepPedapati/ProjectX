package com.example.projectx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.concurrent.TimeUnit;

public class Restarter extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Broadcast Listened", "Service tried to stop");
        Toast.makeText(context, "Service restarted", Toast.LENGTH_SHORT).show();

        WorkRequest saveRequest = new OneTimeWorkRequest.Builder(JobWorker.class)
                .setInitialDelay(10, TimeUnit.SECONDS)
                .addTag("TAG_OUTPUT")
                .build();
        WorkManager
                .getInstance(context)
                .enqueue(saveRequest);
    }


}