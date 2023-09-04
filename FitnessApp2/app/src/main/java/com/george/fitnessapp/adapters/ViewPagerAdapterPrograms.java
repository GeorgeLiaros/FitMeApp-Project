package com.george.fitnessapp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.george.fitnessapp.InfoActivity;
import com.george.fitnessapp.Program_Fragment;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ViewPagerAdapterPrograms extends FragmentStateAdapter {

    ArrayList<Fragment> programFragments;

    String[] titles = new String[]{
        "Extreme Weight Loss",
        "Weight Loss",
        "Weight Maintenance",
        "Weight Gain",
        "Extreme Weight Gain"
    };

    String[] descriptions = new String[]{
            //Extreme weight loss description
            "The extreme weight loss program is a program that refers to people who want to see fast results. " +
                    "Daily calories fall sharply, thus increasing the difficulty of processing it.",
            //Weight loss description
            "The weight loss program will help you lose weight without much effort. You will see results based on time and a little effort.",
            //Weight maintenance description
            "This program gives you the opportunity to stay at your weight. It's a program that everyone will need at some point.",
            //Weight gain description
            "The weight gain program helps you, as its name suggests, to increase your body weight without much effort. The key to this program is patience.",
            //Extreme weight gain description
            "The extreme weight gain program is designed for people who are trying to gain weight fast. " +
                    "The difficulty of the program goes up a lot due to the need to cover daily calories."
    };

    String[] weightProgram = new String[]{
            "Estimated weight loss :",
            "Estimated weight loss :",
            "Estimated weight loss/gain :",
            "Estimated weight gain :",
            "Estimated weight gain :",
    };

    String[] weightPerWeek = new String[]{
            "0.5",
            "0.25",
            "0",
            "0.25",
            "0.5",
    };

    Double[] AMRs = new Double[5];

    public ViewPagerAdapterPrograms(@NonNull FragmentActivity fragmentActivity, Double AMR) {
        super(fragmentActivity);
        programFragments = new ArrayList<>();
        AMRs[0] = AMR - 550;
        AMRs[1] = AMR - 225;
        AMRs[2] = AMR;
        AMRs[3] = AMR + 225;
        AMRs[4] = AMR + 550;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        programFragments.add(new Program_Fragment(titles[position],descriptions[position],AMRs[position],weightProgram[position],weightPerWeek[position]));
        return programFragments.get(position);
    }

    public Program_Fragment getFragment(int position){
        return (Program_Fragment) programFragments.get(position);
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
