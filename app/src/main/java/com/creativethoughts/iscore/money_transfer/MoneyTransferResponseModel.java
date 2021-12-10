package com.creativethoughts.iscore.money_transfer;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vishnu on 11/28/2017 - 11:47 AM.
 */

public class  MoneyTransferResponseModel {
    @SerializedName( "StatusCode" )
    private String statusCode;

    @SerializedName( "Status" )
    private String status;
    @SerializedName( "TrasactionID" )
    private String transactionId;
    @SerializedName( "otpRefNo" )
    private String otpRefNo;
    @SerializedName( "message" )
    private String message;

    public String getTransactionId() {
        return transactionId;
    }

    public String getOtpRefNo() {
        return otpRefNo;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

}
