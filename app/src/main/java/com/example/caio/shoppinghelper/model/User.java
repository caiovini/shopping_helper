package com.example.caio.shoppinghelper.model;

import com.example.caio.shoppinghelper.config.FirebaseConfig;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import java.io.Serializable;

public class User implements Serializable {

    private String id;
    private String name;
    private String secondName;
    private String email;
    private String password;
    private String cardNumber;
    private String cardSecurityNumber;
    private String cardPersonName;
    private String cardMonthExpiryDate;
    private String cardYearExpiryDate;
    private String Phone;

    public User(){}


    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    @Exclude
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getCardSecurityNumber() {
        return cardSecurityNumber;
    }

    public void setCardSecurityNumber(String cardSecurityNumber) {
        this.cardSecurityNumber = cardSecurityNumber;
    }

    public String getCardPersonName() {
        return cardPersonName;
    }

    public void setCardPersonName(String cardPersonName) {
        this.cardPersonName = cardPersonName;
    }

    public String getCardMonthExpiryDate() {
        return cardMonthExpiryDate;
    }

    public void setCardMonthExpiryDate(String cardMonthExpiryDate) {
        this.cardMonthExpiryDate = cardMonthExpiryDate;
    }

    public String getCardYearExpiryDate() {
        return cardYearExpiryDate;
    }

    public void setCardYearExpiryDate(String cardYearExpiryDate) {
        this.cardYearExpiryDate = cardYearExpiryDate;
    }

    @Exclude
    public void save(){

        DatabaseReference firebaseReference = FirebaseConfig.getFirebase();
        firebaseReference.child(ConstStrings.getUserTable()).child( getId() ).setValue( this );
    }

}