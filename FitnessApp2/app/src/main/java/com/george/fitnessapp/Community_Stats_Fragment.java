package com.george.fitnessapp;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Community_Stats_Fragment extends Fragment {

    FirebaseUser currentUser;
    DatabaseReference kcalRef,AMRRef,foodRef,usersRef;
    ArrayList<Entry> lineEntries;
    ArrayList<PieEntry> pieEntries;
    ArrayList<BarEntry> barEntries;
    ArrayList<Integer> colors;
    LineChart lineChart;
    PieChart pieChart;
    BarChart barChart;

    LinearLayout favFoodLayout1,favFoodLayout2;

    SimpleDateFormat sdf;
    Calendar c;

    float[] averageWeeklyKcal,averageWeeklyWater;

    float averageAMR = 0;
    float userCounter = 0;
    float averageBreakfastPercentage = 0;
    float averageLunchPercentage = 0;
    float averageDinnerPercentage = 0;
    float averageSnackPercentage = 0;
    float averageMissingPercentage = 0;

    public Community_Stats_Fragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Get current signed-in user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //Move reference
        kcalRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid()).child("Meals");
        AMRRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid()).child("Info");
        foodRef = FirebaseDatabase.getInstance().getReference("Foods");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        //Get each user's AMR and create an average
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot users) {
                averageAMR = 0;
                userCounter = 0;
                for (DataSnapshot user:users.getChildren()) {
                    //For each user access its info and get his AMR
                    averageAMR += Float.parseFloat(user.child("Info").child("AMR").getValue().toString());
                    userCounter++;
                }
                averageAMR = averageAMR/userCounter;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "onCancelled: ", error.toException());
            }
        });

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Find graphViews
        lineChart = view.findViewById(R.id.linegraph);
        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);

        //Find layouts
        favFoodLayout1 = view.findViewById(R.id.linearLayout6);
        favFoodLayout2 = view.findViewById(R.id.linearLayout8);

        //Hide layouts with favourite food for community
        favFoodLayout1.setVisibility(View.GONE);
        favFoodLayout2.setVisibility(View.GONE);

        lineEntries = new ArrayList<>();
        pieEntries = new ArrayList<>();
        barEntries = new ArrayList<>();

        averageWeeklyKcal = new float[7];
        averageWeeklyWater = new float[7];

        CreateLineChartData();
        CreatePieChartData();
        CreateBarChartData();
    }

    private void CreateBarChartData() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot users) {
                //Clear array
                Arrays.fill(averageWeeklyWater,0);

                for (DataSnapshot user:users.getChildren()) {
                    int counter = 0;
                    sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    c = Calendar.getInstance();
                    c.add(Calendar.DATE,-6);

                    for (DataSnapshot day:user.child("Meals").getChildren()) {
                        while(counter < 7){
                            //If day is not in database add one day to calendar and stay in the same day in database
                            if(!day.getKey().equals(sdf.format(c.getTime()))){
                                averageWeeklyWater[counter] += 0;
                                c.add(Calendar.DATE,1);
                                counter++;
                            }
                            //If day is in database add one day to calendar and move to the next day
                            else{
                                if(day.child("Water").getValue()!=null){
                                    averageWeeklyWater[counter] += Float.parseFloat(day.child("Water").getValue().toString());
                                }
                                else{
                                    averageWeeklyWater[counter] += 0;
                                }

                                c.add(Calendar.DATE,1);
                                counter++;
                                //Move to next day to database
                                break;
                            }
                        }
                    }
                    //if entries are less than 7, add empty ones
                    while(counter < 7){
                        averageWeeklyWater[counter] += 0;
                        counter++;
                    }
                }
                for (int i = 0; i < 7; i++) {
                    averageWeeklyWater[i] = averageWeeklyWater[i]/userCounter;
                    barEntries.add(new BarEntry(i,averageWeeklyWater[i]));
                }
                CreateBarChart(barEntries);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "onCancelled: ", error.toException());
            }
        });
    }

    private void CreateBarChart(ArrayList<BarEntry> entries) {
        BarDataSet barDataSet = new BarDataSet(entries, "");

        BarData data = new BarData(barDataSet);
        barChart.setData(data);
        barDataSet.setValueTextColor(getResources().getColor(R.color.gray_dark));
        barDataSet.setValueTextSize(14);
        barChart.invalidate();

        barChart.setNoDataText("No data added yet :(");

        //Remove unwanted axis and descriptions
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getDescription().setEnabled(false);

        //Add Dates in X axis and change text sizes of axis
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(GetLastWeekDates()));
        barChart.getXAxis().setTextSize(15);
        barChart.getAxisLeft().setTextSize(15);

        //Set colors of axis
        barChart.getXAxis().setTextColor(getResources().getColor(R.color.gray_dark));
        barChart.getAxisLeft().setTextColor(getResources().getColor(R.color.gray_dark));

        //graph padding
        barChart.setExtraBottomOffset(10);

        //Disable Legend
        barChart.getLegend().setEnabled(false);
    }


    private void CreatePieChartData() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot users) {

                //Create all counters
                float breakfastWeeklyKcal = 0;
                float lunchWeeklyKcal = 0;
                float dinnerWeeklyKcal = 0;
                float snackWeeklyKcal = 0;

                for (DataSnapshot user:users.getChildren()) {
                    //Get AMR from each user
                    float AMR = Float.parseFloat(user.child("Info").child("AMR").getValue().toString());

                    //For each day
                    for (DataSnapshot day : user.child("Meals").getChildren()) {
                        //For each meal
                        for (DataSnapshot meal : day.getChildren()) {
                            switch (meal.getKey()) {
                                case "breakfast":
                                    for (DataSnapshot foods : meal.getChildren()) {
                                        breakfastWeeklyKcal += Float.parseFloat(foods.child("kcal").getValue().toString());
                                    }
                                    break;
                                case "lunch":
                                    for (DataSnapshot foods : meal.getChildren()) {
                                        lunchWeeklyKcal += Float.parseFloat(foods.child("kcal").getValue().toString());
                                    }
                                    break;
                                case "dinner":
                                    for (DataSnapshot foods : meal.getChildren()) {
                                        dinnerWeeklyKcal += Float.parseFloat(foods.child("kcal").getValue().toString());
                                    }
                                    break;
                                case "snack":
                                    for (DataSnapshot foods : meal.getChildren()) {
                                        snackWeeklyKcal += Float.parseFloat(foods.child("kcal").getValue().toString());
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    }

                    //Defining percentages
                    averageBreakfastPercentage += (breakfastWeeklyKcal / (AMR * 7)) * 100;
                    averageLunchPercentage += (lunchWeeklyKcal / (AMR * 7)) * 100;
                    averageDinnerPercentage += (dinnerWeeklyKcal / (AMR * 7)) * 100;
                    averageSnackPercentage += (snackWeeklyKcal / (AMR * 7)) * 100;
                    averageMissingPercentage += 100 - (averageBreakfastPercentage + averageLunchPercentage + averageDinnerPercentage + averageSnackPercentage);

                }
                //initializing colors for the entries
                colors = new ArrayList<>();
                colors.add(getResources().getColor(R.color.purple_medium));
                colors.add(getResources().getColor(R.color.purple));
                colors.add(getResources().getColor(R.color.orange));
                colors.add(getResources().getColor(R.color.blue));
                colors.add(getResources().getColor(R.color.gray_light));

                //Find average Percentages
                averageBreakfastPercentage = averageBreakfastPercentage/userCounter;
                averageLunchPercentage = averageLunchPercentage/userCounter;
                averageDinnerPercentage = averageDinnerPercentage/userCounter;
                averageSnackPercentage = averageSnackPercentage/userCounter;
                averageMissingPercentage = averageMissingPercentage/userCounter;

                //Add entries to Arraylist
                pieEntries.add(new PieEntry( averageBreakfastPercentage, "Breakfast"));
                pieEntries.add(new PieEntry( averageLunchPercentage, "Lunch"));
                pieEntries.add(new PieEntry( averageDinnerPercentage, "Dinner"));
                pieEntries.add(new PieEntry( averageSnackPercentage, "Snacks"));
                pieEntries.add(new PieEntry( averageMissingPercentage, "Remaining"));

                //Create Pie Data
                CreatePieData(pieEntries);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "onCancelled: ", error.toException());
            }
        });
    }

    private void CreatePieData(ArrayList<PieEntry> entries){
        PieDataSet pieDataSet = new PieDataSet(entries,"Meal Percentage");

        //TODO check how to remove values from graph
        pieDataSet.setValueTextSize(14);
        pieDataSet.setColors(colors);

        PieData data = new PieData(pieDataSet);

        pieChart.setData(data);
        pieChart.invalidate();
        pieDataSet.setValueFormatter(new PercentFormatter(pieChart));
        pieChart.setUsePercentValues(true);
        pieChart.setDrawEntryLabels(false);
        pieChart.getDescription().setEnabled(false);

        //Set text colors of legend
        pieChart.getLegend().setTextColor(getResources().getColor(R.color.gray_dark));

        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setHoleColor(Color.parseColor("#00000000"));
    }

    private void CreateLineChartData() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot users) {
                //Clear array
                Arrays.fill(averageWeeklyKcal,0);

                for (DataSnapshot user:users.getChildren()) {
                    int counter = 0;
                    sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    c = Calendar.getInstance();
                    c.add(Calendar.DATE,-6);

                    for (DataSnapshot day:user.child("Meals").getChildren()) {
                        while(counter < 7){
                            //If day is not in database add one day to calendar and stay in the same day in database
                            if(!day.getKey().equals(sdf.format(c.getTime()))){
                                averageWeeklyKcal[counter] += 0;
                                c.add(Calendar.DATE,1);
                                counter++;
                            }
                            //If day is in database add one day to calendar and move to the next day
                            else{
                                if(day.child("TotalKcal").getValue()!=null){
                                    averageWeeklyKcal[counter] += Float.parseFloat(day.child("TotalKcal").getValue().toString());
                                }
                                else{
                                    averageWeeklyKcal[counter] += 0;
                                }

                                c.add(Calendar.DATE,1);
                                counter++;
                                //Move to next day to database
                                break;
                            }
                        }
                    }
                    //if entries are less than 7, add empty ones
                    while(counter < 7){
                        averageWeeklyKcal[counter] += 0;
                        counter++;
                    }
                }
                for (int i = 0; i < 7; i++) {
                    averageWeeklyKcal[i] = averageWeeklyKcal[i]/userCounter;
                    lineEntries.add(new Entry(i,averageWeeklyKcal[i]));
                }
                CreateLineChart(lineEntries);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "onCancelled: ", error.toException());
            }
        });
    }

    private void CreateLineChart (ArrayList < Entry > entries) {
        LineDataSet lineDataSet = new LineDataSet(entries, "Community Calories");

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);

        LineData data = new LineData(dataSets);
        lineChart.setData(data);
        lineDataSet.setValueTextSize(14);
        lineChart.invalidate();

        lineChart.setNoDataText("No data added yet :(");

        //Remove unwanted axis and descriptions
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getDescription().setEnabled(false);

        //Add Dates in X axis and change text sizes of axis
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(GetLastWeekDates()));
        lineChart.getXAxis().setTextSize(15);
        lineChart.getAxisLeft().setTextSize(15);

        //graph padding
        lineChart.setExtraBottomOffset(10);

        //Disable Legend
        lineChart.getLegend().setEnabled(false);

        //Curved lines
        lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

        //Fill gradient background
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillDrawable(getResources().getDrawable(R.drawable.gradient_graph));
    }

    private ArrayList<String> GetLastWeekDates () {
        ArrayList<String> d = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_WEEK, -6);

        //To get each date, we loop current date and we remove 1 day
        for (int i = 0; i < 7; i++) {
            switch (calendar.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.MONDAY:
                    d.add("Mo");
                    break;
                case Calendar.TUESDAY:
                    d.add("Tu");
                    break;
                case Calendar.WEDNESDAY:
                    d.add("We");
                    break;
                case Calendar.THURSDAY:
                    d.add("Th");
                    break;
                case Calendar.FRIDAY:
                    d.add("Fr");
                    break;
                case Calendar.SATURDAY:
                    d.add("Sat");
                    break;
                case Calendar.SUNDAY:
                    d.add("Sun");
                    break;
            }
            calendar.add(Calendar.DAY_OF_WEEK, 1);
        }
        return d;
    }
}
