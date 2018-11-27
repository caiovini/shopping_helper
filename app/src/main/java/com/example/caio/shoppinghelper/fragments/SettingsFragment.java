package com.example.caio.shoppinghelper.fragments;


import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.example.caio.shoppinghelper.R;
import com.example.caio.shoppinghelper.config.FirebaseConfig;
import com.example.caio.shoppinghelper.helper.CCUtils;
import com.example.caio.shoppinghelper.helper.Preferences;
import com.example.caio.shoppinghelper.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.vicmikhailau.maskededittext.MaskedFormatter;
import com.vicmikhailau.maskededittext.MaskedWatcher;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class SettingsFragment extends Fragment {


    public SettingsFragment() {
        // Required empty public constructor
    }

    private EditText firstName;
    private EditText secondName;
    private EditText cardNumber;
    private EditText cardSecurityNumber;
    private EditText cardPersonName;
    private Button buttonUpdate;

    private TextView textSettings;
    private TextView textCardOpt;
    private TextView textSecurityNumber;
    private TextView textExpiryDate;

    private Spinner  spinnerMonth;
    private Spinner  spinnerYear;

    private MaskedFormatter formatterCardNumber;
    private MaskedFormatter formatterSecurityNumber;
    private User user;

    DatabaseReference firebase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_settings, container, false);


        firstName = (EditText) view.findViewById(R.id.editText_first_name_update);
        secondName = (EditText) view.findViewById(R.id.editText_second_name_update);
        cardNumber = (EditText) view.findViewById(R.id.editText_card_number_update);
        cardSecurityNumber = (EditText) view.findViewById(R.id.editText_card_securityNumberUpdate);
        cardPersonName = (EditText) view.findViewById(R.id.editText_card_name_update);

        buttonUpdate = (Button) view.findViewById(R.id.register_button_update);
        spinnerMonth = (Spinner) view.findViewById(R.id.spinnerMonthUpdate);
        spinnerYear = (Spinner) view.findViewById(R.id.spinnerYearUpdate);


        Typeface typeface = ResourcesCompat.getFont(getActivity() , R.font.cobotocondensed_bold);

        textSettings = (TextView) view.findViewById(R.id.idTextRegistrationSettings);
        textCardOpt = (TextView)  view.findViewById(R.id.textViewCardOptUpdate);
        textSecurityNumber = (TextView)  view.findViewById(R.id.textViewCardSecurityNumberUpdate);
        textExpiryDate = (TextView) view.findViewById(R.id.textViewCardExpiryDateUpdate);

        textSettings.setTypeface(typeface);
        textCardOpt.setTypeface(typeface);
        textSecurityNumber.setTypeface(typeface);
        textExpiryDate.setTypeface(typeface);

        ArrayAdapter<String> adapter;
        List<String> list = new ArrayList<>();
        list.add("Year");
        int yearSpinner = Calendar.getInstance().get(Calendar.YEAR);

        for (int i = 0; i < 10; i ++){

            list.add(String.valueOf(yearSpinner));
            yearSpinner += 1;
        }

        adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item,
                list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(adapter);


        //Format field
        formatterCardNumber = new MaskedFormatter("####.####.####.####");
        cardNumber.addTextChangedListener(new MaskedWatcher(formatterCardNumber, cardNumber));

        formatterSecurityNumber = new MaskedFormatter("###");
        cardSecurityNumber.addTextChangedListener(new MaskedWatcher(formatterSecurityNumber , cardSecurityNumber));

        Preferences preferences = new Preferences(getActivity());
        String idUser = preferences.getIdentifier();


        firebase = FirebaseConfig.getFirebase();
        firebase.child("user")
                .child(idUser)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        user = dataSnapshot.getValue(User.class);
                        firstName.setText(user.getName());
                        secondName.setText(user.getSecondName());
                        cardNumber.setText(user.getCardNumber());
                        cardPersonName.setText(user.getCardPersonName());
                        cardSecurityNumber.setText(user.getCardSecurityNumber());

                        for(int i = 0; i < spinnerMonth.getAdapter().getCount(); i ++){

                            if(spinnerMonth.getAdapter().getItem(i).toString().equals(user.getCardMonthExpiryDate())){

                                spinnerMonth.setSelection(i);
                                break;
                            }
                        }

                        for(int i = 0; i < spinnerYear.getAdapter().getCount(); i ++){

                            if(spinnerYear.getAdapter().getItem(i).toString().equals(user.getCardYearExpiryDate())){

                                spinnerYear.setSelection(i);
                                break;
                            }
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Hide keyboard
                View viewKeyBoard = getActivity().getCurrentFocus();
                if (viewKeyBoard != null) {
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }


                performUpdate();

            }
        });

        return  view;
    }

    public void performUpdate(){


        if (!firstName.getText().toString().isEmpty()){

            user.setName(firstName.getText().toString());
        }else{

            firstName.requestFocus();
            Toast.makeText(getActivity(), "First name must be filled up" , Toast.LENGTH_SHORT).show();
            return;
        }

        if (!secondName.getText().toString().isEmpty()){

            user.setSecondName(secondName.getText().toString());
        }


        if (!cardNumber.getText().toString().isEmpty()){

            String unmaskedString;

            String textCardNumber = cardNumber.getText().toString();
            unmaskedString = formatterCardNumber.formatString(textCardNumber).getUnMaskedString();
            user.setCardNumber(unmaskedString);

            String textCardSecurityNumber = cardSecurityNumber.getText().toString();
            unmaskedString = formatterSecurityNumber.formatString(textCardSecurityNumber).getUnMaskedString();
            user.setCardSecurityNumber(unmaskedString);

            user.setCardPersonName(cardPersonName.getText().toString());
            user.setCardSecurityNumber(cardSecurityNumber.getText().toString());
            user.setCardMonthExpiryDate(spinnerMonth.getSelectedItem().toString());
            user.setCardYearExpiryDate(spinnerYear.getSelectedItem().toString());


            if(user.getCardPersonName().isEmpty()){

                cardPersonName.requestFocus();
                Toast.makeText(getActivity(), "Name on your card must be filled up" , Toast.LENGTH_SHORT).show();
                return;
            }else if (user.getCardSecurityNumber().isEmpty()){

                cardSecurityNumber.requestFocus();
                Toast.makeText(getActivity(), "Security number be filled up" , Toast.LENGTH_SHORT).show();
                return;
            }else if (user.getCardMonthExpiryDate().equals("Month")) {

                spinnerMonth.requestFocus();
                Toast.makeText(getActivity(), "You must inform card's expiry month", Toast.LENGTH_SHORT).show();
                return;
            }else if(user.getCardYearExpiryDate().equals("Year")){

                spinnerYear.requestFocus();
                Toast.makeText(getActivity(), "You must inform card's expiry year", Toast.LENGTH_SHORT).show();
                return;}

            try {

                if(!CCUtils.validCC(user.getCardNumber())){
                    //If false then invalid credit card
                    cardNumber.requestFocus();
                    Toast.makeText(getContext(), "Invalid credit card number" , Toast.LENGTH_SHORT).show();
                    return;
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Invalid credit card number" , Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Preferences preferences = new Preferences(getActivity());
        String idUser = preferences.getIdentifier();

        firebase.child("user")
                .child(idUser)
                .setValue(user);
        Toast.makeText(getContext(), "Data up to date" , Toast.LENGTH_SHORT).show();

    }

}

