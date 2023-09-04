package com.george.fitnessapp;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Personal_Stats_Fragment extends Fragment {

    FirebaseUser currentUser;
    DatabaseReference kcalRef,AMRRef,foodRef;
    ArrayList<Entry> lineEntries;
    ArrayList<PieEntry> pieEntries;
    ArrayList<BarEntry> barEntries;
    ArrayList<Integer> colors;
    LineChart lineChart;
    PieChart pieChart;
    BarChart barChart;

    ImageView breakfastView,lunchView,dinnerView,snackView;
    TextView breakfastTextView,lunchTextView,dinnerTextView,snackTextView;

    SimpleDateFormat sdf;
    Calendar c;

    float AMR = 0;
    float breakfastWeeklyKcal = 0;
    float lunchWeeklyKcal = 0;
    float dinnerWeeklyKcal = 0;
    float snackWeeklyKcal = 0;

    public Personal_Stats_Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Get current signed-in user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //Move reference
        kcalRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid()).child("Meals");
        AMRRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid()).child("Info");
        foodRef = FirebaseDatabase.getInstance().getReference("Foods");

        //Get User AMR
        AMRRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot info) {
                AMR = Float.parseFloat(info.child("AMR").getValue().toString());
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

        //Find imageviews
        breakfastView = view.findViewById(R.id.breakfastFavImage);
        lunchView = view.findViewById(R.id.lunchFavImage);
        dinnerView = view.findViewById(R.id.dinnerFavImage);
        snackView = view.findViewById(R.id.snackFavImage);

        //Find TextViews
        breakfastTextView = view.findViewById(R.id.breakfastFavName);
        lunchTextView = view.findViewById(R.id.lunchFavName);
        dinnerTextView = view.findViewById(R.id.dinnerFavName);
        snackTextView = view.findViewById(R.id.snackFavName);

        lineEntries = new ArrayList<>();
        pieEntries = new ArrayList<>();
        barEntries = new ArrayList<>();

        CreateLineChartData();
        CreatePieChartData();
        CreateBarChartData();
        PrintFavouriteFoods();
    }

    private void PrintFavouriteFoods() {
        kcalRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot days) {
                //Reset all counters
                ArrayList<String> breakfastFoods = new ArrayList<>();
                ArrayList<String> lunchFoods = new ArrayList<>();
                ArrayList<String> dinnerFoods = new ArrayList<>();
                ArrayList<String> snackFoods = new ArrayList<>();

                //For each day
                for (DataSnapshot day : days.getChildren()) {
                    //For each meal
                    for (DataSnapshot meal : day.getChildren()) {
                        switch (meal.getKey()) {
                            case "breakfast":
                                for (DataSnapshot foods : meal.getChildren()) {
                                    breakfastFoods.add(foods.child("name").getValue().toString());
                                }
                                break;
                            case "lunch":
                                for (DataSnapshot foods : meal.getChildren()) {
                                    lunchFoods.add(foods.child("name").getValue().toString());
                                }
                                break;
                            case "dinner":
                                for (DataSnapshot foods : meal.getChildren()) {
                                    dinnerFoods.add(foods.child("name").getValue().toString());
                                }
                                break;
                            case "snack":
                                for (DataSnapshot foods : meal.getChildren()) {
                                    snackFoods.add(foods.child("name").getValue().toString());
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
                //Find most frequent food for each meal
                String breakfastFrequentFood = FindMostFrequentFood(breakfastFoods);
                String lunchFrequentFood = FindMostFrequentFood(lunchFoods);
                String dinnerFrequentFood =  FindMostFrequentFood(dinnerFoods);
                String snackFrequentFood = FindMostFrequentFood(snackFoods);

                PrintMostFrequentFood(breakfastView,breakfastTextView,breakfastFrequentFood);
                PrintMostFrequentFood(lunchView,lunchTextView,lunchFrequentFood);
                PrintMostFrequentFood(dinnerView,dinnerTextView,dinnerFrequentFood);
                PrintMostFrequentFood(snackView,snackTextView,snackFrequentFood);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "onCancelled: ", error.toException());
            }
        });
    }

    private void PrintMostFrequentFood(ImageView imageView, TextView textView, String food) {
        foodRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot foods) {
                //If frequent food exists print it in imageview and textview
                if(food!= null && !food.equals("")){
                    String frequentFoodImage = foods.child(food).child("Picture").getValue().toString();
                    Picasso.get().load(Uri.parse(frequentFoodImage)).into(imageView);
                    textView.setText(food);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "onCancelled: ", error.toException());
            }
        });
    }

    private String FindMostFrequentFood(ArrayList<String> foods) {
        Set<String> distinct = new HashSet<>(foods);
        HashMap<String,Integer> map = new HashMap<>();
        for (String s: distinct) {
            map.put(s,Collections.frequency(foods, s));
        }

        String maxFood = "";

        int max = 0;
        for (Map.Entry<String, Integer> m:map.entrySet()) {
            if(m.getValue() > max){
                max = m.getValue();
                maxFood = m.getKey();
            }
        }
        return maxFood;
    }

    private void CreateBarChartData() {
        kcalRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot days) {

                int counter = 0;
                sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                c = Calendar.getInstance();
                c.add(Calendar.DATE,-6);

                for (DataSnapshot day:days.getChildren()) {

                    while(counter < 7){
                        //If day is not in database add one day to calendar and stay in the same day in database
                        if(!day.getKey().equals(sdf.format(c.getTime()))){
                            barEntries.add(new BarEntry(counter, 0));
                            c.add(Calendar.DATE,1);
                            counter++;
                        }
                        //If day is in database add one day to calendar and move to the next day
                        else{
                            if(day.child("Water").getValue() != null){
                                barEntries.add(new BarEntry(counter, Float.parseFloat(day.child("Water").getValue().toString())));
                            }else{
                                barEntries.add(new BarEntry(counter, 0));
                            }
                            c.add(Calendar.DATE,1);
                            counter++;
                            //Move to next day to database
                            break;
                        }
                    }
                }
                //if entries are less than 7, add empty ones
                while(barEntries.size() < 7){
                    barEntries.add(new BarEntry(counter,0));
                    counter++;
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
        kcalRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot days) {
                //Reset all counters
                breakfastWeeklyKcal = 0;
                lunchWeeklyKcal = 0;
                dinnerWeeklyKcal = 0;
                snackWeeklyKcal = 0;

                //For each day
                for (DataSnapshot day:days.getChildren()) {
                    //For each meal
                    for (DataSnapshot meal:day.getChildren()) {
                        switch(meal.getKey()){
                            case "breakfast":
                                for (DataSnapshot foods:meal.getChildren()) {
                                    breakfastWeeklyKcal += Float.parseFloat(foods.child("kcal").getValue().toString());
                                }
                                break;
                            case "lunch":
                                for (DataSnapshot foods:meal.getChildren()) {
                                    lunchWeeklyKcal += Float.parseFloat(foods.child("kcal").getValue().toString());
                                }
                                break;
                            case "dinner":
                                for (DataSnapshot foods:meal.getChildren()) {
                                    dinnerWeeklyKcal += Float.parseFloat(foods.child("kcal").getValue().toString());
                                }
                                break;
                            case "snack":
                                for (DataSnapshot foods:meal.getChildren()) {
                                    snackWeeklyKcal += Float.parseFloat(foods.child("kcal").getValue().toString());
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
                //Defining percentages
                float breakfastPercentage = (breakfastWeeklyKcal/(AMR*7))*100;
                float lunchPercentage = (lunchWeeklyKcal/(AMR*7))*100;
                float dinnerPercentage = (dinnerWeeklyKcal/(AMR*7))*100;
                float snackPercentage = (snackWeeklyKcal/(AMR*7))*100;
                float missingPercentage = 100-(breakfastPercentage+lunchPercentage+dinnerPercentage+snackPercentage);

                //initializing colors for the entries
                colors = new ArrayList<>();
                colors.add(getResources().getColor(R.color.purple_medium));
                colors.add(getResources().getColor(R.color.purple));
                colors.add(getResources().getColor(R.color.orange));
                colors.add(getResources().getColor(R.color.blue));
                colors.add(getResources().getColor(R.color.gray_light));

                //Add entries to Arraylist
                pieEntries.add(new PieEntry( breakfastPercentage, "Breakfast"));
                pieEntries.add(new PieEntry( lunchPercentage, "Lunch"));
                pieEntries.add(new PieEntry( dinnerPercentage, "Dinner"));
                pieEntries.add(new PieEntry( snackPercentage, "Snack"));
                pieEntries.add(new PieEntry( missingPercentage, "Remaining"));

                //Create Pie Data
                CreatePieChart(pieEntries);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "onCancelled: ", error.toException());
            }
        });
    }

    private void CreatePieChart(ArrayList<PieEntry> entries){
        PieDataSet pieDataSet = new PieDataSet(entries,"Meal Percentage");

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
        kcalRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot days) {

                int counter = 0;
                sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                c = Calendar.getInstance();
                c.add(Calendar.DATE,-6);

                for (DataSnapshot day:days.getChildren()) {

                    while(counter < 7){
                        //If day is not in database add one day to calendar and stay in the same day in database
                        if(!day.getKey().equals(sdf.format(c.getTime()))){
                            lineEntries.add(new Entry(counter, 0));
                            c.add(Calendar.DATE,1);
                            counter++;
                        }
                        //If day is in database add one day to calendar and move to the next day
                        else{
                            if(day.child("TotalKcal").getValue() != null){
                                lineEntries.add(new Entry(counter, Float.parseFloat(day.child("TotalKcal").getValue().toString())));
                            }else{
                                lineEntries.add(new Entry(counter, 0));
                            }
                            c.add(Calendar.DATE,1);
                            counter++;
                            //Move to next day to database
                            break;
                        }
                    }
                }
                //if entries are less than 7, add empty ones
                while(lineEntries.size() < 7){
                    lineEntries.add(new Entry(counter,0));
                    counter++;
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
        LineDataSet lineDataSet = new LineDataSet(entries,"");

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
