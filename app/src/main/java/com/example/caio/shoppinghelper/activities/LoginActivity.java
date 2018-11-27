package com.example.caio.shoppinghelper.activities;


//Packages
import com.example.caio.shoppinghelper.R;
import com.example.caio.shoppinghelper.helper.Base64Custom;
import com.example.caio.shoppinghelper.model.User;
import com.example.caio.shoppinghelper.helper.Preferences;
import com.example.caio.shoppinghelper.config.FirebaseConfig;
import com.example.caio.shoppinghelper.model.ConstStrings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    //Case user wants to register
    private TextView regText;

    //Case user forgets password
    private TextView forgotPasswordText;

    //Email field
    private EditText email;

    //Password field
    private EditText password;

    //Login button
    private Button buttonLogin;

    //New instance of user
    private User user;

    //private FirebaseAuth usuarioFirebase;
    private FirebaseAuth auth;

    private ProgressBar loading;

    private String loginUser;
    private DatabaseReference firebase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        if (Build.VERSION.SDK_INT > 23){

            if ((ContextCompat.checkSelfPermission(LoginActivity.this
                    , android.Manifest.permission.SEND_SMS ) == PackageManager.PERMISSION_DENIED ) ||

            (ContextCompat.checkSelfPermission(LoginActivity.this
                    , android.Manifest.permission.CAMERA ) == PackageManager.PERMISSION_DENIED )){

                String[] listPermissions = {android.Manifest.permission.SEND_SMS , android.Manifest.permission.CAMERA };

                ActivityCompat.requestPermissions(LoginActivity.this
                        , listPermissions , ConstStrings.getPermissionRequestCode());}}

        user = new User();
        auth = FirebaseConfig.getFirebaseAuth();

        Bundle extras = getIntent().getExtras();
        if(extras != null){

            String source = extras.get("SOURCE").toString();
            if (source.equals("MainActivity")){

                auth.signOut();
            }
        }



        if( auth.getCurrentUser() != null){

            auth = FirebaseConfig.getFirebaseAuth();
            String emailUser = auth.getCurrentUser().getEmail();
            String encodeUserEmail = Base64Custom.encodeBase64(emailUser);
            Preferences preferences = new Preferences(LoginActivity.this);

            preferences.saveData(encodeUserEmail , " " );

            Intent intent = new Intent(this , MainActivity.class);
            startActivity(intent);
            finish();

        }


        //New user
        regText = (TextView) findViewById(R.id.registerText);
        forgotPasswordText = (TextView) findViewById(R.id.forgotPasswordText);
        email = (EditText) findViewById(R.id.idemail);
        password = (EditText) findViewById(R.id.idPassword);
        buttonLogin = (Button) findViewById(R.id.button);
        loading = (ProgressBar) findViewById(R.id.progressBar);


        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //get email
                user.setEmail(email.getText().toString().trim());

                //Validate email
                if (!validateField(user.getEmail())){
                    email.requestFocus();
                    Toast.makeText(LoginActivity.this, "Email in blank" , Toast.LENGTH_SHORT).show();
                    return;
                }else if (!isValidEmail(user.getEmail())){
                    email.requestFocus();
                    Toast.makeText(LoginActivity.this, "invalid email" , Toast.LENGTH_SHORT).show();
                    return;
                }

                //Get password
                user.setPassword(password.getText().toString().trim());

                //Validate field
                if(!validateField(user.getPassword())){

                    password.requestFocus();
                    Toast.makeText(LoginActivity.this, "Password in blank" , Toast.LENGTH_SHORT).show();
                    return;
                }

                Login commitLogin = new Login();
                commitLogin.execute();
            }
        });


        regText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { callActivityRegister(); }
        });

        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { callActivityForgotPassword(); }
        });

    }


    public void callActivityRegister(){

        Intent intent = new Intent(this , RegisterActivity.class);
        startActivity(intent);
    }

    public void callActivityForgotPassword(){

        Intent intent = new Intent(this , ForgotPasswordActivity.class);
        startActivity(intent);
    }

    public void callActivityMain(){

        Intent intent = new Intent(this , MainActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isValidEmail(CharSequence email){

        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean validateField(String string){

        if (string.isEmpty() || string == null){
            return false;
        }else{
            return true;

        }

    }

    /*
       Used to execute Login
       Show progress bar to user
       Disable TOUCHABLE in the application
       Enable TOUCHABLE back and commit login
       Call main activity

    */

    class Login extends AsyncTask <String, String, String>{


        @Override
        protected String doInBackground(String... params) {



            auth = FirebaseConfig.getFirebaseAuth();
            auth.signInWithEmailAndPassword(
                    user.getEmail(),
                    user.getPassword()
            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()){

                        //Update preferences before calling onDataChange
                        loginUser = Base64Custom.encodeBase64(user.getEmail());
                        Preferences preferences = new Preferences(LoginActivity.this);
                        preferences.saveData( loginUser , " " );


                        firebase = FirebaseConfig.getFirebase();
                        firebase.child(ConstStrings.getUserTable())
                                .child( loginUser )
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        User recoverUser = dataSnapshot.getValue( User.class );
                                        Preferences preferences = new Preferences(LoginActivity.this);
                                        preferences.saveData( loginUser ,recoverUser.getName() );

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                        //Call screen responsible to show list of products
                        callActivityMain();
                        finish();

                    }else{

                        //Error at the login
                        Toast.makeText(LoginActivity.this, "Login failed !" , Toast.LENGTH_SHORT).show();

                        //Enable fields
                        email.setEnabled(true);
                        password.setEnabled(true);

                        loading.setVisibility(View.GONE);

                    }
                }

            });


            return null;
        }


        @Override
        protected void onPostExecute(String result) {

            //Enable touchable
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            buttonLogin.setClickable(true);
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {


            //Hide keyboard
            View view = LoginActivity.this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }


            //Progressbar
            loading.setVisibility(View.VISIBLE);

            //Disable fields
            email.setEnabled(false);
            password.setEnabled(false);
            buttonLogin.setClickable(false);

            //Disable touchable
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            super.onPreExecute();
        }
    }

}
