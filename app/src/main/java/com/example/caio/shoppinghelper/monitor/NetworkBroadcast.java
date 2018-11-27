package com.example.caio.shoppinghelper.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NetworkBroadcast extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {

        int status = NetworkUtil.getConnectivityStatusString(context);

        if(status==NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){

            // new ForceExitPause(context).execute();
            Toast.makeText(context , "You are offline" , Toast.LENGTH_LONG).show();

        }else{

            // new ResumeForceExitPause(context).execute();
            // Toast.makeText(MainActivity.this , "You are online again" , Toast.LENGTH_LONG).show();
        }
    }

}
