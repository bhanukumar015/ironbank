package hyperface.cms.model

import hyperface.cms.model.enums.FeeType
import hyperface.cms.model.enums.LedgerTransactionType
import hyperface.cms.model.enums.MoneyMovementIndicator
import hyperface.cms.model.enums.OnUsOffUsIndicator

import java.time.LocalDate

class LedgerTransaction {
    String accountNumber;
    LocalDate postingDate;
    Amount openingBalance;
    Amount closingBalance;
    MoneyMovementIndicator moneyMovementIndicator;
    LedgerTransactionType transactionType;
    OnUsOffUsIndicator onUsOffUsIndicator;
    FeeType feeType;
    Amount transactionAmount;
    String txnRefNumber;
    String txnDescription;
}
