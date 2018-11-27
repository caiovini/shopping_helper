package com.example.caio.shoppinghelper.activities;

import com.example.caio.shoppinghelper.R;
import com.google.firebase.FirebaseApp;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertTrue;


@RunWith(RobolectricTestRunner.class)
public class LoginActivityTest {

    private ActivityController<LoginActivity> controller;
    private LoginActivity activity;

    @Before
    public void setUp(){

        controller = Robolectric.buildActivity(LoginActivity.class);

        //Initialize firebase from here, otherwise an exception will be thrown
        FirebaseApp.initializeApp(controller.get());

        Intent intent = new Intent(RuntimeEnvironment.application , MainActivity.class);
        intent.putExtra("activity_extra", "my extra value");
        activity = controller
                .newIntent(intent)
                .create()
                .start()
                .resume()
                .visible()
                .get();

    }

    @Test
    public void testActivityInit() {

        assertNotNull("Activity could not be initialized", activity);
    }

    @Test
    public void testLoginButton() {


        Button loginButton = (Button) activity.findViewById(R.id.button);
        loginButton.performClick();

        assertNotNull("Button could not be found", loginButton);
        assertTrue("Button contains incorrect text",
                "Login".equals(loginButton.getText().toString()));

    }

    @Test
    public void testEmailTyped(){

        EditText emailField = (EditText) activity.findViewById(R.id.idemail);
        emailField.setText("name@name.com");

        assertNotNull("Email field could not be found", emailField);
        assertTrue("Email field contains incorrect text",
                "name@name.com".equals(emailField.getText().toString()));

    }

    @After
    public void tearDown() {
        // Destroy activity after every test
        controller
                .pause()
                .stop()
                .destroy();
    }

}