package com.example.projectx;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.projectx.model.MessageModel;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ArrayList<MessageModel> messagesList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            messagesList.add(new MessageModel("Hi", i % 2 == 0 ? ChatListAdapter.MESSAGE_TYPE_IN : ChatListAdapter.MESSAGE_TYPE_OUT));
        }

        ChatListAdapter adapter = new ChatListAdapter(this, messagesList);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }
}