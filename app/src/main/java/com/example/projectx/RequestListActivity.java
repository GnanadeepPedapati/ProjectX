package com.example.projectx;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.example.projectx.model.businessmodels.RequestListModel;

import java.util.ArrayList;

public class RequestListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_list);

        ListView requestListView = (ListView) findViewById(R.id.request_list_item);
        ArrayList<RequestListModel> requests = new ArrayList<>();

        RequestListAdapter requestListAdapter = new RequestListAdapter(this.getApplicationContext(), requests);
        requestListView.setAdapter(requestListAdapter);
    }
}