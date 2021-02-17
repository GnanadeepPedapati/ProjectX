package com.example.projectx;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import com.example.projectx.model.ResponseOverview;

import java.util.ArrayList;

public class ResponsesListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responses_list);

        ListView responsesList = (ListView) findViewById(R.id.responses_list_item);

        ArrayList<ResponseOverview> responseOverviews = new ArrayList<>();
        responseOverviews.add(new ResponseOverview("Playboy Ram", "3", "23:11", "Dude I am done with the assigned work"));
        responseOverviews.add(new ResponseOverview("Rahul", "5", "21:11", "Rey Ekkada"));
        responseOverviews.add(new ResponseOverview("Vineetha", "1", "Yesterday", "Hi Macha"));
        responseOverviews.add(new ResponseOverview("Havish", "2", "Yesterday", "Bye"));
        responseOverviews.add(new ResponseOverview("Akka", "1", "22/01/2021", "Em chesthunav ra"));
        responseOverviews.add(new ResponseOverview("Playboy Ram", "3", "23:11", "Dude I am done with the assigned work"));
        responseOverviews.add(new ResponseOverview("Rahul", "5", "21:11", "Rey Ekkada"));
        responseOverviews.add(new ResponseOverview("Vineetha", "1", "Yesterday", "Hi Macha"));
        responseOverviews.add(new ResponseOverview("Havish", "2", "Yesterday", "Bye"));
        responseOverviews.add(new ResponseOverview("Akka", "1", "22/01/2021", "Em chesthunav ra"));

        responsesList.setAdapter(new ResponseListAdapter(getApplicationContext(), responseOverviews));
    }
}