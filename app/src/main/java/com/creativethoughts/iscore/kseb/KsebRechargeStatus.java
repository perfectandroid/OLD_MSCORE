package com.creativethoughts.iscore.kseb;

import com.google.gson.annotations.SerializedName;

public class KsebRechargeStatus {
    @SerializedName("StatusCode")
    private int statusCode;

    @SerializedName("RefID")
    private String refId;

    @SerializedName("MobileNumber")
    private String mobileNo;

    @SerializedName("Amount")
    private String amount;

    @SerializedName("AccNumber")
    private String accNo;

    public int getStatusCode() {
        return statusCode;
    }

    public String getRefId() {
        return refId;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public String getAmount() {
        return amount;
    }

    public String getAccNo() {
        return accNo;
    }


}
