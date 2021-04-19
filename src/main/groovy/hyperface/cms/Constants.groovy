package hyperface.cms

class Constants {

    enum Currency { INR, USD, EUR, GBP, SGD, AUD }
    enum CardScheme { Visa, Mastercard, Rupay, Diners, Amex }

    enum CardProgramType { Consumer, Corporate, Commercial }
    enum CardHost { Hyperface, Worldline, Euronet, Maximus } // move to database

    enum LedgerEntryType { Credit, Debit }

}
