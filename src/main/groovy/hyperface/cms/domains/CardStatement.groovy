package hyperface.cms.domains

import org.hibernate.annotations.GenericGenerator

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import java.time.ZonedDateTime

@Entity
class CardStatement {
    @Id
    @GenericGenerator(name = "statement_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "statement_id")
    String id

    @ManyToOne
    @JoinColumn(name = "credit_account_id", referencedColumnName = "id")
    CreditAccount creditAccount

    Double totalAmountDue
    Double minAmountDue
    ZonedDateTime dueDate
    Double totalCredits
    Double totalDebits
    Double openingBalance
    Double closingBalance
    Double deferredInterest
    Double residualInterest
    Double unpaidResidualBalance
    Double billedInterest
    Double waivedInterest
    Double netTaxOnInterest
    Double netTaxOnFees
    Double netFinanceCharges
    Double netFeeCharges
    Double netRepayments
    Double refunds
    Double netCashback
    Double netPurchases
}
