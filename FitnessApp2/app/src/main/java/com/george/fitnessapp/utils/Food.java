package com.george.fitnessapp.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Food implements Parcelable {

    String foodName,category,foodID;
    float quantity,proteins,carbs,fats,kcal;
    Uri foodImage;

    public Food(){
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public void setFoodID(String foodID) {
        this.foodID = foodID;
    }

    public void setProteins(float proteins) {
        this.proteins = proteins;
    }

    public void setCarbs(float carbs) {
        this.carbs = carbs;
    }

    public void setFats(float fats) {
        this.fats = fats;
    }

    public void setKcal(float kcal) {
        this.kcal = kcal;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setFoodImage(Uri foodImage) {
        this.foodImage = foodImage;
    }

    public String getFoodName() {
        return foodName;
    }

    public String getFoodID() {
        return foodID;
    }

    public float getProteins() {
        return proteins;
    }

    public float getCarbs() {
        return carbs;
    }

    public float getFats() {
        return fats;
    }

    public float getKcal() {
        return kcal;
    }

    public Uri getFoodImage() {
        return foodImage;
    }

    public String getCategory() {
        return category;
    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
