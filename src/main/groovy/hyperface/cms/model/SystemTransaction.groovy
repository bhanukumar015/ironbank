package hyperface.cms.model

import hyperface.cms.model.enums.FeeType

import java.time.LocalDateTime

class SystemTransaction extends BaseTransaction {
    FeeType feeType;
    String cashbackFundingAccountRef;
    Boolean hasExecuted = Boolean.FALSE;
    LocalDateTime executeAfter;
    LocalDateTime executedOn;
}
