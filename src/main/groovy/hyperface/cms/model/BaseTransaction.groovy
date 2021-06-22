package hyperface.cms.model


import hyperface.cms.model.enums.TransactionSourceIndicator
import hyperface.cms.model.enums.TransactionType
import lombok.Data

import java.time.LocalDate
import java.time.LocalDateTime

@Data
abstract class BaseTransaction {
    private String accountNumber;
    private LocalDateTime txnTimeStamp;
    private LocalDate txnPostingDate;
    private LocalDateTime createdOnTimeStamp;
    private TransactionType transactionType;
    private Amount txnAmountInLocalCurrency;
    private String referenceNumber;
    private TransactionSourceIndicator txnSourceIndicator;
    private String txnDescription;
}
