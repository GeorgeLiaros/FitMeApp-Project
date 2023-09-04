package com.george.fitnessapp;

import android.content.Intent;
import android.icu.text.IDNA;
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

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    //Google Login
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;
    DatabaseReference mRef;

    Button loginButton,loginWithGoogleButton;
    EditText email,password;
    Animation fade_in_right_animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Find EditTexts
        email = findViewById(R.id.Email);
        password = findViewById(R.id.Password);

        //Find Buttons
        loginButton = findViewById(R.id.loginButton);
        loginWithGoogleButton = findViewById(R.id.loginWithGoogleButton);

        //Find animation
        fade_in_right_animation = AnimationUtils.loadAnimation(this, R.anim.fade_in_right);

        //Start animations
        email.startAnimation(fade_in_right_animation);

        password.startAnimation(fade_in_right_animation);
        loginButton.startAnimation(fade_in_right_animation);
        loginWithGoogleButton.startAnimation(fade_in_right_animation);

        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference("Users");

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("733703561090-mq6j4uf2pbq9hvdn880j52i6kbe2gcjj.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
    }

    public void LoginWithGoogle(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("GoogleLogin", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("GoogleLogin", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("GoogleLogin", "signInWithCredential:success");

                            //Store user in firebase
                            mRef.child(mAuth.getCurrentUser().getUid()).child("Info").child("UserName").setValue(mAuth.getCurrentUser().getDisplayName());

                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("GoogleLogin", "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    public void LoginWithFacebook(View view) {

    }

    public void Login(View view) {
        try{
            mAuth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        Toast.makeText(LoginActivity.this, "Wrong credentials, please try again", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        //Exception for empty required fields
        catch(IllegalArgumentException e){
            Log.e("LoginWithEmail", "Login: ", e);
            Toast.makeText(LoginActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
        }
        //General exception for more safety
        catch (Exception e){
            Log.e("LoginWithEmail", "Login: ", e);
            Toast.makeText(LoginActivity.this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
        }
    }

    public void GoToRegister(View view) {
        Intent registerIntent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);
        finish();
    }

    public void ResetPassword(View view) {
        try{
            mAuth.sendPasswordResetEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(LoginActivity.this, "Email has sent.", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(LoginActivity.this, "User with this email does not exist.\n Try again!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        //Exception for empty required fields
        catch(IllegalArgumentException exception){
            Toast.makeText(LoginActivity.this, "Please add your email again!", Toast.LENGTH_SHORT).show();
        }
        //General exception for more safety
        catch(Exception exception){
            Log.e("TAG", "ResetPassword: ", exception);
            Toast.makeText(LoginActivity.this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
        }

    }
}
