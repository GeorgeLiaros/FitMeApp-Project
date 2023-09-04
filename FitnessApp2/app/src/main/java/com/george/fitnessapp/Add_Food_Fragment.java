package com.george.fitnessapp;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.george.fitnessapp.adapters.RecyclerViewAdapterFoods;
import com.george.fitnessapp.utils.Food;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Add_Food_Fragment extends Fragment implements View.OnClickListener {

    private static final int RESULT_LOAD_IMG = 123;
    String meal,day;
    String imageUri = "https://firebasestorage.googleapis.com/v0/b/fitness-app-ea2a6.appspot.com/o/Foods%2Fno_food.png?alt=media&token=d307796a-82fe-4349-97aa-1a48a1480af5";
    boolean hasSameName = false;

    RecyclerView recyclerView;
    SearchView searchView;
    Button allButton,fruitsButton,dairyProductsButton,legumesButton,drinksButton,userMadeButton,addSpecialFoodButton,cancelButton;
    FloatingActionButton addFoodButton;
    ConstraintLayout addFoodLayout;
    ImageView closeLayoutButton, foodPicture;
    EditText foodName,foodCalories,foodProteins,foodCarbs,foodFats;

    RecyclerViewAdapterFoods myAdapter;
    ArrayList<Food> foods;

    FirebaseDatabase database;
    DatabaseReference foodReference, mRef;
    StorageReference storageReference;
    LinearLayoutManager layoutManager;

    ValueEventListener foodEventListener;

    Animation bottom_sheet_fade_in_animation, fade_in_animation, bottom_sheet_fade_out_animation, fade_out_animation;

    public Add_Food_Fragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        database = FirebaseDatabase.getInstance();
        foodReference = database.getReference("Foods");
        storageReference = FirebaseStorage.getInstance().getReference("Foods");

        foods = new ArrayList<>();

        Bundle data = this.getArguments();
        if (data != null) {
            meal = data.getString("meal");
            day = data.getString("day");
        }

        foodEventListener = new ValueEventListener() {
            @Override
            public void onDataChange (@NonNull DataSnapshot snapshot){
                foods.clear();
                for (DataSnapshot food : snapshot.getChildren()) {
                    //Create new object Food
                    Food currentFood = new Food();

                    //Set Values
                    currentFood.setFoodName(food.getKey());
                    currentFood.setFoodImage(Uri.parse(food.child("Picture").getValue().toString()));
                    currentFood.setProteins(Float.parseFloat(food.child("Proteins").getValue().toString()));
                    currentFood.setCarbs(Float.parseFloat(food.child("Carbohydrates").getValue().toString()));
                    currentFood.setFats(Float.parseFloat(food.child("Fats").getValue().toString()));
                    currentFood.setKcal(Float.parseFloat(food.child("Kcal").getValue().toString()));
                    currentFood.setCategory(food.child("Category").getValue().toString());

                    //Add food object to list
                    foods.add(currentFood);
                }
                myAdapter.notifyDataSetChanged();

                //handle back button event
                allButton.performClick();
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error){
                Log.e("TAG", "onCancelled: ", error.toException());
            }
        };

        return inflater.inflate(R.layout.fragment_add_food,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Find Views
        recyclerView = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.searchView);
        addFoodLayout = view.findViewById(R.id.addFoodLayout);

        //Find EditTexts
        foodName = view.findViewById(R.id.editTextFoodName);
        foodCalories= view.findViewById(R.id.editTextCalories);
        foodProteins = view.findViewById(R.id.editTextProteins);
        foodCarbs = view.findViewById(R.id.editTextCarbs);
        foodFats = view.findViewById(R.id.editTextFats);

        //Find buttons in view
        allButton = view.findViewById(R.id.allButton);
        fruitsButton = view.findViewById(R.id.fruitsButton);
        dairyProductsButton = view.findViewById(R.id.dairyProductsButton);
        legumesButton = view.findViewById(R.id.legumesButton);
        drinksButton = view.findViewById(R.id.drinksButton);
        userMadeButton = view.findViewById(R.id.userMadeButton);
        addFoodButton = view.findViewById(R.id.addFoodButton);
        addSpecialFoodButton = view.findViewById(R.id.add_special_food_button);
        cancelButton = view.findViewById(R.id.cancel_button);

        //Find imageView
        closeLayoutButton = view.findViewById(R.id.clickView3);
        foodPicture = view.findViewById(R.id.food_picture);

        //Find animation
        bottom_sheet_fade_in_animation = AnimationUtils.loadAnimation(getContext(),R.anim.bottom_sheet_fade_in);
        bottom_sheet_fade_out_animation = AnimationUtils.loadAnimation(getContext(),R.anim.bottom_sheet_fade_out);
        fade_in_animation = AnimationUtils.loadAnimation(getContext(),R.anim.fade_in);
        fade_out_animation = AnimationUtils.loadAnimation(getContext(),R.anim.fade_out);

        //Better performance
        recyclerView.setHasFixedSize(true);
        foodPicture.setImageResource(R.drawable.no_food);

        //Create layout manager and adapter
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        myAdapter = new RecyclerViewAdapterFoods(getContext(),foods,meal,day);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(myAdapter);

        //handle add food event
        foodReference.addValueEventListener(foodEventListener);

        //Set search text listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                FilterFoodByText(text);
                return false;
            }
        });

        //Set on click listener to layout to override on click listener of imageview
        addFoodLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Don't do anything
            }
        });

        //Set on click listeners for category buttons
        fruitsButton.setOnClickListener(this);
        dairyProductsButton.setOnClickListener(this);
        allButton.setOnClickListener(this);
        legumesButton.setOnClickListener(this);
        drinksButton.setOnClickListener(this);
        userMadeButton.setOnClickListener(this);

        addFoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFoodButton.setVisibility(View.GONE);
                addFoodLayout.setVisibility(View.VISIBLE);
                closeLayoutButton.setVisibility(View.VISIBLE);
                addFoodLayout.clearAnimation();
                addFoodLayout.startAnimation(bottom_sheet_fade_in_animation);
                closeLayoutButton.clearAnimation();
                closeLayoutButton.startAnimation(fade_in_animation);
            }
        });

        closeLayoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFoodLayout.setVisibility(View.GONE);
                closeLayoutButton.setVisibility(View.GONE);
                addFoodLayout.clearAnimation();
                addFoodLayout.startAnimation(bottom_sheet_fade_out_animation);
                closeLayoutButton.clearAnimation();
                closeLayoutButton.startAnimation(fade_out_animation);
                addFoodButton.setVisibility(View.VISIBLE);
                addFoodButton.clearAnimation();
                addFoodButton.startAnimation(fade_in_animation);

                //wait 500ms for animation to end
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ClearTexts();
                    }
                },500);

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeLayoutButton.performClick();
            }
        });

        foodPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open stream for picking a photo
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
            }
        });

        addSpecialFoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    foodReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot foods) {
                            for (DataSnapshot food: foods.getChildren()) {
                                if(foodName.getText().toString().equals(food.getKey())){
                                    hasSameName = true;
                                }
                            }
                            if(!hasSameName){
                                //If all fields are filled
                                if(!foodName.getText().toString().isEmpty() &&
                                        !foodCalories.getText().toString().isEmpty() &&
                                        !foodProteins.getText().toString().isEmpty() &&
                                        !foodCarbs.getText().toString().isEmpty() &&
                                        !foodFats.getText().toString().isEmpty()) {
                                    //Fill database with data
                                    mRef = foodReference.child(foodName.getText().toString());

                                    //Create a map and pass values in it
                                    //This is used for pass values to database all at once
                                    Map<String, Object> values = new HashMap<>();
                                    values.put("Kcal", Float.parseFloat(foodCalories.getText().toString()) / 100);
                                    values.put("Proteins", Float.parseFloat(foodProteins.getText().toString()) / 100);
                                    values.put("Carbohydrates", Float.parseFloat(foodCarbs.getText().toString()) / 100);
                                    values.put("Fats", Float.parseFloat(foodFats.getText().toString()) / 100);
                                    values.put("Category", "User-Made");

                                    //upload image to firebase storage for later uses
                                    UploadImageToFirestore(Uri.parse(imageUri), foodName.getText().toString(), values);

                                    cancelButton.performClick();
                                }
                                else{
                                    Toast.makeText(getContext(), "Please fill in all fields to continue.", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else{
                                Toast.makeText(getContext(), foodName.getText().toString() + " already exists.", Toast.LENGTH_SHORT).show();
                                hasSameName = false;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("TAG", "onCancelled: ",  error.toException());
                        }
                    });

                }catch(IllegalArgumentException exception){
                    Toast.makeText(getContext(), "Please provide all required fields.", Toast.LENGTH_SHORT).show();
                }catch (Exception exception){
                    Toast.makeText(getContext(), "Something went wrong. Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Clear all texts and set image to default
    private void ClearTexts() {
        foodName.getText().clear();
        foodCalories.getText().clear();
        foodProteins.getText().clear();
        foodCarbs.getText().clear();
        foodFats.getText().clear();
        foodPicture.setImageResource(R.drawable.no_food);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == RESULT_LOAD_IMG){
            //Case for safety, doesn't produce NullPointerException
            if(getActivity()!=null && data!= null){
                imageUri = data.getData().toString();
                foodPicture.setImageURI(Uri.parse(imageUri));
            }
        }
    }

    public void UploadImageToFirestore(Uri uri, String foodName, Map<String, Object> values){
        StorageReference mFileReference = storageReference.child(foodName + ".jpg");
        mFileReference.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    //Successful upload
                    DownloadImageFromFirestore(foodName, values);
                }
                else{
                    //Unsuccessful upload (user didn't add image)
                    values.put("Picture", imageUri);

                    //Pass values in database
                    mRef.setValue(values);

                    //notify data for changes
                    myAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void DownloadImageFromFirestore(String foodName, Map<String, Object> values) {
        StorageReference mFileReference = storageReference.child(foodName + ".jpg");
        mFileReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Log.d("TAG", "Download from Firebase Uri : " + task.getResult());
                    imageUri = task.getResult().toString();
                    values.put("Picture", imageUri);

                    //Pass values in database
                    mRef.setValue(values);

                    //notify data for changes
                    myAdapter.notifyDataSetChanged();
                }
                else{
                    Log.d("TAG", "Download from Firebase Uri : Failed");
                }
            }
        });
    }

    private void FilterFoodByCategory(String category) {
        //Create a filtered list with foods
        ArrayList<Food> filteredFoodsByCategory = new ArrayList<>();

        //Add foods to list based on user's input
        for (Food food:foods) {
            if(food.getCategory().equals(category)){
                filteredFoodsByCategory.add(food);
            }
        }

        //update recycler view adapter
        myAdapter.FilterList(filteredFoodsByCategory);
    }

    private void FilterFoodByText(String text) {
        //Create a filtered list with foods
        ArrayList<Food> filteredFoodsByText = new ArrayList<>();

        //Add foods to list based on user's input
        for (Food food:foods) {
            if(food.getFoodName().toLowerCase().contains(text.toLowerCase())){
                filteredFoodsByText.add(food);
            }
        }

        //update recycler view adapter
        myAdapter.FilterList(filteredFoodsByText);
    }

    @Override
    public void onClick(View v) {
        //TODO add all types of foods as buttons
        switch (v.getId()) {
            case R.id.allButton:
                myAdapter.FilterList(foods);
                break;
            case R.id.fruitsButton:
                FilterFoodByCategory(fruitsButton.getText().toString());
                break;
            case R.id.dairyProductsButton:
                FilterFoodByCategory(dairyProductsButton.getText().toString());
                break;
            case R.id.legumesButton:
                FilterFoodByCategory(legumesButton.getText().toString());
                break;
            case R.id.drinksButton:
                FilterFoodByCategory(drinksButton.getText().toString());
                break;
            case R.id.userMadeButton:
                FilterFoodByCategory(userMadeButton.getText().toString());
                break;
            default:
                break;
        }

    }
}
