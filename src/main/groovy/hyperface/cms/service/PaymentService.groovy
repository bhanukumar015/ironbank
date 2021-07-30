package hyperface.cms.service

import hyperface.cms.Constants
import hyperface.cms.appdata.TxnNotEligible
import hyperface.cms.commands.AuthorizationRequest
import hyperface.cms.commands.CustomerTransactionRequest
import hyperface.cms.commands.CustomerTransactionResponse
import hyperface.cms.commands.GenericErrorResponse
import hyperface.cms.commands.SettlementRequest
import hyperface.cms.domains.Account
import hyperface.cms.domains.Card
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.CustomerTransaction
import hyperface.cms.domains.CustomerTxn
import hyperface.cms.domains.batch.CurrencyConversion
import hyperface.cms.domains.interest.InterestCriteria
import hyperface.cms.domains.ledger.LedgerEntry
import hyperface.cms.domains.ledger.TransactionLedger
import hyperface.cms.model.enums.AuthorizationType
import hyperface.cms.model.enums.LedgerTransactionType
import hyperface.cms.model.enums.MoneyMovementIndicator
import hyperface.cms.model.enums.SovereigntyIndicator
import hyperface.cms.model.enums.TransactionStatus
import hyperface.cms.model.enums.TransactionType
import hyperface.cms.repository.CardRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.repository.CustomerTransactionRepository
import hyperface.cms.repository.CustomerTxnRepository
import hyperface.cms.repository.LedgerEntryRepository
import hyperface.cms.repository.TransactionLedgerRepository
import hyperface.cms.repository.batch.CurrencyConversionRepository
import io.vavr.control.Either
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit


@Service
class PaymentService {

    Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository

    @Autowired
    private CreditAccountRepository creditAccountRepository

    @Autowired
    CardRepository cardRepository

    @Autowired
    private CustomerTxnRepository customerTxnRepository

    @Autowired
    private CustomerTransactionRepository customerTransactionRepository

    @Autowired
    private CurrencyConversionRepository currencyConversionRepository

    @Autowired
    private TransactionLedgerRepository transactionLedgerRepository

    public Either<TxnNotEligible, Boolean> checkEligibility(CustomerTransactionRequest req) {
        if(req.card.hotlisted) {
            return Either.left(new TxnNotEligible(reason: "Card is blocked"))
        }
        else if (req.card.isLocked) {
            return Either.left(new TxnNotEligible(reason: "Card is locked"))
        }
        else if(req.transactionCurrency != req.card.creditAccount.defaultCurrency && !req.card.enableOverseasTransactions) {
            return Either.left(new TxnNotEligible(reason: "International transactions are disabled"))
        }
        return Either.right(true)
    }

    public Either<GenericErrorResponse,CustomerTransaction> createCustomerTxn(CustomerTransactionRequest req) {

        Account account = req.card.creditAccount
        if (account == null) {
            return Either.left(new GenericErrorResponse(reason:  "Account not found"))
        }
        CustomerTransaction txn = new CustomerTransaction()
        txn.card = req.card
        txn.txnDate = req.transactionDate ?: ZonedDateTime.now()
        txn.transactionType = (TransactionType)req.transactionType
        txn.txnDescription = req.transactionDescription
        txn.transactionAmount = req.transactionAmount
        txn.transactionCurrency = req.transactionCurrency
        txn.authorizationType = AuthorizationType.NOT_APPLICABLE
        txn.billingAmount = req.transactionAmount
        txn.billingCurrency = req.transactionCurrency
        if (txn.transactionCurrency != account.defaultCurrency) {
            txn.sovereigntyIndicator = SovereigntyIndicator.INTERNATIONAL
            CurrencyConversion currencyConversion =
                    currencyConversionRepository.findBySourceCurrencyAndDestinationCurrency(
                            req.transactionCurrency, account.defaultCurrency)
            txn.billingAmount = req.transactionAmount * currencyConversion.conversionRate
            txn.billingCurrency = account.defaultCurrency
        }
        if (txn.billingAmount > account.availableCreditLimit) {
            return Either.left(new GenericErrorResponse(reason:  "Insufficient balance"))
        }
        txn.txnStatus = TransactionStatus.NOT_APPLICABLE
        txn.mid = req.merchantTerminalId
        txn.tid = req.merchantTerminalId
        txn.mcc = req.merchantCategoryCode
        customerTransactionRepository.save(txn)
        if (txn.transactionType == TransactionType.SETTLEMENT_DEBIT)
            account.availableCreditLimit -= txn.billingAmount
        else
            account.availableCreditLimit += txn.billingAmount
        creditAccountRepository.save(account)

        switch (txn.transactionType) {
            case TransactionType.SETTLEMENT_DEBIT:
                createDebitEntry(txn)
                break
            case TransactionType.SETTLEMENT_CREDIT:
                createCreditEntry(txn)
                break
        }
        return Either.right(txn)
    }

    public CustomerTransactionResponse getCustomerTransactionResponse(CustomerTransaction txn) {
        CustomerTransactionResponse txnResponse = new CustomerTransactionResponse()
        txnResponse.id = txn.id
        txnResponse.cardId = txn.card.id
        txnResponse.transactionAmount = txn.transactionAmount
        txnResponse.transactionCurrency = txn.transactionCurrency
        txnResponse.billingAmount = txn.billingAmount
        txnResponse.billingCurrency = txn.billingCurrency
        txnResponse.transactionStatus = txn.txnStatus
        txnResponse.transactionDescription = txn.txnDescription
        txnResponse.transactionDate = txn.txnDate

        return txnResponse
    }
    public CustomerTxn processAuthorization(AuthorizationRequest req) {
        CustomerTxn txn = createCustomerTxn(req)
        // reduce this balance from the available credit limit
        // same currency
        req.card.creditAccount.availableCreditLimit -= txn.billingAmount
        txn.availableBalanceAfterTxn = req.card.creditAccount.availableCreditLimit
        customerTxnRepository.save(txn)
        log.info "Txn created for incoming authorization request: " + txn.dump()
        creditAccountRepository.save(req.card.creditAccount)
        return txn
    }

    // think about merging this with processAuthorization
    public CustomerTxn processReversalRequest(AuthorizationRequest req) {
        CustomerTxn txn = createCustomerTxn(req)
        req.card.creditAccount.availableCreditLimit += txn.billingAmount
        txn.availableBalanceAfterTxn = req.card.creditAccount.availableCreditLimit
        customerTxnRepository.save(txn)
        creditAccountRepository.save(req.card.creditAccount)
        return txn
    }

    // TODO - how to make sure that this is processed only once?
   @Deprecated public void processSettlementDebit(SettlementRequest req) {
        Card card = cardRepository.findById(req.cardId).get()
        assert card != null
        CreditAccount creditAccount = card.creditAccount

        CustomerTxn txn = customerTxnRepository.findAuthTxnByCardAndRRN(card, req.retrievalReferenceNumber)
        // an approximate defense
        if(txn.capturedAmount + req.settlementAmount > 1.2 * txn.authorizedAmount) {
            throw new IllegalArgumentException("This entry looks to have been processed already")
        }
        txn.postedToLedger = true
        LedgerEntry ledgerEntry = createDebitEntry(creditAccount, txn, req.settlementAmount)
        if(txn.capturedAmount < txn.authorizedAmount && (txn.capturedAmount + req.settlementAmount) > txn.authorizedAmount) {
            creditAccount.availableCreditLimit -= ((txn.capturedAmount + req.settlementAmount) - txn.authorizedAmount)
        }
        else if(txn.capturedAmount >= txn.authorizedAmount) {
            creditAccount.availableCreditLimit -= req.settlementAmount
        }
        creditAccount.currentBalance -= ledgerEntry.amount
        txn.capturedAmount += req.settlementAmount
        // set txntype
        if(txn.txnType == Constants.TxnType.SETTLE_DEBIT) {
            ledgerEntry.txnType = Constants.TxnType.PURCHASE
        }
        else if(txn.txnType == Constants.TxnType.SETTLE_DEBIT_CASH) {
            ledgerEntry.txnType = Constants.TxnType.CASH_WITHDRAWAL
        }
        ledgerEntryRepository.save(ledgerEntry)
        creditAccountRepository.save(creditAccount)
        customerTxnRepository.save(txn)
    }

    public void processSettlementCredit(SettlementRequest req) {
        Card card = cardRepository.findById(req.cardId).get()
        assert card != null
        CreditAccount creditAccount = card.creditAccount
        CustomerTxn txn = customerTxnRepository.findAuthTxnByCardAndRRN(card, req.retrievalReferenceNumber)
        LedgerEntry ledgerEntry = createCreditEntry(creditAccount, txn, req.settlementAmount)
        creditAccount.currentBalance += ledgerEntry.amount
        if(txn.txnType == Constants.TxnType.SETTLE_CREDIT) {
            ledgerEntry.txnType = Constants.TxnType.PURCHASE_REVERSAL
        }
        else if(txn.txnType == Constants.TxnType.SETTLE_CREDIT_CASH) {
            ledgerEntry.txnType = Constants.TxnType.CASH_WITHDRAWAL_REVERSAL
        }
        ledgerEntryRepository.save(ledgerEntry)
        creditAccountRepository.save(creditAccount)
        customerTxnRepository.save(txn)
    }

    public Integer getInterestRateForTxn(TransactionLedger ledgerEntry) {
        CreditCardProgram program = ledgerEntry.transaction.card.cardProgram
        InterestCriteria matchedCriteria = program.scheduleOfCharges.interestCriteriaList?.find({
            return it.checkForMatch(ledgerEntry)
        })
        return matchedCriteria?.getAprInBps()?:program.annualizedPercentageRateInBps
    }

    private TransactionLedger createDebitEntry(CustomerTransaction txn) {
        CreditAccount account = txn.card.creditAccount
        TransactionLedger debitEntry = new TransactionLedger()
        debitEntry.transactionAmount = txn.billingAmount
        debitEntry.txnDescription = txn.txnDescription
        debitEntry.postingDate = ZonedDateTime.now()
        debitEntry.moneyMovementIndicator = MoneyMovementIndicator.DEBIT
        if (txn.transactionType == TransactionType.SETTLEMENT_DEBIT) {
            debitEntry.transactionType = LedgerTransactionType.PURCHASE
        }
        debitEntry.openingBalance = account.currentBalance
        debitEntry.closingBalance = account.currentBalance - txn.billingAmount
        debitEntry.transaction = txn
        debitEntry.account = account
        transactionLedgerRepository.save(debitEntry)
        txn.txnStatus = TransactionStatus.APPROVED
        customerTransactionRepository.save(txn)
        account.currentBalance = debitEntry.closingBalance
        creditAccountRepository.save(account)
        return debitEntry
    }

    private TransactionLedger createCreditEntry(CustomerTransaction txn) {
        CreditAccount account = txn.card.creditAccount
        TransactionLedger creditEntry = new TransactionLedger()
        creditEntry.transactionAmount = txn.billingAmount
        creditEntry.txnDescription = txn.txnDescription
        creditEntry.postingDate = ZonedDateTime.now()
        creditEntry.moneyMovementIndicator = MoneyMovementIndicator.CREDIT
        if (txn.transactionType == TransactionType.SETTLEMENT_CREDIT) {
            creditEntry.transactionType = LedgerTransactionType.PURCHASE_REVERSAL
        }
        creditEntry.openingBalance = account.currentBalance
        creditEntry.closingBalance = account.currentBalance + txn.billingAmount
        creditEntry.transaction = txn
        creditEntry.account = account
        transactionLedgerRepository.save(creditEntry)
        txn.txnStatus = TransactionStatus.APPROVED
        customerTransactionRepository.save(txn)
        account.currentBalance = creditEntry.closingBalance
        creditAccountRepository.save(account)
        return creditEntry
    }

    public Double calculateInterest(CreditAccount account, TransactionLedger ledgerEntry) {
        Double amount = ledgerEntry.transactionAmount
        Double interestRateInPct = getInterestRateForTxn(ledgerEntry)/100
        Integer noOfDays = ChronoUnit.DAYS.between(ledgerEntry.postingDate,account.currentBillingEndDate) + 1
        Integer noOfDaysInBaseYear = 365
        Double interestAmount = amount * (1/noOfDaysInBaseYear) * noOfDays * (interestRateInPct/100)

        if (ledgerEntry.moneyMovementIndicator == MoneyMovementIndicator.CREDIT) {
            interestAmount = -interestAmount
        }
        return interestAmount
    }

    public Double calculateInterestByDate(CreditAccount account,ZonedDateTime startDate, ZonedDateTime endDate, Double amount) {
        Double interestRate = account.cards[0].cardProgram.annualizedPercentageRateInBps
        Integer noOfDays = ChronoUnit.DAYS.between(startDate,endDate) + 1
        Integer noOfDaysInBaseYear = 365
        Double interestAmount = amount * (1/noOfDaysInBaseYear) * noOfDays * (interestRate/100)
        return interestAmount
    }
}
