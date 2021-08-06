package hyperface.cms.domains.interest

import hyperface.cms.domains.ledger.TransactionLedger

class InterestCriteria {
    String name
    List<Condition> conditions
    Integer aprInBps
    Integer precedence = 1

    public boolean checkForMatch(TransactionLedger ledgerEntry) {
        return conditions.find({
            return !it.checkForMatch(ledgerEntry)
        }) ? false : true
    }

}

class Condition {
    enum Parameter {
        TRANSACTION_TYPE,
        TRANSACTION_CURRENCY // FOR FOREX TRANSACTIONS
    }
    enum MatchCriteria {
        EQUALS,
        EQUALS_IGNORE_CASE,
        CONTAINS,
        IN_LIST,
        // numeric comparisons
//        EQUAL_TO,
//        LT,
//        GT,
//        LTE,
//        GTE,
    }
    Parameter parameter
    MatchCriteria matchCriteria
    String value

    public boolean checkForMatch(TransactionLedger ledgerEntry) {
        String subject = "defaultStringWontMatch"
        if(this.parameter == Parameter.TRANSACTION_TYPE) {
            subject = ledgerEntry.transactionType.name()
        }
        else if(this.parameter == Parameter.TRANSACTION_CURRENCY) {
            subject = ledgerEntry.transaction.transactionCurrency
        }

        switch(matchCriteria) {
            case MatchCriteria.CONTAINS:
                return value.contains(subject)
            case MatchCriteria.EQUALS:
                return value.equals(subject)
            case MatchCriteria.EQUALS_IGNORE_CASE:
                return value.equalsIgnoreCase(subject)
            case MatchCriteria.IN_LIST:
                return (value.split(",") as List).contains(subject)
        }
        return false
    }

}
