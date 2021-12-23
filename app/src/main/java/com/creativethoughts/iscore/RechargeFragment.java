package com.creativethoughts.iscore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;

import com.creativethoughts.iscore.Helper.Config;
import com.creativethoughts.iscore.adapters.OperatorSpinnerAdapter;
import com.creativethoughts.iscore.custom_alert_dialogs.KeyValuePair;
import com.creativethoughts.iscore.custom_alert_dialogs.AlertMessageFragment;
import com.creativethoughts.iscore.db.dao.PBAccountInfoDAO;
import com.creativethoughts.iscore.db.dao.RechargeDAO;
import com.creativethoughts.iscore.db.dao.SettingsDAO;
import com.creativethoughts.iscore.db.dao.UserCredentialDAO;
import com.creativethoughts.iscore.db.dao.model.AccountInfo;
import com.creativethoughts.iscore.db.dao.model.RechargeModel;
import com.creativethoughts.iscore.db.dao.model.SettingsModel;
import com.creativethoughts.iscore.db.dao.model.UserCredential;
import com.creativethoughts.iscore.utility.CommonUtilities;
import com.creativethoughts.iscore.utility.DialogUtil;
import com.creativethoughts.iscore.utility.NetworkUtil;
import com.creativethoughts.iscore.utility.RechargeValue;
import com.creativethoughts.iscore.utility.network.NetworkManager;
import com.creativethoughts.iscore.utility.network.ResponseManager;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;


import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.app.Activity.RESULT_OK;


public class RechargeFragment extends Fragment implements View.OnClickListener {

    private static final String BUNDLE_TYPE = "bundle_type";
    private static final int PICK_CONTACT = 1;

    private View view;
    private AutoCompleteTextView mMobileNumEt;
    private EditText mAmountEt;
    private ImageButton selectContactImgBtn;
    private TextView mTitle;
    private Spinner mOperatorSpinner;
    private Spinner mCircleSpinner;
    private Spinner mAccountSpinner;
    private TextInputLayout mAccNumIpl;
    private TextInputLayout phonenumberLayout;
    private AppCompatEditText mAccNumEdt;

    private int mSelectedType = 0;

    public RechargeFragment() {
        // Required empty public constructor
    }

    public static RechargeFragment newInstance(int type) {
        RechargeFragment fragment = new RechargeFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_TYPE, type);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach( context );

        Bundle bundle = getArguments();

        if (bundle != null) {
            mSelectedType = bundle.getInt(BUNDLE_TYPE, 0);
        }
    }
    @Override
    public final void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

         view = inflater.inflate(R.layout.fragment_recharge, container, false);

        mMobileNumEt =   view.findViewById(R.id.phoneno);

        setAdapter(mMobileNumEt);
        mMobileNumEt.setOnItemClickListener((parent, viewTemp, position, id) -> {
            RechargeModel rechargeModel = new RechargeModel();
            rechargeModel.mobileNo = mMobileNumEt.getText().toString();
            String serveceProvider = RechargeDAO.getInstance().getServiceProvider(rechargeModel);
            int positionOnSpinner  = RechargeValue.getOperatorName(serveceProvider);
            if(positionOnSpinner < 0) positionOnSpinner = 0;
            mOperatorSpinner.setSelection(positionOnSpinner-1);

        });

        mAmountEt = view.findViewById(R.id.amount);
        selectContactImgBtn = view.findViewById(R.id.select_contact_image);
        selectContactImgBtn.setOnClickListener(this);
        mAccNumIpl = view.findViewById(R.id.account_number_inputlayout);
        phonenumberLayout = view.findViewById(R.id.phoneno_layout);
        mAccNumEdt = view.findViewById(R.id.account_number);

        mAccNumIpl.setVisibility(View.GONE);
        mAccNumEdt.setVisibility(View.GONE);

        mOperatorSpinner = view.findViewById(R.id.operator_spinner);
        mCircleSpinner = view.findViewById(R.id.circle_spinner);
        mAccountSpinner = view.findViewById(R.id.spnAccountNum);

        Button mSubmitButton = view.findViewById(R.id.btn_submit);
        Button mCancelButton = view.findViewById(R.id.btn_clear);
        mSubmitButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mTitle = view.findViewById(R.id.recharge_header_title);
        setOperator(0);

        setCircle();

        setAccountNumber();

        updateType(mSelectedType);

        return view;
    }

    private void setOperator(int type) {
        String title;
        Activity activity = getActivity();
        if ( activity == null )
            return;
        OperatorSpinnerAdapter operatorSpinnerAdapter = new OperatorSpinnerAdapter(activity);
        switch (type) {
            case 0:
                operatorSpinnerAdapter.addItems(RechargeValue.getAllOperator());
                title = "Prepaid";
                mTitle.setText( title );
                break;
            case 1:
                operatorSpinnerAdapter.addItems(RechargeValue.getAllPostPaidOperator());
                title = "Postpaid";
                mTitle.setText( title );
                break;
            case 2:
                operatorSpinnerAdapter.addItems(RechargeValue.getAllLandLineOperator());
                title = "Landline";
                mTitle.setText( title );
                break;
            case 3:
                title = "DTH";
                operatorSpinnerAdapter.addItems(RechargeValue.getAllDTHOperator());
                mTitle.setText( title );
                break;
            case 4:
                title = "Datacard";
                operatorSpinnerAdapter.addItems(RechargeValue.getAllDataCardOperators());
                mTitle.setText( title );
                break;
            default:
        }

        mOperatorSpinner.setAdapter(operatorSpinnerAdapter);
        mOperatorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (IScoreApplication.DEBUG) Log.d("TAG", "position : " + position);
                if (IScoreApplication.DEBUG) Log.d("TAG", "Item : " + mOperatorSpinner.getSelectedItem().toString());

                mAccNumIpl.setVisibility(View.GONE);
                mAccNumEdt.setVisibility(View.GONE);

                if (( mSelectedType == 1 || mSelectedType == 2 ) && isCircleAccountNumberMandatory() ) {

                    mAccNumIpl.setVisibility(View.VISIBLE);
                    mAccNumEdt.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing
            }
        });

    }


    private void setCircle() {
        if ( getActivity() == null )
            return;
        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(getActivity(), R.layout.simple_spinner_item_dark, RechargeValue.getAllCircle());

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mCircleSpinner.setAdapter(spinnerAdapter);

        mCircleSpinner.setSelection(10);

        mCircleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (mSelectedType == 2) {

                    if (isCircleAccountNumberMandatory()) {
                        mAccNumIpl.setVisibility(View.VISIBLE);
                        mAccNumEdt.setVisibility(View.VISIBLE);
                    } else {
                        mAccNumIpl.setVisibility(View.GONE);
                        mAccNumEdt.setVisibility(View.GONE);
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing
            }
        });
    }

    private boolean isCircleAccountNumberMandatory() {
        return (mSelectedType == 1 || mSelectedType == 2) && ((mOperatorSpinner.getSelectedItem().toString().contains("MTNL") && mCircleSpinner.getSelectedItem().toString().contains("Delhi")) || mOperatorSpinner.getSelectedItem().toString().contains("BSNL"));


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
        if ( customerId == null  || getContext() == null )
            return;
        CommonUtilities.transactionActivitySetAccountNumber(customerId, mAccountSpinner, getActivity());

    }

    private void updateType(int position) {
        switch (position) {
            case 0: // Mobile Recharge
                phonenumberLayout.setHint("Mobile number");
                mSelectedType = 0;


                break;
            case 1: // Postpaid Mobile
                phonenumberLayout.setHint("Mobile number");

                mSelectedType = 1;
                break;
            case 2: // Landlines Bill Payment
                phonenumberLayout.setHint("Phone Number");
                selectContactImgBtn.setVisibility(View.GONE);
                mSelectedType = 2;
                break;
            case 3: // DTH Recharge
                phonenumberLayout.setHint("SUBSCRIBER ID");
                selectContactImgBtn.setVisibility(View.GONE);
                mSelectedType = 3;
                break;
            case 4: //Datacard recharge
                phonenumberLayout.setHint("SUBSCRIBER ID");
                selectContactImgBtn.setVisibility(View.VISIBLE);
                mSelectedType = 4;

                break;
                default:
        }

        setOperator(mSelectedType);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.btn_submit:
                onClickSubmit();
                break;
            case R.id.btn_clear:
                mMobileNumEt.setText("");
                mAmountEt.setText("");
                break;
            case R.id.select_contact_image:
                contactSelect();
                break;
            default:
                break;
        }
    }

    private void showToast(String value) {
        if (  getActivity()  == null )
            return;
        Toast toast = Toast.makeText(getActivity(), value, Toast.LENGTH_SHORT);
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View toastView = layoutInflater.inflate(R.layout.custom_toast, view.findViewById(R.id.container));
        TextView textView = toastView.findViewById(R.id.text);
        textView.setText(value);
        toast.setView(toastView);
        toast.show();
    }



    private void onClickSubmit() {
        final String mobileNumber   = mMobileNumEt.getText().toString();
        final String amount         = mAmountEt.getText().toString();

        final String operatorName = mOperatorSpinner.getSelectedItem().toString();
        final String operatorId;
        final String message;

        int imageResource;
        String tempAmountInr = " And Amount INR.";
        String tempForRecharging = " For Recharging";
        String confirmSubscriberId = "Please Confirm The SUBSCRIBER ID ";

        switch (mSelectedType) {
            case 0:
                operatorId = RechargeValue.getOperatorId(operatorName);
                message = "Please Confirm The Mobile No " + mobileNumber + tempAmountInr + amount + tempForRecharging;
                imageResource = R.mipmap.phone;
                break;
            case 1:
                operatorId = RechargeValue.getPostPaidOperatorId(operatorName);
                message = "Please Confirm The Mobile No " + mobileNumber + tempAmountInr + amount + tempForRecharging;
                imageResource = R.mipmap.phone;
                break;
            case 2:
                operatorId = RechargeValue.getLandLineOperatorId(operatorName);
                message = "Please Confirm The Phone No " + mobileNumber + tempAmountInr + amount + tempForRecharging;
                imageResource = R.mipmap.landline;
                break;
            case 3:
                operatorId = RechargeValue.getDTHOperatorId(operatorName);
                message = confirmSubscriberId + mobileNumber + tempAmountInr + amount + tempForRecharging;
                imageResource = R.mipmap.dish;
                break;
            case 4:
                operatorId = RechargeValue.getDataCardOperatorsId(operatorName);
                message = confirmSubscriberId + mobileNumber + tempAmountInr + amount + tempForRecharging;

                imageResource = R.mipmap.datacard;
                break;
            default:
                operatorId = RechargeValue.getOperatorId(operatorName);
                message = confirmSubscriberId + mobileNumber + tempAmountInr + amount + tempForRecharging;

                imageResource = R.mipmap.phone;
                break;
        }

        final String circleName = mCircleSpinner.getSelectedItem().toString();
        final String circleId = RechargeValue.getCircleId(circleName);

        final String accountNumber = mAccountSpinner.getSelectedItem().toString();
        final String circleAccountNo = mAccNumEdt.getText().toString();

        try{
            Long tempMobile = Long.parseLong(mobileNumber);
            if ( IScoreApplication.DEBUG )
                Log.d("parse long", tempMobile.toString() );
        }catch (Exception e){
            showToast("Please enter valid  mobile number");
            return ;
        }
        if( (mSelectedType == 0 || mSelectedType == 1) && mobileNumber.length() != 10 ){
            showToast("Please enter valid 10 digit mobile number");
        }
        else if( (mSelectedType == 2) && ( (mobileNumber.length() <10 ) || (mobileNumber.length() > 15  ) ) ){
            showToast("Please enter valid land line number");
        }
        else if( (mSelectedType == 3) && ( (mobileNumber.length() < 6) || (mobileNumber.length()>15) ) ){
            showToast("Please enter valid subscriber ID");
        }
        else if( (mSelectedType == 4) && ( (mobileNumber.length() < 6) || (mobileNumber.length()>15) ) ){
            showToast("Please enter valid subscriber ID");
        }
        else{
            if(amount.length() > 0){
                try {
                    Integer.parseInt(amount);

                }catch (Exception e){
                    showToast("Please enter valid amount");
                    return;
                }
                if(TextUtils.isDigitsOnly(amount.trim())){
                     long value = Long.parseLong(amount.trim());
                    if( value < 10 || value > 10000){
                        showToast("Please enter the amount 10 to 10000 to recharge");
                        return;
                    }
                }
                else return;
            }
            else{
                showToast("Please enter the amount to recharge");
                return;
            }


            if (NetworkUtil.isOnline() && getContext() != null ) {

                new SweetAlertDialog(getContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                        .setTitleText("Are you sure?")
                        .setContentText(message)
                        .setCancelText("No")
                        .setConfirmText("yes")
                        .showCancelButton(true)
                        .setCustomImage(imageResource)
                        .setCancelClickListener(SweetAlertDialog::cancel)
                        .setConfirmClickListener(sDialog -> {
                            sDialog.dismissWithAnimation();
                            recharge( accountNumber, mSelectedType, mobileNumber, circleId, operatorId, amount, circleAccountNo );
                        })
                        .show();
            } else {
                DialogUtil.showAlert(getActivity(),
                        "Network is currently unavailable. Please try again later.");
            }
        }
    }
    private void recharge(String mAccountNumber,int type, String mMobileNumber, String mCircleId, String mOperatorId, String mAmount, String mCircleAccNo ) {

        try {


            SharedPreferences pref =getActivity().getApplicationContext().getSharedPreferences(Config.SHARED_PREF7, 0);
            String BASE_URL=pref.getString("baseurl", null);
            UserCredential loginCredential = UserCredentialDAO.getInstance().getLoginCredential();

            String url =  BASE_URL+ "/api/MV3";

            mAccountNumber = mAccountNumber.replace(mAccountNumber.substring(mAccountNumber.indexOf(" (")+1, mAccountNumber.indexOf(')')+1), "");
            mAccountNumber = mAccountNumber.replace(" ","");
            AccountInfo accountInfo = PBAccountInfoDAO.getInstance().getAccountInfo(mAccountNumber);
            String accountType = accountInfo.accountTypeShort;
            String operatorString = "&Operator=";
            String circleString = "&Circle=";
            String amountString = "&Amount=";
            String accountNoString = "&AccountNo=";
            String moduleString = "&Module=";
            String pinString = "&Pin=";
            switch (type) {
                case 0:
                    url +=
                            "/MobileRecharge?MobileNumer=" + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mMobileNumber))
                                    + operatorString + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mOperatorId)) + circleString
                                    + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mCircleId)) + amountString
                                    +IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mAmount))  + accountNoString
                                    +IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mAccountNumber))  +
                                    moduleString +IScoreApplication.encodedUrl(IScoreApplication.encryptStart(accountType))  +
                                    pinString + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(loginCredential.pin));
                    break;
                case 1:
                case 2:
                    url +=
                            "/POSTPaidBilling?MobileNumer=" +IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mMobileNumber))
                                    + operatorString + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mOperatorId)) +
                                    circleString+ IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mCircleId)) +
                                    "&Circleaccount=" + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mCircleAccNo))
                                    + amountString + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mAmount)) +
                                    accountNoString+ IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mAccountNumber)) +
                                    moduleString + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(accountType)) +
                                    pinString + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(loginCredential.pin));
                    break;
                case 3:

                    url +=
                            "/DTHRecharge?SUBSCRIBER_ID=" +IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mMobileNumber))
                                    + operatorString +IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mOperatorId))
                                    + circleString + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mCircleId)) +
                                    amountString + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mAmount)) +
                                    accountNoString+IScoreApplication.encodedUrl (IScoreApplication.encryptStart(mAccountNumber)) +
                                    moduleString + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(accountType)) +
                                    pinString +IScoreApplication.encodedUrl (IScoreApplication.encryptStart(loginCredential.pin));

                    break;
                case 4: // Datacard Recharge
                    url +=
                            "/DTHRecharge?SUBSCRIBER_ID=" +IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mMobileNumber))
                                    + operatorString +IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mOperatorId))
                                    + circleString + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mCircleId)) +
                                    amountString + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mAmount)) +
                                    accountNoString+IScoreApplication.encodedUrl (IScoreApplication.encryptStart(mAccountNumber)) +
                                    moduleString + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(accountType)) +
                                    pinString +IScoreApplication.encodedUrl (IScoreApplication.encryptStart(loginCredential.pin));

                    break;
                default:
                    break;
            }

            NetworkManager.getInstance().connector(url, new ResponseManager() {
                @Override
                public void onSuccess(String result) {

                    parseResponse( result, mMobileNumber, mOperatorId );
                }

                @Override
                public void onError(String error) {
                    Toast.makeText( getContext(), error, Toast.LENGTH_SHORT ).show();
                }
            }, getActivity(), "Recharge processing. Please wait...");


        } catch (Exception e) {
            if ( IScoreApplication.DEBUG )
                Log.d("exc", e.toString() );
        }

    }
    private void parseResponse( String result , String mMobileNumber, String mOperatorId){
        Gson gson = new Gson();
        RechargeModel rechargeModel = new RechargeModel();
        rechargeModel.mobileNo = mMobileNumber;
        rechargeModel.type = mSelectedType;
        rechargeModel.serviceProvider = mOperatorId;
        try {
            RechargeResult rechargeResult = gson.fromJson( result, RechargeResult.class );
            int statusCode = rechargeResult.getStatusCode();
            Context context = getContext();
            assert context != null;
            if ( statusCode == 0 ){
                alertMessage("Oops...!", new ArrayList<>(), "Failed. Does not have sufficient balance in selected account", false, false);
            }else if ( statusCode == 1 ){
                insertDb( rechargeModel );
                showSuccess( rechargeResult );
            }else if ( statusCode == 2 ){
                showFailure();
            }else if ( statusCode == 3 ){
                insertDb( rechargeModel );
                showPending();
            }else {
                new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(IScoreApplication.OOPS)
                        .setContentText("Service not available")
                        .show();
            }
        }catch ( Exception e){
            Toast.makeText( getContext(),"An error occured", Toast.LENGTH_SHORT ).show();
        }


    }
    private void insertDb(RechargeModel rechargeModel)  {
        RechargeDAO rechargeDAO = new RechargeDAO();
        rechargeDAO.insertValues(rechargeModel);
    }
    private void showSuccess( RechargeResult rechargeResult ) {
        if ( getContext() == null)
            return;
        String message = "Recharge Success. Amount: Rs."+rechargeResult.getAmount()  +"Ref.No:"+rechargeResult.getRefId();
        ArrayList<KeyValuePair> keyValuePairs = new ArrayList<>();
        KeyValuePair keyValuePair = new KeyValuePair();
        keyValuePair.setKey("Amount");
        keyValuePair.setValue(rechargeResult.getAmount() );
        keyValuePairs.add( keyValuePair );
        keyValuePair = new KeyValuePair();
        keyValuePair.setKey("Ref.Id");
        keyValuePair.setValue(rechargeResult.getRefId() );
        keyValuePair = new KeyValuePair();
        keyValuePair.setKey("Mobile No");
        keyValuePair.setValue( rechargeResult.getMobileNumber() );
        keyValuePairs.add( keyValuePair );

        switch (mSelectedType) {
            case 0:
                message = "Mobile Recharge success.";
                break;
            case 1:
                message = "PostPaid Bill Payment successfully.";
                break;
            case 2:
                message = "Land line Bill Payment successfully.";
                break;
            case 3:
                message = "DTH Bill Payment successfully .";
                break;
            default:
                break;
        }
        alertMessage( "Success", keyValuePairs, message,true, false );


    }
    private void alertMessage(String title, ArrayList<KeyValuePair> keyValueList,String message ,boolean isHappy, boolean isBackButtonEnabled ){
        getFragmentManager().beginTransaction().replace( R.id.container, AlertMessageFragment.getInstance( keyValueList  ,title, message,
                isHappy, isBackButtonEnabled ) ).commit();
    }
    private void showFailure() {
        if ( getContext() == null)
            return;
        String message = "Recharge failed";
        switch (mSelectedType) {
            case 0:
                message = "Mobile Recharge failed";
                break;
            case 1:
                message = "Postpaid Recharge failed";
                break;
            case 2:
                message = "LandLine Recharge failed";
                break;
            case 3: // DTH Recharge
                message = "DTH Recharge failed";
                break;
            default:
                break;
        }


       ArrayList<KeyValuePair> keyValuePairs = new ArrayList<>();
       KeyValuePair keyValuePair = new KeyValuePair();
       keyValuePair.setKey("Message");
       keyValuePair.setValue(message);
       keyValuePairs.add( keyValuePair );
       alertMessage(  message, keyValuePairs, "",false, false);
    }

    private void showPending() {
        String message  ;
        switch (mSelectedType) {
            case 0: // Mobile Recharge
                message = "Mobile Recharge Pending";
                break;
            case 1: // Postpaid Mobile
                message = "PostPaid Bill Payment Pending";
                break;
            case 2: // Landlines Bill Payment
                message = "Land line Bill Pay Request Pending";
                break;
            case 3: // DTH Recharge
                message = "DTH Recharge Pending";
                break;
            default:
                message = "";
                break;
        }
        alertMessage( "Pending", new ArrayList<>(), message, false, false);
    }

    private void setAdapter(AutoCompleteTextView autoCompleteTextView){
        if ( getContext() == null )
            return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.select_dialog_item, RechargeDAO.getInstance().getPhoneDthNumber(mSelectedType));
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.setAdapter(adapter);
    }




    private void contactSelect(){

        Intent intent = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        intent.setType( ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE );
        startActivityForResult(intent, PICK_CONTACT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_CONTACT && resultCode == RESULT_OK && getActivity() != null ){
           try{
               Uri uriContact = data.getData();
               assert uriContact != null;
               Cursor cursor =
                       getActivity().getContentResolver().query(
                               uriContact, null, null, null, null);

               assert cursor != null;
               cursor.moveToFirst();
               String tempContact = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
               mMobileNumEt.setText(extractPhoneNumber(tempContact));

               closeCursor(cursor);
           }catch (Exception e){
               if(IScoreApplication.DEBUG)Log.e("contact ex", e.toString());
           }
        }
    }

    private String extractPhoneNumber(String resultPhoneNumber){
        String result;
        try{
            result = resultPhoneNumber.replaceAll("\\D+","");
            if(result.length() > 10){
                result = result.substring( result.length()-10,result.length());
            }
        }catch (Exception e){
            result = "";
        }
        return  result;
    }

    private void closeCursor(Cursor cursor){
        try {
            cursor.close();
        }catch (Exception e){
            if(IScoreApplication.DEBUG) Log.e("Null pointer ex", e.toString());
        }
    }
    private class RechargeResult{

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

        private int getStatusCode() {
            return statusCode;
        }

        private String getRefId() {
            return refId;
        }

        private String getMobileNumber() {
            return mobileNumber;
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
}
