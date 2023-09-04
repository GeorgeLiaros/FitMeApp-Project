package com.george.fitnessapp;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    //Firebase Stuff
    FirebaseAuth mAuth;
    DatabaseReference chatRef;

    SharedPreferences preferences;
    SharedPreferences.Editor preferencesEditor;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,new Home_Fragment()).commit();
        }else{
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        chatRef = FirebaseDatabase.getInstance().getReference("Chats");

        preferences = getSharedPreferences("preferences",MODE_PRIVATE);
        preferencesEditor = preferences.edit();
        if(preferences.getString("nightmode","none").equals("true")){
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
        }else if(preferences.getString("nightmode","none").equals("false")){
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
        }

        String forWater = getIntent().getStringExtra("notification");
        //If main activity is opened from notification
        if(forWater!= null){
            //Move to add fragment to add water
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,new Add_Fragment()).commit();

        }else {
            //Move to Home fragment
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new Home_Fragment()).commit();
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Fragment currentFragment = null;

                switch (item.getItemId()){
                    case R.id.navigation_home:
                        currentFragment = new Home_Fragment();
                        break;
                    case R.id.navigation_statistics:
                        currentFragment = new Statistics_Fragment();
                        break;
                    case R.id.navigation_user:
                        currentFragment = new User_Profile_Fragment();
                        break;
                    case R.id.navigation_add:
                        currentFragment = new Add_Fragment();
                        break;
                    case R.id.navigation_chats:
                        currentFragment = new Chats_Fragment();
                        break;
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,currentFragment).commit();

                return true;
            }
        });
    }
}