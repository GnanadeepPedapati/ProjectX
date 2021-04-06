package com.example.projectx;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.graphics.drawable.DrawableCompat;

import com.example.projectx.model.ResponseOverview;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class ResponseListAdapter extends BaseAdapter {


    private Context context;
    private List<ResponseOverview> responsesOverviews;

    public ResponseListAdapter(Context context, List<ResponseOverview> responsesOverviews) {
        this.context = context;
        this.responsesOverviews = responsesOverviews;
    }


    @Override
    public int getCount() {
        return responsesOverviews.size();
    }

    @Override
    public Object getItem(int position) {
        return responsesOverviews.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        if (convertView == null) {
            convertView = LayoutInflater.from(context).
                    inflate(R.layout.response_list_item, parent, false);
        }

        // get current item to be displayed
        ResponseOverview responseOverview = responsesOverviews.get(position);

        // get the TextView for item name and item description
        TextView entityName = (TextView)
                convertView.findViewById(R.id.entityName);
        TextView lastMessage = (TextView)
                convertView.findViewById(R.id.lastMessage);
        TextView messagesCount = (TextView)
                convertView.findViewById(R.id.messagesCount);
        TextView lastReceivedTime = (TextView)
                convertView.findViewById(R.id.lastReceivedTime);

        Button icon = (Button) convertView.findViewById(R.id.round_icon);
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));


        Drawable background = icon.getBackground();
        DrawableCompat.setTint(background, color);
        if (responseOverview.getEntityName() != null && responseOverview.getEntityName().length() != 0) {
            icon.setVisibility(View.VISIBLE);
            icon.setText(responseOverview.getEntityName().substring(0, 1).toUpperCase());
        } else
            icon.setVisibility(View.INVISIBLE);


        //sets the text for item name and item description from the current item object
        entityName.setText(responseOverview.getEntityName());
        lastMessage.setText(responseOverview.getLastMessage());


        if (responseOverview.getLastReceivedTime() != null && responseOverview.getLastReceivedTime() != "") {
            SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss a");
            Date date = null;
            try {
                date = sfd.parse(responseOverview.getLastReceivedTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
            if (sdf1.format(date).compareTo(sdf1.format(new Date())) < 0) {
                sfd = new SimpleDateFormat("dd/MM/yy");
            } else {
                sfd = new SimpleDateFormat("HH:mm a");
            }

            String text = sfd.format(date);

            lastReceivedTime.setText(text);

        } else {

            lastReceivedTime.setText("");
        }
        if (responseOverview.getNewMessageCount() != null && !"0".equals(responseOverview.getNewMessageCount())) {
            messagesCount.setVisibility(View.VISIBLE);
            messagesCount.setText(responseOverview.getNewMessageCount());
        } else
            messagesCount.setVisibility(View.INVISIBLE);

        // returns the view for the current row
        return convertView;
    }
}


