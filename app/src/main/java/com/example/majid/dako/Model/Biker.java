package com.example.majid.dako.Model;

/**
 * Created by Majid on 6/17/2017.
 */

public class Biker {

    private String KEY;
    private String name;
    private String phoneNumber;
    private double currentLat;
    private double currentLong;
    private boolean onLine;
    private boolean onRide;
    private boolean loggedIn;

    public Biker() {


    }


    public String getKEY() {
        return KEY;
    }

    public void setKEY(String KEY) {
        this.KEY = KEY;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public double getCurrentLat() {
        return currentLat;
    }

    public void setCurrentLat(double currentLat) {
        this.currentLat = currentLat;
    }

    public double getCurrentLong() {
        return currentLong;
    }

    public void setCurrentLong(double currentLong) {
        this.currentLong = currentLong;
    }

    public boolean isOnLine() {
        return onLine;
    }

    public void setOnLine(boolean onLine) {
        this.onLine = onLine;
    }

    public boolean isOnRide() {
        return onRide;
    }

    public void setOnRide(boolean onRide) {
        this.onRide = onRide;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }
}





