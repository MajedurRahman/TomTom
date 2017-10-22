package com.example.majid.dako.Model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Majid on 6/18/2017.
 */

public class User {

    private String UsersName, UsersPhone , ID;
    private LatLng UsersCurrentPosition, usersDestination;

    public User(){


    }

    public User(String usersName, String usersPhone, LatLng usersCurrentPosition) {
        UsersName = usersName;
        UsersPhone = usersPhone;
        UsersCurrentPosition = usersCurrentPosition;
    }

    public String getUsersName() {
        return UsersName;
    }

    public void setUsersName(String usersName) {
        UsersName = usersName;
    }

    public String getUsersPhone() {
        return UsersPhone;
    }

    public void setUsersPhone(String usersPhone) {
        UsersPhone = usersPhone;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public LatLng getUsersCurrentPosition() {
        return UsersCurrentPosition;
    }

    public void setUsersCurrentPosition(LatLng usersCurrentPosition) {
        UsersCurrentPosition = usersCurrentPosition;
    }


}
