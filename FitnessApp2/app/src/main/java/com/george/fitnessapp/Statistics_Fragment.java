package com.george.fitnessapp;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.george.fitnessapp.adapters.ViewPagerAdapterStats;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class Statistics_Fragment extends Fragment {


    TabLayout tabLayout;
    ViewPager2 viewPager;


    public Statistics_Fragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_statistics,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Find tabview
        tabLayout = view.findViewById(R.id.tabLayout);

        //Find ViewPager
        viewPager = view.findViewById(R.id.statsViewPager);

        ArrayList<String> tabTexts = new ArrayList<>();
        tabTexts.add("Personal");
        tabTexts.add("Community");

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout,viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(tabTexts.get(position));
            }
        });

        ViewPagerAdapterStats adapterStats = new ViewPagerAdapterStats(getActivity());
        viewPager.setAdapter(adapterStats);

        tabLayoutMediator.attach();

    }
}
