package hyperface.cms.model

import hyperface.cms.model.enums.AuthorizationType
import hyperface.cms.model.enums.OnUsOffUsIndicator
import hyperface.cms.model.enums.SovereigntyIndicator
import hyperface.cms.model.enums.TransactionStatus
import lombok.Data

@Data
class CustomerTransaction extends BaseTransaction{
    private String cardNumber;
    private AuthorizationType authorizationType;
    private OnUsOffUsIndicator onUsOffUsIndicator;
    private SovereigntyIndicator sovereigntyIndicator;
    private Amount transactionAmount;
    private Amount pendingTxnAmount;
    private TransactionStatus txnStatus;
    private String authCode;
    private String tid;
    private String mid;
    private String mcc;
    private String schemeReferenceId;
    private String merchantCountryCode;
    private String posEntryMode;
}
