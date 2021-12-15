package com.creativethoughts.iscore;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.creativethoughts.iscore.Helper.Config;
import com.creativethoughts.iscore.Retrofit.APIInterface;
import com.creativethoughts.iscore.utility.DialogUtil;
import com.creativethoughts.iscore.utility.NetworkUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


public class SplashScreen extends AppCompatActivity {

    public static final String BASE_URL="https://202.164.150.65:14264/Mscore";
    public static final String BANK_KEY="d.22333";
    public static final String BANK_HEADER="PERFECT SCORE BANK HEAD OFFICE";
    public static final String HOSTNAME_SUBJECT="STATIC-VM";
    public static final String CERTIFICATE_ASSET_NAME="mscoredemo.pem";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


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
        SharedPreferences hostnameSP = getApplicationContext().getSharedPreferences(Config.SHARED_PREF23, 0);
        SharedPreferences.Editor hostnameEditer = hostnameSP.edit();
        hostnameEditer.putString("hostname", HOSTNAME_SUBJECT);
        hostnameEditer.commit();
        SharedPreferences assetnameSP = getApplicationContext().getSharedPreferences(Config.SHARED_PREF24, 0);
        SharedPreferences.Editor assetnameEditer = assetnameSP.edit();
        assetnameEditer.putString("certificateassetname", CERTIFICATE_ASSET_NAME);
        assetnameEditer.commit();

        String splashScreen = getString( R.string.splash_screen );
        if ( splashScreen.equals("ON") ){
           // new Handler().postDelayed( this::startUserregistrationActivity, 3000);
            new Handler().postDelayed( this::getResellerData, 3000);
        }else {
            //startUserregistrationActivity();
            getResellerData();
        }

    }
    private void startUserregistrationActivity(){
        Intent intent = new Intent( SplashScreen.this, UserRegistrationActivity.class);
        startActivity(  intent );
        finish();
    }


    private SSLSocketFactory getSSLSocketFactory()
            throws CertificateException, KeyStoreException, IOException,
            NoSuchAlgorithmException,
            KeyManagementException {
        SharedPreferences sslnamepref =getApplicationContext().getSharedPreferences(Config.SHARED_PREF24, 0);
        String asset_Name=sslnamepref.getString("certificateassetname", null);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        //  InputStream caInput = getResources().openRawResource(Common.getCertificateAssetName());
        // File path: app\src\main\res\raw\your_cert.cer
        InputStream caInput =  IScoreApplication.getAppContext().
                getAssets().open(asset_Name);
        Certificate ca = cf.generateCertificate(caInput);
        caInput.close();
        KeyStore keyStore = KeyStore.getInstance("BKS");
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);
        TrustManager[] wrappedTrustManagers = getWrappedTrustManagers(tmf.getTrustManagers());
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, wrappedTrustManagers, null);
        return sslContext.getSocketFactory();
    }

    private TrustManager[] getWrappedTrustManagers(TrustManager[] trustManagers) {
        final X509TrustManager originalTrustManager = (X509TrustManager) trustManagers[0];
        return new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return originalTrustManager.getAcceptedIssuers();
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        try {
                            if (certs != null && certs.length > 0) {
                                certs[0].checkValidity();
                            } else {
                                originalTrustManager.checkClientTrusted(certs, authType);
                            }
                        } catch (CertificateException e) {
                            Log.w("checkClientTrusted", e.toString());
                        }
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        try {
                            if (certs != null && certs.length > 0) {
                                certs[0].checkValidity();
                            } else {
                                originalTrustManager.checkServerTrusted(certs, authType);
                            }
                        } catch (CertificateException e) {
                            Log.w("checkServerTrusted", e.toString());
                        }
                    }
                }
        };
    }

    private HostnameVerifier getHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
    }

    private void getResellerData() {
        if (NetworkUtil.isOnline()) {
            try {
                OkHttpClient client = new OkHttpClient.Builder()
                        .sslSocketFactory(getSSLSocketFactory())
                        .hostnameVerifier(getHostnameVerifier())
                        .build();
                Gson gson = new GsonBuilder()
                        .setLenient()
                        .create();
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL+"/")
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .client(client)
                        .build();
                APIInterface apiService = retrofit.create(APIInterface.class);
                final JSONObject requestObject1 = new JSONObject();
                try {
                    requestObject1.put("ReqMode",IScoreApplication.encryptStart("20"));
                    requestObject1.put("BankKey",IScoreApplication.encryptStart(BANK_KEY));
                    requestObject1.put("BankHeader",IScoreApplication.encryptStart(BANK_HEADER));

                    Log.e("requestObject1   344   ",""+requestObject1);


                } catch (Exception e) {
                    e.printStackTrace();

                }
                RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), requestObject1.toString());
                Call<String> call = apiService.getResellerDetails(body);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        try {
                            Log.e("Imagedetails   344   ",response.body());
                            // Toast.makeText(getActivity(),response.body(),Toast.LENGTH_LONG).show();
                            JSONObject jObject = new JSONObject(response.body());
                            String statuscode = jObject.getString("StatusCode");

                            if(statuscode.equals("0")){
                                JSONObject jobjt = jObject.getJSONObject("ResellerDetails");
                                startUserregistrationActivity();

                            }

                            else {
                                try{
                                    JSONObject jobjt = jObject.getJSONObject("ResellerDetails");
                                    String ResponseMessage = jobjt.getString("ResponseMessage");
                                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SplashScreen.this);
                                    builder.setMessage(ResponseMessage)
                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                    android.app.AlertDialog alert = builder.create();
                                    alert.show();

                                }
                                catch (JSONException e){
                                    String EXMessage = jObject.getString("EXMessage");
                                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SplashScreen.this);
                                    builder.setMessage(EXMessage)
//                                builder.setMessage("No data found.")
                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                    android.app.AlertDialog alert = builder.create();
                                    alert.show();

                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("TAG","Exception   651   "+e.toString());

                        }
                    }
                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.i("Imagedetails","Something went wrong");
                    }
                });
            }
            catch (Exception e) {
                Log.e("Imagedetails",e.toString());

                e.printStackTrace();
            }
        }
        else {
            DialogUtil.showAlert(SplashScreen.this,
                    "Network is currently unavailable. Please try again later.");
        }
    }
}
