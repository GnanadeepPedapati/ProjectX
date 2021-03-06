package com.example.projectx;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.projectx.model.businessmodels.RequestListModel;

import java.util.List;

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
        createDate.setText(requestListModel.getCreatedAt().toDate().toString());

        // returns the view for the current row
        return convertView;
    }
}
