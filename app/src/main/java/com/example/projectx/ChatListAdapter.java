package com.example.projectx;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import lombok.SneakyThrows;

import com.bumptech.glide.Glide;
import com.example.projectx.model.MessageModel;
import com.google.firebase.Timestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    ArrayList<MessageModel> list;
    public String user = null;
    boolean isImageFitToScreen;

    FirebaseStorage storage;
    StorageReference storageReference;

    public ChatListAdapter(Context context, ArrayList<MessageModel> list, String user) { // you can pass other parameters in constructor
        this.context = context;
        this.list = list;
        this.user = user;
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return new RightChatBubbleViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_right_bubble, parent, false));
        }
        return new LeftChatBubbleViewHolder(LayoutInflater.from(context).inflate(R.layout.chat_left_bubble, parent, false));
    }

    @SneakyThrows
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (list.get(position).getSender().equals(user)) {
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
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        Log.d("user", "sender : " + list.get(position).getSender().equals(user));
        Log.d("user1", "user : " + user);

        return list.get(position).getSender().equals(user) ? 1 : 0;
    }


    private class RightChatBubbleViewHolder extends RecyclerView.ViewHolder {

        TextView messageTV, dateTV;
        ImageView imageView;

        RightChatBubbleViewHolder(final View itemView) {
            super(itemView);
            messageTV = itemView.findViewById(R.id.message_text);
            dateTV = itemView.findViewById(R.id.date_text);
            imageView = itemView.findViewById(R.id.message_image);


        }

        void bind(int position) throws ParseException {
            MessageModel messageModel = list.get(position);
            String messageText = messageModel.getMessage();
            if (!messageText.contains("images")) {
                messageTV.setText(messageText);
                //Some issue with recyler view - so we need to set all visiblity and gone pro[erly
                messageTV.setVisibility(View.VISIBLE);

                Glide.with(context).clear(imageView);
                imageView.setVisibility(View.GONE);
            } else {
                StorageReference imageStorage = storageReference.child(messageText);
                imageView.setVisibility(View.VISIBLE);
                messageTV.setVisibility(View.GONE);

                Glide.with(context)
                        .load(imageStorage)
                        .into(imageView);

            }
//            Timestamp ts = new Timestamp();
            if(messageModel.getMessageTime() != null) {
                SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss a");
                Date date = sfd.parse(messageModel.getMessageTime());
                sfd = new SimpleDateFormat("HH:mm a");
               String text =  sfd.format(date);
//                String date = sfd.format(new Date(messageModel.getMessageTime()).getTime());
                dateTV.setText(text);
            }


            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(context, PhotoViewActivity.class);
                    intent.putExtra("ref", messageText);
                    context.startActivity(intent);
                }
            });
        }
    }


    private class LeftChatBubbleViewHolder extends RecyclerView.ViewHolder {

        TextView messageTV, dateTV;
        ImageView imageView;

        LeftChatBubbleViewHolder(final View itemView) {
            super(itemView);
            messageTV = itemView.findViewById(R.id.message_text);
            dateTV = itemView.findViewById(R.id.date_text);
            imageView = itemView.findViewById(R.id.message_image);

        }


        void bind(int position) throws ParseException {
            MessageModel messageModel = list.get(position);
            String messageText = messageModel.getMessage();
            if (!messageText.contains("images"))
                messageTV.setText(messageText);
            else {
                StorageReference imageStorage = storageReference.child(messageText);
                imageView.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(imageStorage)
                        .into(imageView);

            }
            if(messageModel.getMessageTime() != null) {
                SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss a");
                Date date = sfd.parse(messageModel.getMessageTime());
                sfd = new SimpleDateFormat("HH:mm a");
                String text =  sfd.format(date);
//                String date = sfd.format(new Date(messageModel.getMessageTime()).getTime());
                dateTV.setText(text);
            }
        }
    }
}
