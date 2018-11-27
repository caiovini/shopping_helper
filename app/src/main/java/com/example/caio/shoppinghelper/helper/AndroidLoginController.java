package com.example.caio.shoppinghelper.helper;

import android.app.Application;
import android.text.TextUtils;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class AndroidLoginController extends Application {

    private static final String TAG = AndroidLoginController.class.getSimpleName();
    private RequestQueue requestQueue;
    private static AndroidLoginController mInstance;

    @Override
    public void onCreate() {

        super.onCreate();
        mInstance = this;
    }

    public static synchronized AndroidLoginController getmInstance(){

        return mInstance;
    }

    public RequestQueue getRequestQueue(){

        if(requestQueue == null){

            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request , String tag){

        request.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(request);
    }

}

