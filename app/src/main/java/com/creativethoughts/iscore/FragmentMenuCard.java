package com.creativethoughts.iscore;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.creativethoughts.iscore.Helper.Config;
import com.creativethoughts.iscore.Retrofit.APIInterface;
import com.creativethoughts.iscore.db.dao.DynamicMenuDao;
import com.creativethoughts.iscore.db.dao.UserDetailsDAO;
import com.creativethoughts.iscore.db.dao.model.DynamicMenuDetails;
import com.creativethoughts.iscore.db.dao.model.UserDetails;
import com.creativethoughts.iscore.money_transfer.MoneyTransferFragment;
import com.creativethoughts.iscore.neftrtgs.OtherBankFundTransferServiceChooserFragment;
import com.creativethoughts.iscore.utility.DialogUtil;
import com.creativethoughts.iscore.utility.NetworkUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.creativethoughts.iscore.Helper.Common;;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class FragmentMenuCard extends Fragment implements View.OnClickListener {

    private static boolean isFirstLaunch = true;

    Fragment fragment = null;
    private static final String argHideRecharge = "";
    private static final String argHideMoneyTransfer = "";
    private String mParamHideRecharge, mParamHideMoneyTransfer, cusid,cusno;
    private ImageView imgv_customerimg, imRecharge;
    private HorizontalScrollView mHorizontalScrollVew;
    private TextView txtNeftRtgs,tvRecharge;
    private android.app.AlertDialog.Builder builder;
    private DynamicMenuDetails dynamicMenuDetails;
    CircleImageView profileIma, profileImag,profileImg, profileIm, profileImage, profileImga;
    public FragmentMenuCard() {
    }

    android.app.AlertDialog alertDialog=null;
    android.app.AlertDialog alertDialog1=null;
    android.app.AlertDialog alertDialog2=null;
   /* LinearLayout llOtherbank;
    LinearLayout llOwnbank;
    LinearLayout llPassbook;
    LinearLayout llAccountsummary;
    LinearLayout llNotice;
    LinearLayout  llStandingInstruction;
    LinearLayout llSearch;

    LinearLayout llPrepaid;
    LinearLayout llDTH;
    LinearLayout llLandLine;
    LinearLayout llPostpaid;
    LinearLayout llDatacard;
    LinearLayout llKSEB;*/


    CardView llOtherbank;
    CardView llOwnbank;


    CardView txtPassbook;
    CardView txtAccountsummary;
    CardView txtNotice;
    CardView txtStandingInstruction;
    CardView txtSearch;

    CardView llPrepaid;
    CardView llDTH;
    CardView llLandLine;
    CardView llPostpaid;
    CardView llDatacard;
    CardView llKSEB;

   /* Fragment fragment = null;*/

    boolean isFABOpen;
    FloatingActionButton fab,fab1,fab2,fab3;

    // TODO: Rename and change types and number of parameters
    public  static Fragment newInstance(String hideRecharge, String hideMoneyTransfer ) {
        FragmentMenuCard fragment = new FragmentMenuCard();
        Bundle args = new Bundle();
        if (!hideRecharge.equals("EMPTY") || !hideMoneyTransfer.equals("EMPTY")){
            args.putString(argHideRecharge, hideRecharge);
            args.putString(argHideMoneyTransfer, hideMoneyTransfer);
        }else {
            args.putString(argHideRecharge,"");
            args.putString(argHideMoneyTransfer, "");
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            
            mParamHideRecharge = getArguments().getString(argHideRecharge);
            mParamHideMoneyTransfer = getArguments().getString(argHideMoneyTransfer);
        }
     }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_menu_card, container, false);

        txtNeftRtgs             = view.findViewById( R.id.txt_neft_rtgs );

        TextView tvName         = view.findViewById(R.id.tvName);
        TextView tvCustId       = view.findViewById(R.id.tvCustId);
        TextView tvAddress      = view.findViewById(R.id.tvAddress);
        TextView tvPhone        = view.findViewById(R.id.tvPhone);
        TextView tvLoginDate    = view.findViewById(R.id.tvLoginDate);
        TextView tvRecharge    = view.findViewById(R.id.tvRecharge);


        UserDetails userDetails = UserDetailsDAO.getInstance().getUserDetail();
        cusid = userDetails.customerId;
        cusno = userDetails.userCustomerNo;
        tvName.setText(userDetails.userCustomerName);
        tvCustId.setText("( Customer Id : " + cusno + " )");
        tvAddress.setText(userDetails.userCustomerAddress1+","+userDetails.userCustomerAddress2);
        tvPhone.setText(userDetails.userMobileNo);


        SharedPreferences pref = getContext().getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        tvLoginDate.setText("Last Login : "+pref.getString("logintime", null));


        imgv_customerimg = (ImageView) view.findViewById(R.id.imgv_customerimg);
        imRecharge = (ImageView) view.findViewById(R.id.imRecharge);


        SharedPreferences sperf = getContext().getApplicationContext().getSharedPreferences(Config.SHARED_PREF1, 0);
        if(sperf.getString("custimage", null) == null||sperf.getString("custimage", null).isEmpty() ) {
            getCustomerImage();
        }else{
            if(!sperf.getString("custimage", null).isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(sperf.getString("custimage", null), Base64.DEFAULT);
                    ByteArrayToBitmap(decodedString);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    decodedByte.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    Glide.with(getActivity())
                            .load(stream.toByteArray())
                            .placeholder(R.drawable.person)
                            .error(R.drawable.person)
                            .into(imgv_customerimg);
                }catch (Exception e){e.printStackTrace();}
            }
        }



        fab                    =  view.findViewById(R.id.fab);
        fab1                   = view.findViewById(R.id.fab1);
        fab2                   = view.findViewById(R.id.fab2);
        fab3                   = view.findViewById(R.id.fab3);
        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);
        fab3.setOnClickListener(this);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFABOpen){
                    showFABMenu();
                }else{
                    closeFABMenu();
                }
            }
        });


        LinearLayout llAcccount         = view.findViewById(R.id.llAcccount);
        LinearLayout llRecharge         = view.findViewById(R.id.llRecharge);
        LinearLayout llFundtransfer     = view.findViewById(R.id.llFundtransfer);
        LinearLayout llDashboard        = view.findViewById(R.id.llDashboard);
        LinearLayout llVirtualcard      = view.findViewById(R.id.llVirtualcard);
        LinearLayout llOthers           = view.findViewById(R.id.llOthers);
        llVirtualcard.setOnClickListener(this);
        llRecharge.setOnClickListener(this);
  /*      llDashboard.setOnClickListener(this);
        llOthers.setOnClickListener(this);*/
        llDashboard.setOnClickListener(v -> {

            Intent i=new Intent(getContext(),DashboardtabActivity.class);
            startActivity(i);

        });
        llOthers.setOnClickListener(v -> {

            Intent i=new Intent(getContext(),BankActivity.class);
            startActivity(i);
        });
        llFundtransfer.setOnClickListener(v ->
                moneyTransferPopup()
        );
        llAcccount.setOnClickListener(v -> accountDetailsPopup());
       /* llRecharge.setOnClickListener(v ->
                getRechargesubmenu()
        );*/

        mHorizontalScrollVew        =   view.findViewById(R.id.recharge_paybill_horizontal_scroll_view);
        ImageButton mRightButton    =   view.findViewById(R.id.right_button);
        ImageButton mLeftButton     =   view.findViewById(R.id.left_button);
        mRightButton.setOnClickListener(this);
        mLeftButton.setOnClickListener(this);

        //getCustomerImage();


        try {
            dynamicMenuDetails = DynamicMenuDao.getInstance().getMenuDetails();
            if ( IScoreApplication.decryptStart( dynamicMenuDetails.getRecharge()).equals("0") &&  IScoreApplication.decryptStart( dynamicMenuDetails.getKseb()).equals("0")){
                tvRecharge.setText("Profile");
                imRecharge.setImageResource(R.drawable.usersprofile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        activeProgressDialog();
        return view;

    }

    public Bitmap ByteArrayToBitmap(byte[] byteArray) {
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(byteArray);
        Bitmap bitmap = BitmapFactory.decodeStream(arrayInputStream);
        return bitmap;
    }


    protected void activeProgressDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Rate Us");
        dialog.setMessage("Do you love this app? Please rate us.");
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                final String appPackageName = getContext().getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            } });
        dialog.setCancelable(false);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() { dialog.show(); }
            }, /*2000*/(long) 2.592e+9);//30days delay
    }

    private void getRechargesubmenu() {
        try {
            dynamicMenuDetails = DynamicMenuDao.getInstance().getMenuDetails();
            if ( IScoreApplication.decryptStart( dynamicMenuDetails.getRecharge()).equals("0") &&  IScoreApplication.decryptStart( dynamicMenuDetails.getKseb()).equals("0")){
              /*  Intent i=new Intent(getContext(),ProfileActivity.class);
                startActivity(i);*/
                fragment = ProfileFragment.newInstance();
            }else {
                  rechargePopup();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goingTo(View view){
        int tempId = view.getId();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        assert activity != null;
        ActionBar actionBar = activity.getSupportActionBar();
        Fragment fragment = null;
        switch (tempId){
            case R.id.fab1:
                fragment = new MessagesFragment();
                assert actionBar != null;
                actionBar.setTitle("Message" );
                break;
            case R.id.fab2:
                fragment = new OffersFragment();
                assert actionBar != null;
                actionBar.setTitle("Offer" );
                break;
            case R.id.fab3:
                fragment = new DuedatesFragment();
                assert actionBar != null;
                actionBar.setTitle("Due Dates Calender" );
                break;
            case R.id.llRecharge:
                try {
                    dynamicMenuDetails = DynamicMenuDao.getInstance().getMenuDetails();
                    if ( IScoreApplication.decryptStart( dynamicMenuDetails.getRecharge()).equals("0") &&  IScoreApplication.decryptStart( dynamicMenuDetails.getKseb()).equals("0")){
                        fragment = new ProfileFragment();
                        assert actionBar != null;
                        actionBar.setTitle( "Profile" );
                    }else {
                        rechargePopup();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case R.id.account_info:
                fragment = new AccountInfoFragment();
                assert actionBar != null;
                actionBar.setTitle( getString(R.string.title_section1) );
                break;
            case R.id.pass_book:
                fragment = new HomeFragment();
                assert actionBar != null;
                actionBar.setTitle( "Passbook" );
                break;
            case R.id.searc_h:
                fragment = new SearchFragment();
                assert actionBar != null;
                actionBar.setTitle( getString( R.string.title_section2 ) );
                break;
            case R.id.llAccountsummary:
                fragment = new AccountInfoFragment();
                assert actionBar != null;
                actionBar.setTitle( getString(R.string.title_section1) );
                alertDialog1.dismiss();
                break;
            case R.id.llPassbook:
                fragment = new HomeFragment();
                assert actionBar != null;
                actionBar.setTitle( "Passbook" );
                alertDialog1.dismiss();
                break;
            case R.id.llSearch:
                fragment = new SearchFragment();
                assert actionBar != null;
                actionBar.setTitle( getString( R.string.title_section2 ) );
                alertDialog1.dismiss();
                break;
       /*     case R.id.llDashboard:
                fragment = new DashBoardFragment();
                assert actionBar != null;
                actionBar.setTitle( "Dash Board" );
                break;*/
         /*   case R.id.llOthers:
                fragment = new BankFragment();
                assert actionBar != null;
                actionBar.setTitle( "Branch Locations" );
                break;*/
            case R.id.mobile_prepaid:
                //noinspection AccessStaticViaInstance
                dynamicMenuDetails = DynamicMenuDao.getInstance().getMenuDetails();
                builder = new android.app.AlertDialog.Builder(getContext());
                try {
                    if (IScoreApplication.decryptStart(dynamicMenuDetails.getRecharge()).equals("0")) {
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                        alertDialog.setCancelable(true);
                        alertDialog.setTitle("No Access");
                        alertDialog.setMessage("No Access for Prepaid option!");
                        alertDialog.setIcon(R.drawable.ic_warning);
                        alertDialog.show();
                    } else {
                        fragment = new RechargeFragment().newInstance(0);
                        assert actionBar != null;
                        actionBar.setTitle(getString(R.string.title_prepaid));

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case R.id.llPrepaid:
                //noinspection AccessStaticViaInstance
                dynamicMenuDetails = DynamicMenuDao.getInstance().getMenuDetails();
                builder = new android.app.AlertDialog.Builder(getContext());
                try {
                    if (IScoreApplication.decryptStart(dynamicMenuDetails.getRecharge()).equals("0")) {
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                        alertDialog.setCancelable(true);
                        alertDialog.setTitle("No Access");
                        alertDialog.setMessage("No Access for Prepaid option!");
                        alertDialog.setIcon(R.drawable.ic_warning);
                        alertDialog.show();
                    } else {
                        fragment = new RechargeFragment().newInstance(0);
                        assert actionBar != null;
                        actionBar.setTitle( getString(R.string.title_prepaid) );
                        alertDialog2.dismiss();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case R.id.mobile_postpaid:
                //noinspection AccessStaticViaInstance
                dynamicMenuDetails = DynamicMenuDao.getInstance().getMenuDetails();
                builder = new android.app.AlertDialog.Builder(getContext());
                try {

                    if (IScoreApplication.decryptStart(dynamicMenuDetails.getRecharge()).equals("0")) {
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                        alertDialog.setCancelable(true);
                        alertDialog.setTitle("No Access");
                        alertDialog.setMessage("No Access for Postpaid option!");
                        alertDialog.setIcon(R.drawable.ic_warning);
                        alertDialog.show();
                    } else {
                        fragment = new RechargeFragment().newInstance(1);
                        assert actionBar != null;
                        actionBar.setTitle(getString(R.string.title_postpaid));
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case R.id.llPostpaid:
                //noinspection AccessStaticViaInstance
                dynamicMenuDetails = DynamicMenuDao.getInstance().getMenuDetails();
                builder = new android.app.AlertDialog.Builder(getContext());
                try {
                    if ( IScoreApplication.decryptStart( dynamicMenuDetails.getRecharge() ).equals("0")){
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                        alertDialog.setCancelable(true);
                        alertDialog.setTitle("No Access");
                        alertDialog.setMessage("No Access for Postpaid option!");
                        alertDialog.setIcon(R.drawable.ic_warning);
                        alertDialog.show();

                    }else {
                        fragment = new RechargeFragment().newInstance(1);
                        assert actionBar != null;
                        actionBar.setTitle( getString(R.string.title_postpaid) );
                        alertDialog2.dismiss();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case R.id.land_line:
                //noinspection AccessStaticViaInstance
                fragment = new RechargeFragment().newInstance(2);
                assert actionBar != null;
                actionBar.setTitle( getString(R.string.title_landline) );
                break;
            case R.id.llLandLine:
                //noinspection AccessStaticViaInstance
                dynamicMenuDetails = DynamicMenuDao.getInstance().getMenuDetails();
                builder = new android.app.AlertDialog.Builder(getContext());
                try {
                    if (IScoreApplication.decryptStart(dynamicMenuDetails.getRecharge()).equals("0")) {
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                        alertDialog.setCancelable(true);
                        alertDialog.setTitle("No Access");
                        alertDialog.setMessage("No Access for LandLine option!");
                        alertDialog.setIcon(R.drawable.ic_warning);
                        alertDialog.show();
                    } else {
                        fragment = new RechargeFragment().newInstance(2);
                        assert actionBar != null;
                        actionBar.setTitle( getString(R.string.title_landline) );
                        alertDialog2.dismiss();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

                break;
            case R.id.dt_h:
                fragment = new RechargeFragment().newInstance(3);
                assert actionBar != null;
                actionBar.setTitle( getString(R.string.title_DTH) );
                break;
            case R.id.llDTH:
                //noinspection AccessStaticViaInstance
                dynamicMenuDetails = DynamicMenuDao.getInstance().getMenuDetails();
                builder = new android.app.AlertDialog.Builder(getContext());
                try {
                    if (IScoreApplication.decryptStart(dynamicMenuDetails.getRecharge()).equals("0")) {
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                        alertDialog.setCancelable(true);
                        alertDialog.setTitle("No Access");
                        alertDialog.setMessage("No Access for DTH option!");
                        alertDialog.setIcon(R.drawable.ic_warning);
                        alertDialog.show();
                    } else {
                        fragment = new RechargeFragment().newInstance(3);
                        assert actionBar != null;
                        actionBar.setTitle( getString(R.string.title_DTH) );
                        alertDialog2.dismiss();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case R.id.imps_neft:
                fragment = new MoneyTransferFragment();
                assert actionBar != null;
                actionBar.setTitle( getString(R.string.transfer_title) );
                break;
            case R.id.data_card:
                fragment = new RechargeFragment().newInstance(4);
                assert actionBar != null;
                actionBar.setTitle( getString(R.string.data_card) );
                break;
            case R.id.llDatacard:
                //noinspection AccessStaticViaInstance
                dynamicMenuDetails = DynamicMenuDao.getInstance().getMenuDetails();
                builder = new android.app.AlertDialog.Builder(getContext());
                try {
                    if (IScoreApplication.decryptStart(dynamicMenuDetails.getRecharge()).equals("0")) {
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                        alertDialog.setCancelable(true);
                        alertDialog.setTitle("No Access");
                        alertDialog.setMessage("No Access for DataCard option!");
                        alertDialog.setIcon(R.drawable.ic_warning);
                        alertDialog.show();
                    } else {
                        fragment = new RechargeFragment().newInstance(4);
                        assert actionBar != null;
                        actionBar.setTitle( getString(R.string.data_card) );
                        alertDialog2.dismiss();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case R.id.fund_transfer:
                fragment = new FundTransferFragment().newInstance();
                assert actionBar != null;
                actionBar.setTitle(getString(R.string.fund_transfer_title));
                break;
            case R.id.llOwnbank:
                //noinspection AccessStaticViaInstance
                fragment = new FundTransferFragment().newInstance();
                assert actionBar != null;
                actionBar.setTitle(getString(R.string.fund_transfer_title));
                alertDialog.dismiss();
                break;
            case R.id.kse_b:
                //noinspection AccessStaticViaInstance
                dynamicMenuDetails = DynamicMenuDao.getInstance().getMenuDetails();
                builder = new android.app.AlertDialog.Builder(getContext());
                try {
                    if (IScoreApplication.decryptStart(dynamicMenuDetails.getKseb()).equals("0")) {
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                        alertDialog.setCancelable(true);
                        alertDialog.setTitle("No Access");
                        alertDialog.setMessage("No Access for KSEB option!");
                        alertDialog.setIcon(R.drawable.ic_warning);
                        alertDialog.show();
                    } else {
                        fragment = new KsebBillFragment().newInstance();
                        assert actionBar != null;
                        actionBar.setTitle(getString(R.string.kseb_sub_name_bill_status_on_drawer));
                        break;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            case R.id.llKSEB:
                //noinspection AccessStaticViaInstance
                dynamicMenuDetails = DynamicMenuDao.getInstance().getMenuDetails();
                builder = new android.app.AlertDialog.Builder(getContext());
                try {
                    if ( IScoreApplication.decryptStart( dynamicMenuDetails.getKseb()).equals("0") ){
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                        alertDialog.setCancelable(true);
                        alertDialog.setTitle("No Access");
                        alertDialog.setMessage("No Access for KSEB option!");
                        alertDialog.setIcon(R.drawable.ic_warning);
                        alertDialog.show();
                    }else {

                        fragment = new KsebBillFragment().newInstance();
                        assert actionBar != null;
                        actionBar.setTitle(getString(R.string.kseb_sub_name_bill_status_on_drawer));
                        alertDialog2.dismiss();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                break;
            case R.id.rtgs:
                //noinspection AccessStaticViaInstance
                fragment = /*new NeftRtgsFragment().newInstance()*/ /*new ListSavedBeneficiaryFragment()*/
                        new OtherBankFundTransferServiceChooserFragment();
                assert actionBar != null;
                actionBar.setTitle("IMPS/NEFT");
                break;
            case R.id.llOtherbank:
                //noinspection AccessStaticViaInstance
                fragment =  new OtherBankFundTransferServiceChooserFragment();
                assert actionBar != null;
                actionBar.setTitle("IMPS/NEFT");
                alertDialog.dismiss();
                break;
            case R.id.standing_i:
                fragment = new StandingInstructionFragment().newInstance();
                assert actionBar != null;
                actionBar.setTitle("Standing Instruction");
                break;
            case R.id.llStandingInstruction:
                fragment = new StandingInstructionFragment().newInstance();
                assert actionBar != null;
                actionBar.setTitle("Standing Instruction");
                alertDialog1.dismiss();
                break;
            case R.id.llVirtualcard:
                fragment=new VirtualcardFragment().newInstance();
                assert actionBar != null;
                actionBar.setTitle("Virtual Card");
                break;
            case R.id.intimation:
                fragment = new NotificationPostingFragment().newInstance();
                assert actionBar != null;
                actionBar.setTitle("Intimation");
                break;
            case R.id.llNotice:
                fragment = new NotificationPostingFragment().newInstance();
                assert actionBar != null;
                actionBar.setTitle("Intimation");
                alertDialog1.dismiss();
                break;
            case R.id.branchdetails:
                fragment = new StandingInstructionFragment().newInstance();
                assert actionBar != null;
                actionBar.setTitle("Branch Details");
                break;

        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment).commit();
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }

    }


    private void horizontalViewFocus(View view){
        int id = view.getId();
        switch (id){
            case R.id.left_button:
                mHorizontalScrollVew.fullScroll(HorizontalScrollView.FOCUS_LEFT);
                break;
            case R.id.right_button:
                mHorizontalScrollVew.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                break;
            default:
                break;
        }
    }
    @Override
    public void onClick(View view)
    {
        int id = view.getId();
        if ( id == R.id.left_button || id == R.id.right_button)
            horizontalViewFocus(view);
        else {
            goingTo(view);
        }
    }


    private void getCustomerImage() {
        SharedPreferences pref =getActivity().getApplicationContext().getSharedPreferences(Config.SHARED_PREF7, 0);
        String BASE_URL=pref.getString("baseurl", null);
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

                    requestObject1.put("FK_Customer",/*cusid*/IScoreApplication.encryptStart(cusid));
                    SharedPreferences bankkeypref =getActivity().getApplicationContext().getSharedPreferences(Config.SHARED_PREF9, 0);
                    String BankKey=bankkeypref.getString("bankkey", null);
                    SharedPreferences bankheaderpref =getActivity().getApplicationContext().getSharedPreferences(Config.SHARED_PREF11, 0);
                    String BankHeader=bankheaderpref.getString("bankheader", null);
                    requestObject1.put("BankKey",IScoreApplication.encryptStart(BankKey));
                    requestObject1.put("BankHeader",IScoreApplication.encryptStart(BankHeader));


                } catch (Exception e) {
                    e.printStackTrace();

                }
                RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), requestObject1.toString());
                Call<String> call = apiService.getImage(body);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        try {
                            Log.i("Imagedetails",response.body());
                            // Toast.makeText(getActivity(),response.body(),Toast.LENGTH_LONG).show();
                            JSONObject jObject = new JSONObject(response.body());
                            JSONObject jobjt = jObject.getJSONObject("CustomerImageDets");
                            String statuscode = jObject.getString("StatusCode");
                            if(statuscode.equals("0"))
                            {
                                SharedPreferences custimageSP = getContext().getApplicationContext().getSharedPreferences(Config.SHARED_PREF1, 0);
                                SharedPreferences.Editor custimageSPEditer = custimageSP.edit();
                                custimageSPEditer.putString("custimage", jobjt.getString("CusImage"));
                                custimageSPEditer.commit();
                                try{
                                byte[] decodedString = Base64.decode(jobjt.getString("CusImage"), Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                decodedByte.compress(Bitmap.CompressFormat.PNG, 0, stream);
                                    byte[] bitmapdata = stream.toByteArray();


                                Glide.with(getActivity())
                                        .load(/*stream.toByteArray()*/bitmapdata)
                                        .placeholder(R.drawable.person)
                                        .error(R.drawable.person)
                                        .into(imgv_customerimg);
                                }catch (Exception e){e.printStackTrace();}
                                //Toast.makeText(getActivity(),"Image found",Toast.LENGTH_LONG).show();
                            }
                            else  if(statuscode.equals("-1"))
                            {
                                //  Toast.makeText(getActivity(),"Statuscode -1 so no image found",Toast.LENGTH_LONG).show();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                    }
                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.i("Imagedetails","Something went wrong");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            DialogUtil.showAlert(getContext(),
                    "Network is currently unavailable. Please try again later.");
        }
    }

    private HostnameVerifier getHostnameVerifier() {
        return (hostname, session) -> true;
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
        SharedPreferences sslnamepref =getActivity().getApplicationContext().getSharedPreferences(Config.SHARED_PREF24, 0);
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


    private void moneyTransferPopup() {
        try {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
            LayoutInflater inflater1 = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater1.inflate(R.layout.submenu_popup, null);
            llOtherbank = layout.findViewById(R.id.llOtherbank);
            llOwnbank =  layout.findViewById(R.id.llOwnbank);
            profileImg =  layout.findViewById(R.id.profileImg);
            builder.setView(layout);
            alertDialog = builder.create();
            llOtherbank.setOnClickListener(this);
            llOwnbank.setOnClickListener(this);
            Window window = alertDialog.getWindow();
            window.setGravity(Gravity.CENTER);
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.show();

            dynamicMenuDetails = DynamicMenuDao.getInstance().getMenuDetails();

     /*       if ( !IScoreApplication.decryptStart( dynamicMenuDetails.getImps()).equals("1") &&
                    !IScoreApplication.decryptStart( dynamicMenuDetails.getNeft()).equals("1") &&
                    !IScoreApplication.decryptStart( dynamicMenuDetails.getRtgs()).equals("1") &&
                    ! IScoreApplication.decryptStart( dynamicMenuDetails.getOwnImps()).equals("1")){
                llOtherbank.setVisibility(View.GONE);
                profileImg.setVisibility(View.GONE);
            }*//*else if ( !IScoreApplication.decryptStart( dynamicMenuDetails.getImps()).equals("0") &&
                    !IScoreApplication.decryptStart( dynamicMenuDetails.getNeft()).equals("1") &&
                    !IScoreApplication.decryptStart( dynamicMenuDetails.getRtgs()).equals("1") &&
                    ! IScoreApplication.decryptStart( dynamicMenuDetails.getOwnImps()).equals("1")){
                llOtherbank.setVisibility(View.VISIBLE);
                profileImg.setVisibility(View.VISIBLE);
            }*//*else {
                llOtherbank.setVisibility(View.VISIBLE);
                profileImg.setVisibility(View.VISIBLE);

            }*/

            String tempRtgsNeft = IScoreApplication.decryptStart(dynamicMenuDetails.getRtgs() );
            if(tempRtgsNeft.equals("000")){
                if ( IScoreApplication.decryptStart(dynamicMenuDetails.getImps()).equals("0") ){
                    llOtherbank.setVisibility(View.GONE);
                    profileImg.setVisibility(View.GONE);
                }else
                    llOtherbank.setVisibility(View.VISIBLE);
                profileImg.setVisibility(View.VISIBLE);
            }else {
                llOtherbank.setVisibility(View.VISIBLE);
                profileImg.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void accountDetailsPopup() {
        try {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
            LayoutInflater inflater1 = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater1.inflate(R.layout.account_submenu_popup, null);
            txtPassbook = layout.findViewById(R.id.llPassbook);
            txtAccountsummary =  layout.findViewById(R.id.llAccountsummary);
            txtNotice =  layout.findViewById(R.id.llNotice);
            txtStandingInstruction =  layout.findViewById(R.id.llStandingInstruction);
            txtSearch=  layout.findViewById(R.id.llSearch);
            builder.setView(layout);
            alertDialog1 = builder.create();
            txtPassbook.setOnClickListener(this);
            txtAccountsummary.setOnClickListener(this);
            txtSearch.setOnClickListener(this);
            txtNotice.setOnClickListener(this);
            txtStandingInstruction.setOnClickListener(this);
            Window window = alertDialog1.getWindow();
            window.setGravity(Gravity.CENTER);
            alertDialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog1.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void rechargePopup() {
        try {

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
            LayoutInflater inflater1 = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout              =  inflater1.inflate(R.layout.recharge_submenu_popup, null);
            llPrepaid                =  layout.findViewById(R.id.llPrepaid);
            llDTH                    =  layout.findViewById(R.id.llDTH);
            llLandLine               =  layout.findViewById(R.id.llLandLine);
            llPostpaid               =  layout.findViewById(R.id.llPostpaid);
            llDatacard               =  layout.findViewById(R.id.llDatacard);
            llKSEB                   =  layout.findViewById(R.id.llKSEB);
            profileIma                   =  layout.findViewById(R.id.profileIma);
            profileImag                =  layout.findViewById(R.id.profileImag);
            profileImg               =  layout.findViewById(R.id.profileImg);
            profileIm     =  layout.findViewById(R.id.profileIm);
            profileImage             =  layout.findViewById(R.id.profileImage);
            profileImga             =  layout.findViewById(R.id.profileImga);
            builder.setView(layout);
            alertDialog2             = builder.create();
            llPrepaid.setOnClickListener(this);
            llDTH.setOnClickListener(this);
            llLandLine.setOnClickListener(this);
            llPostpaid.setOnClickListener(this);
            llDatacard.setOnClickListener(this);
            llKSEB.setOnClickListener(this);
            Window window = alertDialog2.getWindow();
            window.setGravity(Gravity.CENTER);
            alertDialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog2.show();

            try {
                if ( IScoreApplication.decryptStart( dynamicMenuDetails.getRecharge()).equals("0") ){
                    llPrepaid.setVisibility(View.GONE);
                    llDTH.setVisibility(View.GONE);
                    llLandLine .setVisibility(View.GONE);
                    llPostpaid .setVisibility(View.GONE);
                    llDatacard.setVisibility(View.GONE);
                    profileImag.setVisibility(View.GONE);
                    profileImg.setVisibility(View.GONE);
                    profileIm .setVisibility(View.GONE);
                    profileImage .setVisibility(View.GONE);
                    profileImga.setVisibility(View.GONE);
                }else{
                    llPrepaid.setVisibility(View.VISIBLE);
                    llDTH.setVisibility(View.VISIBLE);
                    llLandLine .setVisibility(View.VISIBLE);
                    llPostpaid .setVisibility(View.VISIBLE);
                    llDatacard.setVisibility(View.VISIBLE);
                    profileImag.setVisibility(View.VISIBLE);
                    profileImg.setVisibility(View.VISIBLE);
                    profileIm .setVisibility(View.VISIBLE);
                    profileImage .setVisibility(View.VISIBLE);
                    profileImga.setVisibility(View.VISIBLE);
                }
                if ( IScoreApplication.decryptStart( dynamicMenuDetails.getKseb()).equals("0") ){
                    llKSEB.setVisibility(View.GONE);
                    profileIma.setVisibility(View.GONE);
                }else {
                    llKSEB.setVisibility(View.VISIBLE);
                    profileIma.setVisibility(View.VISIBLE);
                }
             } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("RestrictedApi")
    private void showFABMenu(){
        isFABOpen=true;
        fab1.setVisibility(View.VISIBLE);
        fab2.setVisibility(View.VISIBLE);
        fab3.setVisibility(View.VISIBLE);
        fab1.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        fab2.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
        fab3.animate().translationY(-getResources().getDimension(R.dimen.standard_155));
    }

    @SuppressLint("RestrictedApi")
    private void closeFABMenu(){
        isFABOpen=false;
        fab1.animate().translationY(0);
        fab2.animate().translationY(0);
        fab3.animate().translationY(0);
        fab1.setVisibility(View.INVISIBLE);
        fab2.setVisibility(View.INVISIBLE);
        fab3.setVisibility(View.INVISIBLE);
    }

}
