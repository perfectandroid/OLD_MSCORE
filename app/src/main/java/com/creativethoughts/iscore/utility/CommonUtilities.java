package com.creativethoughts.iscore.utility;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.creativethoughts.iscore.R;
import com.creativethoughts.iscore.db.dao.PBAccountInfoDAO;
import com.creativethoughts.iscore.db.dao.SettingsDAO;
import com.creativethoughts.iscore.db.dao.model.SettingsModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.creativethoughts.iscore.Helper.Common;

/**
 * Created by Brijesh on 26-03-2015.
 */

public final class CommonUtilities {

    private CommonUtilities(){
        throw new IllegalStateException("Illegal");
    }

    private static final String BASE_URL_KEY = "iscore_base_url_key";
private static final String BASE_URL = Common.getBaseUrl();
    private static final String URI = Common.getApiName();


    public static String getBaseUrl() {
        String tempBaserUrl = PreferenceUtil.getInstance().getStringValue(BASE_URL_KEY, "");

        if(TextUtils.isEmpty(tempBaserUrl.trim())) {
            return BASE_URL;
        }

        return tempBaserUrl;
    }

    public static String getUrl() {
            return BASE_URL + URI;
    }
    private static Date convertStingToDate(String inputDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a", Locale.getDefault());
        Date date = null;
        try {
            date = simpleDateFormat.parse(inputDate);
        } catch (ParseException e) {
            //Do nothing
        }
        return date;
    }

    public static String getFormatedMsgDate(String inputDate) {
        Date date = convertMsgStingToDate(inputDate);

        if (date == null) {
            return "";
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH );

        return simpleDateFormat.format(date);
    }

    private static Date convertMsgStingToDate(String inputDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyy hh:mm:ss a", Locale.getDefault());
        Date date = null;
        try {
            date = simpleDateFormat.parse(inputDate);
        } catch (ParseException e) {
            //Do nothing
        }
        return date;
    }

    public static long getTimeDifferenceFromNow(String inputDate) {

        Date date = convertStingToDate(inputDate);

        if(date == null) {
            return System.currentTimeMillis();
        }

        Calendar now = Calendar.getInstance();
        Calendar theDay = Calendar.getInstance();
        theDay.setTime(date);

        return now.getTimeInMillis() - theDay.getTimeInMillis();
    }



    public static String getFormatedDate(String inputDate) {
        Date date = convertStingToDate(inputDate);

        if (date == null) {
            return "";
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH );

        return simpleDateFormat.format(date);
    }

    public static String getDateString(String inputDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH );
        Date date = null;
        try {
            date = simpleDateFormat.parse(inputDate);
        } catch (ParseException e) {
            //Do nothing
        }


        if (date == null) {
            return "";
        }

        SimpleDateFormat convetDateFormat = new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH);

        return convetDateFormat.format(date);
    }




    public static void setAccountNumber(String accountNo, Spinner spinner, Activity activity) {
        setAccountNumber(accountNo, spinner, activity, R.layout.simple_spinner_item_dark);
    }
    public static void setAccountNumberPassbook(String accountNo, Spinner spinner, Activity activity) {
        setAccountNumber(accountNo, spinner, activity, R.layout.simple_spinner_item );
    }

    private static void setAccountNumber(String accountNo, Spinner spinner, Activity activity, int layout) {
        List<String> accountNos = PBAccountInfoDAO.getInstance().getAccountNos();

        if (accountNos.isEmpty()) {
            return;
        }

        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(activity, layout, accountNos);

        spinner.setAdapter(spinnerAdapter);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if (accountNo == null) {
            return;
        }

        for (int i = 0; i < accountNos.size(); i++) {
            String account = accountNos.get(i);

            if (TextUtils.isEmpty(account)) {
                continue;
            }

            if (account.equalsIgnoreCase(accountNo)) {
                spinner.setSelection(i);

                break;
            }
        }
    }
    public static void transactionActivitySetAccountNumber(String accNo, Spinner spinner, Activity activity){
        if (accNo.isEmpty())
            return;
        List<String> accountSpinnerItems  ;
        accountSpinnerItems = PBAccountInfoDAO.getInstance().getAccountNos();
        ArrayList<String> itemTemp =  new ArrayList<>();

        if (accountSpinnerItems.isEmpty())
            return;

        for (int i = 0; i< accountSpinnerItems.size(); i++){

            if (!accountSpinnerItems.get(i).contains("SB") && !accountSpinnerItems.get(i).contains("CA") && !accountSpinnerItems.get(i).contains("OD"))
                itemTemp.add(accountSpinnerItems.get(i));

        }
        for ( String item: itemTemp ) {
            accountSpinnerItems.remove(item);
        }

        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter< >(activity, R.layout.simple_spinner_item_dark, accountSpinnerItems);
        spinner.setAdapter(spinnerAdapter);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        for (int i = 0; i < accountSpinnerItems.size(); i++) {
            String account = accountSpinnerItems.get(i);

            if (TextUtils.isEmpty(account)) {
                continue;
            }
            SettingsModel settingsModel = SettingsDAO.getInstance().getDetails();
            if (account.equalsIgnoreCase(settingsModel.customerId)) {
                spinner.setSelection(i);

                break;
            }
        }

    }

}
