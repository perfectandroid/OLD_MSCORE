package com.creativethoughts.iscore.Retrofit;



import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Anusree on 03-10-2019.
 */

public interface APIInterface {

    @POST("api/Image/CustomerImageDets")
    Call<String> getImage(@Body RequestBody body);

    @POST("api/AccountSummary/AccountSummaryDetails")
    Call<String> getCustomerModules(@Body RequestBody body);

    @POST("api/AccountSummary/AccountModuleDetailsListInfo")
    Call<String> getAccountSummary(@Body RequestBody body);

    @POST("api/AccountSummary/StandingInstructionDetails")
    Call<String> getStandingInstruction(@Body RequestBody body);

    @POST("api/AccountSummary/NoticePostingDetails")
    Call<String> getIntimations(@Body RequestBody body);

    @POST("api/AccountSummary/DashBoardAssetsDataDetails")
    Call<String> getDashboard(@Body RequestBody body);

    @POST("api/AccountSummary/DashBoardLaibilityDataDetails")
    Call<String> getDashboardLaibility(@Body RequestBody body);

    @POST("api/AccountSummary/DashBoardDataSavingsBankDetails")
    Call<String> getDashboardSavingsbank(@Body RequestBody body);

    @POST("api/AccountSummary/DashBoardDataPaymentAndReceiptDetails")
    Call<String> getDashboardpaymentrecept(@Body RequestBody body);

    @POST("api/AccountSummary/BranchLocationDetails")
    Call<String> getBankLocation(@Body RequestBody body);

    @POST("api/AccountSummary/BankBranchDetails")
    Call<String> getBranchDetail(@Body RequestBody body);

    @POST("api/AccountSummary/CustomerBankDetails")
    Call<String> getBankbranchList(@Body RequestBody body);

    @POST("api/AccountSummary/CustomerProfileDetails")
    Call<String> getProfile(@Body RequestBody body);

    @POST("api/AccountSummary/AccountDueDateDetails")
    Call<String> getDuedate(@Body RequestBody body);

    @POST("api/AccountSummary/BarcodeFormatDet")
    Call<String> getBardCodeData(@Body RequestBody body);

    @POST("api/AccountSummary/BarcodeAgainstCustomerAccountDets")
    Call<String>getAccountList(@Body RequestBody body);

}
