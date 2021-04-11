package com.example.projectx;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.projectx.model.businessmodels.RequestListModel;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import lombok.SneakyThrows;

public class RequestListAdapter extends BaseAdapter {

    private Context context;
    private List<RequestListModel> listItems;
    StorageReference storageReference;

    RequestListAdapter(Context context, List<RequestListModel> listItems) {

        this.context = context;
        this.listItems = listItems;
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Object getItem(int position) {
        return listItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SneakyThrows
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // inflate the layout for each list row
        if (convertView == null) {
            convertView = LayoutInflater.from(context).
                    inflate(R.layout.request_list_card, parent, false);
        }

        // get current item to be displayed
        RequestListModel requestListModel = listItems.get(position);

        // get the TextView for item name and item description
        TextView requestText = (TextView)
                convertView.findViewById(R.id.request_text);
        TextView createDate = (TextView)
                convertView.findViewById(R.id.request_create_time);
        Button responsesCount = (Button)
                convertView.findViewById(R.id.responses_count);
        responsesCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ResponsesListActivity.class);
                intent.putExtra("requestId", requestListModel.getRequestId());
                intent.putExtra("requestText", requestListModel.getRequest());
                intent.putExtra("imageUrl", requestListModel.getImageUrl());
                intent.putExtra("createdDate", requestListModel.getCreatedAt());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Toast.makeText(context, requestListModel.getRequestId(), Toast.LENGTH_LONG).show();
                context.startActivity(intent);
            }
        });
        View expandButton = convertView.findViewById(R.id.request_heading);
        ImageView expandedImageView = convertView.findViewById(R.id.expanded_image_view);

        ImageView imageView = (ImageView) convertView.findViewById(R.id.bitmap_image);
        if (Objects.nonNull(requestListModel.getImageUrl()) && requestListModel.getImageUrl() != "") {
            StorageReference imageStorage = storageReference.child(requestListModel.getImageUrl());
            imageView.setVisibility(View.VISIBLE);
            expandButton.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(imageStorage)
                    .fitCenter()
                    .thumbnail(0.1f).circleCrop()
                    .into(imageView);

            Glide.with(context)
                    .clear(expandedImageView);
        } else {
            Glide.with(context)
                    .clear(imageView);
            Glide.with(context)
                    .clear(expandedImageView);

        }

        expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.nonNull(requestListModel.getImageUrl()) && requestListModel.getImageUrl() != "") {

                    StorageReference imageStorage = storageReference.child(requestListModel.getImageUrl());
                    imageView.setVisibility(View.GONE);
                    Glide.with(context).clear(imageView);
                    expandedImageView.setVisibility(View.VISIBLE);
                    Glide.with(context)
                            .load(imageStorage)
                            .fitCenter()
                            .useAnimationPool(true)
                            .into(expandedImageView);
                }
            }
        });


        if(requestListModel.getResponsesCount() == 0){
            responsesCount.setText("No Responses Yet");
            responsesCount.setEnabled(true);
        }
        else
        {
            responsesCount.setText("View all " +String.valueOf(requestListModel.getResponsesCount()) + " Responses");
            responsesCount.setEnabled(true);

        }

        //sets the text for item name and item description from the current item object
        requestText.setText(requestListModel.getRequest());
        if (requestListModel.getCreatedAt() != null && requestListModel.getCreatedAt() != "") {
            SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss a");
            Date date = sfd.parse(requestListModel.getCreatedAt());
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
            if (sdf1.format(date).compareTo(sdf1.format(new Date())) < 0) {
                sfd = new SimpleDateFormat("dd/MM/yyyy HH:mm a");
            } else {
                sfd = new SimpleDateFormat("HH:mm a");
            }

            String text = sfd.format(date);
//                String date = sfd.format(new Date(messageModel.getMessageTime()).getTime());
//                dateTV.setText(text);
            createDate.setText(text);
        }
        else
            createDate.setText("");

//        createDate.setText(requestListModel.getCreatedAt().toDate().toString());

        // returns the view for the current row
        return convertView;
    }
}
