package com.creativethoughts.iscore;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.creativethoughts.iscore.Retrofit.APIInterface;
import com.creativethoughts.iscore.adapters.DuedateAdapter;
import com.creativethoughts.iscore.db.dao.UserCredentialDAO;
import com.creativethoughts.iscore.db.dao.UserDetailsDAO;
import com.creativethoughts.iscore.db.dao.model.UserCredential;
import com.creativethoughts.iscore.db.dao.model.UserDetails;
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

import com.creativethoughts.iscore.Helper.Common;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class DuedateActivity extends AppCompatActivity implements View.OnClickListener{
    RecyclerView rv_standinginst;
    String cusid, token,acctype;
    ProgressDialog progressDialog;
    LinearLayout ll_standnginstr;
    TextView tvDeposit,tvLoan;
    String strHeader="Deposit";
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_duedate);

        ll_standnginstr = findViewById(R.id.ll_standnginstr);
        rv_standinginst = findViewById(R.id.rv_standinginst);
        UserDetails userDetails = UserDetailsDAO.getInstance().getUserDetail();
        cusid = userDetails.customerId;
        UserCredential loginCredential = UserCredentialDAO.getInstance( ).getLoginCredential( );
        token = loginCredential.token;

        tvDeposit = findViewById(R.id.tvDeposit);
        tvDeposit.setOnClickListener(this);
        tvLoan = findViewById(R.id.tvLoan);
        tvLoan.setOnClickListener(this);

        acctype ="1";
        getDuedate();
    }

    public void getDuedate(){

        if (NetworkUtil.isOnline()) {
            try{
                progressDialog = new ProgressDialog(DuedateActivity.this, R.style.Progress);
                progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar);
                progressDialog.setCancelable(false);
                progressDialog.setIndeterminate(true);
                progressDialog.setIndeterminateDrawable(this.getResources()
                        .getDrawable(R.drawable.progress));
                progressDialog.show();
                OkHttpClient client = new OkHttpClient.Builder()
                        .sslSocketFactory(getSSLSocketFactory())
                        .hostnameVerifier(getHostnameVerifier())
                        .build();
                Gson gson = new GsonBuilder()
                        .setLenient()
                        .create();
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(Common.getBaseUrl())
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .client(client)
                        .build();
                APIInterface apiService = retrofit.create(APIInterface.class);
                final JSONObject requestObject1 = new JSONObject();
                try {
                    requestObject1.put("ReqMode",IScoreApplication.encryptStart("8") );
                    requestObject1.put("FK_Customer",IScoreApplication.encryptStart(cusid) );
                    requestObject1.put("Token",IScoreApplication.encryptStart(token) );
                    requestObject1.put("AccountType",IScoreApplication.encryptStart(acctype));
                    requestObject1.put("BankKey",IScoreApplication.encryptStart(getResources().getString(R.string.BankKey)));
                    requestObject1.put("BankHeader",IScoreApplication.encryptStart(getResources().getString(R.string.BankHeader)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), requestObject1.toString());
                Call<String> call = apiService.getDuedate(body);
                call.enqueue(new Callback<String>() {
                    @Override public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                        try {
                            progressDialog.dismiss();
                            JSONObject jObject = new JSONObject(response.body());
                            Log.i("Duedatelist",response.body());
                            if(jObject.getString("StatusCode").equals("0")) {
                                ll_standnginstr.setVisibility(View.VISIBLE);
                                JSONObject jobj = jObject.getJSONObject("AccountDueDateDetailsIfo");
                                JSONArray jarray = jobj.getJSONArray("AccountDueDateDetails");
                                if(jarray.length()!=0){
                                    GridLayoutManager lLayout = new GridLayoutManager(DuedateActivity.this, 1);
                                    rv_standinginst.setLayoutManager(lLayout);
                                    rv_standinginst.setHasFixedSize(true);
                                    DuedateAdapter adapter = new DuedateAdapter(DuedateActivity.this, jarray,strHeader);
                                    rv_standinginst.setAdapter(adapter);
                                }else {
                                    ll_standnginstr.setVisibility(View.GONE);
                                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(DuedateActivity.this);
                                    builder.setMessage("No data found.")
                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                    android.app.AlertDialog alert = builder.create();
                                    alert.show();
                                }
                            }else {
                                ll_standnginstr.setVisibility(View.GONE);
                                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(DuedateActivity.this);
                                builder.setMessage("No data found.")
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                android.app.AlertDialog alert = builder.create();
                                alert.show();
                            }

                        } catch (JSONException e) {
                            ll_standnginstr.setVisibility(View.GONE);
                            progressDialog.dismiss();
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(Call<String> call, Throwable t) { progressDialog.dismiss();}
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            DialogUtil.showAlert(DuedateActivity.this,
                    "Network is currently unavailable. Please try again later.");
        }
    }

    private HostnameVerifier getHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
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

    private SSLSocketFactory getSSLSocketFactory()
            throws CertificateException, KeyStoreException, IOException,
            NoSuchAlgorithmException,
            KeyManagementException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        //  InputStream caInput = getResources().openRawResource(Common.getCertificateAssetName());
        // File path: app\src\main\res\raw\your_cert.cer
        InputStream caInput =  IScoreApplication.getAppContext().
                getAssets().open(Common.getCertificateAssetName());
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvDeposit:
                tvLoan.setBackground(ContextCompat.getDrawable(DuedateActivity.this, R.drawable.toggle3));
                tvDeposit.setBackground(ContextCompat.getDrawable(DuedateActivity.this, R.drawable.toggle1));
                tvLoan.setTextColor(Color.parseColor("#000000"));
                tvDeposit.setTextColor(Color.parseColor("#ffffff"));
                acctype ="1";
                getDuedate();
                strHeader="Deposit";
                break;
            case R.id.tvLoan:
                tvLoan.setBackground(ContextCompat.getDrawable(DuedateActivity.this, R.drawable.toggle));
                tvDeposit.setBackground(ContextCompat.getDrawable(DuedateActivity.this, R.drawable.toggle4));
                tvLoan.setTextColor(Color.parseColor("#ffffff"));
                tvDeposit.setTextColor(Color.parseColor("#000000"));
                acctype ="2";
                getDuedate();
                strHeader="Loan";
                break;
        }
    }
}
