package hyperface.cms.model

import hyperface.cms.model.enums.FeeType
import hyperface.cms.model.enums.LedgerTransactionType
import hyperface.cms.model.enums.MoneyMovementIndicator
import hyperface.cms.model.enums.OnUsOffUsIndicator
import lombok.Data

import java.time.LocalDate

@Data
class LedgerTransaction {
    private String accountNumber;
    private LocalDate postingDate;
    private Amount openingBalance;
    private Amount closingBalance;
    private MoneyMovementIndicator moneyMovementIndicator;
    private LedgerTransactionType transactionType;
    private OnUsOffUsIndicator onUsOffUsIndicator;
    private FeeType feeType;
    private Amount transactionAmount;
    private String txnRefNumber;
    private String txnDescription;
}
