package hyperface.cms

class Constants {

    enum Currency {
        INR, USD, EUR, GBP, SGD, AUD
    }
    enum CardScheme { Visa, Mastercard, Rupay, Diners, Amex }

    enum CardProgramType { Consumer, Corporate, Commercial }
    enum CardHost { Hyperface, Worldline, Euronet, Maximus, Nium } // move to database
    enum CardSwitch { Nium, Maximus, Euronet, Worldline }

    enum LedgerEntryType { Credit, Debit }

    enum RejectionCode {
        // https://en.wikipedia.org/wiki/ISO_8583#Response_code
        // TODO - create more rejection codes
        DO_NOT_HONOR("05", "Do Not Honor"),
        EXPIRED_CARD("33", "Expired Card"),
        INSUFFICIENT_FUNDS("51", "Insufficient Funds"),
        EXCEEDS_WL_LIMIT("61", "Exceeds Withdrawal Limit"),
        OTHER_ERROR("06", "Error"),
        CARD_NOT_ACTIVATED("06", "Card Not Activated"),
        CARD_HOTLISTED("05", "Do Not Honor")

        RejectionCode(String code, String description) {
            this.code = code
            this.description = description
        }
        String code
        String description
    }

}
