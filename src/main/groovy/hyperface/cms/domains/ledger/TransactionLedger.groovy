package hyperface.cms.domains.ledger

import hyperface.cms.domains.Transaction
import hyperface.cms.model.enums.FeeType
import hyperface.cms.model.enums.LedgerTransactionType
import hyperface.cms.model.enums.MoneyMovementIndicator
import hyperface.cms.model.enums.OnUsOffUsIndicator
import org.hibernate.annotations.GenericGenerator

import javax.persistence.*
import java.time.LocalDate

@Entity
class TransactionLedger {
    @Id
    @GenericGenerator(name = "ledger_entry_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "ledger_entry_id")
    String id;

    String accountNumber;
    LocalDate postingDate;
    Double openingBalance;
    Double closingBalance;
    Double transactionAmount;

    @Enumerated(EnumType.STRING)
    MoneyMovementIndicator moneyMovementIndicator;

    @Enumerated(EnumType.STRING)
    LedgerTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    OnUsOffUsIndicator onUsOffUsIndicator;

    @Enumerated(EnumType.STRING)
    FeeType feeType;

    @ManyToOne
    @JoinColumn(name = "txn_ref_id", referencedColumnName = "id")
    Transaction transaction;

    String txnDescription;
}
