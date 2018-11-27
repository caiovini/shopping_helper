package com.example.caio.shoppinghelper.helper;

import com.example.caio.shoppinghelper.model.ConstStrings;
import com.example.caio.shoppinghelper.model.SMSNumbers;
import com.example.caio.shoppinghelper.config.FirebaseConfig;
import com.google.firebase.database.DatabaseReference;

public class SaveUserInformations {

    private DatabaseReference firebase;



    public boolean saveSMS(String idUser , SMSNumbers sms){

        try{
            firebase = FirebaseConfig.getFirebase().child(ConstStrings.getDataUserInformations());
            firebase.child(idUser)
                    .setValue(sms);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

}
