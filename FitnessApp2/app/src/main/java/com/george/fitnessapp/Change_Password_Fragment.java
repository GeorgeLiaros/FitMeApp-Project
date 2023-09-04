package com.george.fitnessapp;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Change_Password_Fragment extends Fragment {

    EditText email,oldPassword,newPassword;
    TextView changePasswordButton;
    ImageView backButton;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    public Change_Password_Fragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //get current user
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        return inflater.inflate(R.layout.fragment_change_password,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //initialize all the parts of the fragment
        email = view.findViewById(R.id.change_password_email);
        oldPassword = view.findViewById(R.id.change_password_old_password);
        newPassword = view.findViewById(R.id.change_password_new_password);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        backButton = view.findViewById(R.id.backButton);

        //set email for user convenience
        email.setText(currentUser.getEmail());
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    AuthCredential authCredential = EmailAuthProvider.getCredential(email.getText().toString(), oldPassword.getText().toString());

                    //re authenticate user with the current data
                    currentUser.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //update old password to new password
                                currentUser.updatePassword(newPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("TAG", "Password changed successfully!");
                                            Toast.makeText(getContext(), "Password changed successfully!", Toast.LENGTH_SHORT).show();
                                            //Move back to user fragment
                                            if (getFragmentManager() != null) {
                                                getFragmentManager().popBackStack();
                                            }
                                        } else {
                                            Log.d("TAG", "onComplete: Something went wrong. \nPassword didn't change :(.");
                                            Toast.makeText(getContext(), "Something went wrong. \nPassword didn't change :(.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Log.d("TAG", "onComplete: Authentication failed. Please try again.");
                                Toast.makeText(getContext(), "Authentication failed. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }catch(Exception exception){
                    Log.e("TAG", "onClick: Change Password", exception);
                    Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
                }

            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getFragmentManager() != null) {
                    getFragmentManager().popBackStack();
                }
            }
        });
    }
}
