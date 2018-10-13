package com.firebase.curd.app.support;

public class UserModel {
    String userID,userName,userMobile,userEmail, userPassword;

    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserMobile() {
        return userMobile;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public UserModel(String userID, String userName, String userMobile, String userEmail, String userPassword) {
        this.userID = userID;
        this.userName = userName;
        this.userMobile = userMobile;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }

    public UserModel() {
    }
}
