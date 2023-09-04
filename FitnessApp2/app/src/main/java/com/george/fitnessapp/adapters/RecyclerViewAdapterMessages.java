package com.george.fitnessapp.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.george.fitnessapp.R;
import com.george.fitnessapp.utils.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RecyclerViewAdapterMessages extends RecyclerView.Adapter {

    Context context;
    ArrayList<Message> messages;
    final int SEND_CODE = 1;
    final int RECEIVE_CODE = 2;


    public RecyclerViewAdapterMessages(Context context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //if type of message is SEND use the send layout
        //else if the type is RECEIVE use the receive layout
        if(viewType == SEND_CODE){
            View v = LayoutInflater.from(context).inflate(R.layout.recycler_view_send_layout, parent, false);
            return new SendViewHolder(v);
        }
        else{
            View v = LayoutInflater.from(context).inflate(R.layout.recycler_view_receive_layout, parent, false);
            return new ReceiveViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //Get current message
        Message message = messages.get(position);

        //If message is sent
        if(holder.getClass() == SendViewHolder.class){
            //Create holder of class SendViewHolder
            SendViewHolder viewHolder = (SendViewHolder) holder;

            //Pass message object data to TextViews
            viewHolder.messageSendView.setText(message.getTextMessage());
            viewHolder.timeSendView.setText(message.getCurrentTime());
            Picasso.get().load(Uri.parse(message.getSenderImage())).into(viewHolder.chatSendProfileImage);
        }
        else{
            //Create holder of class ReceiveViewHolder
            ReceiveViewHolder viewHolder = (ReceiveViewHolder) holder;

            //Pass message object data to TextViews
            viewHolder.messageReceiveView.setText(message.getTextMessage());
            viewHolder.timeReceiveView.setText(message.getCurrentTime());
            Picasso.get().load(Uri.parse(message.getSenderImage())).into(viewHolder.chatReceiveProfileImage);
        }
    }

    @Override
    public int getItemViewType(int position) {
        //Get current message
        Message message = messages.get(position);

        //If message's sender is the signed in user, return Send code to Create View Holder
        //else return Receive code
        if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(message.getSenderID())){
            return SEND_CODE;
        }
        else{
            return RECEIVE_CODE;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SendViewHolder extends RecyclerView.ViewHolder{

        TextView messageSendView, timeSendView;
        ImageView chatSendProfileImage;

        public SendViewHolder(@NonNull View view) {
            super(view);

            messageSendView = view.findViewById(R.id.messageSendView);
            timeSendView = view.findViewById(R.id.timeSendView);
            chatSendProfileImage = view.findViewById(R.id.chatSendProfileImage);
        }
    }

    static class ReceiveViewHolder extends RecyclerView.ViewHolder{

        TextView messageReceiveView, timeReceiveView;
        ImageView chatReceiveProfileImage;

        public ReceiveViewHolder(@NonNull View view) {
            super(view);

            messageReceiveView = view.findViewById(R.id.messageReceiveView);
            timeReceiveView = view.findViewById(R.id.timeReceiveView);
            chatReceiveProfileImage = view.findViewById(R.id.chatReceiveProfileImage);

        }
    }
}
