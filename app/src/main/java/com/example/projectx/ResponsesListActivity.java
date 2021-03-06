package com.example.projectx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.projectx.model.MessageModel;
import com.example.projectx.model.ResponseOverview;
import com.example.projectx.model.UserRequests;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ResponsesListActivity extends Activity {

    ArrayList<ResponseOverview> responseOverviews = new ArrayList<>();
    ResponseListAdapter responseListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responses_list);


        String requestId = getIntent().getStringExtra("requestId");
        ListView responsesList = (ListView) findViewById(R.id.responses_list_item);
        getData(requestId);

        responseListAdapter = new ResponseListAdapter(getApplicationContext(), responseOverviews);
        responsesList.setAdapter(responseListAdapter);
        responsesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ResponseOverview responseOverview = responseOverviews.get(position);
                Intent intent = new Intent(ResponsesListActivity.this, ChatActivity.class);
                intent.putExtra("otherUser", responseOverview.getOtherUser());
                startActivity(intent);
            }
        });
    }


    public void getData(String requestId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usrRef = db.collection("UserRequests");
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Chats");

        Query query = usrRef.whereEqualTo("requestId", requestId).whereEqualTo("hasReplied", true);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (UserRequests userRequest : task.getResult().toObjects(UserRequests.class)) {
                                ResponseOverview responseOverview = new ResponseOverview();
                                responseOverview.setEntityName(userRequest.getReceiver());
                                responseOverview.setOtherUser(userRequest.getReceiver());
                                responseOverviews.add(responseOverview);
                                responseListAdapter.notifyDataSetChanged();
                                String loggedInUser = UserDetailsUtil.getUID();
                                String chatId = UserDetailsUtil.generateChatId(loggedInUser, userRequest.getReceiver());
                                mDatabase.child(chatId).child("unseenCount" + loggedInUser).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            Integer lastUnseenCount = task.getResult().getValue(Integer.class);
                                            if (Objects.nonNull(lastUnseenCount)) {
                                                responseOverview.setNewMessageCount(lastUnseenCount.toString());
                                                responseListAdapter.notifyDataSetChanged();
                                            }

                                        }
                                    }
                                });


                                mDatabase.child(chatId).child("lastMessage").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        MessageModel lastmessage = snapshot.getValue(MessageModel.class);
                                        if (Objects.nonNull(lastmessage)) {
                                            responseOverview.setLastMessage(lastmessage.getMessage());
                                            // responseOverview.setLastReceivedTime(lastmessage.getMessageTime().toDate().toString());
                                            responseListAdapter.notifyDataSetChanged();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });


                            }


                        } else {
                            Log.d("fires1", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}