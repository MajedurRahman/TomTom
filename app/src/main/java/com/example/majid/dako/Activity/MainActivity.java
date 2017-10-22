package com.example.majid.dako.Activity;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.majid.dako.ConnectivityReceiver;
import com.example.majid.dako.MyApplication;
import com.example.majid.dako.R;


/**
 * Created by Majid on 5/20/2017.
 */
public class MainActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    boolean isInternetAvailabe=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        CardView ridebutton = (CardView) findViewById(R.id.startRideBtn);
        ridebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkConnection()){

                    if (isGpsEnable()){

                        startActivity(new Intent(MainActivity.this, MapActivity.class));
                    }
                    else {

                        Toast.makeText(MainActivity.this, " Please Turn On your GPS Or Location ", Toast.LENGTH_SHORT).show();
                    }


                }
                else {
                    Toast.makeText(MainActivity.this, " Please Turn On Your Internet Connection .", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
    private boolean checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
       return isConnected;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register connection status listener
        MyApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

        isInternetAvailabe = isConnected;
    }

    public boolean isGpsEnable(){

        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsProviderEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        return gpsProviderEnabled;
    }
}



