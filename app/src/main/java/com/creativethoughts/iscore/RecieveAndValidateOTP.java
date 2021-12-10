package com.creativethoughts.iscore;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.creativethoughts.iscore.Helper.Config;
import com.creativethoughts.iscore.db.dao.BankVerifier;
import com.creativethoughts.iscore.db.dao.DbSync;
import com.creativethoughts.iscore.db.dao.NewTransactionDAO;
import com.creativethoughts.iscore.db.dao.PBAccountInfoDAO;
import com.creativethoughts.iscore.db.dao.PBMessagesDAO;
import com.creativethoughts.iscore.db.dao.SettingsDAO;
import com.creativethoughts.iscore.db.dao.UserCredentialDAO;
import com.creativethoughts.iscore.db.dao.UserDetailsDAO;
import com.creativethoughts.iscore.db.dao.model.UserCredential;
import com.creativethoughts.iscore.gsonmodel.SyncParent;
import com.creativethoughts.iscore.receiver.MySMSBroadcastReceiver;
import com.creativethoughts.iscore.receiver.SMSReceiver;
import com.creativethoughts.iscore.utility.CommonUtilities;
import com.creativethoughts.iscore.utility.DialogUtil;
import com.creativethoughts.iscore.utility.NetworkUtil;
import com.creativethoughts.iscore.utility.SyncUtils;
import com.creativethoughts.iscore.utility.network.NetworkManager;
import com.creativethoughts.iscore.utility.network.ResponseManager;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class RecieveAndValidateOTP extends Activity implements MySMSBroadcastReceiver.SmsReceiveHandler {
    private EditText mEtVerificationCode;
    SweetAlertDialog mSweetAlertDialog;

//    private SMSVerificationCodeReceiver mSmsVerificationCodeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_verification);

     /*   if (mSmsVerificationCodeReceiver == null) {
            mSmsVerificationCodeReceiver = new SMSVerificationCodeReceiver();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mSmsVerificationCodeReceiver,
                new IntentFilter(SMSReceiver.SMS_VERIFICATION));*/

        mEtVerificationCode =   findViewById(R.id.verificationCode);

        Button button =   findViewById(R.id.btnVerify);

        SmsRetrieverClient smsRetrieverClient = SmsRetriever.getClient( this );
        Task<Void> task = smsRetrieverClient.startSmsRetriever();
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
//                Toast.makeText( getApplicationContext(), "started", Toast.LENGTH_LONG ).show();
            }
        });
        task.addOnFailureListener(e -> {
//                Toast.makeText( getApplicationContext(), "Failed", Toast.LENGTH_LONG ).show();
        });

        button.setOnClickListener(v -> {

            if (NetworkUtil.isOnline()) {

                UserCredential userCredential =
                        UserCredentialDAO.getInstance().getLoginCredential();
                try{
                    mSweetAlertDialog = new SweetAlertDialog( RecieveAndValidateOTP.this, SweetAlertDialog.PROGRESS_TYPE );
                    mSweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                    mSweetAlertDialog.setTitleText( "Verify PIN" );
                    mSweetAlertDialog.setCancelable( false );
                    mSweetAlertDialog.show();
                    String otp = mEtVerificationCode.getText().toString();
                    final String url =
                            CommonUtilities.getUrl() + "/VerifyOTP?" +
                                    "Mobno=" + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(userCredential.countryCode +
                                    userCredential.mobileNumber ))+
                                    "&OTP=" +  IScoreApplication.encodedUrl(IScoreApplication.encryptStart( otp )) +
                                    "&NoOfDays=" +IScoreApplication.encodedUrl(IScoreApplication.encryptStart("30"));
                    NetworkManager.getInstance().connector(url, new ResponseManager() {
                        @Override
                        public void onSuccess(String result) {
                            processResponse( analyzeResponse( result, otp ) );
                            if ( mSweetAlertDialog != null )
                                mSweetAlertDialog.dismiss();
                        }

                        @Override
                        public void onError(String error) {
                            if ( mSweetAlertDialog != null )
                                mSweetAlertDialog.dismiss();
                            DialogUtil.showAlert(RecieveAndValidateOTP.this,
                                    error);
                        }
                    }, null , "Verifying otp");
                }catch ( Exception e ){
                    DialogUtil.showAlert(RecieveAndValidateOTP.this,
                            "App blocked");
                }

            } else {
                DialogUtil.showAlert(RecieveAndValidateOTP.this,
                        "Network is currently unavailable. Please try again later.");
            }
        });

    }
    @Override
    public void onResume(){
        super.onResume();
        MySMSBroadcastReceiver.smsReceiveHandler = this;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

       /* if  (mSmsVerificationCodeReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mSmsVerificationCodeReceiver);
        }*/
    }

    @Override
    public void received(String message) {
        if ( mEtVerificationCode != null ){

            String[] splited = message.split("\\s+");
            if ( splited.length > 6 ){
                String otpString = splited[5];
                int otpInt;
                try{
                    otpInt = Integer.parseInt( otpString );
                }catch ( NumberFormatException e ){
                    otpInt = 0;
                }
                if ( otpInt > 0 ){
                    mEtVerificationCode.setText( otpString );
                }
            }
        }
    }

    @Override
    public void timout(String data) {

    }

    class SMSVerificationCodeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if ( intent.getAction() != null && intent.getAction().equalsIgnoreCase(SMSReceiver.SMS_VERIFICATION))
            {
                String verificationCode = intent.getStringExtra(SMSReceiver.SMS_VERIFICATION_CODE);

                if (mEtVerificationCode != null) {
                    mEtVerificationCode.setText(verificationCode);
                }
            }
        }
    }
    private int analyzeResponse( String response, String otp){

        if ( !TextUtils.isEmpty( response ) ){
            String nullInfo = "{\"acInfo\":null}";
            response = response.trim();
            if ( response.equals( nullInfo ) ){
                return -1;
            }else if ( response.equals( IScoreApplication.SERVICE_NOT_AVAILABLE ) ){
                return 0;
            }else {

                UserCredentialDAO.getInstance().
                        updateNewPin(otp);

                UserCredentialDAO.getInstance().updateUserLogin();
                BankVerifier.getInstance().insertValue("1");
                //Remove all the cached data in DB
                UserDetailsDAO.getInstance().deleteAllRows();
                PBAccountInfoDAO.getInstance().deleteAllRows();
                NewTransactionDAO.getInstance().deleteAllRow();
                PBMessagesDAO.getInstance().deleteAllRows();
                SettingsDAO.getInstance().deleteAllRows();

                SyncParent syncParent = new Gson().fromJson( response, SyncParent.class );

                return DbSync.getInstance().sync( syncParent,true );
            }

        }else {
            return IScoreApplication.FLAG_NETWORK_EXCEPTION;
        }
    }
    private void processResponse( int value ){
        if (IScoreApplication.checkPermissionIemi(value,RecieveAndValidateOTP.this)) {
            switch (value) {
                case -1:

                    DialogUtil.showAlert(RecieveAndValidateOTP.this,
                            "OTP Mismatch.");

                    break;

                case 0:
                    IScoreApplication.simpleAlertDialog(RecieveAndValidateOTP.this, new IScoreApplication.AlertProcess() {
                        @Override
                        public void ok() {
                            //Do nothing
                        }

                        @Override
                        public void cancel() {
                            //Do nothing
                        }
                    }, IScoreApplication.SERVICE_NOT_AVAILABLE );
                    break;

                case 1:


                    SyncUtils.startAlarmManage(RecieveAndValidateOTP.this);
                    try{
                        mSweetAlertDialog.dismissWithAnimation();
                    }catch (Exception e ){
                        //Do nothing
                    }
                    moveToHomeScreen();

                    break;

                case IScoreApplication.FLAG_NETWORK_EXCEPTION:
                    DialogUtil.showAlert(RecieveAndValidateOTP.this,
                            IScoreApplication.MSG_EXCEPTION_NETWORK);
                    break;

                default:
                    break;
            }

        }
    }
    private void moveToHomeScreen() {
         Date date = Calendar.getInstance().getTime();
         SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm a");
         String formattedDate = df.format(date);

         SharedPreferences loginSP = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
                                      SharedPreferences.Editor loginEditer = loginSP.edit();
                                      loginEditer.putString("logintime", formattedDate);
                                      loginEditer.commit();

         SharedPreferences ImageSP = getApplicationContext().getSharedPreferences(Config.SHARED_PREF1, 0);
                                      SharedPreferences.Editor imageEditer = ImageSP.edit();
                                      imageEditer.putString("custimage", "");
                                      imageEditer.commit();
                                                                                                                                 
        UserCredentialDAO.getInstance().updateUserLogin();
        Intent passBookAccount = new Intent(RecieveAndValidateOTP.this, HomeActivity.class);
        startActivity(passBookAccount);
        finish();










    }
}
