package com.creativethoughts.iscore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.creativethoughts.iscore.Helper.Config;


public class SplashScreen extends AppCompatActivity {

    public static final String BASE_URL="https://202.164.150.65:14264/Mscore";
  //  private static final String API_NAME= "/api/MV3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        SharedPreferences baseurlSP = getApplicationContext().getSharedPreferences(Config.SHARED_PREF7, 0);
        SharedPreferences.Editor baseurlEditer = baseurlSP.edit();
        baseurlEditer.putString("baseurl", BASE_URL );
        baseurlEditer.commit();

        String splashScreen = getString( R.string.splash_screen );
        if ( splashScreen.equals("ON") ){
            new Handler().postDelayed( this::startUserregistrationActivity, 3000);
        }else {
            startUserregistrationActivity();
        }
    }
    private void startUserregistrationActivity(){
        Intent intent = new Intent( SplashScreen.this, UserRegistrationActivity.class);
        startActivity(  intent );
        finish();
    }
}
