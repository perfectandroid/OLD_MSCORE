package com.creativethoughts.iscore;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.creativethoughts.iscore.Helper.Config;
import com.creativethoughts.iscore.custom_alert_dialogs.AlertMessageFragment;
import com.creativethoughts.iscore.custom_alert_dialogs.KeyValuePair;
import com.creativethoughts.iscore.db.dao.KsebBillDAO;
import com.creativethoughts.iscore.db.dao.PBAccountInfoDAO;
import com.creativethoughts.iscore.db.dao.SettingsDAO;
import com.creativethoughts.iscore.db.dao.UserCredentialDAO;
import com.creativethoughts.iscore.db.dao.model.AccountInfo;
import com.creativethoughts.iscore.db.dao.model.KsebBillModel;
import com.creativethoughts.iscore.db.dao.model.SettingsModel;
import com.creativethoughts.iscore.kseb.KsebRechargeStatus;
import com.creativethoughts.iscore.kseb.KsebSectionSelectionActivity;
import com.creativethoughts.iscore.kseb.SectionDetails;
import com.creativethoughts.iscore.utility.CommonUtilities;
import com.creativethoughts.iscore.utility.ConnectionUtil;
import com.creativethoughts.iscore.utility.Validation;
import com.google.gson.Gson;


import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link KsebBillFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class KsebBillFragment extends Fragment implements View.OnClickListener {

    private static final int REQUEST_SELECT_SECTION = 100;
    private String sectionCode = "";
    private Spinner accountNumberSelector;
    private TextView txtMobileNoErrorMessage;
    private TextView txtConsumerNameErrorMessage;
    private TextView txtConsumerNoErrorMessage;
    private TextView txtBillNoErrorMessage;
    private TextView txtAmountErrorMessage;
    private TextView txtViewName;
    private TextView txtViewMobileNo;
    private TextView txtViewConsumerNo;
    private TextView txtViewSection;
    private TextView txtViewBillNo;
    private TextView txtViewAmount;
    private TextView txtViewAccountNo;
    private TextView txtSectionName;
    private EditText edtTxtBillNo;
    private EditText edtTxtAmount;
    private AutoCompleteTextView autoCompleConsumerName;
    private AutoCompleteTextView autoCompleMobileNumber;
    private AutoCompleteTextView autoCompleConsumerNo;
    private LinearLayout mLinearForm;
    private String tempStringMobileNumber;
    private String tempStringConsumerName;
    private String tempStringConsumerNo;
    private String tempStringSectionList;
    private String tempStringBillNo;
    private String tempStringAmount;
    private String tempStringAccountNo;
    private SweetAlertDialog mSweetAlertDialog;

    public KsebBillFragment() {
        // Required empty public constructor
    }


    public static KsebBillFragment newInstance() {
        KsebBillFragment fragment = new KsebBillFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            showAlertDialogue(getString(R.string.kseb_bill_expiry_alert));
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView               = inflater.inflate(R.layout.fragment_kseb_bill, container, false);
        addViewForm(rootView);
        addViewToBillData(rootView);

        AppCompatActivity activity  = (AppCompatActivity) getActivity();
        if ( activity != null && activity.getSupportActionBar() != null ){
            ActionBar actionBar = activity.getSupportActionBar();
            assert actionBar != null;
            actionBar.setTitle(getString(R.string.kseb_title_action_bar));

            getAccountNumber();
        }
        rootView.findViewById( R.id.lnr_lyt_select_section).setOnClickListener( this );
        return rootView;
    }
    private void addViewForm(View rootView){

        mLinearForm                 =   rootView.findViewById(R.id.form_scroll_view);
        txtMobileNoErrorMessage     =  rootView.findViewById(R.id.mobile_no_error);
        txtConsumerNameErrorMessage =   rootView.findViewById(R.id.name_error);
        txtConsumerNoErrorMessage   =   rootView.findViewById(R.id.consumer_no_error);
        txtBillNoErrorMessage       =   rootView.findViewById(R.id.bill_no_error);
        txtAmountErrorMessage       =  rootView.findViewById(R.id.amount_error);
        accountNumberSelector       =     rootView.findViewById(R.id.account_number_selector_spinner);
        txtSectionName              = rootView.findViewById( R.id.txt_section_name );
        Button proceedToPayButton   =   rootView.findViewById(R.id.proceedToPay);
        proceedToPayButton.setOnClickListener(this);
        Button edit                 =   rootView.findViewById(R.id.edit);
        edit.setOnClickListener(this);
        Button pay                  =  rootView.findViewById(R.id.pay);
        pay.setOnClickListener(this);
        Button btnClearAll          =   rootView.findViewById(R.id.clear_all);
        btnClearAll.setOnClickListener(this);

        autoCompleConsumerName      =  rootView.findViewById(R.id.consumer_name);
        autoCompleConsumerName.setSingleLine(true);
        edtTxtBillNo                =   rootView.findViewById(R.id.bill_no);
        edtTxtBillNo.setSingleLine(true);
        edtTxtAmount                =   rootView.findViewById(R.id.amount);

        autoCompleMobileNumber      =   rootView.findViewById(R.id.mobile_no);
        autoCompleMobileNumber.setSingleLine(true);
        autoCompleConsumerNo        =  rootView.findViewById(R.id.consumer_no);
        autoCompleConsumerNo.setSingleLine(true);


        /*Add textchangelistener to autocomplete textview*/
        addTextChange(autoCompleMobileNumber, rootView, R.id.mobile_no, "mobile_no");
        addTextChange(autoCompleConsumerNo, rootView, R.id.consumer_no, "consumer_no");
        addTextChange(autoCompleConsumerName, rootView, R.id.consumer_name, "consumer_name");

        autoCompleConsumerName.setOnItemClickListener((p, v, pos, id) -> autoSelect(autoCompleConsumerName.getText().toString()));

    }
    private void addViewToBillData(View rootView){
        txtViewName = rootView.findViewById(R.id.view_name);
        txtViewMobileNo = rootView.findViewById(R.id.view_phone);
        txtViewConsumerNo = rootView.findViewById(R.id.view_consumer_no);
        txtViewSection = rootView.findViewById(R.id.view_section);
        txtViewBillNo = rootView.findViewById(R.id.view_bill_);
        txtViewAmount = rootView.findViewById(R.id.view_amount);
        txtViewAccountNo = rootView.findViewById(R.id.view_ac_number);
    }
    private void showAlertDialogue(String message){
        if ( getActivity() != null ){
            AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setMessage(message);
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("Ok", (dialog, which) ->
                //Action
                dialog.dismiss()

           );
            alertDialogBuilder.show();
        }
    }
    private void showAlertDialogue(String title, String message, final int status){
        if ( getContext() == null )
            return;
        if(status ==1){

            new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(title)
                    .setContentText(message)
                    .setConfirmClickListener(sDialog -> {
                        Fragment fragment = new HomeFragment();
                        FragmentManager fragmentManager = getFragmentManager();
                        if ( fragmentManager != null ){
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.container, fragment);
                            fragmentTransaction.addToBackStack("Kseb");
                            fragmentTransaction.commit();
                        }
                        sDialog.dismiss();
                    })
                    .show();
        }
        else{

            new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(title)
                    .setContentText(message)
                    .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation)
                    .show();
        }
    }

    private void getAccountNumber(){
        SettingsModel settingsModel = SettingsDAO.getInstance().getDetails();
        if ( settingsModel != null )
            settingAccountNumber(settingsModel.customerId);
    }
    private void settingAccountNumber(String customerId){
        //noinspection ObjectEqualsNull
        CommonUtilities.transactionActivitySetAccountNumber(customerId, accountNumberSelector, getActivity());

    }
    private void proceedToPay() {
        if ( getContext() == null )
            return;
        String tempMobileNoErrorMessage;
        String tempConsumerNameErrorMessage;
        String tempAmountErrorMessage;
        String tempBillNoErrorMessage;
        String tempConsumerNoErrroMessage;

        int validationCount = 0;
        tempStringMobileNumber = autoCompleMobileNumber.getText().toString();
        tempStringConsumerName = autoCompleConsumerName.getText().toString();
        tempStringConsumerNo = autoCompleConsumerNo.getText().toString();
        tempStringBillNo = edtTxtBillNo.getText().toString();
        tempStringAmount = edtTxtAmount.getText().toString();
        tempStringAccountNo = accountNumberSelector.getSelectedItem().toString();

        tempMobileNoErrorMessage = Validation.mobileNumberValidator(tempStringMobileNumber);
        tempConsumerNameErrorMessage = Validation.nameValidation(tempStringConsumerName);
        tempAmountErrorMessage = Validation.amount(tempStringAmount);
        tempConsumerNoErrroMessage = Validation.consumerNoValidation(tempStringConsumerNo);
        tempBillNoErrorMessage = Validation.billNoValidator(tempStringBillNo);

        this.txtMobileNoErrorMessage.setText(tempMobileNoErrorMessage);
        this.txtConsumerNameErrorMessage.setText(tempConsumerNameErrorMessage);
        this.txtAmountErrorMessage.setText(tempAmountErrorMessage);
        this.txtBillNoErrorMessage.setText(tempBillNoErrorMessage);
        txtConsumerNoErrorMessage.setText(tempConsumerNoErrroMessage);
        autoCompletesetLineColor(autoCompleConsumerName, R.color.text_tertary);
        autoCompletesetLineColor(autoCompleConsumerNo, R.color.text_tertary);
        autoCompletesetLineColor(autoCompleMobileNumber, R.color.text_tertary);
        editTextSetLineColor(edtTxtAmount, R.color.text_tertary);
        editTextSetLineColor(edtTxtBillNo, R.color.text_tertary);

        if(!tempConsumerNameErrorMessage.equals("")){
            validationCount++;

            autoCompletesetLineColor(autoCompleConsumerName, R.color.error);
        }
        if(!tempMobileNoErrorMessage.equals("")){
            validationCount++;
            autoCompletesetLineColor(autoCompleMobileNumber, R.color.error);
        }
        if(!tempConsumerNoErrroMessage.equals("")){
            validationCount++;
            autoCompletesetLineColor(autoCompleConsumerNo, R.color.error);
        }
        if(!tempAmountErrorMessage.equals("")){
            validationCount++;
            editTextSetLineColor(edtTxtAmount, R.color.error);
        }
        if(!tempBillNoErrorMessage.equals("")){
            validationCount++;
            editTextSetLineColor(edtTxtBillNo, R.color.error);
        }
        if(!Validation.sectionCodeValidator(sectionCode).equals("")){
            validationCount++;
            txtSectionName.setTextColor( ContextCompat.getColor( getContext(), R.color.error ) );
        }

        if(validationCount == 0){
            hideFormShowDetails();
        }

        txtViewMobileNo.setText(tempStringMobileNumber);
        txtViewName.setText(tempStringConsumerName);
        txtViewConsumerNo.setText(tempStringConsumerNo);
        txtViewSection.setText(tempStringSectionList);
        txtViewBillNo.setText(tempStringBillNo);
        txtViewAmount.setText(tempStringAmount);
        txtViewAccountNo.setText(tempStringAccountNo);
    }

    @Override
    public void onClick(View view){
        int id = view.getId();
        switch (id){
            case R.id.proceedToPay:
                proceedToPay();
                break;
            case R.id.edit:
                hideDetailsShowForm();
                break;
            case R.id.pay:
                pay();
                break;
            case R.id.clear_all:
                clearAll();
                break;
            case R.id.lnr_lyt_select_section:
                requestForSearchList();
                break;
            default:
                break;

        }
    }
    private void requestForSearchList(){
        Intent intent = new Intent( getContext(), KsebSectionSelectionActivity.class );
        startActivityForResult( intent, REQUEST_SELECT_SECTION );
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if ( requestCode == REQUEST_SELECT_SECTION && resultCode == RESULT_OK ){
            Bundle bundle = data.getExtras();
            try{
                if ( bundle != null ){
                    SectionDetails sectionDetails = bundle.getParcelable( getString( R.string.kseb_section_list ) );
                    if ( sectionDetails != null ){
                        String tempDisplaySection = sectionDetails.getSectionName() + '(' + sectionDetails.getSectionCode() + ')';
                        txtSectionName.setText( tempDisplaySection );
                        sectionCode = sectionDetails.getSectionCode();
                        tempStringSectionList = tempDisplaySection;
                    }
                }
                else {
                    Toast.makeText( getContext(), "Error occured", Toast.LENGTH_SHORT ).show();
                }
            }catch ( Exception e ){
                if ( IScoreApplication.DEBUG )
                    Log.e("exc section ", e.toString() );
            }
        }
    }

    private void hideFormShowDetails(){
        mLinearForm.setVisibility(View.GONE);

    }
    private void hideDetailsShowForm(){
        mLinearForm.setVisibility(View.VISIBLE);
    }


    private void clearAll(){
        Context context = getContext();
        if ( context == null )
            return;
        autoCompleConsumerName.setText("");
        edtTxtBillNo.setText("");
        edtTxtAmount.setText("");
        autoCompleMobileNumber.setText("");
        autoCompleConsumerNo.setText("");
        sectionCode = "";
        txtSectionName.setText( getString( R.string.select_section_name ));
        txtSectionName.setTextColor( ContextCompat.getColor( context, R.color.black_75_per ));
        txtMobileNoErrorMessage.setText("");
        txtConsumerNameErrorMessage.setText("");
        txtConsumerNoErrorMessage.setText("");
        txtBillNoErrorMessage.setText("");
        txtAmountErrorMessage.setText("");

    }
    private void addAdapterToAutoComplteView(View rootView, int id, String column){
        if ( getContext() == null )
            return;
        ArrayAdapter<String> adapter;
        AutoCompleteTextView actv;
        if ( getContext() != null ){

                adapter = new ArrayAdapter<>
                        (getContext(), android.R.layout.select_dialog_item, KsebBillDAO.getInstance().getListFromDb(column));
                actv= rootView.findViewById(id);
                actv.setThreshold(1);
                actv.setAdapter(adapter);
            }
            adapter = new ArrayAdapter<>
                    (getContext(), android.R.layout.select_dialog_item, KsebBillDAO.getInstance().getListFromDb(column));
            actv= rootView.findViewById(id);
            actv.setThreshold(1);
            actv.setAdapter(adapter);

    }
    private void addTextChange(final AutoCompleteTextView autoCompleteTextView, final View view, final int id, final String colomn){
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addAdapterToAutoComplteView(view, id, colomn);
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Do nothing

            }
        });
    }



    private void autoCompletesetLineColor(AutoCompleteTextView layout, int idColor){
        if ( getContext() != null ){
            layout.getBackground().mutate().setColorFilter(ContextCompat.getColor(getContext(),
                    idColor), PorterDuff.Mode.SRC_ATOP);
        }
    }

    private void editTextSetLineColor(EditText editText, int idColor){
        if ( getContext() != null ){
            editText.getBackground().mutate().setColorFilter(ContextCompat.getColor(getContext(),
                    idColor), PorterDuff.Mode.SRC_ATOP);
        }
    }





    private void autoSelect(String value){
        KsebBillModel ksebBillModel = KsebBillDAO.getInstance().getRow("consumer_name",value);
        autoCompleConsumerName.setText(ksebBillModel.consumerName); autoCompleMobileNumber.setText(ksebBillModel.mobileNo); autoCompleConsumerNo.setText(ksebBillModel.consumerNo);
    }
    private void pay(){
        if ( getContext() == null )
            return;

        mSweetAlertDialog = new SweetAlertDialog( getContext(), SweetAlertDialog.PROGRESS_TYPE );
        mSweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        mSweetAlertDialog.setTitleText( "Loading" );
        mSweetAlertDialog.setCancelable( false );
        mSweetAlertDialog.show();

        getObservable(  )
                .subscribeOn( Schedulers.io() )
                .observeOn( AndroidSchedulers.mainThread() )
                .subscribe(  getObserver() );
    }
    private Observable< String > getObservable( ){
        return Observable.fromCallable(this::payKsebBill);
    }

    private Observer< String > getObserver(  ){
        return  new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                //Do nothing
            }

            @Override
            public void onNext(String result ) {
                processResponse( result );

            }

            @Override
            public void onError(Throwable e) {
                //Do nothing
            }

            @Override
            public void onComplete() {
                //Do nothing
                mSweetAlertDialog.dismiss();
            }
        };
    }
    private void processResponse(String response){
        try{
            KsebRechargeStatus ksebRechargeStatus = new Gson().fromJson( response, KsebRechargeStatus.class );
            if ( ksebRechargeStatus.getStatusCode() > 0 ){
                String message = "Kseb bill payment is on pending.\n";
                ArrayList<KeyValuePair> keyValuePairs = new ArrayList<>();
                KeyValuePair keyValuePair = new KeyValuePair();
                keyValuePair.setKey("A/c.");
                keyValuePair.setValue( ksebRechargeStatus.getAccNo() );
                keyValuePairs.add( keyValuePair );
                keyValuePair = new KeyValuePair();
                keyValuePair.setKey("Amount");
                keyValuePair.setValue( ksebRechargeStatus.getAmount() );
                keyValuePairs.add( keyValuePair );
                keyValuePair = new KeyValuePair();
                keyValuePair.setKey("Ref.No");
                keyValuePair.setValue( ksebRechargeStatus.getRefId() );
                keyValuePairs.add( keyValuePair );
                getFragmentManager().beginTransaction().replace( R.id.container, AlertMessageFragment.getInstance( keyValuePairs  ,"Pending", message,
                        true, false ) ).commit();

            }else if ( ksebRechargeStatus.getStatusCode()< 0 ){
                if ( ksebRechargeStatus.getStatusCode() == -72 ){
                    String message =  "You have no sufficient balance on your account\n";
                    ArrayList<KeyValuePair> keyValuePairs = new ArrayList<>();
                    KeyValuePair keyValuePair = new KeyValuePair();
                    keyValuePairs.add( keyValuePair );
                    getFragmentManager().beginTransaction().replace( R.id.container, AlertMessageFragment.getInstance( keyValuePairs  ,"Failed", message,
                            false, false ) ).commit();


                }else if ( ksebRechargeStatus.getStatusCode() == -55 ){
                    String message =   "Your transaction is already processed.";
                    ArrayList<KeyValuePair> keyValuePairs = new ArrayList<>();
                    KeyValuePair keyValuePair = new KeyValuePair();
                    keyValuePairs.add( keyValuePair );
                    getFragmentManager().beginTransaction().replace( R.id.container, AlertMessageFragment.getInstance( keyValuePairs  ,"Failed", message,
                            false, false ) ).commit();
                }else {
                    String message =   "Transaction Failed";
                    ArrayList<KeyValuePair> keyValuePairs = new ArrayList<>();
                    KeyValuePair keyValuePair = new KeyValuePair();
                    keyValuePairs.add( keyValuePair );
                    getFragmentManager().beginTransaction().replace( R.id.container, AlertMessageFragment.getInstance( keyValuePairs  ,"Failed", message,
                            false, false ) ).commit();
                }
//                String message = "Kseb bill payment is  failed.\n";
//                ArrayList<KeyValuePair> keyValuePairs = new ArrayList<>();
//
//                getFragmentManager().beginTransaction().replace( R.id.container, AlertMessageFragment.getInstance( keyValuePairs  ,"Failed", message,
//                        false, false ) ).commit();
            }
        }catch ( Exception e ){
            Toast.makeText( getContext(), "Error occured", Toast.LENGTH_LONG ).show();
        }
        /*try{
            int resultResponse = Integer.parseInt(response.trim());
            if(resultResponse > 0){
                showAlertDialogue(getString(R.string.app_name), "Kseb bill payment is on pending.\n"+"Your reference no:"+response, 1);
                KsebBillDAO.getInstance().insertValues(tempStringConsumerName, tempStringMobileNumber, tempStringConsumerNo, tempStringSectionList,
                        "", "");
            }
            else if(resultResponse < 0){
                if(resultResponse == -72){
                    showAlertDialogue(getString(R.string.app_name), "You have no sufficient balance on your account", 2);
                }
                else if(resultResponse == -55){
                    showAlertDialogue(getString(R.string.app_name), "Your transaction is already processed.\n Please try after some time", 2);
                }
                else
                    showAlertDialogue(getString(R.string.app_name), "Transaction Failed", 2);
            }
        }catch (Exception e){
            if(IScoreApplication.DEBUG) Log.e("Response parse error" , response);
            showAlertDialogue(getString(R.string.app_name), IScoreApplication.SERVICE_NOT_AVAILABLE, 2);
        }*/
    }
    private String payKsebBill(){
        String module;
        String pin;
        String  response = "";
        String  url;
        AccountInfo accountInfo;
        pin = UserCredentialDAO.getInstance().getLoginCredential().pin;

        //extracting account number
        try{

            String extractedAccNo = tempStringAccountNo;
            extractedAccNo = extractedAccNo.
                        /*replace(extractedAccNo.substring(extractedAccNo.indexOf(" (")+1, extractedAccNo.indexOf(") ")+1), "")*/
                                replace(extractedAccNo.substring(extractedAccNo.indexOf(" (")+1, extractedAccNo.indexOf(')')+1), "");
            extractedAccNo = extractedAccNo.replace(" ","");
            accountInfo = PBAccountInfoDAO.getInstance().getAccountInfo(extractedAccNo);
            module = accountInfo.accountTypeShort;

            SharedPreferences pref =getActivity().getApplicationContext().getSharedPreferences(Config.SHARED_PREF7, 0);
            String BASE_URL=pref.getString("baseurl", null);
            url =  BASE_URL+ "/api/MV3"+"/KSEBPaymentRequest?ConsumerName="+
                    IScoreApplication.encodedUrl(IScoreApplication.encryptStart(tempStringConsumerName)) +"&MobileNo="+
                    IScoreApplication.encodedUrl(IScoreApplication.encryptStart(tempStringMobileNumber))+"&ConsumerNo="+
                    IScoreApplication.encodedUrl(IScoreApplication.encryptStart(tempStringConsumerNo))+ "&SectionList="+
                    IScoreApplication.encodedUrl(IScoreApplication.encryptStart(sectionCode)) +"&BillNo="+
                    IScoreApplication.encodedUrl(IScoreApplication.encryptStart(tempStringBillNo))+ "&Amount="+
                    IScoreApplication.encodedUrl(IScoreApplication.encryptStart(tempStringAmount)) + "&AccountNo="+
                    IScoreApplication.encodedUrl(IScoreApplication.encryptStart(extractedAccNo)) + "&Module="+
                    IScoreApplication.encodedUrl(IScoreApplication.encryptStart(module)) + "&Pin="+
                    IScoreApplication.encodedUrl(IScoreApplication.encryptStart(pin));
            response = ConnectionUtil.getResponse(url);
            return response;
        }catch (Exception e){
            if(IScoreApplication.DEBUG) Log.e("ksebexcetpion", e.toString());
        }
        return response;
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
