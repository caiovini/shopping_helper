package com.example.caio.shoppinghelper.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.example.caio.shoppinghelper.R;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;


public class SnackBarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snack_bar);


        Snackbar snackbar =
                Snackbar.with(this)
                        .text("                       Payment confirmed !!!")
                        .actionLabel("OK")
                        .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                        .actionListener(new ActionClickListener() {
                            @Override
                            public void onActionClicked(Snackbar snackbar) {

                                onBackPressed();
                                finish();
                            }
                        });

        snackbar.dismissOnActionClicked(true);
        SnackbarManager.show(snackbar);
    }

}
