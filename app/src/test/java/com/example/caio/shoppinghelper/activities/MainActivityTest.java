package com.example.caio.shoppinghelper.activities;
import android.content.Intent;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.caio.shoppinghelper.config.FirebaseConfig;
import com.example.caio.shoppinghelper.helper.AndroidLoginController;
import com.example.caio.shoppinghelper.helper.Configure;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import java.util.HashMap;
import java.util.Map;
import static junit.framework.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    private ActivityController<MainActivity> controller;
    private MainActivity activity;


    @Before
    public void setUp() {

        controller = Robolectric.buildActivity(MainActivity.class);
        FirebaseApp.initializeApp(controller.get());
    }

    private void createWithIntent(String extra) {

        Intent intent = new Intent(RuntimeEnvironment.application , MainActivity.class);
        intent.putExtra("activity_extra", extra);
        activity = controller
                .newIntent(intent)
                .create()
                .start()
                .resume()
                .visible()
                .get();


    }

    @Test
    public void createsAndDestroysActivity() {

        createWithIntent("my extra_value");
        assertNotNull("Activity not initialized correctly", activity);
    }

    @Test
    public void testFirebaseConnection() {

        FirebaseAuth auth = FirebaseConfig.getFirebaseAuth();
        assertNotNull("Firebase not initialized correctly", auth);
    }

    @Test
    public void testApiImageRecognition(){

        StringRequest stringRequest = new StringRequest(StringRequest.Method.POST, Configure.REQUEST_IMAGE_ANALYSIS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try{

                    JSONObject jsonObject = new JSONObject(response);
                    final String fruit = jsonObject.getString("label");

                    assertNotNull(fruit);

                    // Destroy activity after every test

                    controller
                            .pause()
                            .stop()
                            .destroy();

                }catch(JSONException ex){

                    Log.i("Json error" , ex.toString());
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                throw new RuntimeException("Error: " + error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String , String> params = new HashMap<>();
                params.put("image_encoded" , StaticString.encryptedImage);

                return params;
            }
        };

        String myTag = "req_ai_fruit_reader";
        AndroidLoginController.getmInstance().addToRequestQueue(stringRequest, myTag);

    }

}
