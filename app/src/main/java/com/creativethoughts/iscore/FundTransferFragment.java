package com.creativethoughts.iscore;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.creativethoughts.iscore.Helper.Common;
import com.creativethoughts.iscore.Helper.Config;
import com.creativethoughts.iscore.Retrofit.APIInterface;
import com.creativethoughts.iscore.adapters.AccountSummaryAdapter;
import com.creativethoughts.iscore.adapters.CustomListAdapter;
import com.creativethoughts.iscore.custom_alert_dialogs.AlertMessageFragment;
import com.creativethoughts.iscore.custom_alert_dialogs.KeyValuePair;
import com.creativethoughts.iscore.db.dao.PBAccountInfoDAO;
import com.creativethoughts.iscore.db.dao.SettingsDAO;
import com.creativethoughts.iscore.db.dao.UserCredentialDAO;
import com.creativethoughts.iscore.db.dao.UserDetailsDAO;
import com.creativethoughts.iscore.db.dao.model.AccountInfo;
import com.creativethoughts.iscore.db.dao.model.SettingsModel;
import com.creativethoughts.iscore.db.dao.model.UserCredential;
import com.creativethoughts.iscore.db.dao.model.UserDetails;
import com.creativethoughts.iscore.model.BarcodeAgainstCustomerAccountList;
import com.creativethoughts.iscore.model.CustomerModulesModel;
import com.creativethoughts.iscore.utility.CommonUtilities;
import com.creativethoughts.iscore.utility.ConnectionUtil;
import com.creativethoughts.iscore.utility.DialogUtil;
import com.creativethoughts.iscore.utility.NetworkUtil;
import com.creativethoughts.iscore.utility.network.NetworkManager;
import com.creativethoughts.iscore.utility.network.ResponseManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static com.creativethoughts.iscore.IScoreApplication.FLAG_NETWORK_EXCEPTION;

/**
 * A simple {@link Fragment} subclass.
 */
public class FundTransferFragment extends Fragment implements View.OnClickListener, EditText.OnEditorActionListener,
        View.OnFocusChangeListener, TextWatcher{

    private static final int ZXING_CAMERA_PERMISSION = 1;
    private Button button;
    private Spinner mAccountSpinner;
    private Spinner mAccountTypeSpinner;
    private AppCompatEditText mAccountNumberEt;
    private AppCompatEditText mConfirmAccountNumberEt;
    private AppCompatEditText mAmountEt;
    private AppCompatEditText mMessageEt;
    private ProgressDialog mProgressDialog;
    private String mScannedValue;
    ProgressDialog progressDialog;

    private EditText edtTxtAccountNoFirstBlock;
    private EditText edtTxtAccountNoSecondBlock;
    private EditText edtTxtAccountNoThirdBlock;

    private EditText edtTxtConfirmAccountNoFirstBlock;
    private EditText edtTxtConfirmAccountNoSecondBlock;
    private EditText edtTxtConfirmAccountNoThirdBlock;

    private EditText edtTxtAmount;
    private String token,cusid,dataItem;
    ListView list_view;
    TextView tv_popuptitle;
    private ArrayList<BarcodeAgainstCustomerAccountList>CustomerList = new ArrayList<>();

    public FundTransferFragment() {
        // Required empty public constructor
    }

    public static FundTransferFragment newInstance() {

        return new FundTransferFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fund_transferfragment, container, false);

        mAccountSpinner = view.findViewById(R.id.spn_account_num);
        mAccountTypeSpinner = view.findViewById(R.id.spn_account_type);

        mAccountNumberEt = view.findViewById(R.id.account_number);
        mConfirmAccountNumberEt = view.findViewById(R.id.confirm_account_number);

        edtTxtAccountNoFirstBlock = view.findViewById(R.id.acc_no_block_one);
        edtTxtAccountNoSecondBlock = view.findViewById(R.id.acc_no_block_two);
        edtTxtAccountNoThirdBlock = view.findViewById(R.id.acc_no_block_three);

        edtTxtAccountNoFirstBlock.setOnEditorActionListener(this);
        edtTxtAccountNoSecondBlock.setOnEditorActionListener(this);
        edtTxtAccountNoThirdBlock.setOnEditorActionListener(this);

        edtTxtAccountNoFirstBlock.setOnFocusChangeListener(this);
        edtTxtAccountNoSecondBlock.setOnFocusChangeListener(this);
        edtTxtAccountNoThirdBlock.setOnFocusChangeListener(this);

        edtTxtAccountNoFirstBlock.addTextChangedListener(this);
        edtTxtAccountNoSecondBlock.addTextChangedListener(this);
        edtTxtAccountNoThirdBlock.addTextChangedListener(this);

        edtTxtConfirmAccountNoFirstBlock = view.findViewById(R.id.confirm_acc_no_block_one);
        edtTxtConfirmAccountNoSecondBlock = view.findViewById(R.id.confirm_acc_no_block_two);
        edtTxtConfirmAccountNoThirdBlock = view.findViewById(R.id.confirm_acc_no_block_three);

        edtTxtConfirmAccountNoFirstBlock.setOnEditorActionListener(this);
        edtTxtConfirmAccountNoSecondBlock.setOnEditorActionListener(this);
        edtTxtConfirmAccountNoThirdBlock.setOnEditorActionListener(this);

        edtTxtConfirmAccountNoFirstBlock.setOnFocusChangeListener(this);
        edtTxtConfirmAccountNoSecondBlock.setOnFocusChangeListener(this);
        edtTxtConfirmAccountNoThirdBlock.setOnFocusChangeListener(this);

        edtTxtConfirmAccountNoFirstBlock.addTextChangedListener(this);
        edtTxtConfirmAccountNoSecondBlock.addTextChangedListener(this);
        edtTxtConfirmAccountNoThirdBlock.addTextChangedListener(this);

        edtTxtAmount = view.findViewById(R.id.edt_txt_amount);

        mAccountNumberEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s != null && s.length() < 14 ) {

                        mScannedValue = null;

                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Do nothing
            }
        });

        mAmountEt = view.findViewById(R.id.amount);
        mMessageEt = view.findViewById(R.id.message);

        button = view.findViewById(R.id.btn_submit);
        button.setOnClickListener(this);

        Button btnScanAccounttNo = view.findViewById(R.id.btn_scan_acnt_no);
        btnScanAccounttNo.setOnClickListener(this);

        Button scan = view.findViewById(R.id.scan);
        scan.setOnClickListener(this);


        UserCredential loginCredential = UserCredentialDAO.getInstance( ).getLoginCredential( );
        token =  loginCredential.token;
        UserDetails userDetails = UserDetailsDAO.getInstance().getUserDetail();
        cusid = userDetails.customerId;

        setAccountNumber();
        setAccountType();
        return view;
    }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        try {
            int id;
            //noinspection ConstantConditions
            id = getActivity().getCurrentFocus().getId();
            changeFocusOnTextFill(id);
        }catch (NullPointerException e){
            if (IScoreApplication.DEBUG)
                Log.d("Error", e.toString());
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

       //Do nothing
    }

    @Override
    public void afterTextChanged(Editable s) {
        //Do nothing

    }

    private void changeFocusOnTextFill(int id){
        switch (id){
            case R.id.acc_no_block_one:
                if (edtTxtAccountNoFirstBlock.getText().toString().length() == 3)
                    edtTxtAccountNoSecondBlock.requestFocus();
                break;
            case R.id.acc_no_block_two:
                if (edtTxtAccountNoSecondBlock.getText().toString().length() == 3)
                    edtTxtAccountNoThirdBlock.requestFocus();
                break;
            case R.id.acc_no_block_three:
                if (edtTxtAccountNoThirdBlock.getText().toString().length() == 6)
                    edtTxtConfirmAccountNoFirstBlock.requestFocus();
                    break;
            case R.id.confirm_acc_no_block_one:
                if (edtTxtConfirmAccountNoFirstBlock.getText().toString().length() == 3)
                    edtTxtConfirmAccountNoSecondBlock.requestFocus();
                    break;
            case R.id.confirm_acc_no_block_two:
                if (edtTxtConfirmAccountNoSecondBlock.getText().toString().length() == 3)
                    edtTxtConfirmAccountNoThirdBlock.requestFocus();
                break;

            default:break;
        }

    }
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent i){
        String editorAction = "editor_action";
        assert v != null;
        int id = v.getId();
        if (actionId == EditorInfo.IME_ACTION_DONE  || actionId == EditorInfo.IME_ACTION_NEXT  || actionId == EditorInfo.IME_ACTION_NONE) {
            switch (id){
                case R.id.acc_no_block_one:
                    findValueForEditText(edtTxtAccountNoFirstBlock.getText().toString(), id, editorAction );
                    break;
                case R.id.acc_no_block_two:
                    findValueForEditText(edtTxtAccountNoSecondBlock.getText().toString(), id, editorAction);
                    break;
                case R.id.acc_no_block_three:
                    findValueForEditText(edtTxtAccountNoThirdBlock.getText().toString(), id, editorAction);
                    break;
                case R.id.confirm_acc_no_block_one:
                    findValueForEditText(edtTxtConfirmAccountNoFirstBlock.getText().toString(), id, editorAction);
                    break;
                case R.id.confirm_acc_no_block_two:
                    findValueForEditText(edtTxtConfirmAccountNoSecondBlock.getText().toString(), id, editorAction);
                    break;
                case R.id.confirm_acc_no_block_three:
                    findValueForEditText(edtTxtConfirmAccountNoThirdBlock.getText().toString(), id, editorAction);
                    break;

                default:break;
            }
        }
        return false;
    }
    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        String focusChange = "focus_change";
        if (!hasFocus) {
            int id = view.getId();
            switch (id){
                case R.id.acc_no_block_one:
                    findValueForEditText(edtTxtAccountNoFirstBlock.getText().toString(), R.id.acc_no_block_one, focusChange);
                    break;
                case R.id.acc_no_block_two:
                    findValueForEditText(edtTxtAccountNoFirstBlock.getText().toString(), R.id.acc_no_block_one, focusChange);
                    findValueForEditText(edtTxtAccountNoSecondBlock.getText().toString(), R.id.acc_no_block_two, focusChange);
                    break;
                case R.id.acc_no_block_three:
                    findValueForEditText(edtTxtAccountNoFirstBlock.getText().toString(), R.id.acc_no_block_one, focusChange);
                    findValueForEditText(edtTxtAccountNoSecondBlock.getText().toString(), R.id.acc_no_block_two, focusChange);
                    findValueForEditText(edtTxtAccountNoThirdBlock.getText().toString(), R.id.acc_no_block_three, focusChange);
                    break;

                case R.id.confirm_acc_no_block_one:
                    findValueForEditText(edtTxtConfirmAccountNoFirstBlock.getText().toString(), R.id.confirm_acc_no_block_one, focusChange);
                    break;
                case R.id.confirm_acc_no_block_two:
                    findValueForEditText(edtTxtConfirmAccountNoFirstBlock.getText().toString(), R.id.confirm_acc_no_block_one, focusChange);
                    findValueForEditText(edtTxtConfirmAccountNoSecondBlock.getText().toString(), R.id.confirm_acc_no_block_two, focusChange);
                    break;
                case R.id.confirm_acc_no_block_three:
                    findValueForEditText(edtTxtConfirmAccountNoFirstBlock.getText().toString(), R.id.confirm_acc_no_block_one, focusChange);
                    findValueForEditText(edtTxtConfirmAccountNoSecondBlock.getText().toString(), R.id.confirm_acc_no_block_two, focusChange);
                    findValueForEditText(edtTxtConfirmAccountNoThirdBlock.getText().toString(), R.id.confirm_acc_no_block_three, focusChange);
                    break;

                default:break;
            }
        }
    }
    private void findValueForEditText(String value, int id, String from){
        int stringLength = value.length();
        if (( id == R.id.acc_no_block_one || id == R.id.acc_no_block_two || id == R.id.confirm_acc_no_block_one || id == R.id.confirm_acc_no_block_two)
                && (stringLength > 0 && stringLength < 4)){
            switch (stringLength){
                case 1:
                    value = "00"+value;
                    break;
                case 2:
                    value = "0"+value;
                    break;

                default:break;
            }
        }
        else if ((id == R.id.acc_no_block_three || id == R.id.confirm_acc_no_block_three) && ( stringLength > 0 && stringLength < 7 )){
            switch (stringLength){
                case 1:
                    value = "00000"+value;
                    break;
                case 2:
                    value = "0000"+value;
                    break;
                case 3:
                    value = "000"+value;
                    break;
                case 4:
                    value = "00"+value;
                    break;
                case  5:
                    value = "0"+value;
                    break;
                default:break;
            }
        }
        if (from.equals("editor_action"))
            assignTextToEdtTextOnKeyBoardImeAction(value, id);
        else
            assignTextToEdtTextOnFocusChange(value, id);
    }
    private void assignTextToEdtTextOnFocusChange(String value, int id){
        switch (id){
            case R.id.acc_no_block_one:
                edtTxtAccountNoFirstBlock.setText(value);
                break;
            case R.id.acc_no_block_two:
                edtTxtAccountNoSecondBlock.setText(value);
                break;
            case R.id.acc_no_block_three:
                edtTxtAccountNoThirdBlock.setText(value);
                break;
            case R.id.confirm_acc_no_block_one:
                edtTxtConfirmAccountNoFirstBlock.setText(value);
                break;
            case R.id.confirm_acc_no_block_two:
                edtTxtConfirmAccountNoSecondBlock.setText(value);
                break;
            case R.id.confirm_acc_no_block_three:
                edtTxtConfirmAccountNoThirdBlock.setText(value);
                break;
            default:break;
        }
    }
    private void assignTextToEdtTextOnKeyBoardImeAction(String value, int id){
        switch (id){
            case R.id.acc_no_block_one:
                edtTxtAccountNoFirstBlock.setText(value);
                edtTxtAccountNoSecondBlock.requestFocus();
                break;
            case R.id.acc_no_block_two:
                edtTxtAccountNoSecondBlock.setText(value);
                edtTxtAccountNoThirdBlock.requestFocus();
                break;
            case R.id.acc_no_block_three:
                edtTxtAccountNoThirdBlock.setText(value);
                break;
            case R.id.confirm_acc_no_block_one:
                edtTxtConfirmAccountNoFirstBlock.setText(value);
                edtTxtConfirmAccountNoSecondBlock.requestFocus();
                break;
            case R.id.confirm_acc_no_block_two:
                edtTxtConfirmAccountNoSecondBlock.setText(value);
                edtTxtConfirmAccountNoSecondBlock.requestFocus();
                break;
            case R.id.confirm_acc_no_block_three:
                edtTxtConfirmAccountNoThirdBlock.setText(value);
                break;
            default:break;
        }
    }

    private void setAccountNumber() {

        SettingsModel settingsModel = SettingsDAO.getInstance().getDetails();

        if (settingsModel == null) {
            settingAccountNumber(null);
        } else {
            settingAccountNumber(settingsModel.customerId);
        }
    }
    private void settingAccountNumber(String customerId){
        CommonUtilities.transactionActivitySetAccountNumber(customerId, mAccountSpinner, getActivity());
    }

    private void setAccountType() {

        ArrayList<String> items = new ArrayList<>();
        items.add(getString(R.string.savings_bank));
        items.add(getString(R.string.current_account));
        items.add(getString(R.string.cash_credit));
        items.add(getString(R.string.member_loan));
        items.add(getString(R.string.recurring_deposit));
        items.add(getString(R.string.jewell_loan));
        items.add(getString(R.string.gds));

        if ( getActivity() == null )
            return;

        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(getActivity(), R.layout.simple_spinner_item_dark, items);

        mAccountTypeSpinner.setAdapter(spinnerAdapter);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if ( getActivity() == null || getContext() == null )
            return;
        switch (id) {
            case R.id.btn_submit:
                submit();
                break;
            case R.id.scan:
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED ) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
                } else {
                    Intent intent = new Intent(getActivity(), ScannerActivity.class);
                    startActivityForResult(intent, 100);
                }

                break;
            case R.id.btn_scan_acnt_no:
                if (ActivityCompat.checkSelfPermission(getContext(),
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA},
                            ZXING_CAMERA_PERMISSION);
                }else {
                    Intent intent = new Intent(getActivity(), ScannerActivity.class);
                    startActivityForResult(intent, 100);
                }
                break;
            default:break;
        }
    }
    private void submit(){
        if ( getContext() == null )
            return;
        String recieverAccountNo = confirmAndSetRecieversAccountNo();
        if (isValid() && recieverAccountNo.length() == 12) {
            if (NetworkUtil.isOnline()) {
                final String accountNumber = mAccountSpinner.getSelectedItem().toString();

                final String amount = edtTxtAmount.getText().toString();
                final String accNumber = recieverAccountNo;

                new SweetAlertDialog(getContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                        .setTitleText("Are you sure?")
                        .setContentText("Do you want to transfer INR " + amount + " to A/C no " + accNumber + " ..?")
                        .setCancelText("No,cancel pls!")
                        .setConfirmText("Ok!")
                        .showCancelButton(true)
                        .setCustomImage(R.mipmap.fund_sure)
                        .setCancelClickListener(SweetAlertDialog::cancel)
                        .setConfirmClickListener(sDialog -> {
                            sDialog.dismissWithAnimation();
                            String  accountType = mAccountTypeSpinner.getSelectedItem().toString();

                            final String type;

                            if (accountType.equalsIgnoreCase(getString(R.string.savings_bank))) {
                                type = "SB";
                            } else if (accountType.equalsIgnoreCase( getString(R.string.current_account) )) {
                                type = "CA";
                            }
                            else if (accountType.equalsIgnoreCase( getString(R.string.cash_credit) )) {
                                type = "OD";
                            }
                            else if (accountType.equalsIgnoreCase( getString(R.string.member_loan) )) {
                                type = "ML";
                            }
                            else if (accountType.equalsIgnoreCase( getString(R.string.recurring_deposit) )) {
                                type = "RD";
                            }
                            else if (accountType.equalsIgnoreCase( getString(R.string.jewell_loan) )) {
                                type = "JL";
                            }
                            else {
                                type = "GD";
                            }


                            //new FundTransferAsyncTask(accountNumber, type, accNumber, amount).execute();
                            startTransfer( accountNumber, type, accNumber, amount);
                        })
                        .show();

            } else {
                DialogUtil.showAlert(getActivity(),
                        "Network is currently unavailable. Please try again later.");
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults){


        if ( requestCode == ZXING_CAMERA_PERMISSION ){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(getActivity(), ScannerActivity.class);
                startActivityForResult(intent, 100);
            }else {
                Toast.makeText(getContext(), "App need permission for use camera to scan account number", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isValid() {

        String amount = edtTxtAmount.getText().toString();


        if (amount.length() <1)
            return false;
        final double amt = Double.parseDouble(amount);

        if(amt < 1 || amt > 50000) {

            edtTxtAmount.setError("Please enter amount between 0 and 50000");
            return false;
        }


        edtTxtAmount.setError(null);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == 100 && resultCode == 200) {
            String value = data.getStringExtra("Value");

            if(TextUtils.isEmpty(value)) {
                return;
            }

            if(value.trim().length() >= 14) {
                value = value.substring(0, 14);
            }
            String customerNumber = value.substring(0,12);
            String Submodule = value.substring(12,14);
            CustomerList.clear();
            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                LayoutInflater inflater1 = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater1.inflate(R.layout.cusmodule_popup, null);
                list_view = (ListView) layout.findViewById(R.id.list_view);
                tv_popuptitle = (TextView) layout.findViewById(R.id.tv_popuptitle);
                tv_popuptitle.setText("Select Account");
                builder.setView(layout);
                final AlertDialog alertDialog = builder.create();
                getCustomerAccount(alertDialog,customerNumber,Submodule);
                alertDialog.show();
            }catch (Exception e){e.printStackTrace();}


        }
    }

    private void getCustomerAccount(AlertDialog alertDialog, String value, String submodule) {

        SharedPreferences pref =getActivity().getApplicationContext().getSharedPreferences(Config.SHARED_PREF7, 0);
        String BASE_URL=pref.getString("baseurl", null);
        if (NetworkUtil.isOnline()) {
            try{
                progressDialog = new ProgressDialog(getActivity(), R.style.Progress);
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
                        .baseUrl(BASE_URL+"/")
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .client(client)
                        .build();
                APIInterface apiService = retrofit.create(APIInterface.class);
                final JSONObject requestObject1 = new JSONObject();
                String  accountType = mAccountTypeSpinner.getSelectedItem().toString();
                final String type;

                if (accountType.equalsIgnoreCase(getString(R.string.savings_bank))) {
                    type = "SB";
                } else if (accountType.equalsIgnoreCase( getString(R.string.current_account) )) {
                    type = "CA";
                }
                else if (accountType.equalsIgnoreCase( getString(R.string.cash_credit) )) {
                    type = "OD";
                }
                else if (accountType.equalsIgnoreCase( getString(R.string.member_loan) )) {
                    type = "ML";
                }
                else if (accountType.equalsIgnoreCase( getString(R.string.recurring_deposit) )) {
                    type = "RD";
                }
                else if (accountType.equalsIgnoreCase( getString(R.string.jewell_loan) )) {
                    type = "JL";
                }
                else {
                    type = "GD";
                }

                //String sourceAccount = mAccountSpinner.getSelectedItem().toString();
                try {
                    requestObject1.put("ReqMode",IScoreApplication.encryptStart("10") );
                    requestObject1.put("CustomerNoumber",IScoreApplication.encryptStart(value) );
                    requestObject1.put("Token",IScoreApplication.encryptStart(token));
                    requestObject1.put("SubModule",IScoreApplication.encryptStart(type) );
                    requestObject1.put("ModuleCode",IScoreApplication.encryptStart(submodule) );
                    requestObject1.put("FK_Customer",IScoreApplication.encryptStart(cusid));
                    SharedPreferences bankkeypref =getActivity().getApplicationContext().getSharedPreferences(Config.SHARED_PREF9, 0);
                    String BankKey=bankkeypref.getString("bankkey", null);
                    SharedPreferences bankheaderpref =getActivity().getApplicationContext().getSharedPreferences(Config.SHARED_PREF11, 0);
                    String BankHeader=bankheaderpref.getString("bankheader", null);
                    requestObject1.put("BankKey",IScoreApplication.encryptStart(BankKey));
                    requestObject1.put("BankHeader",IScoreApplication.encryptStart(BankHeader));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), requestObject1.toString());
                Call<String> call = apiService.getAccountList(body);
                call.enqueue(new Callback<String>() {
                    @Override public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                        try {
                            progressDialog.dismiss();
                            JSONObject jObject = new JSONObject(response.body());
                            if(jObject.getString("StatusCode").equals("0")){
                                JSONObject jobj = jObject.getJSONObject("BarcodeAgainstCustomerAccountDets");
                                JSONArray jarray = jobj.getJSONArray( "BarcodeAgainstCustomerAccountList");
                                if(jarray.length()!=0){
                                    for (int k = 0; k < jarray.length(); k++) {
                                        JSONObject jsonObject = jarray.getJSONObject(k);
                                        CustomerList.add(new BarcodeAgainstCustomerAccountList (jsonObject.getString("FK_Customer"),jsonObject.getString("CustomerName"),jsonObject.getString("AccountName"),jsonObject.getString("AccountNumber")));
                                    }
                                    if(jarray.length()==1){
                                        dataItem = CustomerList.get(0).getAccountNumber();
                                        mScannedValue = dataItem;
                                        displayAccountNumber(dataItem,alertDialog);

                                    }else {


                                        HashSet<BarcodeAgainstCustomerAccountList> hashSet = new HashSet<>(CustomerList);
                                        CustomerList.clear();
                                        CustomerList.addAll(hashSet);
                                        CustomListAdapter adapter = new CustomListAdapter(getContext(),CustomerList);
                                        list_view.setAdapter(adapter);
                                        list_view.setOnItemClickListener((parent, view, position, id) -> {
                                            dataItem = CustomerList.get(position).getAccountNumber();
                                            mScannedValue = dataItem;
                                            displayAccountNumber(dataItem,alertDialog);
                                        });

                                    }


                                }

                            }else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setMessage("No data found.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {

                                                dialog.dismiss();
                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                        }
                    }
                    @Override
                    public void onFailure(Call<String> call, Throwable t) { progressDialog.dismiss();}
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            DialogUtil.showAlert(getContext(),
                    "Network is currently unavailable. Please try again later.");
        }



    }

    private void displayAccountNumber(String data,AlertDialog alertDialog){

        if(data.trim().length() >= 14) {
            data = data.substring(0, 14);
        }


        if(data.startsWith("01")) {
            mAccountTypeSpinner.setSelection(0);
        } else if(data.startsWith("02")) {
            mAccountTypeSpinner.setSelection(1);
        }
        String firstValueQrScanned = data.substring(0,3);
        String secondValueQrScanned = data.substring(3,6);
        String thirdValueQrScanned = data.substring(6,12);

        edtTxtAccountNoFirstBlock.setText(firstValueQrScanned);
        edtTxtAccountNoSecondBlock.setText(secondValueQrScanned);
        edtTxtAccountNoThirdBlock.setText(thirdValueQrScanned);

        edtTxtConfirmAccountNoFirstBlock.setText(firstValueQrScanned);
        edtTxtConfirmAccountNoSecondBlock.setText(secondValueQrScanned);
        edtTxtConfirmAccountNoThirdBlock.setText(thirdValueQrScanned);

        mAccountNumberEt.setText(data);
        mConfirmAccountNumberEt.setText(data);

        mAmountEt.requestFocus();
        mAmountEt.setSelection(mAmountEt.getText().length());
        alertDialog.dismiss();
    }


    private String confirmAndSetRecieversAccountNo(){
        String recieverAccountNo = edtTxtAccountNoFirstBlock.getText().toString()+
                edtTxtAccountNoSecondBlock.getText().toString()+
                edtTxtAccountNoThirdBlock.getText().toString();
        String confirmRecieverAccountNo = edtTxtConfirmAccountNoFirstBlock.getText().toString()+
                edtTxtConfirmAccountNoSecondBlock.getText().toString()+
                edtTxtConfirmAccountNoThirdBlock.getText().toString();
        if ( recieverAccountNo.equals( confirmRecieverAccountNo ) &&
                recieverAccountNo.length() == 12 && confirmRecieverAccountNo.length() == 12){
            return recieverAccountNo;
        }
        else if ( !recieverAccountNo.equals( confirmRecieverAccountNo ) &&  recieverAccountNo.length() == 12 && confirmRecieverAccountNo.length() == 12){
            showAlert();
        }
        else {
            if (edtTxtAccountNoFirstBlock.getText().toString().length() < 3)
                 edtTxtAccountNoFirstBlock.setError("Atleast 3 digit are required");

            if (edtTxtAccountNoSecondBlock.getText().toString().length() <3)
                edtTxtAccountNoSecondBlock.setError("Atleast 3 digit are required");

            if (edtTxtAccountNoThirdBlock.getText().toString().length() < 6)
                edtTxtAccountNoThirdBlock.setError("Atleast 6 digits are required");

            if (edtTxtConfirmAccountNoFirstBlock.getText().toString().length() < 3)
                edtTxtConfirmAccountNoFirstBlock.setError("Atleast 3 digits are required");

            if (edtTxtConfirmAccountNoSecondBlock.getText().toString().length() < 3)
                edtTxtConfirmAccountNoSecondBlock.setError("Atleast 3 digits are required");

            if (edtTxtConfirmAccountNoThirdBlock.getText().toString().length() < 6)
                edtTxtConfirmAccountNoThirdBlock.setError("Atleast 6 digits are required");
        }
        return "";
    }

    private void showAlert(){
        if ( getContext() == null )
            return;
        new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Oops...")
                .setContentText("Both account number and confirm account number are not matching")
                .show();
    }


    private void startTransfer( String accountNo, String type, String receiverAccNo, String amount) {


        UserCredential loginCredential = UserCredentialDAO.getInstance().getLoginCredential();


        if (TextUtils.isEmpty(mScannedValue)) {
            mScannedValue = "novalue";
        }

        mScannedValue = mScannedValue.replaceAll(" ", "%20");

        /*Extract account number*/
        accountNo = accountNo.replace(accountNo.substring(accountNo.indexOf(" (") + 1, accountNo.indexOf(")") + 1), "");
        accountNo = accountNo.replace(" ", "");

        AccountInfo accountInfo = PBAccountInfoDAO.getInstance().getAccountInfo(accountNo);
        String accountType = accountInfo.accountTypeShort;
        final String tempFromAccNo = accountNo +"("+ accountType +")";
        final String tempToAccNo = receiverAccNo +"("+ type +")";
        /*End of Extract account number*/

        SharedPreferences pref =getActivity().getApplicationContext().getSharedPreferences(Config.SHARED_PREF7, 0);
        String BASE_URL=pref.getString("baseurl", null);
        try{
            String url =
                    BASE_URL+ "/api/MV3"+ "/FundTransferIntraBank?AccountNo="
                            + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(accountNo))
                            + "&Module=" + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(accountType))
                            + "&ReceiverModule=" + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(type))
                            + "&ReceiverAccountNo=" + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(receiverAccNo.trim()))
                            + "&amount=" + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(amount.trim()))
                            + "&Pin=" + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(loginCredential.pin))
                            + "&QRCode=" + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mScannedValue));
            NetworkManager.getInstance().connector(url, new ResponseManager() {
                @Override
                public void onSuccess(String result) {
                    try{
                        Gson gson = new Gson();
                        FundTransferResult fundTransferResult = gson.fromJson( result, FundTransferResult.class );
                        int statusCode = fundTransferResult.getStatusCode();
                        if ( statusCode == 1 ){
                            ArrayList<KeyValuePair> keyValuePairs = new ArrayList<>();

                            KeyValuePair keyValuePair = new KeyValuePair();
                            keyValuePair.setKey("Ref. No");
                            keyValuePair.setValue( fundTransferResult.getRefId() );
                            keyValuePairs.add( keyValuePair );

                            keyValuePair = new KeyValuePair();
                            keyValuePair.setKey("Amount");
                            keyValuePair.setValue( fundTransferResult.getAmount() );
                            keyValuePairs.add( keyValuePair );

                            keyValuePair = new KeyValuePair();
                            keyValuePair.setKey("From Acc.No");
                            keyValuePair.setValue( tempFromAccNo );
                            keyValuePairs.add( keyValuePair );

                            keyValuePair = new KeyValuePair();
                            keyValuePair.setKey("To Acc.No");
                            keyValuePair.setValue( tempToAccNo );
                            keyValuePairs.add( keyValuePair );

                            alertMessage("Success...!", keyValuePairs, "Successfully transferred the amount", true, false);
                        }
                        else if ( statusCode == 2 ){
                            alertMessage("Oops....!", new ArrayList<>(), "Transaction Failed, Please try again later", false, false);
                        }
                        else if ( statusCode == 3 ){
                            alertMessage("Transaction failed...!", new ArrayList<>(), "Invalid QR code", false, false);
                        }
                        else if ( statusCode == 4 ){
                            alertMessage("Transaction failed...!", new ArrayList<>(), "Invalid Account number", false, false);
                        }
                        else if ( statusCode == 5 ){
                            alertMessage("Transaction failed...!", new ArrayList<>(), "Does Not have sufficient balance to Transfer", false, false);
                        }
                        else {
                            alertMessage("Transaction failed...!", new ArrayList<>(), "Please try again later", false, false);
                        }
                    }catch ( Exception e ){

                        alertMessage("Transaction failed...!", new ArrayList<>(), "Please try again later", false, false);

                    }
                }

                @Override
                public void onError(String error) {
                    alertMessage("Transaction failed...!", new ArrayList<>(), "Please try again later", false, false);
                }
            }, getActivity(), "Talking to server. Please wait....");
        }catch ( Exception e ){
            //Do nothing
        }

    }
    private void alertMessage(String title, ArrayList<KeyValuePair> keyValueList, String message , boolean isHappy, boolean isBackButtonEnabled ){
        getFragmentManager().beginTransaction().replace( R.id.container, AlertMessageFragment.getInstance( keyValueList  ,title, message,
                isHappy, isBackButtonEnabled ) ).commit();
    }


    private class FundTransferResult{
        @SerializedName("StatusCode")
        private int statusCode;
        @SerializedName("RefID")
        private String refId;
        @SerializedName("MobileNumber")
        private String mobileNumber;
        @SerializedName("Amount")
        private String amount;
        @SerializedName("AccNumber")
        private String accNo;

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public String getRefId() {
            return refId;
        }

        public void setRefId(String refId) {
            this.refId = refId;
        }

        public String getMobileNumber() {
            return mobileNumber;
        }

        public void setMobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getAccNo() {
            return accNo;
        }

        public void setAccNo(String accNo) {
            this.accNo = accNo;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    // handle back button's click listener
                    //  Toast.makeText(getActivity(), "Back press", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getActivity(),HomeActivity.class);
                    startActivity(i);
                    getActivity().finish();
                    return true;
                }
                return false;
            }
        });

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


}
