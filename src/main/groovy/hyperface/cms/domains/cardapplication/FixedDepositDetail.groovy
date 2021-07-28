package hyperface.cms.domains.cardapplication

import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import java.time.ZonedDateTime

@Entity
class FixedDepositDetail {
    enum FdStatus {
        PENDING,
        COMPLETE
    }

    enum LienStatus {
        LOCKED,
        UNMARKED,
        VOLUNTARY_CLOSURE,
        FORCE_CLOSURE
    }
    @Id
    @GenericGenerator(name = "fd_detail_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "fd_detail_id")
    String id

    @ManyToOne(optional = false)
    @JoinColumn(name = "card_application_id", referencedColumnName = "id")
    CardApplication cardApplication

    String accountNumber
    Double fixedDepositAmount
    ZonedDateTime maturityDate
    String nomineeName
    String nomineeDob
    String nomineeGuardian
    String motherMaidenName
    Boolean fatcaConfirmed

    @Enumerated(EnumType.STRING)
    FdStatus fdStatus

    @Enumerated(EnumType.STRING)
    LienStatus lienStatus
}
