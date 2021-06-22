package hyperface.cms.model

import hyperface.cms.model.enums.FeeType
import lombok.Data

import java.time.LocalDateTime

@Data
class SystemTransaction extends BaseTransaction {
    private FeeType feeType;
    private String cashbackFundingAccountRef;
    private String feeAccountRef;
    private String interestAccountRef;
    private String taxAccountRef;
    private Boolean hasExecuted = Boolean.FALSE;
    private LocalDateTime executeAfter;
    private LocalDateTime executedOn;
}
