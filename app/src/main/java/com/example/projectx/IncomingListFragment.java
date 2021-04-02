package com.example.projectx;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link IncomingListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class IncomingListFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    ResponseListAdapter responseListAdapter;
    private SwipeRefreshLayout pullToRefresh;

    ArrayList<ResponseOverview> responseOverviews = new ArrayList<>();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public IncomingListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ResponsesListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static IncomingListFragment newInstance(String param1, String param2) {
        IncomingListFragment fragment = new IncomingListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_incoming_list, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        ListView responsesList = (ListView) getView().findViewById(R.id.incoming_list);
         pullToRefresh = getView().findViewById(R.id.pullToRefresh);

        pullToRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData(); // your code
            }
        });
        getData();
        responseListAdapter = new ResponseListAdapter(getActivity().getApplicationContext(), responseOverviews);
        responsesList.setAdapter(responseListAdapter);
        responsesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ResponseOverview responseOverview = responseOverviews.get(position);
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra("otherUser", responseOverview.getOtherUser());
                intent.putExtra("updateHasReplied", String.valueOf(responseOverview.isHasReplied()));
                startActivity(intent);
            }
        });

    }

    private void getData() {
        responseOverviews.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usrRef = db.collection("UserRequests");
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Chats");


        Set<String> senderSet = new HashSet<String>();


        Query query = usrRef.whereEqualTo("receiver", UserDetailsUtil.getUID());
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (UserRequests userRequest : task.getResult().toObjects(UserRequests.class)) {
                                if (!senderSet.contains(userRequest.getSender())) {
                                    ResponseOverview responseOverview = new ResponseOverview();
                                    senderSet.add(userRequest.getSender());
                                    responseOverview.setEntityName("");
                                    responseOverview.setOtherUser(userRequest.getSender());
                                    responseOverviews.add(responseOverview);

                                    List<UserRequests> userRequests = task.getResult().toObjects(UserRequests.class);

                                    List<UserRequests> filterSameSenderReq = filterSameSenders(userRequests, userRequest.getSender());
                                    for (UserRequests usr : filterSameSenderReq) {
                                        if (usr.isHasReplied() == false)
                                            responseOverview.setHasReplied(true);
                                    }

                                    getDisplayName(responseOverview, userRequest.getSender());
                                    responseListAdapter.notifyDataSetChanged();
                                    String loggedInUser = UserDetailsUtil.getUID();
                                    String chatId = UserDetailsUtil.generateChatId(loggedInUser, userRequest.getSender());

                                    mDatabase.child(chatId).child("unseenCount" + loggedInUser).addValueEventListener(new ValueEventListener() {
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
                                                            return o1.getLastReceivedTime().compareTo(o2.getLastReceivedTime());
                                                        else
                                                            return 1;
                                                    }
                                                });
                                                responseListAdapter.notifyDataSetChanged();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });


                                }
                            }
                            pullToRefresh.setRefreshing(false);


                        } else {
                            Log.d("fires1", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private List<UserRequests> filterSameSenders(List<UserRequests> userRequests, String sender) {
        return userRequests.stream().filter(userRequest -> userRequest.getSender().equals(sender)).collect(Collectors.toList());
    }


    private void getDisplayName(ResponseOverview responseOverview, String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("UserDetails").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        UserDetails userDetails = document.toObject(UserDetails.class);
                        if (userDetails.getIsBusiness() == null || userDetails.getIsBusiness() == Boolean.FALSE) {
                            responseOverview.setEntityName(userDetails.getDisplayName());
                        } else
                            responseOverview.setEntityName((String) document.get("businessName"));

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