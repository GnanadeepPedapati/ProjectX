package com.example.projectx;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

public class RequestListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_list);

        ListView requestListView = (ListView) findViewById(R.id.request_list_item);
        ArrayList<String> requests = new ArrayList<>();
        requests.add("Request1 I Know why it is called Request 1 and Why I do need to call it as Request 2 I am also caleld rreust 3");
        requests.add("Request2");
        RequestListAdapter requestListAdapter = new RequestListAdapter(this.getApplicationContext(), requests);
        requestListView.setAdapter(requestListAdapter);
    }
}