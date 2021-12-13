package com.creativethoughts.iscore.neftrtgs;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.creativethoughts.iscore.Helper.Config;
import com.creativethoughts.iscore.IScoreApplication;
import com.creativethoughts.iscore.R;
import com.creativethoughts.iscore.custom_alert_dialogs.AlertMessageFragment;
import com.creativethoughts.iscore.custom_alert_dialogs.KeyValuePair;
import com.creativethoughts.iscore.db.dao.PBAccountInfoDAO;
import com.creativethoughts.iscore.db.dao.SettingsDAO;
import com.creativethoughts.iscore.db.dao.UserCredentialDAO;
import com.creativethoughts.iscore.db.dao.model.AccountInfo;
import com.creativethoughts.iscore.db.dao.model.SettingsModel;
import com.creativethoughts.iscore.db.dao.model.UserCredential;
import com.creativethoughts.iscore.otp.OtpFragment;
import com.creativethoughts.iscore.utility.CommonUtilities;
import com.creativethoughts.iscore.utility.DialogUtil;
import com.creativethoughts.iscore.utility.NetworkUtil;
import com.creativethoughts.iscore.utility.network.NetworkManager;
import com.creativethoughts.iscore.utility.network.ResponseManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.util.ArrayList;



public class NeftRtgsFragment extends Fragment implements View.OnClickListener {


    private EditText mEdtTxtBeneficiaryName;
    private EditText  mEdtTxtBeneficiaryAccNo;
    private EditText  mEdtTxtBeneficiaryConfirmAccNo;
    private EditText mEdtTxtIfscNo;
    private EditText  mEdtTxtAmount ;
    private Spinner mSpinnerAccountNo;
    private Button mBtnClear;
    private CheckBox mCheckSaveBeneficiary;
    private RelativeLayout mLinearParent;
    private ScrollView mScrollView;
    private int mModeNeftRtgs;
    private PaymentModel mPaymentModel;
    private static final String BENEFICIARY_DETAILS = "beneficiary details";
    private static final String MODE = "MODE";
    public NeftRtgsFragment( ) {
        //Do nothing
    }

    public static NeftRtgsFragment newInstance( String mode ) {
        Bundle bundle = new Bundle();
        bundle.putString( MODE, mode );
        NeftRtgsFragment neftRtgsFragment =  new NeftRtgsFragment();
        neftRtgsFragment.setArguments( bundle );
        return neftRtgsFragment;
    }
    public static NeftRtgsFragment newInstance( BeneficiaryDetailsModel beneficiaryDetailsModel, String mode ){
        Bundle bundle = new Bundle();
        bundle.putParcelable( BENEFICIARY_DETAILS, beneficiaryDetailsModel );
        bundle.putString( MODE, mode );
        NeftRtgsFragment neftRtgsFragment =  new NeftRtgsFragment();
        neftRtgsFragment.setArguments( bundle );
        return neftRtgsFragment;
    }
    private InputFilter inputFilterAccountNumber =   new InputFilter() {
        @Override
        public CharSequence filter( CharSequence source, int start, int end, Spanned dest, int dstart, int dend ) {
            String accNo = mEdtTxtBeneficiaryConfirmAccNo.getText().toString();
            /*if ( accNo.contains( "4" ) ){
                return "s";
            }*/
            return null;
        }
    };
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState ) {

        View view = inflater.inflate( R.layout.fragment_neft_rtgs, container, false );

        mEdtTxtBeneficiaryName              =   view.findViewById( R.id.edt_txt_neft_rtgs_benificiary_name );
        mEdtTxtBeneficiaryAccNo             =   view.findViewById( R.id.edt_txt_neft_rtgs_benificiary_acc_no );
        mEdtTxtBeneficiaryConfirmAccNo      =   view.findViewById( R.id.edt_txt_neft_rtgs_confirm_benificiary_acc_no );
        mEdtTxtAmount                       =   view.findViewById( R.id.edt_txt_neft_rtgs_amount );
        mEdtTxtIfscNo                       =   view.findViewById( R.id.edt_txt_neft_rtgs_ifsc_code );
        mSpinnerAccountNo                   =   view.findViewById( R.id.spinner_neft_rtgs_acc_no );
        mLinearParent                       =   view.findViewById( R.id.lnear_impes_rtgs_parent );
        mScrollView                         =   view.findViewById( R.id.scroll_view_rtgs_neft );
        mBtnClear                           =   view.findViewById( R.id.btn_neft_rtgs_clear );
        Button btnSubmit                    =   view.findViewById( R.id.btn_neft_rtgs_submit );
        TextView txtViewChooseBeneficiary   =   view.findViewById( R.id.txt_view__neft_rtgs_choose_benefeciary );
        TextView mTxtHeader = view.findViewById(R.id.txt_header);
        mCheckSaveBeneficiary               =   view.findViewById( R.id.chk_save_ben );
        mLinearParent.setOnClickListener( this );
        btnSubmit.setOnClickListener(this );
        mBtnClear.setOnClickListener(this );
        txtViewChooseBeneficiary.setOnClickListener(this );
        mEdtTxtBeneficiaryConfirmAccNo.setFilters( new InputFilter[]{ inputFilterAccountNumber } );
       try{
           Bundle bundle = getArguments();
           assert bundle != null;

           String title = bundle.getString( MODE );
           switch (title) {
               case IScoreApplication.OTHER_FUND_TRANSFER_MODE_NEFT:
                   mModeNeftRtgs = 2;
                   break;
               case IScoreApplication.OTHER_FUND_TRANSFER_MODE_RTGS:
                   mModeNeftRtgs = 1;
                   break;
               case IScoreApplication.OTHER_FUND_TRANSFER_MODE_IMPS:
                   mModeNeftRtgs = 3;
                   break;
               default:
                   mModeNeftRtgs = 0;
                   break;
           }
           mTxtHeader.setText( title );

           BeneficiaryDetailsModel beneficiaryDetailsModel = bundle.getParcelable(BENEFICIARY_DETAILS);

           txtViewChooseBeneficiary.setVisibility( View.GONE );
           if ( beneficiaryDetailsModel != null ){
               mCheckSaveBeneficiary.setVisibility( View.GONE );
           }
           mBtnClear.setVisibility( View.GONE );
           mBtnClear.setOnClickListener( null );

           //assert beneficiaryDetailsModel != null;
           mEdtTxtBeneficiaryName.setText( beneficiaryDetailsModel.getBeneficiaryName( ) );
           mEdtTxtIfscNo.setText( beneficiaryDetailsModel.getBeneficiaryIfsc( )  );
           mEdtTxtBeneficiaryAccNo.setText( beneficiaryDetailsModel.getBeneficiaryAccNo( ) );
           mEdtTxtBeneficiaryConfirmAccNo.setText( beneficiaryDetailsModel.getBeneficiaryAccNo( )  );

           disableField();

        }catch ( NullPointerException e ){
            //Do nothing
       }

        setAccountNo(  );
        return view;
    }

    @Override
    public void onClick(View view ){
        int id = view.getId( );

        switch (id ){
            case R.id.btn_neft_rtgs_clear:
                clearAll( );
                break;
            case R.id.btn_neft_rtgs_submit:
                submit(  );
                break;

            case R.id.txt_view__neft_rtgs_choose_benefeciary:
                break;
            default:
                break;
        }
    }
    private boolean isValid( ){
        changeBackground(mEdtTxtBeneficiaryName, false );
        changeBackground(mEdtTxtBeneficiaryAccNo, false );
        changeBackground(mEdtTxtBeneficiaryConfirmAccNo, false );
        changeBackground(mEdtTxtIfscNo,false );

       if (mEdtTxtBeneficiaryName.getText( ).toString( ).isEmpty( ) ){
           showSnackBar("Please enter Beneficiary name" );
           changeBackground(mEdtTxtBeneficiaryName, true );
           focusScrollView(mEdtTxtBeneficiaryName );
           return false;
       }
        String tempBeneficiaryAccNo = mEdtTxtBeneficiaryAccNo.getText( ).toString( );
        String tempBeneficiaryConfirmAccNo = mEdtTxtBeneficiaryConfirmAccNo.getText( ).toString( );

        if (tempBeneficiaryAccNo.isEmpty( )    ){
            showSnackBar("Beneficiary account number is required" );
            changeBackground(mEdtTxtBeneficiaryAccNo, true );
            changeBackground(mEdtTxtBeneficiaryConfirmAccNo, true );
            focusScrollView(mEdtTxtBeneficiaryAccNo );
            return false;
        }
        if ( tempBeneficiaryConfirmAccNo.isEmpty( )   ){
            showSnackBar("Confirm Beneficiary account number is required" );
            changeBackground(mEdtTxtBeneficiaryConfirmAccNo, true );
            focusScrollView(mEdtTxtBeneficiaryConfirmAccNo );
            return false;
        }

        if (! tempBeneficiaryAccNo.matches("^\\d+$" ) ){
            showSnackBar("Invalid beneficiary account numbers " );
            changeBackground(mEdtTxtBeneficiaryAccNo, true );
            focusScrollView(mEdtTxtBeneficiaryAccNo );
            return false;
        }
        if ( ( !tempBeneficiaryAccNo.equals(tempBeneficiaryConfirmAccNo )  )  ){
            showSnackBar("Beneficiary account numbers don't match" );
            changeBackground(mEdtTxtBeneficiaryConfirmAccNo, true );
            focusScrollView(mEdtTxtBeneficiaryConfirmAccNo );
            return false;
        }
        try {
            String ifscNumber = mEdtTxtIfscNo.getText( ).toString( );
            if (ifscNumber.isEmpty( ) ){
                showSnackBar("Please enter IFSC" );
                changeBackground(mEdtTxtIfscNo, true );
                focusScrollView(mEdtTxtIfscNo );
                return false;
            }


        }catch (Exception e ){

            if (IScoreApplication.DEBUG ) Log.d("ifscParseExc", e.toString( ) );
            focusScrollView(mEdtTxtIfscNo );
            return false;

        }
        try {
            String amount = mEdtTxtAmount.getText( ).toString( );
            int tempAmount = Integer.parseInt(amount );
            if (tempAmount < 0  ){
                showSnackBar("Invalid amount " );
                changeBackground(mEdtTxtAmount, true );
                focusScrollView(mEdtTxtAmount );
                return false;
            }
        }catch (Exception e ){
            showSnackBar("Invalid amount " );
            changeBackground(mEdtTxtAmount, true );
            focusScrollView(mEdtTxtAmount );
            if (IScoreApplication.DEBUG ) Log.d("AmountExc", e.toString( ) );
            return false;

        }

        return true;
    }
    private void changeBackground(EditText editText, boolean errorStatus ){
        if (errorStatus )
            editText.setBackgroundResource( R.drawable.custom_edt_txt_error_back_ground );
        else
            editText.setBackgroundResource( R.drawable.custom_edt_txt_account_border );
    }
    private void focusScrollView(final View view ){
        mScrollView.post(( ) -> mScrollView.scrollTo(0, view.getBottom( ) ) );
    }

    private void submit( ){
        if (!NetworkUtil.isOnline( ) ){
            DialogUtil.showAlert(getActivity( ),
                    "Network is currently unavailable. Please try again later." );
            return;
        }
        UserCredential loginCredential = UserCredentialDAO.getInstance( ).getLoginCredential( );
        if (isValid( ) ){
            String tempAccNo = mSpinnerAccountNo.getSelectedItem( ).toString( );
            tempAccNo = tempAccNo.replace(tempAccNo.substring(tempAccNo.indexOf(" (" )+1, tempAccNo.indexOf( ')' )+1 ), "" );
            tempAccNo = tempAccNo.replace(" ","" );

            AccountInfo accountInfo = PBAccountInfoDAO.getInstance( ).getAccountInfo(tempAccNo );
            final String module = accountInfo.accountTypeShort;

            final String accNo = tempAccNo;

            final String beneficiaryName = mEdtTxtBeneficiaryName.getText( ).toString( );
            final String ifscNumber = mEdtTxtIfscNo.getText( ).toString( );
            final String amount = mEdtTxtAmount.getText( ).toString( );
            final String beneficiaryAccNo = mEdtTxtBeneficiaryAccNo.getText( ).toString( );

            PaymentModel paymentModel = new PaymentModel( );
            paymentModel.setAccNo( accNo  );
            paymentModel.setBeneficiaryAccNo( beneficiaryAccNo  );
            paymentModel.setBeneficiaryName( beneficiaryName  );
            paymentModel.setModule( module  );
            paymentModel.setIfsc(ifscNumber );
            paymentModel.setAmount( amount  );
            paymentModel.setMode(Integer.toString(mModeNeftRtgs ) );
            paymentModel.setPin( loginCredential.pin  );


            if ( mCheckSaveBeneficiary.isChecked() ){
                paymentModel.setBeneficiaryAdd("1" );
            }else
                paymentModel.setBeneficiaryAdd("0" );
            startPayment( paymentModel );


        }

    }
    private void startPayment( PaymentModel paymentModel ){
        NetworkManager.getInstance().connector(prepareUrlForPayment(paymentModel), new ResponseManager() {
            @Override
            public void onSuccess(String result) {
                processResult(result);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Oops. Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }, getActivity(), "Talking to server");
    }

    private String prepareUrlForPayment( PaymentModel paymentModel  ){
        mPaymentModel = paymentModel;
        SharedPreferences pref =getActivity().getApplicationContext().getSharedPreferences(Config.SHARED_PREF7, 0);
        String BASE_URL=pref.getString("baseurl", null);
        String url =  BASE_URL+ "/api/MV3";
        try{

            url += "/NEFTRTGSPayment?AccountNo="+IScoreApplication.encodedUrl(IScoreApplication.encryptStart( paymentModel.getAccNo( )  ) )+
                    "&Module="+IScoreApplication.encodedUrl(IScoreApplication.encryptStart( paymentModel.getModule( )  ) )+
                    "&BeneName="+IScoreApplication.encodedUrl(IScoreApplication.encryptStart( paymentModel.getBeneficiaryName( )  ) )+
                    "&BeneIFSC="+IScoreApplication.encodedUrl(IScoreApplication.encryptStart( paymentModel.getIfsc( )  ) )+
                    "&BeneAccountNumber="+IScoreApplication.encodedUrl(IScoreApplication.encryptStart( paymentModel.getBeneficiaryAccNo( )  ) )+
                    "&amount="+IScoreApplication.encodedUrl(IScoreApplication.encryptStart( paymentModel.getAmount( )  ) )+
                    "&EftType="+IScoreApplication.encodedUrl(IScoreApplication.encryptStart( Integer.toString(  mModeNeftRtgs )  ) )+
                    "&BeneAdd="+IScoreApplication.encodedUrl(IScoreApplication.encryptStart( paymentModel.getBeneficiaryAdd( ) ) )+
                    "&Pin=" + IScoreApplication.encodedUrl(IScoreApplication.encryptStart( paymentModel.getPin( )  ) ) +
                    "&OTPRef=&OTPCode=";

        }catch ( Exception e  ){
            if ( IScoreApplication.DEBUG  )
                Log.e("encryption error", e.toString( )  );
        }
        return url;
    }

    private void showSnackBar(String message ){
        Snackbar snackbar = Snackbar.make(mLinearParent, message, Snackbar.LENGTH_SHORT );
        View snackBarView = snackbar.getView( );
        snackBarView.setBackgroundResource( R.color.red_error_snack_bar );
        snackbar.show( );
    }
    private void clearAll( ){
        mEdtTxtBeneficiaryName.setText(null );
        mEdtTxtBeneficiaryAccNo.setText(null );
        mEdtTxtBeneficiaryConfirmAccNo.setText(null );
        mEdtTxtAmount.setText(null );
        mEdtTxtIfscNo.setText(null );
        mBtnClear.setVisibility( View.GONE );
        if ( mCheckSaveBeneficiary.isChecked() )
            mCheckSaveBeneficiary.toggle();

    }
    private void setAccountNo(  ){
        SettingsModel settingsModel = SettingsDAO.getInstance( ).getDetails( );
        if (settingsModel.customerId.isEmpty( ) )
            return;

        CommonUtilities.transactionActivitySetAccountNumber( settingsModel.customerId, mSpinnerAccountNo, getActivity( ) );
    }






    private void processResult(String result ){
        clearAll( );
        if ( getContext( ) == null  )
            return;

        Gson gson = new Gson( );

        try{
            NeftRtgsOtpResponseModel neftRtgsOtpResponseModel = gson.fromJson( result, NeftRtgsOtpResponseModel.class  );

            if ( getFragmentManager( ) == null || mPaymentModel == null  )
                return;
            if ( neftRtgsOtpResponseModel.getStatusCode( ) == 1 && !neftRtgsOtpResponseModel.getOtpRefNo( ).isEmpty( )  ){

                OtpFragment otpFragment = OtpFragment.newInstance( mPaymentModel, neftRtgsOtpResponseModel  );
                FragmentTransaction fragmentTransaction = getFragmentManager( ).beginTransaction( );
                fragmentTransaction.replace( R.id.container, otpFragment  );
                fragmentTransaction.addToBackStack("aha");
                fragmentTransaction.commit( );

            }
            else if ( neftRtgsOtpResponseModel.getStatusCode( ) < 0  ){

                alertMessage( "Oops...!", new ArrayList<>(), neftRtgsOtpResponseModel.getMessage(), false, false);
            }else if ( neftRtgsOtpResponseModel.getStatusCode( ) == 3  ){
                FragmentTransaction fragmentTransaction = getFragmentManager( ).beginTransaction( );
                fragmentTransaction.remove( this  ).commit( );
            }else{

                alertMessage( "Oops...!", new ArrayList<>(), neftRtgsOtpResponseModel.getMessage(), false, false);
            }

        }catch ( Exception ignored  ){
            if ( IScoreApplication.DEBUG  )
                Log.e("erro", ignored.toString( )  );
        }

    }
    private void alertMessage( String title, ArrayList<KeyValuePair> keyValueList, String message , boolean isHappy, boolean isBackButtonEnabled ){
        getFragmentManager().beginTransaction().replace( R.id.container, AlertMessageFragment.getInstance( keyValueList  ,title, message,
                isHappy, isBackButtonEnabled ) ).commit();
    }
    private void disableField(){
        mEdtTxtBeneficiaryName.setEnabled( false );
        mEdtTxtIfscNo.setEnabled( false );
        mEdtTxtBeneficiaryAccNo.setEnabled( false );
        mEdtTxtBeneficiaryConfirmAccNo.setEnabled( false );
    }

}
