package hyperface.cms.model

import hyperface.cms.model.enums.AuthorizationType
import hyperface.cms.model.enums.OnUsOffUsIndicator
import hyperface.cms.model.enums.SovereigntyIndicator
import hyperface.cms.model.enums.TransactionStatus

class CustomerTransaction extends BaseTransaction {
    String cardNumber;
    AuthorizationType authorizationType;
    OnUsOffUsIndicator onUsOffUsIndicator;
    SovereigntyIndicator sovereigntyIndicator;
    Amount transactionAmount;
    Amount pendingTxnAmount;
    TransactionStatus txnStatus;
    String authCode;
    String tid;
    String mid;
    String mcc;
    String schemeReferenceId;
    String merchantCountryCode;
    String posEntryMode;
}
