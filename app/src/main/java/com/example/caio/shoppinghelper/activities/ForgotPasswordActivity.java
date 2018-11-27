package com.example.caio.shoppinghelper.activities;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.caio.shoppinghelper.R;
import com.example.caio.shoppinghelper.config.FirebaseConfig;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


public class ForgotPasswordActivity extends AppCompatActivity {

    private TextView textTypeEmail;
    private EditText email;
    private Button submit;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);


        textTypeEmail = (TextView)  findViewById(R.id.textViewForgotPassword);
        email = (EditText) findViewById(R.id.idemailForgot);
        submit = (Button) findViewById(R.id.buttonForgotPassword);


        Typeface typeface = ResourcesCompat.getFont(this , R.font.cobotocondensed_bold);
        textTypeEmail.setTypeface(typeface);


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String emailReset = email.getText().toString();

                if (emailReset.isEmpty()){

                    email.requestFocus();
                    Toast.makeText(ForgotPasswordActivity.this, "Email in blank" , Toast.LENGTH_SHORT).show();
                    return;
                }else if (!isValidEmail(emailReset)){

                    email.requestFocus();
                    Toast.makeText(ForgotPasswordActivity.this, "invalid email" , Toast.LENGTH_SHORT).show();
                    return;
                }

                /*

                Apply for the email reset in the future

                auth = FirebaseConfig.getFirebaseAuth();
                  auth.sendPasswordResetEmail(emailReset)
                      .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    Toast.makeText(ForgotPasswordActivity.this,
                                                      "Instructions sent to your email" ,
                                                           Toast.LENGTH_SHORT).show();
                                }else{

                                    Toast.makeText(ForgotPasswordActivity.this,
                                                      "Error requiring reset" ,
                                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                */


                //Go back to Login activity
                onBackPressed();
            }
        });


    }

    private boolean isValidEmail(CharSequence email){

        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public void onBackPressed() {

        finish();
        super.onBackPressed();
    }
}
