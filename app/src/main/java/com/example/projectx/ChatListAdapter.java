package com.example.projectx;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectx.model.MessageModel;

import java.text.DateFormat;
import java.util.ArrayList;

public class ChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    ArrayList<MessageModel> list;
    public String user = null;

    public ChatListAdapter(Context context, ArrayList<MessageModel> list,String user) { // you can pass other parameters in constructor
        this.context = context;
        this.list = list;
        this.user = user;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return new RightChatBubbleViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_right_bubble, parent, false));
        }
        return new LeftChatBubbleViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_left_bubble, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (list.get(position).sender.equals(user)) {
            ((RightChatBubbleViewHolder) holder).bind(position);
        } else {
            ((LeftChatBubbleViewHolder) holder).bind(position);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    @Override
    public int getItemViewType(int position) {
        Log.d("user","sender : "+list.get(position).sender.equals(user));
        Log.d("user1","user : "+user);

        return list.get(position).sender.equals(user)?1:0;
    }



    private class RightChatBubbleViewHolder extends RecyclerView.ViewHolder {

        TextView messageTV,dateTV;
        RightChatBubbleViewHolder(final View itemView) {
            super(itemView);
            messageTV = itemView.findViewById(R.id.message_text);
            dateTV = itemView.findViewById(R.id.date_text);
        }
        void bind(int position) {
            MessageModel messageModel = list.get(position);
            messageTV.setText(messageModel.message);
            dateTV.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(messageModel.messageTime));
        }
    }


    private class LeftChatBubbleViewHolder extends RecyclerView.ViewHolder {

        TextView messageTV, dateTV;

        LeftChatBubbleViewHolder(final View itemView) {
            super(itemView);
            messageTV = itemView.findViewById(R.id.message_text);
            dateTV = itemView.findViewById(R.id.date_text);
        }

        void bind(int position) {
            MessageModel messageModel = list.get(position);
            messageTV.setText(messageModel.message);
            dateTV.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(messageModel.messageTime));
        }
    }
}
