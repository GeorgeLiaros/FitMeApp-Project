package com.george.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    EditText username,email,password,password2;
    Button registerButton;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DatabaseReference mRef;

    Animation fade_in_right_animation;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Find EditText from layout
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        password2 = findViewById(R.id.confirmPassword);

        //Find Buttons
        registerButton = findViewById(R.id.registerButton);

        //Find animation
        fade_in_right_animation = AnimationUtils.loadAnimation(this, R.anim.fade_in_right);

        //Start animations
        username.startAnimation(fade_in_right_animation);
        email.startAnimation(fade_in_right_animation);
        password.startAnimation(fade_in_right_animation);
        password2.startAnimation(fade_in_right_animation);
        registerButton.startAnimation(fade_in_right_animation);

        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference("Users");
    }

    public void RegisterWithEmailAndPassword(View view) {
        /*Register using the following steps:
        * 1. Check if passwords are the same
        * 2. Check if email exists
        * 3. Register to database
        */
        if(password.getText().toString().equals(password2.getText().toString())){
            try{
                mAuth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //get current user
                            currentUser = mAuth.getCurrentUser();

                            //Build a request for passing display name data
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username.getText().toString())
                                    .build();

                            //Execute the request
                            currentUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        //Store user in firebase
                                        mRef.child(currentUser.getUid()).child("Info").child("UserName").setValue(currentUser.getDisplayName());


                                        //start main activity
                                        Toast.makeText(RegisterActivity.this, "You have registered successfully.", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterActivity.this, InfoActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                    else{
                                        Log.d("TAG", "onComplete: Update profile failed.");
                                    }
                                }
                            });

                        }
                        else{
                            Toast.makeText(RegisterActivity.this, "This account already exists.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }catch(IllegalArgumentException exception){
                Toast.makeText(RegisterActivity.this, "Please give all the credentials", Toast.LENGTH_SHORT).show();
            }catch(Exception exception){
                Log.e("TAG", "RegisterWithEmailAndPassword: ", exception);
            }

        }else{
            Toast.makeText(RegisterActivity.this, "Passwords are not the same.", Toast.LENGTH_SHORT).show();
        }
    }

    public void GoToLogin(View view) {
        Intent loginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }
}
