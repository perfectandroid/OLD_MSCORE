package com.creativethoughts.iscore;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;

import com.creativethoughts.iscore.Helper.Config;
import com.creativethoughts.iscore.money_transfer.AddSenderReceiverResponseModel;
import com.creativethoughts.iscore.money_transfer.MoneyTransferResponseModel;
import com.creativethoughts.iscore.utility.CommonUtilities;
import com.creativethoughts.iscore.utility.ConnectionUtil;
import com.creativethoughts.iscore.utility.DialogUtil;
import com.creativethoughts.iscore.utility.NetworkUtil;
import com.google.gson.Gson;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransactionOTPFragment extends Fragment implements View.OnClickListener {


    private static final String BUNDLE_DATA_SENDER_ID = "sender_id";
    private static final String BUNDLE_DATA_RECEIVER_ID = "receiver_id";
    private static final String BUNDLE_DATA_TRANSACTION_ID = "transaction_id";
    private static final String BUNDLE_DATA_IS_TRANSACTION = "is_transaction";
    private static final String BUNDLE_OTP_REFERENCE = "otpReference";
    private static final String BUNDLE_MOBILE = "mobile";
    private static final String BUNDLE_RESEND_LINK = "resend_link";
    private static final String BUNDLE_IS_SENDER = "is_sender";
    private static final String BUNDLE_SENDER_RECEIVER_OBJ = "sender_reciever_obj";
    protected Button button;
    private String mSenderId;
    private String mReceiverId;
    private String mTransactionId;
    private boolean mIsForTransaction;
    private boolean mIsSender;
    private AppCompatEditText mOTPEt;
    private String mOtpReferenceNo;
    private String mMobileNo;
    private ProgressDialog mProgressDialog;
    private String mResendLink;
    private AddSenderReceiverResponseModel mAddSenderReceiverResponseModel;

    public TransactionOTPFragment() {

    }

    public static void openTransactionOTP(Context context, String senderId, String receiverId, String transactionId,
                                          AddSenderReceiverResponseModel addSenderResponseModel, String otpReferenceNo, String resendLink) {
        addSenderResponseModel.setOtpRefNo( otpReferenceNo);
        openOtp(context, senderId, receiverId, transactionId, true, addSenderResponseModel, resendLink, false );
    }

    public static void openSenderOTP(Context context, /*String senderId*/ AddSenderReceiverResponseModel addSenderResponseModel, String resendLink,
                                     boolean isSender) {
        openOtp(context, addSenderResponseModel.getIdSender() , "", "", false, addSenderResponseModel, resendLink, isSender);

    }

    private static void openOtp(Context context, String senderId, String receiverId, String transactionId, boolean isForTransaction,
                                AddSenderReceiverResponseModel addSenderResponseModel, String resendLink, boolean isSender) {
        Intent intent = new Intent(context, TransactionOTPActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_DATA_SENDER_ID, senderId);
        bundle.putString(BUNDLE_DATA_RECEIVER_ID, receiverId);
        bundle.putString(BUNDLE_DATA_TRANSACTION_ID, transactionId);
        bundle.putBoolean(BUNDLE_DATA_IS_TRANSACTION, isForTransaction);
        bundle.putString(BUNDLE_OTP_REFERENCE, addSenderResponseModel.getOtpRefNo() );
        bundle.putString(BUNDLE_MOBILE, addSenderResponseModel.getMobileNo() );
        bundle.putString(BUNDLE_RESEND_LINK, resendLink);
        bundle.putBoolean( BUNDLE_IS_SENDER, isSender);
        bundle.putParcelable(BUNDLE_SENDER_RECEIVER_OBJ, addSenderResponseModel);
        intent.putExtras(bundle);

        context.startActivity(intent);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Bundle bundle = getArguments();

        if (bundle != null) {

            mIsForTransaction = bundle.getBoolean(BUNDLE_DATA_IS_TRANSACTION, false);
            mSenderId = bundle.getString(BUNDLE_DATA_SENDER_ID);
            mReceiverId = bundle.getString(BUNDLE_DATA_RECEIVER_ID);
            mTransactionId = bundle.getString(BUNDLE_DATA_TRANSACTION_ID);
            mOtpReferenceNo = bundle.getString(BUNDLE_OTP_REFERENCE );
            mMobileNo = bundle.getString( BUNDLE_MOBILE );
            mResendLink = bundle.getString( BUNDLE_RESEND_LINK );
            mIsSender = bundle.getBoolean( BUNDLE_IS_SENDER );
            mAddSenderReceiverResponseModel = bundle.getParcelable(BUNDLE_SENDER_RECEIVER_OBJ);
        } else {
            activity.finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_ot, container, false);

        mOTPEt =   view.findViewById(R.id.otp);

        button =   view.findViewById(R.id.btn_submit);

        button.setOnClickListener( this );
        Button btnResendOtp  =   view.findViewById( R.id.btn_resend_otp );
        btnResendOtp.setOnClickListener( this );
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.btn_submit:

                if (isValid()) {
                    if (NetworkUtil.isOnline()) {
                        button.setEnabled(false);
                        String otp = mOTPEt.getText().toString();
                        new confirmOtpAsyncTask(otp).execute();
                    } else {
                        DialogUtil.showAlert(getActivity(),
                                "Network is currently unavailable. Please try again later.");
                    }
                }
                break;
            case R.id.btn_resend_otp:{
                new ResendAsyncTask(mResendLink).execute();
            }
        }
    }

    private boolean isValid() {
        String otp = mOTPEt.getText().toString();

        if (TextUtils.isEmpty(otp)) {
            mOTPEt.setError("Please enter the OTP");

            return false;
        }

        mOTPEt.setError(null);

        return true;
    }

    public String confirmOtp(String otp) {


        try {

            final String url;

            SharedPreferences pref =getActivity().getApplicationContext().getSharedPreferences(Config.SHARED_PREF7, 0);
            String BASE_URL=pref.getString("baseurl", null);
            if (mIsForTransaction) {
                url =
                        BASE_URL+ "/api/MV3" + "/MTVerifyPaymentOTP?senderid=" +
                                IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mSenderId))
                                + "&receiverid=" + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mReceiverId))
                                + "&transcationID=" + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mTransactionId))
                                + "&OTP=" + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(otp.trim()))+
                                "&otpRefNo="+IScoreApplication.encodedUrl( mOtpReferenceNo );
            } else {
                if ( mIsSender ){
                    url =
                            BASE_URL+ "/api/MV3" + "/MTVerifySenderOTP?senderid=" +
                                    IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mSenderId))
                                    + "&OTP=" + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(otp.trim()))+"&otpRefNo="+
                                    IScoreApplication.encodedUrl( IScoreApplication.encryptStart( mOtpReferenceNo.trim() ))+
                                    "&mobile="+IScoreApplication.encodedUrl(IScoreApplication.encryptStart( mMobileNo ));
                }
                else {
                    url =

                            BASE_URL+ "/api/MV3" + "/MTVerifyReceiverOTP?senderid=" +
                                    IScoreApplication.encodedUrl(IScoreApplication.encryptStart(mAddSenderReceiverResponseModel.getIdSender()))+
                                    "&receiverid="+IScoreApplication.encodedUrl( IScoreApplication.encryptStart(mAddSenderReceiverResponseModel.getIdReceiver()))
                                    + "&OTP=" + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(otp.trim()))+"&otpRefNo="+
                                    IScoreApplication.encodedUrl( IScoreApplication.encryptStart( mOtpReferenceNo )) ;
                }

            }



            return   ConnectionUtil.getResponse(url);



        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf( IScoreApplication.FLAG_NETWORK_EXCEPTION );
    }

    private void moneyTransactionOtpVerify( String response ){
        try {
            Gson gson = new Gson();
            MoneyTransferResponseModel moneyTransferResponseModel = gson.fromJson( response, MoneyTransferResponseModel.class );
            if ( moneyTransferResponseModel.getStatusCode().equals( "200" ) ){
                new SweetAlertDialog(getContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                        .setTitleText( moneyTransferResponseModel.getStatus() )
                        .setContentText( moneyTransferResponseModel.getMessage())
                        .setConfirmText("Ok!")
                        .showCancelButton(true)
                        .setCustomImage(R.mipmap.thumbs_up)
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismissWithAnimation();
                                getActivity().finish();
                            }
                        }).show();
            }
            else {
                new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE )
                        .setTitleText( "Oops....!" )
                        .setContentText( " Something went wrong " )
                        .show();
            }
        }catch ( Exception ignored ){
            new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE )
                    .setTitleText( "Oops....!" )
                    .setContentText( " Something went wrong " )
                    .show();
        }
    }

    private void addOrRecieverOtpVerificationResultProcessing(String response){
        Gson gson = new Gson();
        try {

            AddSenderReceiverResponseModel addSenderReceiverResponseModel = gson.fromJson( response, AddSenderReceiverResponseModel.class );
            if ( addSenderReceiverResponseModel.getStatusCode() != null  && addSenderReceiverResponseModel.getStatusCode().equals("200")){
                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE);
                sweetAlertDialog
                        .setCustomImage(R.drawable.aappicon)
                        .setConfirmText("Ok!")
                        .showCancelButton(true)
                        .setTitleText(addSenderReceiverResponseModel.getStatus())
                        .setContentText( addSenderReceiverResponseModel.getMessage() )
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {

                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismissWithAnimation();
                                getActivity().finish();
                            }

                        });
                sweetAlertDialog.show();
            }
            else {
                new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                        .setCustomImage(R.drawable.aappicon)
                        .setConfirmText("Ok!")
                        .showCancelButton(true)
                        .setTitleText(addSenderReceiverResponseModel.getStatus())
                        .setContentText( addSenderReceiverResponseModel.getMessage() )
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismissWithAnimation();
                                getActivity().finish();
                            }
                        })
                        .show();
            }

        }catch ( Exception ignored ){
            new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE )
                    .setTitleText( "Oops....!" )
                    .setContentText( " Something went wrong " )
                    .show();
        }
    }

    private String resendOtp(String url){
        try{
            return ConnectionUtil.getResponse(url);
        }catch (Exception e){
            return String.valueOf( IScoreApplication.FLAG_NETWORK_EXCEPTION );
        }
    }

    class confirmOtpAsyncTask extends AsyncTask<String, android.R.integer, String > {
        private String mOtp;
        private confirmOtpAsyncTask(String otp) {
            mOtp = otp;
        }

        @Override
        protected void onPreExecute() {
            if (mIsForTransaction) {
                mProgressDialog = ProgressDialog
                        .show(getActivity(), "", "Transferring Amount...");

            }else{
                if ( mIsSender ){
                    mProgressDialog = ProgressDialog
                            .show(getActivity(), "", "Adding Sender...");

                }
                else {
                    mProgressDialog = ProgressDialog
                            .show(getActivity(), "", "Adding Receiver...");

                }

            }

        }

        @Override
        protected String doInBackground(String... params) {

            return confirmOtp(mOtp);
        }

        @Override
        protected void onPostExecute(String result) {
            button.setEnabled( true );
            mProgressDialog.dismiss();
            if ( mIsForTransaction ){
                moneyTransactionOtpVerify(result);
            }
            else {
                addOrRecieverOtpVerificationResultProcessing( result );
            }

        }


    }

    private class ResendAsyncTask extends AsyncTask< String, android.R.integer, String>{
        private String mResendUrl;
        private ResendAsyncTask(String resendUrl){
            mResendUrl = resendUrl;
        }
        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog
                    .show(getActivity(), "", "Talking to server...");
        }
        @Override
        protected String doInBackground(String... params) {

            return resendOtp( mResendUrl );
        }
        @Override
        protected void onPostExecute(String serverResponse ) {

            mProgressDialog.dismiss();
            mOtpReferenceNo = serverResponse.trim();

        }
    }
}
