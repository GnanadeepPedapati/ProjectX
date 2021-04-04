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
import android.widget.Toast;

import com.example.projectx.model.Requests;
import com.example.projectx.model.ResponseOverview;
import com.example.projectx.model.businessmodels.RequestListModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RequestsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RequestsListFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ArrayList<RequestListModel> requests = new ArrayList<>();
    RequestListAdapter requestListAdapter;
    private SwipeRefreshLayout pullToRefresh;


    public RequestsListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RequestsList.
     */
    // TODO: Rename and change types and number of parameters
    public static RequestsListFragment newInstance(String param1, String param2) {
        RequestsListFragment fragment = new RequestsListFragment();
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
        return inflater.inflate(R.layout.fragment_requests_list, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        ListView requestListView = (ListView) getView().findViewById(R.id.request_list_item);
        requestListAdapter = new RequestListAdapter(getActivity().getApplicationContext(), requests);

        pullToRefresh = getView().findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData(UserDetailsUtil.getUID()); // your code
            }
        });


        pullToRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        getData(UserDetailsUtil.getUID());
        requestListView.setAdapter(requestListAdapter);

        requestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getContext(), ResponsesListActivity.class);
                intent.putExtra("requestId", requests.get(i).getRequestId());
                intent.putExtra("requestText", requests.get(i).getRequest());
                Toast.makeText(getContext(), requests.get(i).getRequestId(), Toast.LENGTH_LONG).show();
                startActivity(intent);
            }
        });
    }


    public void getData(String userId) {
        requests.clear();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usrRef = db.collection("Requests");
        Query query = usrRef.whereEqualTo("createdBy", userId);

        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("fires", "hello");

                            for (Requests request : task.getResult().toObjects(Requests.class)) {

                                convertToRequestListModel(request);
                            }
                            pullToRefresh.setRefreshing(false);

                        } else {
                            Log.d("fires", "Error getting documents: ", task.getException());
                        }
                    }

                    private void convertToRequestListModel(Requests data) {

                        RequestListModel requestListModel = new RequestListModel();
                        requestListModel.setRequestId(data.getRequestId());
                        requestListModel.setRequest(data.getRequest());
                        requestListModel.setCreatedAt(data.getCreatedAt());
                        requestListModel.setImageUrl(data.getImageUrl());
                        getResponseCount(requestListModel);

                        return;
                    }

                    private void getResponseCount(RequestListModel requestListModel) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        CollectionReference usrRef = db.collection("UserRequests");
                        Query query = usrRef.whereEqualTo("requestId", requestListModel.getRequestId()).whereEqualTo("hasReplied", true);

                        query.get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("fires1", task.getResult().getDocuments().toString());
                                            int size = task.getResult().getDocuments().size();
                                            requests.add(requestListModel);
                                            requestListModel.setResponsesCount(size);

                                            requests.sort(new Comparator<RequestListModel>() {
                                                @Override
                                                public int compare(RequestListModel o1, RequestListModel o2) {
                                                    if (o1.getCreatedAt() != null && o2.getCreatedAt() != null)
                                                        return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                                                    else
                                                        return -1;
                                                }
                                            });
                                            requestListAdapter.notifyDataSetChanged();

                                        } else {
                                            Log.d("fires1", "Error getting documents: ", task.getException());
                                        }
                                    }
                                });

                    }


                });

    }


}