package hyperface.cms.domains.fees

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

//@Entity
class LatePaymentFee extends Fee {
//    @Id
//    @GenericGenerator(name = "late_payment_fee_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
//    @GeneratedValue(generator = "late_payment_fee_id")
//    String id

    int bufferDaysPastDue = 2
    FeeStrategy feeStrategy
}
