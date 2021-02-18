package com.example.projectx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import com.example.projectx.model.MessageModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Map;

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
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Chats").child("ram");
        addPostEventListener(mDatabase);
//        FirebaseAuth.getInstance()
//                .getCurrentUser()
//                .getDisplayName()
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText edit = (EditText) findViewById(R.id.input);
                mDatabase.child("rahim").setValue(new MessageModel(edit.getText().toString(),ChatListAdapter.MESSAGE_TYPE_IN));
                edit.setText("");
            }
        });
//        displayMessages(mDatabase);
    }
    public void displayMessages(DatabaseReference mDatabase){

        mDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());

                }
                else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));

                }
            }
        });
//        ArrayList<MessageModel> messagesList =(ArrayList<MessageModel>)task.getResult().getValue();

//        Toast.makeText(this, "LoggrfdIn", Toast.LENGTH_LONG).show();

    }

    public void displayMessage(MessageModel message){
        ArrayList<MessageModel> messagesList = new ArrayList<>();
        messagesList.add(message);
        ChatListAdapter adapter = new ChatListAdapter(this, messagesList);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    private void addPostEventListener(DatabaseReference mPostReference) {
        // [START post_value_event_listener]
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
//                Map<String,MessageModel> post = dataSnapshot.getValue(Map.class);
//                MessageModel msg = null;
                MessageModel msg = dataSnapshot.getValue(MessageModel.class);
//                if(post != null){
//                    msg = (MessageModel) post.get("rahim");
//                    Log.d("firebase_push", msg.message.toString());
//                    displayMessage(msg);
//                }
                if(msg != null && msg.message != null){
                    Log.d("firebase_push", msg.message);
                    displayMessage(msg);
                }


            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
            }
        };
        mPostReference.child("rahim").addValueEventListener(postListener);
        // [END post_value_event_listener]
    }
}