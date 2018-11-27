package com.example.caio.shoppinghelper.model;

import java.io.Serializable;


public class SMSNumbers implements Serializable{

    private String email;
    private String SMSNumber;


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSMSNumber() {
        return SMSNumber;
    }

    public void setSMSNumber(String SMSNumber) {
        this.SMSNumber = SMSNumber;
    }

}
