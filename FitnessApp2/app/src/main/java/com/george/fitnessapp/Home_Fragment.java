package com.george.fitnessapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Home_Fragment extends Fragment {

    TextView username,waterConsumed,foodConsumed,kcalPercentage;
    ImageView profilePic;
    ProgressBar kcalProgress;
    Button addButton,chatsButton,statisticsButton;

    FirebaseUser currentUser;
    DatabaseReference AMRReference,todayMealReference;

    CardView caloriesLayout, waterLayout, chatLayout, statisticsLayout;

    SimpleDateFormat sdf;
    Calendar c;

    Animation fade_in_animation;

    public Home_Fragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Get current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //Initialize Reference
        if(currentUser!= null){
            AMRReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid()).child("Info");
            todayMealReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
            if(todayMealReference.child("Meals").getKey()!=null){
                todayMealReference = todayMealReference.child("Meals");
            }
        }

        return inflater.inflate(R.layout.fragment_home,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Find TextViews
        username = view.findViewById(R.id.homeUsername);
        kcalPercentage = view.findViewById(R.id.kcalTodayPercentage);
        waterConsumed = view.findViewById(R.id.waterConsumed);
        foodConsumed = view.findViewById(R.id.foodConsumed);

        //Find Buttons
        addButton = view.findViewById(R.id.goToAddButton);
        chatsButton = view.findViewById(R.id.goToChatsButton);
        statisticsButton = view.findViewById(R.id.registerButton);

        //Find Layouts
        caloriesLayout = view.findViewById(R.id.cardView3);
        waterLayout = view.findViewById(R.id.cardView4);
        chatLayout = view.findViewById(R.id.cardView5);
        statisticsLayout = view.findViewById(R.id.cardView6);

        //Find ImageViews
        profilePic = view.findViewById(R.id.homeProfilePicture);

        //Find Progress bar
        kcalProgress = view.findViewById(R.id.kcalTodayBar);

        //Find animation
        fade_in_animation = AnimationUtils.loadAnimation( getContext(), R.anim.main_menu_fade_in);

        //Start animation
        caloriesLayout.startAnimation(fade_in_animation);
        waterLayout.startAnimation(fade_in_animation);
        chatLayout.startAnimation(fade_in_animation);
        statisticsLayout.startAnimation(fade_in_animation);

        sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        c = Calendar.getInstance();

        //If current user exists
        if(currentUser != null){
            //Set his name and profile photo
            username.setText(currentUser.getDisplayName());
            if(currentUser.getPhotoUrl()!=null){
                Picasso.get().load(currentUser.getPhotoUrl()).into(profilePic);
            }

            //Get AMR
            AMRReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot info) {
                    if(info.child("AMR").getValue()!=null){
                        float AMR = Float.parseFloat(info.child("AMR").getValue().toString());
                        CalculateTodayStats(AMR);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("TAG", "onCancelled: ", error.toException());
                }
            });
        }

        //Set on Click Event on buttons
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity()!=null){
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame_layout,new Add_Fragment())
                            .addToBackStack("homeFragment")
                            .commit();
                }
            }
        });

        chatsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity()!=null){
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame_layout,new Chats_Fragment())
                            .addToBackStack("homeFragment")
                            .commit();
                }
            }
        });

        statisticsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity()!=null){
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame_layout,new Statistics_Fragment())
                            .addToBackStack("homeFragment")
                            .commit();
                }
            }
        });
    }

    private void CalculateTodayStats(float AMR) {
        //Get Today Kcal and water consumed
        todayMealReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot days) {
                //Find current day in database
                for (DataSnapshot day:days.getChildren()) {
                    if(day.getKey()!= null){
                        if(day.getKey().equals(sdf.format(c.getTime()))){
                            //If water child exists, print it to consumed water
                            if(day.child("Water").getValue()!=null){
                                waterConsumed.setText(day.child("Water").getValue().toString() + " ml");
                            }
                            if(day.child("TotalKcal").getValue()!=null && AMR !=0) {
                                float percentage = (Float.parseFloat(day.child("TotalKcal").getValue().toString()) / AMR) * 100;
                                foodConsumed.setText(String.format("%.0f", day.child("TotalKcal").getValue(Float.class)));
                                kcalPercentage.setText(String.format("%.0f", percentage) + "%");
                                kcalProgress.setProgress((int) percentage);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "onCancelled: ", error.toException());
            }
        });
    }
}
