package com.example.projectx;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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
    StorageReference storageReference;
    private Uri filePath;

    private static final int STORAGE_PERMISSION_CODE = 123;


    private int PICK_IMAGE_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

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
                    sendMessage(edit.getText().toString().trim());
                }
                edit.setText("");
            }
        });

        ImageButton attachButton = findViewById(R.id.attachFile);
        attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestStoragePermission();
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    attachFile();
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


    @Override
    protected void onResume() {
        if (Objects.nonNull(childEventListener))
            mDatabase.child(chatId).child("messages").addChildEventListener(childEventListener);
        super.onResume();
    }

    private void attachFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

            Cursor returnCursor =
                    getContentResolver().query(filePath, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();

            Toast.makeText(getApplicationContext(), "Selected" + returnCursor.getString(nameIndex), Toast.LENGTH_LONG).show();
            uploadImage();
        }
    }

    private void uploadImage() {
        if (filePath != null) {

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(this);


            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref
                    = storageReference
                    .child(
                            "images/"
                                    + UUID.randomUUID().toString());
            String path = ref.getPath();


            // adding listeners on upload
            // or failure of image
            ref.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(
                                        UploadTask.TaskSnapshot taskSnapshot) {

                                    // Image uploaded successfully
                                    // Dismiss dialog
                                    progressDialog.dismiss();
                                    Toast
                                            .makeText(getApplicationContext(),
                                                    "Image Uploaded!!",
                                                    Toast.LENGTH_SHORT)
                                            .show();

                                    sendMessage(path);

                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            // Error, Image not uploaded
                            progressDialog.dismiss();
                            Toast
                                    .makeText(getApplicationContext(),
                                            "Failed " + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress
                                            = (100.0
                                            * taskSnapshot.getBytesTransferred()
                                            / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage(
                                            "Uploaded "
                                                    + (int) progress + "%");
                                }
                            });
        }
    }


    private void sendMessage(String message) {

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss a");
        String dateAndTime = formatter.format(date);
        MessageModel messageModel = new MessageModel(message, loggedInUser, false, dateAndTime);


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


    //Requesting permission
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }


    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @lombok.NonNull String[] permissions, @lombok.NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the permission. ", Toast.LENGTH_LONG).show();
                requestStoragePermission();
            }
        }
    }

}