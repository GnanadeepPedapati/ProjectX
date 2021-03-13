package com.example.projectx;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.projectx.model.businessmodels.RequestListModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import lombok.SneakyThrows;

public class RequestListAdapter extends BaseAdapter {

    private Context context;
    private List<RequestListModel> listItems;

    RequestListAdapter(Context context, List<RequestListModel> listItems) {

        this.context = context;
        this.listItems = listItems;
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
        TextView responsesCount = (TextView)
                convertView.findViewById(R.id.responses_count);

        responsesCount.setText(String.valueOf(requestListModel.getResponsesCount())+" Responses");
        //sets the text for item name and item description from the current item object
        requestText.setText(requestListModel.getRequest());
        if(requestListModel.getCreatedAt() != null && requestListModel.getCreatedAt() != "") {
            SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss a");
            Date date = sfd.parse(requestListModel.getCreatedAt());
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
            if(sdf1.format(date).compareTo(sdf1.format(new Date())) >0){
                sfd = new SimpleDateFormat("dd/MM/yyyy HH:mm a");
            }
            else{
                sfd = new SimpleDateFormat("HH:mm a");
            }

            String text =  sfd.format(date);
//                String date = sfd.format(new Date(messageModel.getMessageTime()).getTime());
//                dateTV.setText(text);
            createDate.setText(text);
        }

//        createDate.setText(requestListModel.getCreatedAt().toDate().toString());

        // returns the view for the current row
        return convertView;
    }
}
