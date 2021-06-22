package hyperface.cms.domains


import hyperface.cms.model.enums.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.GenericGenerator

import javax.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
class Transaction {
    @Id
    @GenericGenerator(name = "txn_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "txn_id")
    String id;

    String accountNumber;
    String cardHash;
    LocalDateTime txnDate;
    LocalDate txnPostingDate;
    Double txnAmount;
    Double pendingTxnAmount;
    String authCode;
    String tid;
    String mid;
    String mcc;
    String referenceNumber;
    String txnDescription;
    String schemeReferenceId;
    String merchantCountryCode;
    String currencyCode;
    Double txnAmountSrc;
    String posEntryMode;
    String cashbackFundingAccountRef;
    String feeAccountRef;
    String interestAccountRef;
    String taxAccountRef;
    Boolean hasExecuted;
    LocalDateTime executeAfter;
    LocalDateTime executedOn;

    @CreationTimestamp
    LocalDateTime createdOnTimeStamp;

    @Enumerated(EnumType.STRING)
    TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    AuthorizationType authorizationType;

    @Enumerated(EnumType.STRING)
    OnUsOffUsIndicator onUsOffUsIndicator;

    @Enumerated(EnumType.STRING)
    SovereigntyIndicator sovereigntyIndicator;

    @Enumerated(EnumType.STRING)
    FeeType feeType;

    @Enumerated(EnumType.STRING)
    TransactionStatus txnStatus;

    @Enumerated(EnumType.STRING)
    TransactionSourceIndicator txnSourceIndicator;
}
