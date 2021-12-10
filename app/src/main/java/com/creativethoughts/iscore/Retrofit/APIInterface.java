package com.creativethoughts.iscore.Retrofit;



import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Anusree on 03-10-2019.
 */

public interface APIInterface {

    @POST("Mscore/api/Image/CustomerImageDets")
    Call<String> getImage(@Body RequestBody body);

    @POST("Mscore/api/AccountSummary/AccountSummaryDetails")
    Call<String> getCustomerModules(@Body RequestBody body);

    @POST("Mscore/api/AccountSummary/AccountModuleDetailsListInfo")
    Call<String> getAccountSummary(@Body RequestBody body);

    @POST("Mscore/api/AccountSummary/StandingInstructionDetails")
    Call<String> getStandingInstruction(@Body RequestBody body);

    @POST("Mscore/api/AccountSummary/NoticePostingDetails")
    Call<String> getIntimations(@Body RequestBody body);

    @POST("Mscore/api/AccountSummary/DashBoardAssetsDataDetails")
    Call<String> getDashboard(@Body RequestBody body);

    @POST("Mscore/api/AccountSummary/DashBoardLaibilityDataDetails")
    Call<String> getDashboardLaibility(@Body RequestBody body);

    @POST("Mscore/api/AccountSummary/DashBoardDataSavingsBankDetails")
    Call<String> getDashboardSavingsbank(@Body RequestBody body);

    @POST("Mscore/api/AccountSummary/DashBoardDataPaymentAndReceiptDetails")
    Call<String> getDashboardpaymentrecept(@Body RequestBody body);

    @POST("Mscore/api/AccountSummary/BranchLocationDetails")
    Call<String> getBankLocation(@Body RequestBody body);

    @POST("Mscore/api/AccountSummary/BankBranchDetails")
    Call<String> getBranchDetail(@Body RequestBody body);

    @POST("Mscore/api/AccountSummary/CustomerBankDetails")
    Call<String> getBankbranchList(@Body RequestBody body);

    @POST("Mscore/api/AccountSummary/CustomerProfileDetails")
    Call<String> getProfile(@Body RequestBody body);

    @POST("Mscore/api/AccountSummary/AccountDueDateDetails")
    Call<String> getDuedate(@Body RequestBody body);

    @POST("Mscore/api/AccountSummary/BarcodeFormatDet")
    Call<String> getBardCodeData(@Body RequestBody body);

    @POST("Mscore/api/AccountSummary/BarcodeAgainstCustomerAccountDets")
    Call<String>getAccountList(@Body RequestBody body);

}
