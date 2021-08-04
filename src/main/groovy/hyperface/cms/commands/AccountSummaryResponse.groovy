package hyperface.cms.commands

import hyperface.cms.domains.Card
import hyperface.cms.domains.CardStatement
import hyperface.cms.domains.CreditAccount

import java.sql.Statement

class AccountSummaryResponse {
    List<Card> cards
    CreditAccount account
    List<TransactionResponse> latestTransactions
    CardStatement latestStatement
}
