package com.example.projectx;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.widget.Toast;

import java.util.Date;

public class MyJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        // runs on the main thread, so this Toast will appear
        Toast.makeText(this, "test"+ new Date().toString(), Toast.LENGTH_LONG).show();
        // perform work here, i.e. network calls asynchronously


        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

        JobInfo jobInfo = new JobInfo.Builder(1234, new ComponentName(this, MyJobService.class))
                .setPersisted(true)
                .setRequiresDeviceIdle(false)
                .setMinimumLatency(6000)
                .setOverrideDeadline(6500   )
                .build();


        jobScheduler.schedule(jobInfo);
        // returning false means the work has been done, return true if the job is being run asynchronously
        return false;
    }


    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        // if the job is prematurely cancelled, do cleanup work here



        // return true to restart the job
        return false;
    }
}
