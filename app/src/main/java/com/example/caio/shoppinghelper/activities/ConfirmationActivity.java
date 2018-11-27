package com.example.caio.shoppinghelper.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.caio.shoppinghelper.R;
import com.example.caio.shoppinghelper.model.SMSNumbers;
import com.example.caio.shoppinghelper.config.FirebaseConfig;
import com.example.caio.shoppinghelper.helper.Base64Custom;
import com.example.caio.shoppinghelper.helper.Preferences;
import com.example.caio.shoppinghelper.helper.SaveUserInformations;
import com.example.caio.shoppinghelper.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;


public class ConfirmationActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private User user;
    private SMSNumbers smsNumbers;
    private TextView messageConfirmation;
    private EditText confirmationBox;
    private Button buttomOK;

    private ProgressBar loading;

    private String submitReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        String message = "Type here the code sent by SMS";

        messageConfirmation = (TextView) findViewById(R.id.confirmation_text);
        confirmationBox = (EditText) findViewById(R.id.confirmation_box);
        buttomOK = (Button) findViewById(R.id.button_confirmation);
        loading = (ProgressBar) findViewById(R.id.progressBarConfirmation);

        Typeface typeface = ResourcesCompat.getFont(this , R.font.cobotocondensed_bold);
        messageConfirmation.setTypeface(typeface);
        messageConfirmation.setText(message);

        Bundle extras = getIntent().getExtras();
        if(extras != null){

            user = (User) extras.getSerializable("user");
            smsNumbers = (SMSNumbers) extras.getSerializable("sms");

        }

        buttomOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (confirmationBox.getText().toString().equals(smsNumbers.getSMSNumber())){

                    Register registerUser = new Register();

                    //registerUser();
                    registerUser.execute();

                }else{
                    Toast.makeText(ConfirmationActivity.this , "Typed number is incorrect" , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void callActivityMain(){
        Intent intent = new Intent(this , MainActivity.class);
        startActivity(intent);
        finish();
    }

    class Register extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {


            auth = FirebaseConfig.getFirebaseAuth();
            auth.createUserWithEmailAndPassword(
                    user.getEmail(),
                    user.getPassword()
            ).addOnCompleteListener(ConfirmationActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(Task<AuthResult> task) {

                    if(task.isSuccessful()){

                        callActivityMain();

                        Toast.makeText(ConfirmationActivity.this, "Registration success" , Toast.LENGTH_SHORT).show();

                        String userIdentifier = Base64Custom.encodeBase64(user.getEmail());
                        user.setId(userIdentifier);
                        user.save();

                        SaveUserInformations saveUserInformations = new SaveUserInformations();
                        saveUserInformations.saveSMS(user.getId(), smsNumbers);

                        Preferences preferences = new Preferences(ConfirmationActivity.this);
                        preferences.saveData(userIdentifier , user.getName() );

                        submitReturn = "OK";
                        loading.setVisibility(View.GONE);

                    }else{

                        String error = "";

                        try{
                            throw task.getException();
                        } catch (FirebaseAuthWeakPasswordException e) {
                            error = "Choose a password with numbers and letters.";
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            error = "Email is not valid.";
                        } catch (FirebaseAuthUserCollisionException e) {
                            error = "There's already an account using this email";
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Toast.makeText(ConfirmationActivity.this, error , Toast.LENGTH_SHORT).show();

                        //Enable field
                        confirmationBox.setEnabled(true);
                        loading.setVisibility(View.GONE);
                    }
                }

            });

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            //loading.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {

            //Disable field
            confirmationBox.setEnabled(false);

            loading.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            super.onPreExecute();
        }
    }
}
