package hyperface.cms.model

import hyperface.cms.model.enums.FeeType

import java.time.LocalDateTime

class SystemTransaction extends BaseTransaction {
    FeeType feeType;
    String cashbackFundingAccountRef;
    String feeAccountRef;
    String interestAccountRef;
    String taxAccountRef;
    Boolean hasExecuted = Boolean.FALSE;
    LocalDateTime executeAfter;
    LocalDateTime executedOn;
}
