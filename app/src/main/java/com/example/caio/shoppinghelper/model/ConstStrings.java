package com.example.caio.shoppinghelper.model;

public final class ConstStrings {


    private final static String FILE_NAME = "shopping.preferences";
    private final static String IDENTIFIER_KEY = "logedUserdLoged";
    private final static String KEY_NAME = "userLogedName";
    private final static String KEY_URI_PHOTO = "photo";
    private final static String FLAG_LOGGED_KEY = "flagLogged";

    //Database
    private final static String DATA_USER_INFORMATIONS = "sms";
    final public static String USER_TABLE = "user";

    //Permissions
    public static final int PERMISSION_REQUEST_CODE = 1;


    public static String getUserTable() {
        return USER_TABLE;
    }

    public static String getFILE_NAME() {
        return FILE_NAME;
    }

    public static String getIDENTIFIER_KEY() {
        return IDENTIFIER_KEY;
    }

    public static String getKEY_NAME() {
        return KEY_NAME;
    }

    public static String getDataUserInformations() {
        return DATA_USER_INFORMATIONS;
    }

    public static int getPermissionRequestCode() {
        return PERMISSION_REQUEST_CODE;
    }


}
