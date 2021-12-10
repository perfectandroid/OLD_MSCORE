package com.creativethoughts.iscore.money_transfer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;

import com.creativethoughts.iscore.AddSenderReceiverActivity;
import com.creativethoughts.iscore.IScoreApplication;
import com.creativethoughts.iscore.R;
import com.creativethoughts.iscore.TransactionOTPFragment;
import com.creativethoughts.iscore.adapters.SenderReceiverSpinnerAdapter;
import com.creativethoughts.iscore.db.dao.PBAccountInfoDAO;
import com.creativethoughts.iscore.db.dao.SettingsDAO;
import com.creativethoughts.iscore.db.dao.UserCredentialDAO;
import com.creativethoughts.iscore.db.dao.UserDetailsDAO;
import com.creativethoughts.iscore.db.dao.model.AccountInfo;
import com.creativethoughts.iscore.db.dao.model.SettingsModel;
import com.creativethoughts.iscore.db.dao.model.UserCredential;
import com.creativethoughts.iscore.db.dao.model.UserDetails;
import com.creativethoughts.iscore.model.SenderReceiver;
import com.creativethoughts.iscore.utility.CommonUtilities;
import com.creativethoughts.iscore.utility.ConnectionUtil;
import com.creativethoughts.iscore.utility.DialogUtil;
import com.creativethoughts.iscore.utility.NetworkUtil;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoneyTransferFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = MoneyTransferFragment.class.getSimpleName();
    private final ArrayList<SenderReceiver> mSenderReceivers = new ArrayList<>();
    private Button mBtnSubmit;
    private Spinner mAccountSpinner;
    private AppCompatEditText mAmountEt;
    private AppCompatEditText mMessageEt;
    private AppCompatEditText mPin;
    private ProgressDialog mProgressDialog;
    private Spinner mSenderSpinner;
    private Spinner mReceiverSpinner;
    private String mOtpResendLink;
    private boolean mCanLoadSenderReceiver = false;

    private LinearLayout mLnrAnimatorContainer;
    private RelativeLayout mRltvError;
    private TextView mTxtError;


    public MoneyTransferFragment() {
        // Required empty public constructor
    }

    public static MoneyTransferFragment newInstance() {

        return new MoneyTransferFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_money_transfer, container, false);

        mSenderSpinner = view.findViewById(R.id.sender_spinner);
        mReceiverSpinner = view.findViewById(R.id.receiver_spinner);

        mAccountSpinner = view.findViewById(R.id.spn_account_num);

        TextView mAddNewSender = view.findViewById(R.id.add_new_sender);
        TextView mAddNewReceiver = view.findViewById(R.id.add_new_receiver);

        mAmountEt = view.findViewById(R.id.amount);
        mMessageEt = view.findViewById(R.id.message);
        mPin = view.findViewById( R.id.mpin );

        mLnrAnimatorContainer = view.findViewById( R.id.linear_animation_container );
        mRltvError = view.findViewById( R.id.rltv_error );
        mTxtError  = view.findViewById( R.id.txt_error );

        mAddNewSender.setPaintFlags(mAddNewSender.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        mAddNewReceiver.setPaintFlags(mAddNewSender.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        mAddNewSender.setOnClickListener(this);
        mAddNewReceiver.setOnClickListener(this);

        setAccountNumber();

        mBtnSubmit = view.findViewById(R.id.btn_submit);
        mBtnSubmit.setOnClickListener(this);

        Button mBtnForgotMpin = view.findViewById(R.id.btn_forgot_mpin);
        mBtnForgotMpin.setOnClickListener( this );

        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new SenderReceiverAsyncTask().execute();
    }

    private void setSenderSpinner() {
        Activity activity = getActivity();
        if ( activity == null )
            return;
        final SenderReceiverSpinnerAdapter senderAdapter = new SenderReceiverSpinnerAdapter( activity );
        senderAdapter.setIsSender(true);
        final SenderReceiverSpinnerAdapter receiverAdapter = new SenderReceiverSpinnerAdapter( activity );
        receiverAdapter.setIsSender(false);

        ArrayList<SenderReceiver> senders = new ArrayList<>();
        ArrayList<SenderReceiver> receivers = new ArrayList<>();

        final SenderReceiver defaultSelect = new SenderReceiver();
        defaultSelect.senderName = "Select";
        defaultSelect.fkSenderId = -100;

        senders.add(defaultSelect);
        receivers.add(defaultSelect);

        for (int i = 0; i < mSenderReceivers.size(); i++) {
            final SenderReceiver senderReceiver = mSenderReceivers.get(i);

            if (senderReceiver == null) {
                continue;
            }

            if (senderReceiver.mode == 1) {
                senders.add(senderReceiver);
            } else {
                receivers.add(senderReceiver);
            }
        }

        senderAdapter.addItems(senders);
        receiverAdapter.addItems(receivers);

        mSenderSpinner.setAdapter(senderAdapter);
        mReceiverSpinner.setAdapter(receiverAdapter);

        mSenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                SenderReceiver senderRec = (SenderReceiver) senderAdapter.getItem(position);

                ArrayList<SenderReceiver> newSenders = new ArrayList<>();

                newSenders.add(defaultSelect);
                for (int i = 0; i < mSenderReceivers.size(); i++) {
                    final SenderReceiver senderReceiver = mSenderReceivers.get(i);

                    if (senderReceiver == null) {
                        continue;
                    }

                    if (senderReceiver.mode == 2 &&  senderRec.userId == senderReceiver.fkSenderId) {
                        newSenders.add(senderReceiver);
                    }
                }

                receiverAdapter.addItems(newSenders);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        if (mCanLoadSenderReceiver) {
            mCanLoadSenderReceiver = false;

            new SenderReceiverAsyncTask().execute();
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
        if ( customerId != null )
            CommonUtilities.transactionActivitySetAccountNumber(customerId, mAccountSpinner, getActivity());
    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if ( getContext() == null ){
            return;
        }

        switch (id) {
            case R.id.add_new_sender:
                mCanLoadSenderReceiver = true;
                AddSenderReceiverActivity.openActivity(getActivity(), true);
                break;
            case R.id.add_new_receiver:
                mCanLoadSenderReceiver = true;
                AddSenderReceiverActivity.openActivity(getActivity(), false);
                break;
            case R.id.btn_submit:

                if (isValid()) {
                    if (NetworkUtil.isOnline()) {
                        final String amount = mAmountEt.getText().toString();
                        final SenderReceiver receiverObj = ((SenderReceiver) mReceiverSpinner.getSelectedItem());

                        new SweetAlertDialog(getContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                                .setTitleText(getResources().getString(R.string.app_name))
                                .setContentText("Do you want to Transfer amount INR " + amount + " to A/C no " + receiverObj.receiverAccountno + "(" +receiverObj.senderName +") ?")
                                .setCancelText("No")
                                .setConfirmText("Yes")
                                .showCancelButton(true)
                                .setCustomImage(R.drawable.aappicon)
                                .setCancelClickListener(SweetAlertDialog::cancel)
                                .setConfirmClickListener(sDialog -> {
                                    sDialog.dismissWithAnimation();
                                    final String accountNumber = mAccountSpinner.getSelectedItem().toString();


                                    String message = mMessageEt.getText().toString();

                                    SenderReceiver senderObj = ((SenderReceiver) mSenderSpinner.getSelectedItem());


                                    String sender = String.valueOf(senderObj.userId);

                                    String receiver = String.valueOf(receiverObj.userId);

                                    String mPinString = mPin.getText().toString().trim();

                                    new MoneyTransferAsyncTask(accountNumber, sender, receiver, amount, message, mPinString).execute();
                                })
                                .show();
                    } else {
                        DialogUtil.showAlert(getActivity(),
                                "Network is currently unavailable. Please try again later.");
                    }
                }
                break;
            case R.id.btn_forgot_mpin:
                forgotMpin();
            break;
            default:
                break;
        }
    }
    private void forgotMpin(){
        SenderReceiver senderObj = ((SenderReceiver) mSenderSpinner.getSelectedItem());
        if ( senderObj.fkSenderId == -100 ){
            Toast.makeText(getActivity(), "Please select sender", Toast.LENGTH_LONG).show();
            return;
        }
        String sender = String.valueOf(senderObj.userId);
        new ChangeMpinAsync(sender).execute();
    }

    private void showToast(String value) {
        Toast.makeText(getActivity(), value, Toast.LENGTH_SHORT).show();
    }

    private boolean isValid() {

        SenderReceiver sender = ((SenderReceiver) mSenderSpinner.getSelectedItem());

        if (sender == null) {
            showToast("Please add minimum one sender");
            return false;
        }

        if(sender.fkSenderId == -100) {
            showToast("Please select sender");

            return false;
        }

        SenderReceiver receiver = ((SenderReceiver) mReceiverSpinner.getSelectedItem());

        if (receiver == null) {
            showToast("Please add minimum one receiver");
            return false;
        }

        if(receiver.fkSenderId == -100) {
            showToast("Please select receiver");

            return false;
        }

        String amount = mAmountEt.getText().toString();


        if (TextUtils.isEmpty(amount)) {
            mAmountEt.setError("Please enter the amount");
            return false;
        }
        double amt;
        try{
            amt = Double.parseDouble(amount);
        }catch (Exception e){
            mAmountEt.setError("Invalid format");
            return false;
        }

        if(amt < 1) {
            mAmountEt.setError("Please enter the amount");
            return false;
        }

        mAmountEt.setError(null);

        String mPinString = mPin.getText().toString();
        if ( mPinString.trim().length() == 0 ){
            mPin.setError("Please enter the M-PIN");
            return false;
        }


        return true;
    }

    private String transferMoney(String accountNo, String sender, String receiver, String amount, String message, String mPinString) {

        try {



            UserCredential loginCredential = UserCredentialDAO.getInstance().getLoginCredential();

            UserDetails user = UserDetailsDAO.getInstance().getUserDetail();

            String custId = user.customerId;


            /*Extract account number*/
            accountNo = accountNo.replace(accountNo.substring(accountNo.indexOf(" (")+1, accountNo.indexOf(")")+1), "");
            accountNo = accountNo.replace(" ","");
            AccountInfo accountInfo = PBAccountInfoDAO.getInstance().getAccountInfo(accountNo);
            String accountType = accountInfo.accountTypeShort;
            /*End of Extract account number*/

            String url =
                    CommonUtilities.getUrl() +
                            "/MoneyTransferPayment?senderid=" + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(sender.trim()))
                            + "&receiverid=" + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(receiver.trim()))
                            + "&IDCustomer=" + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(custId))
                            + "&amount=" + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(amount.trim()))
                            + "&Messages=" + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(message.trim()))
                            + "&AccountNo=" + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(accountNo))
                            + "&Module=" + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(accountType ))
                    + "&Pin=" + IScoreApplication.encodedUrl(IScoreApplication.encryptStart(loginCredential.pin))
                    +"&MPIN="+ IScoreApplication.encodedUrl(mPinString );
            mOtpResendLink = url;
            if (IScoreApplication.DEBUG)
                Log.d(TAG, "url : " + url);

            return ConnectionUtil.getResponse(url);


        } catch (Exception e) {
            if ( IScoreApplication.DEBUG ){
                Log.d("payment_error", e.toString() );
            }
            return e.toString();
        }

    }



    private class SenderReceiverAsyncTask extends AsyncTask<String, android.R.integer, ArrayList<SenderReceiver>> {


        @Override
        protected void onPreExecute() {
            mBtnSubmit.setEnabled(false);
            mProgressDialog = ProgressDialog
                    .show(getActivity(), "", "Fetching Sender and Receiver...");
        }

        @Override
        protected ArrayList<SenderReceiver> doInBackground(String... params) {

            UserDetails user = UserDetailsDAO.getInstance().getUserDetail();

            String custId = user.customerId;

            String url ;
            try {
                url = CommonUtilities.getUrl() +
                        "/GenerateSenderReceiverList?ID_Customer=" +
                        IScoreApplication.encodedUrl(IScoreApplication.encryptStart(custId));
            } catch (Exception e) {
                url = CommonUtilities.getUrl() +
                        "/GenerateSenderReceiverList?";

            }


            String strResponse = ConnectionUtil.getResponse(url);


            if (!TextUtils.isEmpty(strResponse)) {

                if (!IScoreApplication.containAnyKnownException(strResponse)) {

                    try {
                        JSONObject jsonObject = new JSONObject(strResponse);

                        JSONArray jsonArray = jsonObject.optJSONArray("senderreciverlist");

                        if (jsonArray != null) {
                            ArrayList<SenderReceiver> senderReceivers = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject senRecObj = jsonArray.optJSONObject(i);

                                if (senRecObj == null) {
                                    continue;
                                }

                                SenderReceiver senderReceiver = new SenderReceiver();

                                senderReceiver.userId = senRecObj.optLong("UserID");
                                senderReceiver.fkSenderId = senRecObj.optLong("FK_SenderID");
                                senderReceiver.senderName = senRecObj.optString("SenderName");
                                senderReceiver.senderMobile = senRecObj.optString("SenderMobile");
                                senderReceiver.receiverAccountno = senRecObj.optString("ReceiverAccountno");

                                String modeStr = senRecObj.optString("Mode");

                                if (!TextUtils.isEmpty(modeStr)) {
                                    modeStr = modeStr.trim();

                                    if (TextUtils.isDigitsOnly(modeStr)) {
                                        senderReceiver.mode = Integer.parseInt(modeStr);
                                    } else {
                                        continue;
                                    }
                                } else {
                                    continue;
                                }

                                senderReceivers.add(senderReceiver);
                            }

                            return senderReceivers;
                        }
                    } catch (JSONException e) {
                       if ( IScoreApplication.DEBUG )
                           Log.e("Ex", e.toString() );
                    }
                }else{
                    int flag= IScoreApplication.getFlagException(strResponse);
                    ArrayList<SenderReceiver> senderReceivers = new ArrayList<>();
                    SenderReceiver senderReceiver = new SenderReceiver();
                    senderReceiver.checkError =flag;
                    return senderReceivers;
                }
            }else{

                ArrayList<SenderReceiver> senderReceivers = new ArrayList<>();
                SenderReceiver senderReceiver = new SenderReceiver();
                senderReceiver.checkError = -50800;
                return senderReceivers;

            }

            return null;
        }

        private SenderReceiverAsyncTask() {

        }

        @Override
        protected void onPostExecute(ArrayList<SenderReceiver> result) {
            mBtnSubmit.setEnabled(true);
            mProgressDialog.dismiss();


            mSenderReceivers.clear();

            //noinspection StatementWithEmptyBody
            if ( (result == null || result.isEmpty()) && getContext() != null ) {
                /*new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops...")
                        .setContentText("No list found. Please add sender")
                        .show();*/
                showErrorAnimation( "No list found. Please add sender" );
            } else {
                if (result.get(0).checkError != -50800) {
                    if (IScoreApplication.checkPermissionIemi(result.get(0).checkError, getActivity())) {
                        mSenderReceivers.addAll(result);
                    }
                }else {
                    DialogUtil.showAlert(getActivity(),
                            IScoreApplication.MSG_EXCEPTION_NETWORK);
                }
            }
            setSenderSpinner();
        }

        private void showErrorAnimation( String message ){
            if ( Build.VERSION.SDK_INT > 18 ){
                TransitionManager.beginDelayedTransition( mLnrAnimatorContainer );
            }

            mTxtError.setText( message );
            mRltvError.setVisibility( View.VISIBLE  );

            new CountDownTimer( 5000, 1000){
                public void onTick( long milisUntilFinished ){
                    //Do nothing
                }

                @Override
                public void onFinish() {
                    if ( Build.VERSION.SDK_INT > 18 ){
                        TransitionManager.beginDelayedTransition( mLnrAnimatorContainer );
                    }
                    mRltvError.setVisibility( View.GONE );
                }
            }.start();
        }
    }

    private class MoneyTransferAsyncTask extends AsyncTask<String, android.R.integer, String> {
        private final String mAccNo;
        private final String mSender;
        private final String mReceiver;
        private final String mAmount;
        private final String mMessage;
        private final String mPinString;

        private MoneyTransferAsyncTask(String accountNo, String sender, String receiver, String amount, String message, String mPinString) {
            mAccNo = accountNo;
            mSender = sender;
            mReceiver = receiver;
            mAmount = amount;
            mMessage = message;
            this.mPinString = mPinString;
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog
                    .show(getActivity(), "", "Transferring Amount...");
            mBtnSubmit.setEnabled(false);
        }

        @Override
        protected String doInBackground(String... params) {

            return transferMoney(mAccNo, mSender, mReceiver, mAmount, mMessage, mPinString);
        }

        @Override
        protected void onPostExecute(String result) {

            if ( getContext() == null ){
                if ( IScoreApplication.DEBUG )
                    Log.d("getcontext_error", "No context ");
                return;
            }

            mProgressDialog.dismiss();
            mBtnSubmit.setEnabled(true);

            mAmountEt.setText("");
            mMessageEt.setText("");
            mPin.setText("");

            setAccountNumber();
            setSenderSpinner();

            if ( result.isEmpty() || result.equals( IScoreApplication.SERVICE_NOT_AVAILABLE ) ){
                new SweetAlertDialog( getContext(), SweetAlertDialog.ERROR_TYPE )
                        .setTitleText( "Oops....!" )
                        .setContentText( IScoreApplication.SERVICE_NOT_AVAILABLE )
                        .show();
            }

            try{
                Gson gson = new Gson();
                MoneyTransferResponseModel moneyTransferResponseModel = gson.fromJson( result, MoneyTransferResponseModel.class );
                if ( moneyTransferResponseModel.getStatusCode() != null && moneyTransferResponseModel.getStatusCode().equals("200") &&
                        !moneyTransferResponseModel.getOtpRefNo().equals("0")){
                    TransactionOTPFragment.openTransactionOTP(getActivity(), mSender, mReceiver,
                            moneyTransferResponseModel.getTransactionId(), new AddSenderReceiverResponseModel(),
                            moneyTransferResponseModel.getOtpRefNo(), mOtpResendLink);
                }
                else if ( moneyTransferResponseModel.getStatusCode() != null && moneyTransferResponseModel.getStatusCode().equals("200") &&
                        moneyTransferResponseModel.getOtpRefNo().equals("0")){
                    new SweetAlertDialog( getContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE )
                            .setTitleText( moneyTransferResponseModel.getStatus() )
                            .setContentText( moneyTransferResponseModel.getMessage() )
                            .setCustomImage(R.mipmap.thumbs_up)
                            .show();

                }
                else if ( moneyTransferResponseModel.getStatusCode().equals("500")){
                    new SweetAlertDialog( getContext(), SweetAlertDialog.ERROR_TYPE )
                            .setTitleText( moneyTransferResponseModel.getStatus() )
                            .setContentText( moneyTransferResponseModel.getMessage() )
                            .show();
                }
                else {
                    new SweetAlertDialog( getContext(), SweetAlertDialog.ERROR_TYPE )
                            .setTitleText( "Oops....!" )
                            .setContentText( " Something went wrong " )
                            .show();
                }

            }catch ( Exception ignored){

            }


        }

    }

    private class ChangeMpinAsync extends AsyncTask<String, android.R.integer, String> {
        private final String mSenderId;
        private ChangeMpinAsync(String senderId){
            mSenderId = senderId;
        }
        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog
                    .show(getActivity(), "", "Resending M-Pin...");
        }

        @Override
        protected String doInBackground(String... params) {

            return resendingOtp( mSenderId );
        }

        @Override
        protected void onPostExecute(String response ) {
            mProgressDialog.dismiss();

            if (response.trim().equals("1"))
                Toast.makeText(getActivity(), "M-Pin is resend to your mobile", Toast.LENGTH_LONG).show();
        }
        private String resendingOtp(String senderId){

            try{
                String url = CommonUtilities.getUrl() +
                        "/MTResendMPIN?senderid="+senderId;
                return ConnectionUtil.getResponse(url);

            }catch ( Exception ignored){

            }

            return "";

        }

    }
}
