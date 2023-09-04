package com.george.fitnessapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.george.fitnessapp.Food_Stats_Fragment;
import com.george.fitnessapp.R;
import com.george.fitnessapp.utils.Food;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecyclerViewAdapterFoods extends RecyclerView.Adapter<RecyclerViewAdapterFoods.MyViewHolder> {

    Context context;
    ArrayList<Food> foods;
    String meal,day;

    public RecyclerViewAdapterFoods(Context context, ArrayList<Food> foods, String meal, String day) {
        this.context = context;
        this.foods = foods;
        this.meal = meal;
        this.day = day;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.recycler_view_foods_layout, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.food_name.setText(foods.get(position).getFoodName());
        Picasso.get().load(foods.get(position).getFoodImage()).into(holder.food_image);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Fragment foodStatsFragment = new Food_Stats_Fragment();

                Bundle data = new Bundle();
                data.putParcelable("food",foods.get(holder.getAdapterPosition()));
                data.putString("meal",meal);
                data.putString("day",day);

                foodStatsFragment.setArguments(data);

                AppCompatActivity activity = (AppCompatActivity)v.getContext();
                //open food statistics fragment
                if(activity!=null){
                    activity.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame_layout,foodStatsFragment)
                            .addToBackStack("foodsFragment")
                            .commit();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return foods.size();
    }

    public void FilterList(ArrayList<Food> filteredList) {
        foods = filteredList;
        notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView food_name;
        ImageView food_image;
        CardView cardView;

        public MyViewHolder(@NonNull View view) {
            super(view);

            food_name = view.findViewById(R.id.food_name);
            food_image = view.findViewById(R.id.food_image);
            cardView = view.findViewById(R.id.cardView);
        }
    }
}
