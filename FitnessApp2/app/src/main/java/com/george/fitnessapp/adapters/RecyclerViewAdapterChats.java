package com.george.fitnessapp.adapters;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.george.fitnessapp.Chat_Window_Fragment;
import com.george.fitnessapp.R;
import com.george.fitnessapp.utils.Chat;
import com.george.fitnessapp.utils.Food;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RecyclerViewAdapterChats extends RecyclerView.Adapter<RecyclerViewAdapterChats.MyViewHolder> {

    Context context;
    ArrayList<Chat> chats;

    public RecyclerViewAdapterChats(Context context, ArrayList<Chat> chats) {
        this.context = context;
        this.chats = chats;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.recycler_view_chats_layout, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Chat chat = chats.get(position);

        holder.chatName.setText(chat.getChatName());
        Picasso.get().load(Uri.parse(chat.getChatImage())).into(holder.chatImage);
        if(chat.getMembers().size() > 1){
            holder.membersCount.setText(chat.getMembers().size() + " members");
        }
        else{
            holder.membersCount.setText(chat.getMembers().size() + " member");
        }

        holder.joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Store current user in chat members
                DatabaseReference membersReference = FirebaseDatabase
                        .getInstance()
                        .getReference("Chats")
                        .child(chat.getChatName())
                        .child("Info")
                        .child("members");

                //If chat members doesn't contain current user, add him
                if(!chat.getMembers().contains(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    membersReference.child(String.valueOf(chat.getMembers().size())).setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                }

                //Create bundle with clicked chat object
                Bundle data = new Bundle();
                data.putParcelable("chat", chat);

                //Pass data in new fragment
                Fragment chatWindowFragment = new Chat_Window_Fragment();
                chatWindowFragment.setArguments(data);

                AppCompatActivity activity = (AppCompatActivity)v.getContext();
                //open chat window fragment
                if(activity!=null){
                    activity.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame_layout,chatWindowFragment)
                            .addToBackStack("chatFragment")
                            .commit();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public void FilterList(ArrayList<Chat> filteredChats) {
        chats = filteredChats;
        notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView chatName,membersCount;
        ImageView chatImage;
        Button joinButton;

        public MyViewHolder(@NonNull View view) {
            super(view);

            chatName = view.findViewById(R.id.chatName);
            membersCount = view.findViewById(R.id.membersCount);
            chatImage = view.findViewById(R.id.chatImage);
            joinButton = view.findViewById(R.id.joinButton);
        }
    }
}
