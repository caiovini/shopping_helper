package com.example.caio.shoppinghelper.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.SmsManager;
import com.example.caio.shoppinghelper.R;
import com.example.caio.shoppinghelper.model.SMSNumbers;
import com.example.caio.shoppinghelper.model.User;
import com.example.caio.shoppinghelper.helper.CCUtils;
import com.vicmikhailau.maskededittext.MaskedFormatter;
import com.vicmikhailau.maskededittext.MaskedWatcher;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class RegisterActivity extends AppCompatActivity {

    private Button registerButton;
    private EditText firstName;
    private EditText secondName;
    private EditText email;
    private EditText password;
    private EditText retypePassword;
    private EditText phone;
    private EditText cardNumber;
    private EditText cardPersonName;
    private EditText cardSecurityNumber;

    private Spinner  spinnerMonth;
    private Spinner  spinnerYear;

    private TextView registerText;
    private TextView textCardOpt;
    private TextView textSecurityNumber;
    private TextView textExpiryDate;

    private String phoneNumberSend;
    private String messageSend;

    private MaskedFormatter formatterCardNumber;
    private MaskedFormatter formatterPhoneNumber;
    private MaskedFormatter formatterSecurityNumber;

    User user = new User();
    SMSNumbers smsNumbers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Register button
        registerButton = (Button) findViewById(R.id.register_button);

        registerText = (TextView) findViewById(R.id.idTextRegistration);
        textCardOpt = (TextView)  findViewById(R.id.textViewCardOpt);
        textSecurityNumber = (TextView)  findViewById(R.id.textViewCardSecurityNumber);
        textExpiryDate = (TextView) findViewById(R.id.textViewCardExpiryDate);

        Typeface typeface = ResourcesCompat.getFont(this , R.font.cobotocondensed_bold);
        registerText.setTypeface(typeface);
        textCardOpt.setTypeface(typeface);
        textSecurityNumber.setTypeface(typeface);
        textExpiryDate.setTypeface(typeface);


        firstName = (EditText) findViewById(R.id.editText_first_name);
        secondName = (EditText) findViewById(R.id.editText_second_name);
        email = (EditText) findViewById(R.id.email_register);
        password = (EditText) findViewById(R.id.password_register);
        phone = (EditText) findViewById(R.id.phone_register);
        cardNumber = (EditText) findViewById(R.id.editText_card_number);
        cardPersonName = (EditText) findViewById(R.id.editText_card_name);
        cardSecurityNumber = (EditText) findViewById(R.id.editText_card_securityNumber);
        retypePassword = (EditText) findViewById(R.id.password_register_retype);


        spinnerMonth = (Spinner) findViewById(R.id.spinnerMonth);
        spinnerYear = (Spinner) findViewById(R.id.spinnerYear);


        ArrayAdapter<String> adapter;
        List<String> list = new ArrayList<>();
        list.add("Year");
        int yearSpinner = Calendar.getInstance().get(Calendar.YEAR);

        for (int i = 0; i < 10; i ++){

            list.add(String.valueOf(yearSpinner));
            yearSpinner += 1;
        }

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(adapter);

        //Format fields
        formatterCardNumber = new MaskedFormatter("####.####.####.####");
        cardNumber.addTextChangedListener(new MaskedWatcher(formatterCardNumber, cardNumber));

        formatterPhoneNumber = new MaskedFormatter("+64 (###) ###-####");
        phone.addTextChangedListener(new MaskedWatcher(formatterPhoneNumber , phone));
        phone.setText("+64");

        formatterSecurityNumber = new MaskedFormatter("###");
        cardSecurityNumber.addTextChangedListener(new MaskedWatcher(formatterSecurityNumber , cardSecurityNumber));


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //First name
                user.setName(firstName.getText().toString());

                //Second name
                user.setSecondName(secondName.getText().toString());

                //email
                user.setEmail(email.getText().toString());

                //password
                user.setPassword(password.getText().toString());

                //Card name
                user.setCardPersonName(cardPersonName.getText().toString());

                //Card expiry month and year
                user.setCardMonthExpiryDate(spinnerMonth.getSelectedItem().toString());
                user.setCardYearExpiryDate(spinnerYear.getSelectedItem().toString());

                String unmaskedString;

                //phone number
                String textPhoneNumber = phone.getText().toString();
                unmaskedString = formatterPhoneNumber.formatString(textPhoneNumber).getUnMaskedString();
                user.setPhone(unmaskedString);

                //card number
                String textCardNumber = cardNumber.getText().toString();
                unmaskedString = formatterCardNumber.formatString(textCardNumber).getUnMaskedString();
                user.setCardNumber(unmaskedString);

                //Security number
                String textCardSecurityNumber = cardSecurityNumber.getText().toString();
                unmaskedString = formatterSecurityNumber.formatString(textCardSecurityNumber).getUnMaskedString();
                user.setCardSecurityNumber(unmaskedString);

                if(!validateField(user.getName())){

                    firstName.requestFocus();
                    Toast.makeText(RegisterActivity.this, "First name must be filled up" , Toast.LENGTH_SHORT).show();
                    return;
                }else if (!validateField(user.getEmail())){

                    email.requestFocus();
                    Toast.makeText(RegisterActivity.this, "Email must be filled up" , Toast.LENGTH_SHORT).show();
                    return;
                }else if (!validateField(user.getPassword())) {

                    password.requestFocus();
                    Toast.makeText(RegisterActivity.this, "Password must be filled up", Toast.LENGTH_SHORT).show();
                    return;
                }else if(!user.getPassword().equals(retypePassword.getText().toString())){

                    retypePassword.requestFocus();
                    Toast.makeText(RegisterActivity.this, "Password must be retyped correctly", Toast.LENGTH_SHORT).show();
                    return;
                }else if (!validateField(user.getPhone())) {

                    phone.requestFocus();
                    Toast.makeText(RegisterActivity.this, "Phone must be filled up", Toast.LENGTH_SHORT).show();
                    return;
                }else if (validateField(user.getCardNumber())){

                    if(!validateField(user.getCardPersonName())){

                        cardPersonName.requestFocus();
                        Toast.makeText(RegisterActivity.this, "Name on your card must be filled up" , Toast.LENGTH_SHORT).show();
                        return;
                    }else if (!validateField(user.getCardSecurityNumber())){

                        cardSecurityNumber.requestFocus();
                        Toast.makeText(RegisterActivity.this, "Security number be filled up" , Toast.LENGTH_SHORT).show();
                        return;
                    }else if (user.getCardMonthExpiryDate().equals("Month")) {

                        spinnerMonth.requestFocus();
                        Toast.makeText(RegisterActivity.this, "You must inform card's expiry month", Toast.LENGTH_SHORT).show();
                        return;
                    }else if(user.getCardYearExpiryDate().equals("Year")){

                        spinnerYear.requestFocus();
                        Toast.makeText(RegisterActivity.this, "You must inform card's expiry year", Toast.LENGTH_SHORT).show();
                        return;}

                    //check if card number is valid
                    try {

                        if(!CCUtils.validCC(user.getCardNumber())){
                            //If false then invalid credit card
                            cardNumber.requestFocus();
                            Toast.makeText(RegisterActivity.this, "Invalid credit card number" , Toast.LENGTH_SHORT).show();
                            return;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(RegisterActivity.this, "Invalid credit card number" , Toast.LENGTH_SHORT).show();
                        return;
                    }

                }

                //If everything is fine then call other screen
                registerUser();
            }
        });

    }

    private void registerUser(){

        //Send ConfirmationActivity sms
        Random rand = new Random();
        int randomNumber = rand.nextInt(9999) ;
        if(sendSMS(user.getPhone() , "This is your ConfirmationActivity number: " + randomNumber)) {

            smsNumbers = new SMSNumbers();
            smsNumbers.setEmail(user.getEmail());
            smsNumbers.setSMSNumber(String.valueOf(randomNumber));


            //Call another activity with the message
            callMessageActivity();

        }else{

            Toast.makeText(RegisterActivity.this, "Not possible to send SMS. Reopen the app" , Toast.LENGTH_SHORT).show();
        }
    }

    private void callMessageActivity(){

        Intent intent = new Intent(getApplicationContext() , ConfirmationActivity.class);
        intent.putExtra("user" , user);
        intent.putExtra("sms" , smsNumbers);
        startActivity(intent);
    }

    private boolean validateField(String string){

        if (string.isEmpty() || string == null){
            return false;
        }else{
            return true;
        }
    }

    private boolean sendSMS(String phoneNumber, String message) {

        phoneNumberSend = phoneNumber;
        messageSend = message;

        if (ContextCompat.checkSelfPermission(RegisterActivity.this
                , android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumberSend, null, messageSend, null, null);
            return true;
        }else
            return false;
    }
}
