package com.george.fitnessapp.utils;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Chat implements Parcelable {

    String chatName,chatImage;
    List<String> Members;

    public Chat() {
    }

    public List<String> getMembers() {
        return Members;
    }

    public String getChatName() {
        return chatName;
    }

    public String getChatImage() {
        return chatImage;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public void setChatImage(String chatImage) {
        this.chatImage = chatImage;
    }

    public void setMembers(List<String> members) {
        Members = members;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
