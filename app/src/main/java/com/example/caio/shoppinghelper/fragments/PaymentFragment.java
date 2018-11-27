package com.example.caio.shoppinghelper.fragments;


import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.caio.shoppinghelper.activities.SnackBarActivity;
import com.example.caio.shoppinghelper.config.FirebaseConfig;
import com.example.caio.shoppinghelper.helper.Preferences;
import com.example.caio.shoppinghelper.interfaces.ReloadAppInterface;
import com.example.caio.shoppinghelper.model.TotalProduct;
import com.example.caio.shoppinghelper.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.example.caio.shoppinghelper.R;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import java.io.ByteArrayOutputStream;


public class PaymentFragment extends Fragment implements View.OnClickListener{

    private Button buttonCard;
    private TextView scanText;
    private ImageView QRcodeView;
    private TextView numTotal;
    private TextView totalText;
    private ImageView payCheck;

    private ValueEventListener valueEventListenerProducts;
    private DatabaseReference firebase;
    private String total = "0.00";

    private ProgressBar loading;

    private Activity activity;
    private boolean flagCallSnackbarActivity = false;

    ReloadAppInterface reloadApp;

    public PaymentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_payment, container, false);

        totalText = (TextView) view.findViewById(R.id.iDtotal);
        payCheck = (ImageView) view.findViewById(R.id.iDPayCheck);

        buttonCard = (Button) view.findViewById(R.id.pay_card);
        buttonCard.setOnClickListener(this);

        QRcodeView = (ImageView) view.findViewById(R.id.imageView_qrcode);
        scanText = (TextView) view.findViewById(R.id.textScann);

        numTotal = (TextView) view.findViewById(R.id.iDtotalPay);

        Preferences preferences = new Preferences(getActivity());
        String userIdent = preferences.getIdentifier();


        //Font
        Typeface typeface = ResourcesCompat.getFont(getActivity() , R.font.robotocondensed_light);
        scanText.setTypeface(typeface);
        totalText.setTypeface(typeface);

        loading = (ProgressBar) view.findViewById(R.id.progressBarPay);


        SetImage setImage = new SetImage();
        setImage.execute(userIdent);

        valueEventListenerProducts = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                /*
                   Wait until customer shows qrcode at the cashier
                   The information will be deleted at the database and datasnapshot will be null
                   That's the confirmation that products have been payed
                */
                if(dataSnapshot.getValue() == null && isAdded()){

                    reloadApp();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


        return  view;
    }

    @Override
    public void onClick(View v) {
        // default method for handling onClick Events..

        Preferences preferences = new Preferences(getActivity());
        String userIdent = preferences.getIdentifier();


        DatabaseReference firebase = FirebaseConfig.getFirebase();
        firebase.child("user")
                .child(userIdent)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        User user = dataSnapshot.getValue(User.class);

                        if(user.getCardNumber().isEmpty()){

                            Toast.makeText(getActivity(),
                                    "You must register your credit card number, go to settings and register it or pay using cash",
                                    Toast.LENGTH_LONG).show();

                        }else{
                                                      /*
                                                         Call credit card administrator's API here and wait for approval
                                                         Check if the approval time is too long to decide if implementation
                                                         of a progress bar is needed
                                                      */


                            //Code here


                                                      /*
                                                         If the transaction is ok, delete information from database
                                                         During the implementation it might be a good idea
                                                         to record the shopping historic to a database by calling an API
                                                         before deleting data from firebase
                                                      */

                            //If transaction is approved by credit card administrator
                            if(true) {

                                Preferences preferences = new Preferences(getActivity());
                                String userIdent = preferences.getIdentifier();


                                TotalProduct total = new TotalProduct();
                                total.setTotalProduct("0.0");
                                total.setIsPayed("S");

                                DatabaseReference firebase = FirebaseConfig.getFirebase();

                                //Change total payed
                                firebase.child("products_user")
                                        .child("total")
                                        .child(userIdent)
                                        .setValue(total);



                                //Remove products
                                firebase = FirebaseConfig.getFirebase();
                                firebase.child("products_user")
                                        .child(userIdent)
                                        .removeValue();


                                //If transaction is not approved by credit card administrator
                            }else{

                                Toast.makeText(getActivity(),
                                        "Transaction not approved by the credit/debit card administrator",
                                        Toast.LENGTH_LONG).show();
                            }

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {



                    }
                });


    }

    private void reloadApp(){

        /*
            Do not log out, go back to login activity
            The Login activity will check if the user is still logged and call main activity
            The data will be reloaded and customer can continue shopping or log out
        */

        //Remove listener and call login activity
        firebase.removeEventListener( valueEventListenerProducts );

        Glide.with(getContext()).load(R.drawable.ic_check_green)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .animate(R.anim.fadein)
                .into(payCheck);



        flagCallSnackbarActivity = true;
        Intent intent = new Intent(getActivity() , SnackBarActivity.class);
        startActivity(intent);

    }


    @Override
    public void onResume() {
        super.onResume();

        if(flagCallSnackbarActivity){

            reloadApp.reloadApp();
        }


        Preferences preferences = new Preferences(getActivity());
        String userIdent = preferences.getIdentifier();
        activity = getActivity();
        reloadApp = (ReloadAppInterface) getActivity();
        firebase = FirebaseConfig.getFirebase();
        firebase.child("products_user")
                .child(userIdent)
                .addValueEventListener( valueEventListenerProducts );


        DatabaseReference firebaseTotal = FirebaseConfig.getFirebase();
        firebaseTotal.child("products_user")
                .child("total")
                .child(userIdent)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        TotalProduct totalProduct = dataSnapshot.getValue(TotalProduct.class);
                        total = totalProduct.getTotalProduct().replace("," , ".");

                        final ValueAnimator animator = ValueAnimator.ofFloat(Float.parseFloat("0.00") , Float.parseFloat(total));
                        animator.setDuration(3000);
                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                                numTotal.setText(String.format( "%.2f" , animator.getAnimatedValue()));
                            }
                        });
                        animator.start();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    @Override
    public void onPause() {
        super.onPause();

        firebase.removeEventListener( valueEventListenerProducts );
    }


    class SetImage extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... params) {

            BitMatrix bitMatrix = null;
            try {
                try {
                    bitMatrix = new MultiFormatWriter().encode(
                            params[0],
                            BarcodeFormat.AZTEC.DATA_MATRIX.QR_CODE,
                            400, 400, null
                    );
                } catch (WriterException e) {
                    e.printStackTrace();
                }

            } catch (IllegalArgumentException Illegalargumentexception) {

                return null;
            }
            int bitMatrixWidth = bitMatrix.getWidth();

            int bitMatrixHeight = bitMatrix.getHeight();

            int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

            for (int y = 0; y < bitMatrixHeight; y++) {
                int offset = y * bitMatrixWidth;

                for (int x = 0; x < bitMatrixWidth; x++) {

                    pixels[offset + x] = bitMatrix.get(x, y) ?
                            getResources().getColor(R.color.black):getResources().getColor(R.color.white);
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);
            bitmap.setPixels(pixels, 0, 400, 0, 0, bitMatrixWidth, bitMatrixHeight);

            //Convert the bitmap image to String in order to return in the method
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] b = baos.toByteArray();

            //Return String
            return Base64.encodeToString(b, Base64.DEFAULT);


        }


        @Override
        protected void onPostExecute(String result) {

            //Convert String back to bitmap to show on the screen
            Bitmap bitmap = null;
            try {
                byte[] encodeByte = Base64.decode(result, Base64.DEFAULT);
                bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,
                        encodeByte.length);

            } catch (Exception e) {

                e.getMessage();
            }



            //Animation
            Animation fadeInAnimation = AnimationUtils.loadAnimation(getActivity() , R.anim.fadein);
            QRcodeView.setAnimation(fadeInAnimation);


            QRcodeView.setImageBitmap(bitmap);
            loading.setVisibility(View.GONE);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {

            loading.setVisibility(View.VISIBLE);
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            super.onPreExecute();
        }
    }

}
