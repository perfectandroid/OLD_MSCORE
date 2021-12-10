package com.creativethoughts.iscore;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

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
