package com.creativethoughts.iscore.db.dao.model;

import java.io.Serializable;

/**
 * Created by muthukrishnan on 26/09/15.
 */
public class Transaction implements Serializable {
    public long id;
    public String transactionId;
    public String demandDepositNo;
    public String amount;
    public String chequeNo;
    public String chequeDate;
    public String narration;
    public String transType;
    public String remarks;
    public String effectDate;
    public String transactionNo;
    public String accNo;
    public boolean isNew;
}
