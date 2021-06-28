package hyperface.cms.domains

import hyperface.cms.model.enums.FeeType
import hyperface.cms.model.enums.TransactionSourceIndicator

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import java.time.ZonedDateTime

@Entity
class SystemTransaction extends Transaction {

    SystemTransaction() {
        this.setTxnSourceIndicator(TransactionSourceIndicator.SYSTEM_GENERATED)
    }

    Boolean hasExecuted
    ZonedDateTime executeAfter
    ZonedDateTime executedOn

    @Enumerated(EnumType.STRING)
    FeeType feeType
}
