package com.george.fitnessapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Program_Fragment extends Fragment {

    TextView titleView,descriptionView,AMRView,weightDiffView,weightPerWeekView;
    String title,description,AMR,weightDiff,weightPerWeek;

    public Program_Fragment(String title, String description, Double AMR, String weightDiff, String weightPerWeek) {
        this.title = title;
        this.description = description;
        this.AMR = String.format("%.0f",AMR);
        this.weightDiff = weightDiff;
        this.weightPerWeek = weightPerWeek;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_program, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Find textViews
        titleView = view.findViewById(R.id.titleView);
        descriptionView = view.findViewById(R.id.descriptionView);
        AMRView = view.findViewById(R.id.AMRView);
        weightDiffView = view.findViewById(R.id.estimatedWeight);
        weightPerWeekView = view.findViewById(R.id.weightPerWeek);

        titleView.setText(title);
        descriptionView.setText(description);
        AMRView.setText(AMR + " kcal");
        weightDiffView.setText(weightDiff);
        weightPerWeekView.setText(weightPerWeek + " /Week");
    }

    public String getAMR(){
        return AMR;
    }
}