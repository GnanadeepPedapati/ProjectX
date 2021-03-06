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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import com.example.projectx.model.MessageModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ProgressBar progressBar;
    ArrayList<MessageModel> messagesList = new ArrayList<>();
    ChatListAdapter adapter;
    //  String chatId = "YD8bboxlMFPUIB2wlCCeQv9F6Ui2_ZyO9cTzInrNrqFEM91AZsA2aU8O2";

    String loggedInUser;
    private String chatId;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chatId = getIntent().getStringExtra("chatId");
        setContentView(R.layout.activity_chat);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        progressBar = findViewById(R.id.progress_bar);
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getApplicationContext(), "User not logged In", Toast.LENGTH_LONG).show();
            return;
        }
        loggedInUser = currentUser.getUid();
        final String otherUser = getIntent().getStringExtra("otherUser");
        chatId = UserDetailsUtil.generateChatId(loggedInUser, otherUser);

        adapter = new ChatListAdapter(this, messagesList, loggedInUser);

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

                if (!edit.getText().toString().trim().equals("")) {
                    MessageModel messageModel = new MessageModel(edit.getText().toString().trim(), loggedInUser, false);
                    mDatabase.child(ChatActivity.this.chatId).child("messages").push().setValue(messageModel);
                    mDatabase.child(ChatActivity.this.chatId).child("lastMessage").setValue(messageModel);

                    mDatabase.child(ChatActivity.this.chatId).child("unseenCount" + otherUser).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.isSuccessful()) {
                                Integer lastUnseenCount = task.getResult().getValue(Integer.class);
                                if (Objects.isNull(lastUnseenCount))
                                    lastUnseenCount = 0;
                                mDatabase.child(ChatActivity.this.chatId).child("unseenCount" + otherUser).setValue(lastUnseenCount + 1);

                            }
                        }
                    });
                }
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
                mPostReference.child(chatId).child("unseenCount" + loggedInUser).setValue(0);


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
                mPostReference.child(chatId).child("unseenCount" + loggedInUser).setValue(0);

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

        mPostReference.child(chatId).child("messages").addChildEventListener(childEventListener);

        mPostReference.child(chatId).child("messages").addListenerForSingleValueEvent(new ValueEventListener() {
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