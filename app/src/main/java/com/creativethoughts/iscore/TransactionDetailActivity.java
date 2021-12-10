package com.creativethoughts.iscore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.creativethoughts.iscore.db.dao.model.Transaction;
import com.creativethoughts.iscore.utility.CommonUtilities;

/**
 * Created by muthukrishnan on 27/11/15 - 10:00 AM
 */
public class TransactionDetailActivity extends AppCompatActivity {

    private static final String BUNDLE_TRANSACTION_KEY = "transaction_key";
    private LinearLayout mDetailLayout;

    public static void openTransactionDetails(Context activity, Transaction transaction) {
        Intent intent = new Intent(activity, TransactionDetailActivity.class);
        intent.putExtra(BUNDLE_TRANSACTION_KEY, transaction);

        activity.startActivity(intent);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_transaction_deatils);

        setTitle("Transaction Details");

        mDetailLayout = (LinearLayout) findViewById(R.id.detail_layout);


        Transaction transaction =
                (Transaction) getIntent().getSerializableExtra(BUNDLE_TRANSACTION_KEY);

        if (transaction == null) {
            finish();
        }

        assert transaction != null;
        updateView("Cheque No", transaction.chequeNo);

        if (!TextUtils.isEmpty(transaction.chequeDate)) {
            updateView("Cheque Date", CommonUtilities.getFormatedDate(transaction.chequeDate));
        } else {
            updateView("Cheque Date", "");
        }

//        updateView("Remarks", transaction.remarks);

        if ("C".equalsIgnoreCase(transaction.transType)) {
            updateView("Trans Type", "Credit");
        } else {
            updateView("Trans Type", "Debit");
        }

        final String amountValue;

        if(!TextUtils.isEmpty(transaction.amount)) {
            amountValue = String.format("%.2f", Double.parseDouble(transaction.amount));
        } else {
            amountValue = transaction.amount;
        }

        updateView("Amount", amountValue);

        //        if (TextUtils.isEmpty(transaction.chequeNo) == false) {
        //
        //        }
        //
        //        if (TextUtils.isEmpty(transaction.chequeDate) == false) {
        //            updateView("Cheque Date", transaction.chequeDate);
        //        }
        //
        //        if (TextUtils.isEmpty(transaction.transType) == false) {
        //            if ("C".equalsIgnoreCase(transaction.transType)) {
        //                updateView("Trans Type", "Credit");
        //            } else {
        //                updateView("Trans Type", "Debit");
        //            }
        //        }
        //
        //        if (TextUtils.isEmpty(transaction.remarks) == false) {
        //            updateView("Remarks", transaction.remarks);
        //        }

        if (!TextUtils.isEmpty(transaction.narration)) {
            final TextView narrationDesc = (TextView) findViewById(R.id.narration_desc);
            assert narrationDesc != null;
            narrationDesc.setText(transaction.narration);
        }

    }

    private void updateView(String key, String value) {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(this).inflate(R.layout.row_transaction_details, null);

        TextView keyTv = (TextView) view.findViewById(R.id.key);
        TextView valueTv = (TextView) view.findViewById(R.id.value);

        keyTv.setText(key);
        valueTv.setText(value);

        mDetailLayout.addView(view);
    }

}
