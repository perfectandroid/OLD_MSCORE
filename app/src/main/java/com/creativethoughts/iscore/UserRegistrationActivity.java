package com.creativethoughts.iscore;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.creativethoughts.iscore.Helper.Config;
import com.creativethoughts.iscore.adapters.LoginBannerAdapter;
import com.creativethoughts.iscore.db.dao.UserCredentialDAO;
import com.creativethoughts.iscore.receiver.AppSignatureHelper;
import com.creativethoughts.iscore.utility.CommonUtilities;
import com.creativethoughts.iscore.utility.DialogUtil;
import com.creativethoughts.iscore.utility.NetworkUtil;
import com.creativethoughts.iscore.utility.network.NetworkManager;
import com.creativethoughts.iscore.utility.network.ResponseManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;


import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import me.relex.circleindicator.CircleIndicator;

import static com.creativethoughts.iscore.IScoreApplication.FLAG_NETWORK_EXCEPTION;

public class UserRegistrationActivity extends AppCompatActivity {
    public static final String BASE_URL="https://202.164.150.65:14264/Mscore";
    public static final String BANK_KEY="d.22333";
    public static final String BANK_HEADER="PERFECT SCORE BANK HEAD OFFICE";


    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 100;
    private static final int PHONE_FETCHING = 200;
    private EditText mMobileNumberET;

    private static ViewPager mPager;
    private static int currentPage = 0;
    private static final Integer[] XMEN= {R.drawable.banner1,R.drawable.banner2,R.drawable.banner3};
    private ArrayList<Integer> XMENArray = new ArrayList<Integer>();

    static String bank_Key, bank_Header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(UserRegistrationActivity.this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            setContentView(R.layout.phone_permission_layout);


            SharedPreferences baseurlSP = getApplicationContext().getSharedPreferences(Config.SHARED_PREF7, 0);
            SharedPreferences.Editor baseurlEditer = baseurlSP.edit();
            baseurlEditer.putString("baseurl", BASE_URL );
            baseurlEditer.commit();

            Button mGoToSettingsBtn = findViewById(R.id.buttonOkayPhonePermission);
            assert mGoToSettingsBtn != null;
            mGoToSettingsBtn.setOnClickListener( v -> {
                Log.e("clicked","yes");
                goToSettings();
                finish();
            });
            if (ActivityCompat.shouldShowRequestPermissionRationale(UserRegistrationActivity.this,
                    Manifest.permission.READ_PHONE_STATE)) {

                AlertDialog.Builder alertDialogBuilder  =   new AlertDialog.Builder(UserRegistrationActivity.this);
                alertDialogBuilder.setMessage("We are asking phone permission only for security purpose.Please allow this permission");
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("Ok", (dialog, which) -> {
                    ActivityCompat.requestPermissions(UserRegistrationActivity.this,
                            new String[]{Manifest.permission.READ_PHONE_STATE},
                            MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                    dialog.dismiss();
                });
                alertDialogBuilder.show();
            } else {
                ActivityCompat.requestPermissions(UserRegistrationActivity.this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            }
        }else {
            if (IScoreApplication.DEBUG)Log.e("Iemi", IScoreApplication.getIEMI()+"");
            if (UserCredentialDAO.getInstance().isUserAlreadyLogin()) {
                Intent pinLoginActivity =
                        new Intent(UserRegistrationActivity.this, PinLoginActivity.class);
                startActivity(pinLoginActivity);
                finish();
                return;
            }
            setContentView(R.layout.activity_register_user);

            SharedPreferences baseurlSP = getApplicationContext().getSharedPreferences(Config.SHARED_PREF7, 0);
            SharedPreferences.Editor baseurlEditer = baseurlSP.edit();
            baseurlEditer.putString("baseurl", BASE_URL );
            baseurlEditer.commit();

            SharedPreferences bankkeySP = getApplicationContext().getSharedPreferences(Config.SHARED_PREF9, 0);
            SharedPreferences.Editor bankkeyEditer = bankkeySP.edit();
            bankkeyEditer.putString("bankkey", BANK_KEY);
            bankkeyEditer.commit();
            SharedPreferences bankheaderSP = getApplicationContext().getSharedPreferences(Config.SHARED_PREF11, 0);
            SharedPreferences.Editor bankheaderEditer = bankheaderSP.edit();
            bankheaderEditer.putString("bankheader", BANK_HEADER);
            bankheaderEditer.commit();

            mMobileNumberET = findViewById(R.id.phoneno);
            init();
            queryPhoneNumber();
            addListenerOnButton();
        }
    }
    private void init() {
        for(int i=0;i<XMEN.length;i++)
            XMENArray.add(XMEN[i]);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new LoginBannerAdapter(UserRegistrationActivity.this,XMENArray));
        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(mPager);
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == XMEN.length) {
                    currentPage = 0;
                }
                mPager.setCurrentItem(currentPage++, true);
            }
        };
        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, 4000, 4000);
    }
    private void goToSettings(){

        Log.e("reached","goToSettings");
        try {
            Intent intent = new Intent();
            intent.setAction( Settings.ACTION_APPLICATION_DETAILS_SETTINGS );
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);

        }catch (Exception e){
            if (IScoreApplication.DEBUG)Log.e("exception",e.toString()+"");
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if ( requestCode == MY_PERMISSIONS_REQUEST_READ_PHONE_STATE ){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                finish();
                startActivity(getIntent());

            } else {

                if (ActivityCompat.shouldShowRequestPermissionRationale(UserRegistrationActivity.this,
                        Manifest.permission.READ_PHONE_STATE)) {

                    AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(UserRegistrationActivity.this);
                    alertDialogBuilder.setMessage("We are asking phone permission only for security purpose.Please allow this permission");
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton("Ok", (dialog, which) -> {
                        ActivityCompat.requestPermissions(UserRegistrationActivity.this,
                                new String[]{Manifest.permission.READ_PHONE_STATE},
                                MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                        dialog.dismiss();

                    });
                    alertDialogBuilder.show();

                }
            }
        }

    }
    private void addListenerOnButton() {

        Button mRegisterBtn = findViewById(R.id.btnRegister);
        assert mRegisterBtn != null;
        mRegisterBtn.setOnClickListener(arg0 -> {
            String mobileNumber = mMobileNumberET.getText().toString();

            String countryCode = "91"; // Need to change this hard coded value.

            if(mobileNumber.equalsIgnoreCase("124567")) {

                Toast.makeText(UserRegistrationActivity.this, "This functionality is not availbale",
                        Toast.LENGTH_SHORT).show();
            }

            if (mobileNumber.length() > 10 || mobileNumber.length() < 10) {
                mMobileNumberET.setError("Please enter valid 10 digit mobile number");

            }

            else {
                if (NetworkUtil.isOnline()) {

                    bank_Key = getApplicationContext().getSharedPreferences(Config.SHARED_PREF9, 0).getString("bankkey", null);
                    bank_Header =getApplicationContext().getSharedPreferences(Config.SHARED_PREF11, 0).getString("bankheader", null);

                    SharedPreferences pref =getApplicationContext().getSharedPreferences(Config.SHARED_PREF7, 0);
                    String BASE_URL=pref.getString("baseurl", null);
                    try{
                        String url =
                                BASE_URL+ "/api/MV3"+"/PassBookAuthenticate?Mobno="+
                                        IScoreApplication.encodedUrl(IScoreApplication.encryptStart(countryCode+mobileNumber)) + "&Pin=" +
                                        IScoreApplication.encodedUrl(IScoreApplication.encryptStart("0000"))+"&IMEI=";
//                                        +"&IMEI="+IScoreApplication.encodedUrl(IScoreApplication.encryptStart("123456789"));
    Log.e("urll",""+url);
                        NetworkManager.getInstance().connector(url, new ResponseManager() {
                            @Override
                            public void onSuccess(String result) {
                                Log.e("result_true","1"+result);
                                try{
                                    analyzeResult( result, mobileNumber, countryCode );
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }

                            @Override
                                public void onError(String error) {
                                Log.e("result_true","2  "+error);
                                IScoreApplication.simpleAlertDialog( UserRegistrationActivity.this, new IScoreApplication.AlertProcess() {
                                    @Override
                                    public void ok() {
                                        //Do nothing
                                    }
                                    @Override
                                    public void cancel() {
                                        //Do nothing
                                    }
                                }, error);
                            }
                        }, this, "Loading");
                    }catch ( Exception e ){
                        //Do nothing
                    }

                } else {
                    DialogUtil.showAlert(UserRegistrationActivity.this,
                            "Network is currently unavailable. Please try again later.");
                }
            }

        });
    }
    private void analyzeResult( String result,String mobileNumber, String countryCode ){
        try {
            int response;
            result = result.trim();
            if ( result.equals("true")){
                response = 1;
            }else if ( result.equals("false" ) ){
                response = 6;
            }else if ( result.equalsIgnoreCase(IScoreApplication.SERVICE_NOT_AVAILABLE ) ){
                response = 7;
            }else if(IScoreApplication.containAnyKnownException(result)){
                response = IScoreApplication.getFlagException(result);
            }else {
                response = 0;
            }
            processNetworkResponse( response, mobileNumber, countryCode );
        }catch ( Exception e ){
           Toast.makeText(getApplicationContext(),"Something went wrong 1",Toast.LENGTH_LONG).show();
        }
    }
    private void processNetworkResponse( int result, String mobileNumber, String countryCode ){
        try {
            UserCredentialDAO.getInstance().deleteAllUserData();
            if (IScoreApplication.checkPermissionIemi(result,UserRegistrationActivity.this)) {

                if (result == 1) {
                    UserCredentialDAO.getInstance()
                            .insertUserDetails(mobileNumber, countryCode);

                    Intent passBookAccount =
                            new Intent(UserRegistrationActivity.this, RecieveAndValidateOTP.class);
                    passBookAccount.putExtra("changePin", "false");
                    startActivity(passBookAccount);

                    finish();

                } else if (result == 0) {
                    UserCredentialDAO.getInstance().deleteAllUserData();
                    DialogUtil.showAlert(UserRegistrationActivity.this,
                            "We are experiencing some technical issues, Please try again after some time.");

                } else if (result == 6) {

                    IScoreApplication.simpleAlertDialog(this, new IScoreApplication.AlertProcess() {
                        @Override
                        public void ok() {
                            //Do nothing
                        }
                        @Override
                        public void cancel() {
                            //Do nothing
                        }
                    }, "Please contact bank to activate mobile banking service");
                }else if ( result == 7 ){
                    showAlert( IScoreApplication.SERVICE_NOT_AVAILABLE )   ;
                }else if (result == FLAG_NETWORK_EXCEPTION) {
                    try {
                        UserCredentialDAO.getInstance().deleteAllUserData();
                        showToastMessage();
                    }catch ( IllegalStateException e ){
                        Toast.makeText(getApplicationContext(),"Something went wrong 2",Toast.LENGTH_LONG).show();
                    }
                }else {
                    IScoreApplication.simpleAlertDialog(this, new IScoreApplication.AlertProcess() {
                        @Override
                        public void ok() {
                            //Do nothing
                        }
                        @Override
                        public void cancel() {
                            //Do nothing
                        }
                    }, "Un expected error");
                }
            }
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),"Something went wrong 1",Toast.LENGTH_LONG).show();
        }

    }
    private void showAlert( String message ){
        UserCredentialDAO.getInstance().deleteAllUserData();

        AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(UserRegistrationActivity.this);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("Ok", (dialog, which) ->  dialog.dismiss() );
        alertDialogBuilder.show();
    }
    private void showToastMessage() {
        Toast toast = Toast.makeText(UserRegistrationActivity.this, IScoreApplication.MSG_EXCEPTION_NETWORK, Toast.LENGTH_SHORT);

        toast.setGravity(Gravity.CENTER, 0, 0);

        toast.show();
    }
    private boolean ifGooglePlayAvailable(){
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable( this );
        return status == ConnectionResult.SUCCESS;
    }
    private void queryPhoneNumber(){
        if ( ifGooglePlayAvailable() ){
            try{
                double v = getPackageManager().getPackageInfo(GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE, 0 ).versionCode;
                if ( v >= 10.2 ){
                    requestPhoneNumber();
                }
            }catch ( PackageManager.NameNotFoundException e ){
                if ( IScoreApplication.DEBUG )
                    Log.e("version_error", e.toString() );
            }

        }
    }
    private void requestPhoneNumber() {
        if ( !ifGooglePlayAvailable() )
            return;
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported( true )
                .build();

        GoogleApiClient apiClient = new GoogleApiClient.Builder( this )
                .addApi(Auth.CREDENTIALS_API).enableAutoManage( this , new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                }).build();
        PendingIntent pendingIntent = Auth.CredentialsApi.getHintPickerIntent( apiClient, hintRequest );
        try{
            startIntentSenderForResult( pendingIntent.getIntentSender(), PHONE_FETCHING, null,0,0,0);
        }catch ( IntentSender.SendIntentException e ){

        }
    }
    @Override
    public void onActivityResult( int requestCode, int responseCode, Intent data ){
        super.onActivityResult( requestCode, responseCode, data );
        if ( requestCode == PHONE_FETCHING ){
            if ( responseCode == RESULT_OK ){
                Credential credential = data.getParcelableExtra( Credential.EXTRA_KEY );
                String phoneNum = credential.getId();
                if ( phoneNum!= null && phoneNum.length() >= 10 ){
                    phoneNum = phoneNum.substring( phoneNum.length() - 10 );
                    mMobileNumberET.setText(  phoneNum );
                }
            }
        }
    }

    public static String getBankkey() {
        try {
            return bank_Key;
        }catch (Exception e){
            return IScoreApplication.EXCEPTION_NOIEMI;
        }
    }

    public static String getBankheader() {
        try {
            return bank_Header;
        }catch (Exception e){
            return IScoreApplication.EXCEPTION_NOIEMI;
        }
    }

}
