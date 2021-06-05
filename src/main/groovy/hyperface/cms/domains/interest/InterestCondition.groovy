package hyperface.cms.domains.interest

class InterestCondition {
    List<Condition> conditions
    Integer interestRateInBps
}

class Condition {
    enum Parameter {
        TRANSACTION_TYPE,
        TRANSACTION_CURRENCY // FOR FOREX TRANSACTIONS
    }
    enum MatchCriteria {
        EQUALS,
        CONTAINS,
        EQUAL_TO,
        LT,
        GT,
        LTE,
        GTE
    }
    Parameter parameter
    MatchCriteria matchCriteria
    String value
}
