package com.george.fitnessapp;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.ALARM_SERVICE;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.george.fitnessapp.receivers.NotificationReceiver;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

public class User_Profile_Fragment extends Fragment {

    private static final int RESULT_LOAD_IMG = 123;

    EditText username,fullname,email,telephone;
    ImageView profilePicture,editFullname,editTelephone,options_button,clickView, editAttributes;
    Button logOutButton;
    LinearLayout resetPassword;
    ConstraintLayout optionsLayout;
    Switch nightModeSwitch,notificationsSwitch;
    CardView cardLayout, attributeLayout, resetPasswordLayout;
    TextView ageTextView, heightTextView, weightTextView, activityTextView;

    StorageReference storageReference;
    DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    FirebaseUser user;

    String notifications,nightmode;

    boolean enabled = false;
    boolean telephoneEnabled = false;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    Animation bottom_sheet_fade_in_animation, fade_in_animation, bottom_sheet_fade_out_animation, fade_out_animation,fade_in_bottom,fade_in_top;

    public User_Profile_Fragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Get Current User
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("Users");
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        preferences = getContext().getSharedPreferences("preferences",Context.MODE_PRIVATE);
        editor = preferences.edit();

        //if user is logged in, move to reference of his unique id
        if(user!=null){
            storageReference = storageReference.child(user.getUid());
            databaseReference = databaseReference.child(user.getUid()).child("Info");
        }

        return inflater.inflate(R.layout.fragment_user_profile,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //find Buttons
        logOutButton = view.findViewById(R.id.logOutButton);

        //find textView-editTexts
        username = view.findViewById(R.id.user_username);
        email = view.findViewById(R.id.user_email);
        telephone = view.findViewById(R.id.user_telephone);
        fullname = view.findViewById(R.id.user_fullname);
        ageTextView = view.findViewById(R.id.ageTextView);
        heightTextView = view.findViewById(R.id.heightTextView);
        weightTextView = view.findViewById(R.id.weightTextView);
        activityTextView = view.findViewById(R.id.activityTextView);

        //find imageViews
        profilePicture = view.findViewById(R.id.homeProfilePicture);
        editFullname = view.findViewById(R.id.editFullname);
        editTelephone = view.findViewById(R.id.editTelephone);
        options_button = view.findViewById(R.id.options_button);
        clickView = view.findViewById(R.id.clickView2);
        editAttributes = view.findViewById(R.id.editAttributes);

        //find switches
        nightModeSwitch = view.findViewById(R.id.nightModeSwitch);
        notificationsSwitch = view.findViewById(R.id.notificationsSwitch);

        //find layouts
        resetPassword = view.findViewById(R.id.resetPassword);
        cardLayout = view.findViewById(R.id.cardLayout);
        optionsLayout = view.findViewById(R.id.optionsLayout);
        attributeLayout = view.findViewById(R.id.attributeLayout);
        resetPasswordLayout = view.findViewById(R.id.resetPasswordLayout);

        //Find animation
        bottom_sheet_fade_in_animation = AnimationUtils.loadAnimation(getContext(),R.anim.bottom_sheet_fade_in);
        bottom_sheet_fade_out_animation = AnimationUtils.loadAnimation(getContext(),R.anim.bottom_sheet_fade_out);
        fade_in_animation = AnimationUtils.loadAnimation(getContext(),R.anim.fade_in);
        fade_out_animation = AnimationUtils.loadAnimation(getContext(),R.anim.fade_out);
        fade_in_bottom = AnimationUtils.loadAnimation(getContext(),R.anim.main_menu_fade_in);
        fade_in_top = AnimationUtils.loadAnimation(getContext(),R.anim.fade_in_top);

        profilePicture.startAnimation(fade_in_top);
        username.startAnimation(fade_in_top);
        cardLayout.startAnimation(fade_in_bottom);
        attributeLayout.startAnimation(fade_in_bottom);
        resetPasswordLayout.startAnimation(fade_in_bottom);
        logOutButton.startAnimation(fade_in_bottom);

        //Change their settings with user's data
        username.setText(user.getDisplayName());
        email.setText(user.getEmail());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Check firebase user's information for telephone and full name
                if(snapshot.child("Telephone").getValue() != null){
                    telephone.setText(snapshot.child("Telephone").getValue().toString());
                }
                if(snapshot.child("FullName").getValue() != null){
                    fullname.setText(snapshot.child("FullName").getValue().toString());
                }
                if(snapshot.child("Age").getValue() != null){
                    ageTextView.setText(snapshot.child("Age").getValue().toString() + " y/o");
                }
                if(snapshot.child("Height").getValue() != null){
                    heightTextView.setText(snapshot.child("Height").getValue().toString() + " cm");
                }
                if(snapshot.child("Weight").getValue() != null){
                    weightTextView.setText(snapshot.child("Weight").getValue().toString() + " kg");
                }
                if(snapshot.child("Activity").getValue() != null){
                    activityTextView.setText(snapshot.child("Activity").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "onCancelled: ", error.toException());
            }
        });

        if(user.getPhotoUrl()!= null){
            Picasso.get().load(user.getPhotoUrl()).into(profilePicture);
        }

        nightmode = preferences.getString("nightmode","none");
        notifications = preferences.getString("notifications","none");
        if(nightmode.equals("true")){
            nightModeSwitch.setChecked(true);
        }
        if(notifications.equals("true")){
            notificationsSwitch.setChecked(true);
        }

        //Set on click listener to layout to override on click listener of imageview
        optionsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Don't do anything
            }
        });

        options_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionsLayout.setVisibility(View.VISIBLE);
                clickView.setVisibility(View.VISIBLE);
                optionsLayout.clearAnimation();
                optionsLayout.startAnimation(bottom_sheet_fade_in_animation);
                clickView.clearAnimation();
                clickView.startAnimation(fade_in_animation);
            }
        });

        clickView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionsLayout.setVisibility(View.GONE);
                clickView.setVisibility(View.GONE);
                optionsLayout.clearAnimation();
                optionsLayout.startAnimation(bottom_sheet_fade_out_animation);
                clickView.clearAnimation();
                clickView.startAnimation(fade_out_animation);
            }
        });

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent loginIntent = new Intent(getContext(),LoginActivity.class);
                startActivity(loginIntent);
                //finish parent activity
                if(getActivity()!=null){
                    Log.d("TAG", "onClick: I have activity");
                    getActivity().finish();
                }
            }
        });

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity()!=null){
                    //open change password fragment
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame_layout,new Change_Password_Fragment())
                            .addToBackStack("userFragment")
                            .commit();
                }
            }
        });

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open stream for picking a photo
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
            }
        });

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username.setFocusable(true);
                username.requestFocus();
            }
        });

        editFullname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(enabled){
                    enabled = false;
                    fullname.setEnabled(false);

                    databaseReference.child("FullName").setValue(fullname.getText().toString());

                }
                else{
                    enabled = true;
                    fullname.setEnabled(true);
                    fullname.requestFocus(fullname.getHint().length());
                    fullname.setFocusable(true);
                    if(fullname.hasFocus()){
                        //Show keyboard
                        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    }
                }

            }
        });

        fullname.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    editFullname.performClick();
                }
                return false;
            }
        });

        editTelephone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(telephoneEnabled){
                    telephoneEnabled = false;
                    telephone.setEnabled(false);

                    if(!telephone.getText().toString().equals("")){
                        databaseReference.child("Telephone").setValue(telephone.getText().toString());
                    }
                }
                else{
                    telephoneEnabled = true;
                    telephone.setEnabled(true);
                    telephone.requestFocus(telephone.getHint().length());
                    if(telephone.hasFocus()){
                        //Show keyboard
                        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    }
                    telephone.setFocusable(true);
                }

            }
        });

        telephone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    editTelephone.performClick();
                }
                return false;
            }
        });

        editAttributes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), InfoActivity.class);
                startActivity(intent);
            }
        });

        nightModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    editor.putString("nightmode", "true");
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                }else{
                    editor.putString("nightmode", "false");
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                }
                editor.apply();

            }
        });

        notificationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Calendar c = Calendar.getInstance();

                    Intent intent = new Intent(getContext(), NotificationReceiver.class);

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(),1,intent,PendingIntent.FLAG_UPDATE_CURRENT);

                    //Set alarm manager to repeat notification every 3 hours(hours*minutes*seconds*milliseconds)
                    AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), 3*60*60*1000,pendingIntent);

                    editor.putString("notifications", "true");
                    Toast.makeText(getContext(), "Notifications for water are set", Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent = new Intent(getContext(),NotificationReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                    alarmManager.cancel(pendingIntent);

                    editor.putString("notifications", "false");
                    Toast.makeText(getContext(), "Notifications for water are cancelled", Toast.LENGTH_SHORT).show();

                }
                editor.apply();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == RESULT_LOAD_IMG){
            //Case for safety, doesn't produce NullPointerException
            if(getActivity()!=null && data!= null){
                Uri imageUri = data.getData();
                //profilePicture.setImageURI(imageUri);

                UploadImageToFirestore(imageUri);
            }
        }
    }

    public void UploadImageToFirestore(Uri imageUri){
        final StorageReference mFileReference = storageReference.child("profilePicture.jpg");
        mFileReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    //Successful upload
                    Toast.makeText(getContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    //Download image
                    mFileReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful()){
                                //If download is successful load the uri into imageView
                                Picasso.get().load(task.getResult()).into(profilePicture);

                                //Update user profile image
                                UserProfileChangeRequest profileChanges = new UserProfileChangeRequest.Builder()
                                        .setPhotoUri(task.getResult())
                                        .build();

                                user.updateProfile(profileChanges).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(getContext(), "Your photo profile has been changed", Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            Toast.makeText(getContext(), "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
                else{
                    //Unsuccessful upload
                    Toast.makeText(getContext(), "Image didn't upload. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
