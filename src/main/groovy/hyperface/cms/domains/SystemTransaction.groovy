package hyperface.cms.domains

import hyperface.cms.model.enums.FeeType
import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import java.time.ZonedDateTime

@Entity
class SystemTransaction extends Transaction {
    @Id
    @GenericGenerator(name = "sys_txn_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "sys_txn_id")
    String id

    Boolean hasExecuted
    ZonedDateTime executeAfter
    ZonedDateTime executedOn

    @Enumerated(EnumType.STRING)
    FeeType feeType
}
