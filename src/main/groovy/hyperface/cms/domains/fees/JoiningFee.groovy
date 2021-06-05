package hyperface.cms.domains.fees

class JoiningFee extends Fee {
    enum ApplicationTrigger {
        AFTER_FIRST_PURCHASE_TXN,
        AFTER_FIRST_REPAYMENT
    }
    ApplicationTrigger applicationTrigger
    FeeStrategy feeStrategy
}
