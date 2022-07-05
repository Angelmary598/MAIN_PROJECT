package com.example.blindtravel.models;

import com.google.gson.annotations.SerializedName;

public class SignupUser {

    @SerializedName("name"        ) String name        = null;
    @SerializedName("email"       ) String email       = null;
    @SerializedName("password"    ) String passwrd     = null;
    @SerializedName("mobile"      ) String mobile      = null;
    @SerializedName("currentLat"  ) String currentLat  = null;
    @SerializedName("currentLong" ) String currentLong = null;
    @SerializedName("destination" ) String destination = null;
    @SerializedName("role"        ) String role        = "Client";
    @SerializedName("picturePath" ) String picturePath = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswrd() {
        return passwrd;
    }

    public void setPasswrd(String passwrd) {
        this.passwrd = passwrd;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCurrentLat() {
        return currentLat;
    }

    public void setCurrentLat(String currentLat) {
        this.currentLat = currentLat;
    }

    public String getCurrentLong() {
        return currentLong;
    }

    public void setCurrentLong(String currentLong) {
        this.currentLong = currentLong;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }
}
