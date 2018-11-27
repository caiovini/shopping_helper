package com.example.caio.shoppinghelper.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public final class FirebaseConfig {

    private static DatabaseReference firebaseReference;
    private static FirebaseAuth authentication;
    private static StorageReference storageReference;

    public static StorageReference getStorageReference() {

        if(storageReference == null){
            storageReference = FirebaseStorage.getInstance().getReference();
        }
        return storageReference;
    }


    public static DatabaseReference getFirebase(){

        if( firebaseReference == null ){
            firebaseReference = FirebaseDatabase.getInstance().getReference();
        }

        return firebaseReference;
    }

    public static FirebaseAuth getFirebaseAuth(){

        if( authentication == null ){
            authentication = FirebaseAuth.getInstance();
        }
        return authentication;
    }

}


