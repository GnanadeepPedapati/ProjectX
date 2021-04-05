package com.example.projectx;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectx.model.MessageModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.collect.ImmutableMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.content.ContentValues.TAG;

public class ChatActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ProgressBar progressBar;
    ArrayList<MessageModel> messagesList = new ArrayList<>();
    ChatListAdapter adapter;
    ChildEventListener childEventListener;
    String loggedInUser;
    private String chatId;
    DatabaseReference mDatabase;
    FirebaseFirestore firestoreDb = FirebaseFirestore.getInstance();
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    String updateHasReplied;
    String otherUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chatId = getIntent().getStringExtra("chatId");
        updateHasReplied = getIntent().getStringExtra("updateHasReplied");
        String chatHead = getIntent().getStringExtra("entityName");
        ((AppCompatActivity) this).getSupportActionBar().setTitle(chatHead);


        setContentView(R.layout.activity_chat);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        progressBar = findViewById(R.id.progress_bar);
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getApplicationContext(), "User not logged In", Toast.LENGTH_LONG).show();
            return;
        }

        loggedInUser = currentUser.getUid();
        otherUser = getIntent().getStringExtra("otherUser");
        chatId = UserDetailsUtil.generateChatId(loggedInUser, otherUser);

        adapter = new ChatListAdapter(this, messagesList, loggedInUser);

        displayMessage();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Chats");

        addPostEventListener(mDatabase);
//        FirebaseAuth.getInstance()
//                .getCurrentUser()
//                .getDisplayName()
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText edit = (EditText) findViewById(R.id.input);
                if (!edit.getText().toString().trim().equals("")) {
                    Date date = new Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss a");
                    String dateAndTime = formatter.format(date);
                    MessageModel messageModel = new MessageModel(edit.getText().toString().trim(), loggedInUser, false, dateAndTime);


                    mDatabase.child(ChatActivity.this.chatId).child("messages").push().setValue(messageModel);
                    mDatabase.child(ChatActivity.this.chatId).child("lastMessage").setValue(messageModel);

                    insertNotificationRequest(otherUser);
                    if ("true".equals(updateHasReplied)) {
                        updateUserRequestTable();
                        updateHasReplied = "false";
                    }


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

    private void updateUserRequestTable() {

        // Get all user request table and update them
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usrRef = db.collection("UserRequests");
        Query query = usrRef.whereEqualTo("sender", otherUser).whereEqualTo("receiver", loggedInUser).whereEqualTo("hasReplied", false);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> documents = task.getResult().getDocuments();

                    for (DocumentSnapshot documentSnapshot : documents) {
                        String id = documentSnapshot.getId();
                        usrRef.document(id).update(ImmutableMap.of("hasReplied", true));
                    }
                }
            }
        });


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


    private void insertNotificationRequest(String destinationUserId) {
        Map<String, String> notificationMap = new HashMap<>();

        notificationMap.put("messageNotification", "You have new messages");

        firestoreDb.collection("Notifications")
                .document(destinationUserId)
                .set(notificationMap, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Insert", "DocumentSnapshot successfully written!");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });


    }

    private void addPostEventListener(DatabaseReference mPostReference) {

        childEventListener = new ChildEventListener() {

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
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });
    }

    @Override
    protected void onPause() {
        if (Objects.nonNull(childEventListener))
            mDatabase.child(chatId).child("messages").removeEventListener(childEventListener);
        super.onPause();
    }

}