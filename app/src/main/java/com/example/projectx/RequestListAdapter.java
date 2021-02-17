package com.example.projectx;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class RequestListAdapter extends BaseAdapter {

    private Context context;
    private List<String> listItems;

    RequestListAdapter(Context context, List<String> listItems) {

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
        String name = listItems.get(position);

        // get the TextView for item name and item description
        TextView textViewItemName = (TextView)
                convertView.findViewById(R.id.text_view_item_name);
        TextView textViewItemDescription = (TextView)
                convertView.findViewById(R.id.text_view_item_description);

        //sets the text for item name and item description from the current item object
        textViewItemName.setText(name);
        textViewItemDescription.setText("I am the description");

        // returns the view for the current row
        return convertView;
    }
}
