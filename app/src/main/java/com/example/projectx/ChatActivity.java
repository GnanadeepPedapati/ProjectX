package com.example.projectx;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import com.example.projectx.model.MessageModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ProgressBar progressBar;
    ArrayList<MessageModel> messagesList = new ArrayList<>();
    ChatListAdapter adapter = new ChatListAdapter(this, messagesList, "rahim");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        progressBar = findViewById(R.id.progress_bar);
        displayMessage();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Chats");
        addPostEventListener(mDatabase);
//        FirebaseAuth.getInstance()
//                .getCurrentUser()
//                .getDisplayName()
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText edit = (EditText) findViewById(R.id.input);

                if (!edit.getText().toString().trim().equals(""))
                    mDatabase.child("ram_rahim").push().setValue(new MessageModel(edit.getText().toString().trim(), "rahim", false));
                edit.setText("");
            }
        });
//        displayMessages(mDatabase);
    }
//    public void displayMessages(DatabaseReference mDatabase){
//
//        mDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DataSnapshot> task) {
//                if (!task.isSuccessful()) {
//                    Log.e("firebase", "Error getting data", task.getException());
//
//                }
//                else {
//                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
//
//                }
//            }
//        });
////        ArrayList<MessageModel> messagesList =(ArrayList<MessageModel>)task.getResult().getValue();
//
////        Toast.makeText(this, "LoggrfdIn", Toast.LENGTH_LONG).show();
//
//    }

    public void displayMessage() {
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

    }

    private void addPostEventListener(DatabaseReference mPostReference) {
        // [START post_value_event_listener]
//        ValueEventListener postListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // Get Post object and use the values to update the UI
////                Map<String,MessageModel> post = dataSnapshot.getValue(Map.class);
////                MessageModel msg = null;
//                MessageModel msg = dataSnapshot.getValue(MessageModel.class);
////                if(post != null){
////                    msg = (MessageModel) post.get("rahim");
////                    Log.d("firebase_push", msg.message.toString());
////                    displayMessage(msg);
////                }
//                if(msg != null && msg.message != null){
//                    Log.d("firebase_push", msg.message);
//                    displayMessage(msg);
//                }
//
//
//            }
//        };
        ChildEventListener childEventListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("fire", "onChildAdded:" + dataSnapshot.getKey());
                Log.d("firebase", "child child");
                // A new comment has been added, add it to the displayed list
                MessageModel msg = dataSnapshot.getValue(MessageModel.class);
                messagesList.add(msg);
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messagesList.size() - 1);


                // ...
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("fire", "onChildChanged:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so displayed the changed comment.
                MessageModel msg = dataSnapshot.getValue(MessageModel.class);
                String commentKey = dataSnapshot.getKey();
                messagesList.add(msg);
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messagesList.size() - 1);

                // ...
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        mPostReference.child("ram_rahim").addChildEventListener(childEventListener);


        mPostReference.child("ram_rahim").addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.INVISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });
    }

}