package com.george.fitnessapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import me.angrybyte.numberpicker.listener.OnValueChangeListener;
import me.angrybyte.numberpicker.view.ActualNumberPicker;

public class InfoActivity extends AppCompatActivity {

    RadioGroup activeGroup;
    RadioButton activeButton;
    LinearLayout genderLayout;

    DatabaseReference infoReference;
    FirebaseUser currentUser;

    Button maleButton,femaleButton;

    ActualNumberPicker age,height,weight;

    String gender = "male";

    double BMR = 0;
    double AMR = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        //Find Buttons
        maleButton = findViewById(R.id.maleButton);
        femaleButton = findViewById(R.id.femaleButton);

        //Find RadioGroup
        activeGroup = findViewById(R.id.activeGroup);

        //Find linear Layout
        genderLayout = findViewById(R.id.genderLayout);

        //Database initialization
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        infoReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid()).child("Info");

        //Find numberPickers
        age = findViewById(R.id.age);
        height = findViewById(R.id.height);
        weight = findViewById(R.id.weight);

        maleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maleButton.setBackgroundTintList(getResources().getColorStateList(R.color.purple_medium));
                maleButton.setTypeface(Typeface.DEFAULT_BOLD);
                femaleButton.setTypeface(Typeface.DEFAULT);
                genderLayout.setBackground(getResources().getDrawable(R.drawable.cornered_white_background));
                gender = "male";
            }
        });

        femaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maleButton.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                maleButton.setTypeface(Typeface.DEFAULT);
                femaleButton.setTypeface(Typeface.DEFAULT_BOLD);
                genderLayout.setBackground(getResources().getDrawable(R.drawable.cornered_purple_background));
                gender = "female";
            }
        });
    }

    public void CalculateCalories(View view) {
        try {
            //Get checked button ID
            int checkedActiveButtonID = activeGroup.getCheckedRadioButtonId();

            //Find checked button from view
            activeButton = findViewById(checkedActiveButtonID);

            //Store metrics
            int w = weight.getValue();
            int h = height.getValue();
            int a = age.getValue();

            //Calculate BMR (calories per day from metrics)
            if (gender.equals("male")) {
                BMR = 66.47 + (13.75 * w) + (5.003 * h) - (6.755 * a);
            } else {
                BMR = 665.1 + (9.563 * w) + (1.850 * h) - (4.676 * a);
            }

            //Calculate AMR (calories per day after adding activity)
            if (activeButton.getText().toString().contains("Sedentary")) {
                AMR = BMR * 1.2;
            } else if (activeButton.getText().toString().contains("Lightly")) {
                AMR = BMR * 1.375;
            } else if (activeButton.getText().toString().contains("Moderately")) {
                AMR = BMR * 1.55;
            } else if (activeButton.getText().toString().contains("Very")) {
                AMR = BMR * 1.725;
            }

            infoReference.child("Age").setValue(String.valueOf(a));
            infoReference.child("Height").setValue(String.valueOf(h));
            infoReference.child("Weight").setValue(String.valueOf(w));
            infoReference.child("Activity").setValue(activeButton.getText().toString());
            StartNewActivity();
        }
        //In case Metrics are not filled
        catch (NumberFormatException exception){
            Toast.makeText(this, "Please give all metrics to continue", Toast.LENGTH_SHORT).show();
        }
        catch (Exception exception){
            Log.e("TAG", "CalculateCalories: ", exception);
        }
    }

    private void StartNewActivity() {
        //Start program activity
        Intent intent = new Intent(InfoActivity.this,ProgramActivity.class);
        intent.putExtra("AMR",AMR);
        startActivity(intent);
        finish();
    }
}