package hyperface.cms.service

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import hyperface.cms.commands.AccountSummaryResponse
import hyperface.cms.commands.AccountTransactionsResponse
import hyperface.cms.commands.CreateCreditAccountRequest
import hyperface.cms.commands.FetchAccountTransactionsRequest
import hyperface.cms.commands.GenericErrorResponse
import hyperface.cms.commands.TransactionResponse
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.CustomerTransaction
import hyperface.cms.domains.Transaction
import hyperface.cms.domains.ledger.TransactionLedger
import hyperface.cms.repository.CardStatementRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.repository.CustomerTransactionRepository
import hyperface.cms.repository.TransactionLedgerRepository
import hyperface.cms.util.CardProgramManagement
import io.vavr.control.Either
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.time.ZonedDateTime

@Service
@Slf4j
class AccountService {

    @Autowired
    CreditAccountRepository creditAccountRepository

    @Autowired
    CardProgramManagement cardProgramManagement

    @Autowired
    TransactionLedgerRepository transactionLedgerRepository

    @Autowired
    CustomerTransactionRepository customerTransactionRepository

    @Autowired
    CardStatementRepository cardStatementRepository

    Either<GenericErrorResponse, CreditAccount> createCreditAccount(CreateCreditAccountRequest req) {
        if (!req.cardProgram.isActive) {
            String errorMessage = "Cannot create accounts as card program" + ((req.cardProgram.disableLevel == CreditCardProgram.DisableLevel.MANUAL)
                            ? " was manually disabled"
                            : "'s ${req.cardProgram.disableLevel.toString().toLowerCase()} limit exceeded")
            return Either.left(new GenericErrorResponse(reason: errorMessage))
        }

        CreditAccount creditAccount = new CreditAccount()
        creditAccount.currentBalance = 0
        creditAccount.approvedCreditLimit = req.approvedCreditLimit
        creditAccount.availableCreditLimit = req.approvedCreditLimit
        creditAccount.defaultCurrency = req.cardProgram.baseCurrency
        creditAccount.customer = req.customer
        creditAccountRepository.save(creditAccount)
        cardProgramManagement.updateCardProgramCounts(req.cardProgram)
        return Either.right(creditAccount)
    }

    List<TransactionResponse> fetchAccountTransactions(FetchAccountTransactionsRequest req){
        List<TransactionLedger> ledgerTxns = transactionLedgerRepository.findByAccountInRange(req.account, req.fromDate
                , req.toDate)
        List<CustomerTransaction> customerTxns = customerTransactionRepository.findAuthTransactionsByAccountInRange(req.account
                , req.fromDate, req.toDate)
        // TODO: verify this overlapping logic with Satish
        List<CustomerTransaction> overlappingTxns = ledgerTxns.collect {it.transaction}
                .findAll {it} as List<CustomerTransaction>
        customerTxns.removeAll(overlappingTxns)
        List<Transaction> transactions = new ArrayList<>(ledgerTxns.size() + customerTxns.size())
        transactions.addAll(ledgerTxns.collect{it.transaction})
        transactions.addAll(customerTxns)
        List<TransactionResponse> response = new ArrayList<>(ledgerTxns.size() + customerTxns.size())
        transactions.each(tx -> {
            TransactionResponse txnResp = new TransactionResponse().tap{
                id = tx.id
                amount = tx.transactionAmount
                description = tx.txnDescription
                transactionAmount = tx.transactionAmount
                transactionCurrency = tx.transactionCurrency
                txnType = tx.transactionType
                if(tx instanceof CustomerTransaction){
                    mcc = tx.mcc
                    mid = tx.mid
                    tid = tx.tid
                }
            }
            response.add(txnResp)
        })
        return response
    }

    AccountSummaryResponse fetchAccountSummary(CreditAccount creditAccount){
        FetchAccountTransactionsRequest req = new FetchAccountTransactionsRequest().tap{
            account = creditAccount
            count = 10
            offset = 0
            fromDate = creditAccount.currentBillingStartDate
            toDate = ZonedDateTime.now()
        }
        return new AccountSummaryResponse().tap{
            cards = creditAccount.cards.findAll {it.isPrimaryCard}
            account = creditAccount
            latestTransactions = fetchAccountTransactions(req)
            latestStatement = cardStatementRepository.findTopByCreditAccountOrderByGeneratedOnDesc(creditAccount)
        }
    }

    static AccountTransactionsResponse getAccountTransactionResponse(FetchAccountTransactionsRequest req
                                                                     , List<TransactionResponse> txns){
        return new AccountTransactionsResponse().tap{
            count = req.count
            hasMore = (txns.size() > req.count)
            offset = req.offset
            // TODO: Would a separate call to get the total count be more efficient?
            totalCount = txns.size()
            transactions = txns.drop(req.offset).take(req.count)
        }
    }
}
