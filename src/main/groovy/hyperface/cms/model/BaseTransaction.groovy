package hyperface.cms.model

import hyperface.cms.model.enums.TransactionSourceIndicator
import hyperface.cms.model.enums.TransactionType

import java.time.LocalDate
import java.time.LocalDateTime

abstract class BaseTransaction {
    String accountNumber;
    LocalDateTime txnTimeStamp;
    LocalDate txnPostingDate;
    LocalDateTime createdOn;
    TransactionType transactionType;
    Amount txnAmountInLocalCurrency;
    String referenceNumber;
    TransactionSourceIndicator txnSourceIndicator;
    String txnDescription;
}
