package com.example.caio.shoppinghelper.helper;


import android.content.Context;
import android.content.SharedPreferences;
import com.example.caio.shoppinghelper.model.ConstStrings;


public class Preferences {

    private Context context;
    private SharedPreferences preferences;
    private final int MODE = 0;
    private SharedPreferences.Editor editor;


    public Preferences( Context parameterContext){

        context = parameterContext;
        preferences = context.getSharedPreferences(ConstStrings.getFILE_NAME(), MODE );
        editor = preferences.edit();

    }


    public void saveData( String userIdentifier, String userName ){

        editor.putString(ConstStrings.getIDENTIFIER_KEY(), userIdentifier);
        editor.putString(ConstStrings.getKEY_NAME(), userName);
        editor.commit();

    }

    public String getIdentifier(){return preferences.getString(ConstStrings.getIDENTIFIER_KEY(), null);}

    public String getName(){return preferences.getString(ConstStrings.getKEY_NAME(), null);}


}

