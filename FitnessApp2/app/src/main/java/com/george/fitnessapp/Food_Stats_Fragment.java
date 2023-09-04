package com.george.fitnessapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.george.fitnessapp.utils.Food;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Food_Stats_Fragment extends Fragment {

    FirebaseUser mUser;
    DatabaseReference mealsRef, kcalRef;

    float totalKcal,AMR;
    String meal,day;
    Food food;
    int quantity = 100;

    ProgressBar proteinsBar, carbsBar, fatsBar, kcalBar;
    TextView proteinsPercentage, carbsPercentage, fatsPercentage,kcalPercentage,Kcal;
    Button addFood;
    EditText quantityEditText;

    LinearLayout nutritionsLayout;

    public Food_Stats_Fragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle data = getArguments();
        if(data!= null){
            meal = data.getString("meal");
            food = data.getParcelable("food");
            day = data.getString("day");
        }
        food.setQuantity(100);
        totalKcal = food.getKcal() * food.getQuantity();

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mealsRef = FirebaseDatabase.getInstance().getReference("Users").child(mUser.getUid()).child("Meals").child(day).child(meal);

        kcalRef = FirebaseDatabase.getInstance().getReference("Users").child(mUser.getUid()).child("Info");

        kcalRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                AMR = Float.parseFloat(snapshot.child("AMR").getValue().toString());
                CalculateKcal(totalKcal,AMR);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "onCancelled: ", error.toException());
            }
        });

        return inflater.inflate(R.layout.fragment_food_stats,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Find progress bars
        proteinsBar = view.findViewById(R.id.proteinsBar);
        carbsBar = view.findViewById(R.id.carbsBar);
        fatsBar = view.findViewById(R.id.fatsBar);
        kcalBar = view.findViewById(R.id.kcalBar);

        //Find textViews
        proteinsPercentage = view.findViewById(R.id.proteinsPercentage);
        carbsPercentage = view.findViewById(R.id.carbsPercentage);
        fatsPercentage = view.findViewById(R.id.fatsPercentage);
        kcalPercentage = view.findViewById(R.id.kcalPercentage);
        Kcal = view.findViewById(R.id.Kcal);

        //Find Button
        addFood = view.findViewById(R.id.add_food);

        //Find EditText
        quantityEditText = view.findViewById(R.id.quantityEditText);

        //Find Linear Layout
        nutritionsLayout = view.findViewById(R.id.nutritionsLayout);

        Kcal.setText(String.valueOf(totalKcal));

        quantityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Calculate Kcal
                if(!s.toString().equals("")){
                    //Get quantity
                    food.setQuantity(Integer.parseInt(s.toString()));
                    totalKcal = food.getQuantity() * food.getKcal();

                    //Calculate total kcal
                    CalculateKcal(totalKcal,AMR);

                    //If textViews have percentages calculate them and print it with percentages
                    CalculatePercentage(proteinsPercentage.getText().toString().contains("%"));

                    Kcal.setText(String.format("%.0f",totalKcal));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        addFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create a child in current meal selected with value the name of the selected food
                if(food.getQuantity() > 0 && !quantityEditText.getText().toString().equals("")){
                    mealsRef = mealsRef.push();
                    mealsRef.child("name").setValue(food.getFoodName());
                    mealsRef.child("kcal").setValue(String.format("%.0f",totalKcal));
                    mealsRef.child("quantity").setValue(food.getQuantity());
                    mealsRef.child("uri").setValue(String.valueOf(food.getFoodImage()));

                    //Create Bundle for storing the day
                    Bundle data = new Bundle();
                    data.putString("day",day);

                    //Start add fragment again
                    Fragment addFragment = new Add_Fragment();
                    addFragment.setArguments(data);

                    if(getActivity()!=null){
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.frame_layout,addFragment)
                                .addToBackStack("addFoodsFragment")
                                .commit();
                    }
                }
                else{
                    Toast.makeText(getContext(), "Quantity must be greater of 0!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        CalculatePercentage(true);

        nutritionsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CalculatePercentage(!proteinsPercentage.getText().toString().contains("%"));
            }
        });
    }

    //Calculate total Kcal and set progress to kcal bar
    private void CalculateKcal(float kcal, float amr) {

        float kcalPer = (kcal/amr) * 100;
        Log.d("TAG", "CalculateKcal: "+ (int)kcalPer);
        kcalPercentage.setText(String.format("%.0f",kcalPer) + "%");

        kcalBar.setProgress((int)kcalPer);
    }

    private void CalculatePercentage(boolean percentage) {
        float proteins = food.getProteins();
        float carbs = food.getCarbs();
        float fats = food.getFats();

        float sum = proteins + carbs + fats;

        String proteinsPer= String.format("%.0f",(proteins/sum)*100);
        String carbsPer = String.format("%.0f",(carbs/sum)*100);
        String fatsPer = String.format("%.0f",(fats/sum)*100);

        if(percentage){
            proteinsPercentage.setText(proteinsPer + "%");
            carbsPercentage.setText(carbsPer + "%");
            fatsPercentage.setText(fatsPer + "%");
        }
        else{
            proteinsPercentage.setText(String.format("%.1f",food.getProteins() * food.getQuantity()));
            carbsPercentage.setText(String.format("%.1f",food.getCarbs() * food.getQuantity()));
            fatsPercentage.setText(String.format("%.1f",food.getFats() * food.getQuantity()));
        }

        proteinsBar.setProgress(Integer.parseInt(proteinsPer));
        carbsBar.setProgress(Integer.parseInt(carbsPer));
        fatsBar.setProgress(Integer.parseInt(fatsPer));
    }
}
