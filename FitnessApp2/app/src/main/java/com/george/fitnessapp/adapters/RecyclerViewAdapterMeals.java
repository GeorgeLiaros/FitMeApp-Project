package com.george.fitnessapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.george.fitnessapp.Add_Fragment;
import com.george.fitnessapp.R;
import com.george.fitnessapp.utils.Food;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RecyclerViewAdapterMeals extends RecyclerView.Adapter<RecyclerViewAdapterMeals.MyViewHolder> {

    Context context;
    ArrayList<Food> foods;
    Activity activity;
    DatabaseReference mealReference = FirebaseDatabase.getInstance().getReference("Users");
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    public RecyclerViewAdapterMeals(Context context, Activity activity, ArrayList<Food> foods) {
        this.context = context;
        this.activity = activity;
        this.foods = foods;

        //Move to current User and then to meals
        mealReference = mealReference
                .child(currentUser.getUid())
                .child("Meals");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.recycler_view_meals_layout, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.food_name.setText(foods.get(position).getFoodName());
        holder.quantity.setText(String.format("%.0f",foods.get(position).getQuantity()) + " gr");
        holder.food_kcal.setText(String.format("%.0f",foods.get(position).getKcal()));
        Picasso.get().load(foods.get(position).getFoodImage()).into(holder.food_image);

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mealReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot meals) {
                        for (DataSnapshot day: meals.getChildren()) {
                            for (DataSnapshot dayInfo: day.getChildren()) {
                                if(dayInfo.hasChildren()){
                                    for (DataSnapshot food: dayInfo.getChildren()) {
                                        if(holder.getAdapterPosition() <= foods.size() && holder.getAdapterPosition()!= -1){
                                            if(foods.get(holder.getAdapterPosition()).getFoodID().equals(food.getKey())){
                                                food.getRef().removeValue();

                                                RemoveHolder(holder.getAdapterPosition());

                                                RefreshFragment();
                                            }
                                        }
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
        });
    }

    private void RefreshFragment() {
        AppCompatActivity activity = (AppCompatActivity) context;
        Fragment myFragment = new Add_Fragment();
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, myFragment).addToBackStack(null).commit();
    }

    private void RemoveHolder(int adapterPosition) {
        foods.remove(adapterPosition);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return foods.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView food_name,quantity,food_kcal;
        ImageView food_image,deleteButton;

        public MyViewHolder(@NonNull View view) {
            super(view);

            food_name = view.findViewById(R.id.food_name);
            quantity = view.findViewById(R.id.food_gr);
            food_kcal = view.findViewById(R.id.food_kcal);
            food_image = view.findViewById(R.id.food_uri);
            deleteButton = view.findViewById(R.id.deleteButton);
        }
    }
}
