package com.george.fitnessapp;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.george.fitnessapp.adapters.RecyclerViewAdapterChats;
import com.george.fitnessapp.utils.Chat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Chats_Fragment extends Fragment {

    android.widget.SearchView chatSearchView;
    RecyclerView chatsRecyclerView,availableChatsRecyclerView;
    ArrayList<Chat> joinedChats,availableChats;
    LinearLayoutManager layoutManager,layoutManager1;

    Button joinChatButton;
    ConstraintLayout chatLayout;
    ImageView clickView;

    RecyclerViewAdapterChats adapterChats,adapterChats1;

    DatabaseReference chatReference;

    Animation bottom_sheet_fade_in_animation,fade_in_animation,bottom_sheet_fade_out_animation,fade_out_animation;

    public Chats_Fragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        chatReference = FirebaseDatabase.getInstance().getReference("Chats");

        joinedChats = new ArrayList<>();
        availableChats = new ArrayList<>();

        //Create layout managers and adapters
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager1 = new LinearLayoutManager(getContext());
        layoutManager1.setOrientation(RecyclerView.VERTICAL);

        adapterChats = new RecyclerViewAdapterChats(getContext(), joinedChats);
        adapterChats1 = new RecyclerViewAdapterChats(getContext(), availableChats);

        return inflater.inflate(R.layout.fragment_chats,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Find button
        joinChatButton = view.findViewById(R.id.joinChatButton);
        chatLayout = view.findViewById(R.id.chatsLayout);

        //Find animation
        bottom_sheet_fade_in_animation = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_sheet_fade_in);
        bottom_sheet_fade_out_animation = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_sheet_fade_out);
        fade_in_animation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        fade_out_animation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);

        //ClickView is an imageView outside chat bottom sheet to close it whenever its clicked
        clickView = view.findViewById(R.id.clickView);

        //Set visibility to gone
        chatLayout.setVisibility(View.GONE);
        clickView.setVisibility(View.GONE);

        //Set on click listener to layout to override on click listener of imageview
        chatLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Don't do anything
            }
        });

        joinChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatLayout.setVisibility(View.VISIBLE);
                clickView.setVisibility(View.VISIBLE);
                chatLayout.clearAnimation();
                chatLayout.startAnimation(bottom_sheet_fade_in_animation);
                clickView.clearAnimation();
                clickView.startAnimation(fade_in_animation);
            }
        });

        clickView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatLayout.setVisibility(View.GONE);
                clickView.setVisibility(View.GONE);
                chatLayout.clearAnimation();
                chatLayout.startAnimation(bottom_sheet_fade_out_animation);
                clickView.clearAnimation();
                clickView.startAnimation(fade_out_animation);
            }
        });

        //Find Recycler View
        chatsRecyclerView = view.findViewById(R.id.chatsRecyclerView);
        availableChatsRecyclerView = view.findViewById(R.id.availableChatsRecyclerView);

        //Find Search View
        chatSearchView = view.findViewById(R.id.chatSearch);

        //Set on query listener to filter results every time text is changed
        chatSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                FilterByText(newText);
                return false;
            }
        });

        //Set layout manager and adapter to recycler views
        chatsRecyclerView.setLayoutManager(layoutManager);
        chatsRecyclerView.setAdapter(adapterChats);

        availableChatsRecyclerView.setAdapter(adapterChats1);
        availableChatsRecyclerView.setLayoutManager(layoutManager1);

        chatReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot chats) {
                for (DataSnapshot chat : chats.getChildren()) {
                    //Create Chat object and set name,image
                    Chat testChat= chat.child("Info").getValue(Chat.class);

                    //If user is part of chat
                    if (testChat.getMembers().contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        joinedChats.add(testChat);
                    } else {
                        availableChats.add(testChat);
                    }

                }
                adapterChats.notifyDataSetChanged();
                adapterChats1.notifyDataSetChanged();

                //Set layout manager and adapter to recycler views
                chatsRecyclerView.setLayoutManager(layoutManager);
                chatsRecyclerView.setAdapter(adapterChats);

                availableChatsRecyclerView.setAdapter(adapterChats1);
                availableChatsRecyclerView.setLayoutManager(layoutManager1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "onCancelled: ", error.toException());
            }
        });
    }

    private void FilterByText(String newText) {
        //Create a filtered list with chats
        ArrayList<Chat> filteredChats = new ArrayList<>();

        //Add chats to list based on user's input
        for (Chat chat:joinedChats) {
            if(chat.getChatName().toLowerCase().contains(newText.toLowerCase())){
                filteredChats.add(chat);
            }
        }

        //update recycler view adapter
        adapterChats.FilterList(filteredChats);
    }
}
