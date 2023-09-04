package com.george.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.george.fitnessapp.adapters.ViewPagerAdapterPrograms;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import me.relex.circleindicator.CircleIndicator3;

public class ProgramActivity extends FragmentActivity {

    ViewPagerAdapterPrograms adapter;
    ViewPager2 viewPager;
    CircleIndicator3 circleTabLayout;

    DatabaseReference AMRReference;
    FirebaseUser currentUser;

    Double AMR;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser!= null){
            AMRReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid()).child("Info");
        }

        Bundle bundle = getIntent().getExtras();
        if(bundle!= null){
            AMR = bundle.getDouble("AMR");
        }
        circleTabLayout = findViewById(R.id.circleTabLayout);
        viewPager = findViewById(R.id.viewPager);
        adapter = new ViewPagerAdapterPrograms(this,AMR);
        viewPager.setAdapter(adapter);

        circleTabLayout.setViewPager(viewPager);
    }

    @Override
    public void onBackPressed() {
        if(viewPager.getCurrentItem() == 0){
            super.onBackPressed();
        }else{
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    public void SelectProgram(View view) {
        String selectedAMR = adapter.getFragment(viewPager.getCurrentItem()).getAMR();
        AMRReference.child("AMR").setValue(selectedAMR).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "Your program has registered successfully.", Toast.LENGTH_SHORT).show();

                    //Start main activity
                    Intent intent = new Intent(ProgramActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}
