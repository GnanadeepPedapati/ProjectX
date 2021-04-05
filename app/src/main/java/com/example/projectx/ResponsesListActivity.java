package com.example.projectx;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.projectx.model.MessageModel;
import com.example.projectx.model.ResponseOverview;
import com.example.projectx.model.UserDetails;
import com.example.projectx.model.UserRequests;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class ResponsesListActivity extends Activity {

    ArrayList<ResponseOverview> responseOverviews = new ArrayList<>();
    ResponseListAdapter responseListAdapter;
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Chats");
    int callsInitiated = 0;
    private SwipeRefreshLayout pullToRefresh;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responses_list);

        pullToRefresh = findViewById(R.id.pullToRefresh);


        pullToRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        String requestId = getIntent().getStringExtra("requestId");
        String requestText = getIntent().getStringExtra("requestText");

        TextView requestHeading = findViewById(R.id.request_text_heading);
        requestHeading.setText(requestText);
        ListView responsesList = (ListView) findViewById(R.id.incoming_list);
        getData(requestId); // your code

        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData(requestId); // your code
            }
        });


        responseListAdapter = new ResponseListAdapter(getApplicationContext(), responseOverviews);
        responsesList.setAdapter(responseListAdapter);
        responsesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ResponseOverview responseOverview = responseOverviews.get(position);
                Intent intent = new Intent(ResponsesListActivity.this, ChatActivity.class);
                intent.putExtra("otherUser", responseOverview.getOtherUser());
                intent.putExtra("entityName", responseOverview.getEntityName());
                startActivity(intent);
            }
        });
    }


    public void getData(String requestId) {
        responseOverviews.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usrRef = db.collection("UserRequests");

        Query query = usrRef.whereEqualTo("requestId", requestId).whereEqualTo("hasReplied", true);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (UserRequests userRequest : task.getResult().toObjects(UserRequests.class)) {
                                ResponseOverview responseOverview = new ResponseOverview();
                                responseOverview.setEntityName("");
                                responseOverview.setOtherUser(userRequest.getReceiver());
                                responseOverviews.add(responseOverview);
                                responseListAdapter.notifyDataSetChanged();

                                getDisplayName(responseOverview, userRequest.getReceiver());




                            }


                        } else {
                            Log.d("fires1", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


    private void addLastMessage(ResponseOverview responseOverview, String chatId) {

        mDatabase.child(chatId).child("unseenCount" + UserDetailsUtil.getUID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer lastUnseenCount = snapshot.getValue(Integer.class);

                if (Objects.nonNull(lastUnseenCount)) {
                    responseOverview.setNewMessageCount(lastUnseenCount.toString());
                    responseListAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        responseListAdapter.notifyDataSetChanged();

        mDatabase.child(chatId).child("lastMessage").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                MessageModel lastmessage = snapshot.getValue(MessageModel.class);
                if (Objects.nonNull(lastmessage)) {
                    responseOverview.setLastMessage(lastmessage.getMessage());
                    responseOverview.setLastReceivedTime(lastmessage.getMessageTime());
                    responseOverviews.sort(new Comparator<ResponseOverview>() {
                        @Override
                        public int compare(ResponseOverview o1, ResponseOverview o2) {
                            if (o1.getLastReceivedTime() != null && o2.getLastReceivedTime() != null)
                                return o2.getLastReceivedTime().compareTo(o1.getLastReceivedTime());
                            else
                                return -1;
                        }
                    });
                    responseListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        responseListAdapter.notifyDataSetChanged();
    }


    private void incrementCall() {
        callsInitiated++;
        pullToRefresh.setRefreshing(true);
    }


    private void decrementCall() {
        callsInitiated--;
        if (callsInitiated == 0)
            pullToRefresh.setRefreshing(false);
    }


    private void getDisplayName(ResponseOverview responseOverview, String uid) {

        incrementCall();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("UserDetails").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                decrementCall();
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        UserDetails userDetails = document.toObject(UserDetails.class);
                        if (userDetails.getIsBusiness() == null || userDetails.getIsBusiness() == Boolean.FALSE) {
                            responseOverview.setEntityName(userDetails.getDisplayName());
                        } else
                            responseOverview.setEntityName((String) document.get("businessName"));
                        String loggedInUser = UserDetailsUtil.getUID();
                        String chatId = UserDetailsUtil.generateChatId(loggedInUser, uid);
                        responseListAdapter.notifyDataSetChanged();
                        addLastMessage(responseOverview, chatId);
                        responseListAdapter.notifyDataSetChanged();

                        Log.d("Inert", "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d("Inert", "No such document");
                    }
                } else {
                    Log.d("Inert", "get failed with ", task.getException());
                }
            }
        });
    }

}