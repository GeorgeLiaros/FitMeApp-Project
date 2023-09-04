package com.george.fitnessapp;

import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.george.fitnessapp.adapters.RecyclerViewAdapterMeals;
import com.george.fitnessapp.utils.Food;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class Add_Fragment extends Fragment {

    FirebaseUser mUser;
    DatabaseReference mRef;

    RecyclerView breakfastRV,lunchRV,dinnerRV,snackRV;
    ArrayList<Food> breakfastFoodList,lunchFoodList,dinnerFoodList,snackFoodList;
    RecyclerViewAdapterMeals breakfastAdapter,lunchAdapter,dinnerAdapter,snackAdapter;

    ImageView breakfastAdd,lunchAdd,dinnerAdd,snackAdd,dateBack,dateForward,water1,water2,water3,water4,water5,water6,infoButton;
    ArrayList<ImageView> waterImages;
    TextView date,totalKcalTV;
    Button addwater250,addwater500,addwater125;

    SimpleDateFormat sdf;
    Calendar c;
    String day;

    ValueEventListener eventListener,waterEventListener;

    LinearLayoutManager breakfastLayoutManager,lunchLayoutManager,dinnerLayoutManager,snackLayoutManager;

    float totalKcal = 0;
    int totalwater = 0;
    boolean dayFound = false;

    public Add_Fragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference("Users").child(mUser.getUid()).child("Meals");

        //Store current date in day (etc. 12-3-2022)
        c = Calendar.getInstance();
        sdf = new SimpleDateFormat("dd-MM-yyyy",Locale.getDefault());
        day = sdf.format(c.getTime());

        Bundle data = getArguments();
        if(data!= null){
            day = data.getString("day");
        }

        breakfastFoodList = new ArrayList<>();
        lunchFoodList = new ArrayList<>();
        dinnerFoodList = new ArrayList<>();
        snackFoodList = new ArrayList<>();
        waterImages = new ArrayList<>();

        //Create event listener to read database and print foods to meals
        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot days) {
                //Clear all lists
                breakfastFoodList.clear();
                lunchFoodList.clear();
                dinnerFoodList.clear();
                snackFoodList.clear();
                totalKcal = 0;
                dayFound = false;

                for (DataSnapshot day: days.getChildren()) {
                    //Get selected date from user
                    if(day.getKey().equals(date.getText().toString()))
                    {
                        //Fill the array lists with all foods of each meal
                        for (DataSnapshot meal:day.getChildren()) {
                            switch (meal.getKey()){
                                case "breakfast":
                                    for (DataSnapshot foods:meal.getChildren()) {
                                        Food food = new Food();
                                        food.setFoodID(foods.getKey());
                                        food.setFoodName(foods.child("name").getValue().toString());
                                        food.setQuantity(Float.parseFloat(foods.child("quantity").getValue().toString()));
                                        food.setKcal(Float.parseFloat(foods.child("kcal").getValue().toString()));
                                        food.setFoodImage(Uri.parse(foods.child("uri").getValue().toString()));
                                        breakfastFoodList.add(food);
                                        totalKcal += food.getKcal();
                                    }
                                    break;
                                case "lunch":
                                    for (DataSnapshot foods:meal.getChildren()) {
                                        Food food = new Food();
                                        food.setFoodID(foods.getKey());
                                        food.setFoodName(foods.child("name").getValue().toString());
                                        food.setQuantity(Float.parseFloat(foods.child("quantity").getValue().toString()));
                                        food.setKcal(Float.parseFloat(foods.child("kcal").getValue().toString()));
                                        food.setFoodImage(Uri.parse(foods.child("uri").getValue().toString()));
                                        lunchFoodList.add(food);
                                        totalKcal += food.getKcal();
                                    }
                                    break;
                                case "dinner":
                                    for (DataSnapshot foods:meal.getChildren()) {
                                        Food food = new Food();
                                        food.setFoodID(foods.getKey());
                                        food.setFoodName(foods.child("name").getValue().toString());
                                        food.setQuantity(Float.parseFloat(foods.child("quantity").getValue().toString()));
                                        food.setKcal(Float.parseFloat(foods.child("kcal").getValue().toString()));
                                        food.setFoodImage(Uri.parse(foods.child("uri").getValue().toString()));
                                        dinnerFoodList.add(food);
                                        totalKcal += food.getKcal();
                                    }
                                    break;
                                case "snack":
                                    for (DataSnapshot foods:meal.getChildren()) {
                                        Food food = new Food();
                                        food.setFoodID(foods.getKey());
                                        food.setFoodName(foods.child("name").getValue().toString());
                                        food.setQuantity(Float.parseFloat(foods.child("quantity").getValue().toString()));
                                        food.setKcal(Float.parseFloat(foods.child("kcal").getValue().toString()));
                                        food.setFoodImage(Uri.parse(foods.child("uri").getValue().toString()));
                                        snackFoodList.add(food);
                                        totalKcal += food.getKcal();
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                        mRef.child(day.getKey()).child("TotalKcal").setValue(totalKcal);
                        totalKcalTV.setText(String.format("%.0f",  totalKcal));

                        //Try to get water from database, if it exists fill the cups with it
                        //If water does not exists fill the cups with 0 water and set it
                        try{
                            totalwater = Integer.parseInt(day.child("Water").getValue().toString());
                            FillCups(Integer.parseInt(day.child("Water").getValue().toString()),0);
                        }catch (Exception e){
                            mRef.child(day.getKey()).child("Water").setValue("0");
                            FillCups(0,0);
                        }
                        dayFound = true;
                    }
                }
                //If current day has not set at all in database, empty all cups
                if(!dayFound){
                    FillCups(0,0);
                }
                //Notify adapters that data has changed
                breakfastAdapter.notifyDataSetChanged();
                lunchAdapter.notifyDataSetChanged();
                dinnerAdapter.notifyDataSetChanged();
                snackAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "onCancelled: ", error.toException());
            }
        };

        //Listener for water change in database
        waterEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot days) {
                for (DataSnapshot day: days.getChildren()) {
                    //Get selected date from user
                    if (day.getKey().equals(date.getText().toString())) {
                        //Try to get water from database, if it exists fill the cups with it
                        //If water does not exists fill the cups with 0 water and set it
                        try{
                            totalwater = Integer.parseInt(day.child("Water").getValue().toString());
                            FillCups(Integer.parseInt(day.child("Water").getValue().toString()),0);
                        }catch (Exception e){
                            mRef.child(day.getKey()).child("Water").setValue("0");
                            FillCups(0,0);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "onCancelled: ", error.toException());
            }
        };

        return inflater.inflate(R.layout.fragment_add,container,false);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Find recycler views
        breakfastRV = view.findViewById(R.id.breakfastRV);
        lunchRV = view.findViewById(R.id.lunchRV);
        dinnerRV = view.findViewById(R.id.dinnerRV);
        snackRV = view.findViewById(R.id.snackRV);

        //Find textviews
        date = view.findViewById(R.id.date);
        totalKcalTV = view.findViewById(R.id.totalKcalTV);

        //Find Buttons
        addwater125 = view.findViewById(R.id.water125);
        addwater250 = view.findViewById(R.id.water250);
        addwater500 = view.findViewById(R.id.water500);

        //Find ImageViews
        breakfastAdd = view.findViewById(R.id.add_breakfast);
        lunchAdd = view.findViewById(R.id.add_lunch);
        dinnerAdd = view.findViewById(R.id.add_dinner);
        snackAdd = view.findViewById(R.id.add_snack);
        dateBack = view.findViewById(R.id.dateBack);
        dateForward = view.findViewById(R.id.dateForward);
        water1 = view.findViewById(R.id.water1);
        water2 = view.findViewById(R.id.water2);
        water3 = view.findViewById(R.id.water3);
        water4 = view.findViewById(R.id.water4);
        water5 = view.findViewById(R.id.water5);
        water6 = view.findViewById(R.id.water6);
        infoButton = view.findViewById(R.id.infoButton);

        //Fill ArrayList with water images
        waterImages.add(water1);
        waterImages.add(water2);
        waterImages.add(water3);
        waterImages.add(water4);
        waterImages.add(water5);
        waterImages.add(water6);

        //Create Layout managers
        breakfastLayoutManager = new LinearLayoutManager(getContext());
        breakfastLayoutManager.setOrientation(RecyclerView.VERTICAL);
        lunchLayoutManager = new LinearLayoutManager(getContext());
        lunchLayoutManager.setOrientation(RecyclerView.VERTICAL);
        dinnerLayoutManager = new LinearLayoutManager(getContext());
        dinnerLayoutManager.setOrientation(RecyclerView.VERTICAL);
        snackLayoutManager = new LinearLayoutManager(getContext());
        snackLayoutManager.setOrientation(RecyclerView.VERTICAL);

        //Create adapter objects for each Recycler view
        breakfastAdapter = new RecyclerViewAdapterMeals(getContext(),getActivity(),breakfastFoodList);
        lunchAdapter = new RecyclerViewAdapterMeals(getContext(),getActivity(),lunchFoodList);
        dinnerAdapter = new RecyclerViewAdapterMeals(getContext(),getActivity(),dinnerFoodList);
        snackAdapter = new RecyclerViewAdapterMeals(getContext(),getActivity(),snackFoodList);

        //Set adapters and LayoutManagers to recycler views
        breakfastRV.setLayoutManager(breakfastLayoutManager);
        breakfastRV.setAdapter(breakfastAdapter);
        lunchRV.setLayoutManager(lunchLayoutManager);
        lunchRV.setAdapter(lunchAdapter);
        dinnerRV.setLayoutManager(dinnerLayoutManager);
        dinnerRV.setAdapter(dinnerAdapter);
        snackRV.setLayoutManager(snackLayoutManager);
        snackRV.setAdapter(snackAdapter);

        //Set the last selected day in textView
        sdf = new SimpleDateFormat("dd-MM-yyyy",Locale.getDefault());
        date.setText(day);

        //Set listener for one time in reference, so it can print data before any actions
        mRef.addListenerForSingleValueEvent(eventListener);

        //Set listener forever in reference, so it can fill the cups with water immediately every time user presses add button
        mRef.addValueEventListener(waterEventListener);

        //Add value event listener to Reference for date checking
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dates) {
                for (DataSnapshot date:dates.getChildren()) {
                    try {
                        //If a date is older than one week, delete it from the database
                        if(sdf.parse(Objects.requireNonNull(date.getKey())).before(new Date(System.currentTimeMillis()-7*24*60*60*1000))){
                            mRef.child(date.getKey()).removeValue();
                        }
                        //If a date is newer than today, delete it from the database
                        if(sdf.parse(Objects.requireNonNull(date.getKey())).after(new Date())){
                            mRef.child(date.getKey()).removeValue();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        dateBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String yesterday = getPreviousDate(date.getText().toString());

                try {
                    //If its older than last week which in terms of milliseconds is days*hours*minutes*seconds*milliseconds
                    if(sdf.parse(yesterday).after(new Date(System.currentTimeMillis()-7*24*60*60*1000))){
                        totalwater = 0;
                        date.setText(yesterday);
                        totalKcalTV.setText("0");
                        mRef.addListenerForSingleValueEvent(eventListener);
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }


            }
        });

        dateForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tomorrow = getNextDate(date.getText().toString());

                try {
                    //if its before today
                    if(sdf.parse(tomorrow).before(new Date())){
                        totalwater = 0;
                        date.setText(tomorrow);
                        totalKcalTV.setText("0");
                        mRef.addListenerForSingleValueEvent(eventListener);
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }


            }
        });



        breakfastAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Go to add Food Fragment
                Bundle data = new Bundle();
                data.putString("meal","breakfast");
                data.putString("day",date.getText().toString());

                Fragment add_food_fragment = new Add_Food_Fragment();
                add_food_fragment.setArguments(data);

                if(getActivity()!=null){
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame_layout,add_food_fragment)
                            .addToBackStack("addFoodsFragment")
                            .commit();
                }
            }
        });

        lunchAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Go to add Food Fragment
                Bundle data = new Bundle();
                data.putString("meal","lunch");
                data.putString("day",date.getText().toString());

                Fragment add_food_fragment = new Add_Food_Fragment();
                add_food_fragment.setArguments(data);

                if(getActivity()!=null){
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame_layout,add_food_fragment)
                            .addToBackStack("addFoodsFragment")
                            .commit();
                }
            }
        });

        dinnerAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Go to add Food Fragment
                Bundle data = new Bundle();
                data.putString("meal","dinner");
                data.putString("day",date.getText().toString());

                Fragment add_food_fragment = new Add_Food_Fragment();
                add_food_fragment.setArguments(data);

                if(getActivity()!=null){
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame_layout,add_food_fragment)
                            .addToBackStack("addFoodsFragment")
                            .commit();
                }
            }
        });

        snackAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Go to add Food Fragment
                Bundle data = new Bundle();
                data.putString("meal","snack");
                data.putString("day",date.getText().toString());

                Fragment add_food_fragment = new Add_Food_Fragment();
                add_food_fragment.setArguments(data);

                if(getActivity()!=null){
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame_layout,add_food_fragment)
                            .addToBackStack("addFoodsFragment")
                            .commit();
                }
            }
        });

        //Print information about water add
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "By pressing the following buttons you can add the amount of water you have consumed. " +
                        "Each glass represents 500 ml. In case you have consumed more than 6 glasses and add more, " +
                        "the remaining quantity will be stored normally but without filling the glasses", Toast.LENGTH_LONG).show();
            }
        });

        addwater125.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRef.child(date.getText().toString()).child("Water").setValue(totalwater + 125);
            }
        });
        addwater250.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRef.child(date.getText().toString()).child("Water").setValue(totalwater + 250);
            }
        });
        addwater500.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRef.child(date.getText().toString()).child("Water").setValue(totalwater + 500);
            }
        });
    }


    private void FillCups(int totalwater, int counter) {
        if(counter < 6) {
            if (totalwater >= 500) {
                Picasso.get().load(R.drawable.water4).into(waterImages.get(counter));
                counter++;
                FillCups(totalwater - 500, counter);
            } else if (totalwater >= 375) {
                Picasso.get().load(R.drawable.water3).into(waterImages.get(counter));
                counter++;
                FillCups(0, counter);
            } else if (totalwater >= 250) {
                Picasso.get().load(R.drawable.water2).into(waterImages.get(counter));
                counter++;
                FillCups(0, counter);
            } else if (totalwater >= 125) {
                Picasso.get().load(R.drawable.water1).into(waterImages.get(counter));
                counter++;
                FillCups(0, counter);
            }
            //If water is 0 just empty all other cups
            else {
                while (counter < 6) {
                    Picasso.get().load(R.drawable.water0).into(waterImages.get(counter));
                    counter++;
                }
            }
        }
    }

    private String getPreviousDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy",Locale.getDefault());
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.add(Calendar.DATE, -1);
        return sdf.format(c.getTime());
    }

    private String getNextDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy",Locale.getDefault());
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.add(Calendar.DATE, 1);
        return sdf.format(c.getTime());
    }
}
