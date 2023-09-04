package com.george.fitnessapp;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.george.fitnessapp.adapters.RecyclerViewAdapterMessages;
import com.george.fitnessapp.utils.Chat;
import com.george.fitnessapp.utils.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Chat_Window_Fragment extends Fragment {

    LinearLayoutManager layoutManager;
    RecyclerViewAdapterMessages adapterMessages;

    RecyclerView messagesRecyclerView;
    EditText messageEditText;
    ImageView sendButton,chatImage,chatBackButton;
    TextView chatName;

    DatabaseReference messageReference;
    FirebaseUser currentUser;

    Chat currentChat;
    ArrayList<Message> messages;

    public Chat_Window_Fragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle data = getArguments();
        if(data != null) {
            currentChat = data.getParcelable("chat");
        }

        //Move to messages folder in Realtime database
        messageReference = FirebaseDatabase.getInstance().getReference("Chats")
                .child(currentChat.getChatName())
                .child("Messages");

        //Get current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        messages = new ArrayList<>();

        //Create layout and adapter
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.setStackFromEnd(true);
        adapterMessages = new RecyclerViewAdapterMessages(getContext(),messages);

        messageReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //Add message object from database to Arraylist
                Message testMessage;
                testMessage = snapshot.getValue(Message.class);

                messages.add(testMessage);
                adapterMessages.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "onCancelled: ", error.toException());
            }
        });

        return inflater.inflate(R.layout.fragment_chat_window,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Find Chat Info
        chatImage = view.findViewById(R.id.windowChatImage);
        chatName = view.findViewById(R.id.windowChatName);

        //Set chat info
        if(currentChat != null) {
            chatName.setText(currentChat.getChatName());
            Picasso.get().load(currentChat.getChatImage()).into(chatImage);
        }

        //Find back arrow imageview
        chatBackButton = view.findViewById(R.id.chatBackButton);

        chatBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment chatsFragment = new Chats_Fragment();

                AppCompatActivity activity = (AppCompatActivity)v.getContext();
                //open chats fragment
                if(activity!=null){
                    activity.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame_layout,chatsFragment)
                            .addToBackStack("chatWindowFragment")
                            .commit();
                }
            }
        });

        //Find Recycler View
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);

        //Setup Recycler View
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(adapterMessages);

        //Find EditText
        messageEditText = view.findViewById(R.id.messageEditText);

        //Find ImageView
        sendButton = view.findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create new Message instance
                Message message = new Message();
                message.setTextMessage(messageEditText.getText().toString());
                message.setSenderID(currentUser.getUid());
                message.setCurrentTime(getCurrentTime());
                message.setTimestamp(System.currentTimeMillis()/1000);

                //If user has profile picture set his profile picture
                //Else if user hasn't profile image then set the app foreground icon for his picture
                if(currentUser.getPhotoUrl()!= null){
                    message.setSenderImage(currentUser.getPhotoUrl().toString());
                }else{
                    //String path for no profile image from storage
                    String path = "https://firebasestorage.googleapis.com/v0/b/fitness-app-ea2a6.appspot.com/o/Users%2Fno-profile-img.jpg?alt=media&token=c8e0c065-9c9e-4ec2-ab55-b7b4ad71f5a0";
                    message.setSenderImage(path);
                }

                messageReference.push().setValue(message);

                messageEditText.setText("");

            }
        });
    }

    private String getCurrentTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm aa");
        return simpleDateFormat.format(c.getTime());
    }


}
