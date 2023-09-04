package com.george.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    Animation bottomAnimation;
    TextView logo,slogan;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //Set flags to fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Find animation
        bottomAnimation = AnimationUtils.loadAnimation(this,R.anim.fade_in_bottom);

        //Find Views
        logo = findViewById(R.id.logo);
        slogan = findViewById(R.id.slogan);

        //Set animations
        logo.setAnimation(bottomAnimation);
        slogan.setAnimation(bottomAnimation);

        //Start animations
        logo.animate();
        slogan.animate();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        },5000);
    }
}
