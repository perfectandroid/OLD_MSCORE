package com.creativethoughts.iscore.Helper;

public class Common {

    public static final int timeInMillisecond=180000;
    private static final boolean HOSTNAMEVERFICATION_MANUAL=true;

    private static final String HOSTNAME_SUBJECT="STATIC-VM";
    private static final String CERTIFICATE_ASSET_NAME="mscoredemo.pem";
   // private static final String BASE_URL="https://117.200.76.144:14001";

/*
    public static final String BASE_URL="https://202.164.150.65:14264";
    private static final String API_NAME= "/Mscore/api/MV3";*/

    //==== ==== ==== ===== ===== ===== ==== ==== ==== ==== ==== ==== ==== ==== ==== ====

    public static boolean isHostnameverficationManual() {
        return HOSTNAMEVERFICATION_MANUAL;
    }

    public static String getHostnameSubject() {
        return HOSTNAME_SUBJECT;
    }

    public static String getCertificateAssetName() {
        return CERTIFICATE_ASSET_NAME;
    }

 /*   public static String getBaseUrl() {
        return BASE_URL;
    }

    public static String getApiName() {
        return API_NAME;
    }*/



}
