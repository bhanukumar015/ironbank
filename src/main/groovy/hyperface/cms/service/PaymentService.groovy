package hyperface.cms.service

import hyperface.cms.Constants
import hyperface.cms.appdata.TxnNotEligible
import hyperface.cms.commands.AuthSettlementRequest
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

    Either<TxnNotEligible, Boolean> checkEligibility(CustomerTransactionRequest req) {
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

    Either<GenericErrorResponse,CustomerTransaction> createCustomerTxn(CustomerTransactionRequest req) {

        Account account = req.card.creditAccount
        if (account == null) {
            return Either.left(new GenericErrorResponse(reason: "Account not found"))
        }
        CustomerTransaction txn = new CustomerTransaction()
        txn.card = req.card
        txn.txnDate = req.transactionDate ?: ZonedDateTime.now()
        txn.transactionType = req.transactionType as TransactionType
        txn.txnDescription = req.transactionDescription
        txn.transactionAmount = req.transactionAmount
        txn.transactionCurrency = req.transactionCurrency
        txn.txnStatus = TransactionStatus.APPROVED
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
        if (txn.transactionType == TransactionType.SETTLEMENT_DEBIT_CASH) {
            if (txn.billingAmount > account.availableCashWithdrawalLimit) {
                return Either.left(new GenericErrorResponse(reason:  "Insufficient Cash balance"))
            }
            account.availableCashWithdrawalLimit -= txn.billingAmount
        }
        txn.txnStatus = TransactionStatus.APPROVED
        txn.mid = req.merchantTerminalId
        txn.tid = req.merchantTerminalId
        txn.mcc = req.merchantCategoryCode
        customerTransactionRepository.save(txn)
        switch (txn.transactionType) {
            case TransactionType.SETTLEMENT_DEBIT:
            case TransactionType.SETTLEMENT_DEBIT_CASH:
            case TransactionType.REPAYMENT_REVERSAL:
                account.availableCreditLimit -= txn.billingAmount
                account.availableCashWithdrawalLimit = Math.min(account.availableCreditLimit,account.availableCashWithdrawalLimit)
                createDebitEntry(txn)
                break

            case TransactionType.SETTLEMENT_CREDIT:
            case TransactionType.SETTLEMENT_CREDIT_CASH:
            case TransactionType.REPAYMENT:
                account.availableCreditLimit += txn.billingAmount
                account.availableCashWithdrawalLimit = Math.min(account.availableCreditLimit,account.approvedCashWithdrawalLimit)
                createCreditEntry(txn)
                break

            case TransactionType.AUTHORIZE:
                account.availableCreditLimit -= txn.billingAmount
                account.availableCashWithdrawalLimit = Math.min(account.availableCreditLimit,account.availableCashWithdrawalLimit)
                break

            case TransactionType.AUTHORIZATION_REVERSAL:
                account.availableCreditLimit += txn.billingAmount
                account.availableCashWithdrawalLimit = Math.min(account.availableCreditLimit,account.approvedCashWithdrawalLimit)
                break
        }
        creditAccountRepository.save(account)
        return Either.right(txn)
    }

    CustomerTransactionResponse getCustomerTransactionResponse(CustomerTransaction txn) {
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

    Either<GenericErrorResponse,CustomerTransaction> processSettlementDebit(AuthSettlementRequest req) {

        Account account = req.card.creditAccount

        Optional<CustomerTransaction> customerTransaction = customerTransactionRepository.findById(req.transactionId)
        if (!customerTransaction.isPresent()) {
            return Either.left(new GenericErrorResponse(reason: "Invalid transactionId"))
        }
        CustomerTransaction txn = customerTransaction.get()
        if (txn.txnStatus != TransactionStatus.APPROVED) {
            return Either.left(new GenericErrorResponse(reason: "This transaction has already been settled"))
        }
        double settlementBillingAmount = req.settlementAmount
        if (req.settlementCurrency != account.defaultCurrency) {
            settlementBillingAmount = getConvertedAmount(req.settlementCurrency, account.defaultCurrency, req.settlementAmount)
        }
        TransactionLedger ledgerEntry = createDebitEntry(txn, req.settlementAmount)
        account.availableCreditLimit -= (settlementBillingAmount - txn.billingAmount)
        if (settlementBillingAmount == txn.billingAmount) {
            txn.txnStatus = TransactionStatus.SETTLED
        } else {
            txn.txnStatus = TransactionStatus.PARTIALLY_SETTLED
        }
        creditAccountRepository.save(account)
        customerTxnRepository.save(txn)
        return Either.right(txn)
    }

    Either<GenericErrorResponse,CustomerTransaction> processSettlementCredit(AuthSettlementRequest req) {

        Account account = req.card.creditAccount
        Optional<CustomerTransaction> customerTransaction = customerTransactionRepository.findById(req.transactionId)
        if (!customerTransaction.isPresent()) {
            return Either.left(new GenericErrorResponse(reason: "Invalid transactionId"))
        }
        CustomerTransaction txn = customerTransaction.get()
        if (txn.txnStatus != TransactionStatus.APPROVED) {
            return Either.left(new GenericErrorResponse(reason: "This transaction has already been settled"))
        }
        double settlementBillingAmount = req.settlementAmount
        if (req.settlementCurrency != account.defaultCurrency) {
            settlementBillingAmount = getConvertedAmount(req.settlementCurrency, account.defaultCurrency, req.settlementAmount)
        }
        TransactionLedger ledgerEntry = createCreditEntry(txn, req.settlementAmount)
        account.availableCreditLimit += (settlementBillingAmount - txn.billingAmount)
        if (settlementBillingAmount == txn.billingAmount) {
            txn.txnStatus = TransactionStatus.SETTLED
        } else {
            txn.txnStatus = TransactionStatus.PARTIALLY_SETTLED
        }
        creditAccountRepository.save(account)
        customerTxnRepository.save(txn)
        return Either.right(txn)
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
        debitEntry.transactionType = getLedgerTransactionType(txn.transactionType)
        debitEntry.openingBalance = account.currentBalance
        debitEntry.closingBalance = account.currentBalance - txn.billingAmount
        debitEntry.transaction = txn
        debitEntry.creditAccount = account
        transactionLedgerRepository.save(debitEntry)
        account.currentBalance = debitEntry.closingBalance
        creditAccountRepository.save(account)
        return debitEntry
    }

    private TransactionLedger createDebitEntry(CustomerTransaction txn, Double settlementAmount) {
        CreditAccount account = txn.card.creditAccount
        TransactionLedger debitEntry = new TransactionLedger()
        debitEntry.transactionAmount = settlementAmount
        debitEntry.txnDescription = txn.txnDescription
        debitEntry.postingDate = ZonedDateTime.now()
        debitEntry.moneyMovementIndicator = MoneyMovementIndicator.DEBIT
        debitEntry.transactionType = LedgerTransactionType.PURCHASE
        debitEntry.openingBalance = account.currentBalance
        debitEntry.closingBalance = account.currentBalance - txn.billingAmount
        debitEntry.transaction = txn
        debitEntry.creditAccount = account
        transactionLedgerRepository.save(debitEntry)
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
        creditEntry.transactionType = getLedgerTransactionType(txn.transactionType)
        creditEntry.openingBalance = account.currentBalance
        creditEntry.closingBalance = account.currentBalance + txn.billingAmount
        creditEntry.transaction = txn
        creditEntry.creditAccount = account
        transactionLedgerRepository.save(creditEntry)
        account.currentBalance = creditEntry.closingBalance
        creditAccountRepository.save(account)
        return creditEntry
    }

    private TransactionLedger createCreditEntry(CustomerTransaction txn, Double settleAmount) {
        CreditAccount account = txn.card.creditAccount
        TransactionLedger creditEntry = new TransactionLedger()
        creditEntry.transactionAmount = settleAmount
        creditEntry.txnDescription = txn.txnDescription
        creditEntry.postingDate = ZonedDateTime.now()
        creditEntry.moneyMovementIndicator = MoneyMovementIndicator.CREDIT
        creditEntry.transactionType = getLedgerTransactionType(txn.transactionType)
        creditEntry.openingBalance = account.currentBalance
        creditEntry.closingBalance = account.currentBalance + settleAmount
        creditEntry.transaction = txn
        creditEntry.creditAccount = account
        transactionLedgerRepository.save(creditEntry)
        account.currentBalance = creditEntry.closingBalance
        creditAccountRepository.save(account)
        return creditEntry
    }


    public Double calculateInterest(CreditAccount account, TransactionLedger ledgerEntry) {
        Double amount = ledgerEntry.transactionAmount
        Double interestRateInPct = getInterestRateForTxn(ledgerEntry) / 100
        Integer noOfDays = ChronoUnit.DAYS.between(ledgerEntry.postingDate,account.currentBillingEndDate) + 1
        Integer noOfDaysInBaseYear = 365
        Double interestAmount = amount * (1/noOfDaysInBaseYear) * noOfDays * (interestRateInPct / 100)

        if (ledgerEntry.moneyMovementIndicator == MoneyMovementIndicator.CREDIT) {
            interestAmount = -interestAmount
        }
        return interestAmount
    }

    public Double calculateInterestByDate(CreditAccount account,ZonedDateTime startDate, ZonedDateTime endDate, Double amount) {
        Double interestRateInPct = account.cards[0].cardProgram.annualizedPercentageRateInBps / 100
        Integer noOfDays = ChronoUnit.DAYS.between(startDate, endDate) + 1
        Integer noOfDaysInBaseYear = 365
        Double interestAmount = amount * (1 / noOfDaysInBaseYear) * noOfDays * (interestRateInPct / 100)
        return interestAmount
    }

    private LedgerTransactionType getLedgerTransactionType(TransactionType txnType) {
        if (txnType.SETTLEMENT_DEBIT) {
            return LedgerTransactionType.PURCHASE
        } else if (txnType.SETTLEMENT_CREDIT) {
            return LedgerTransactionType.PURCHASE_REVERSAL
        } else if (txnType.SETTLEMENT_DEBIT_CASH) {
            return LedgerTransactionType.CASH_WITHDRAWAL
        } else if (txnType.SETTLEMENT_CREDIT_CASH) {
            return LedgerTransactionType.CASH_WITHDRAWAL_REFUND
        }
        return txnType as LedgerTransactionType
    }

    private Double getConvertedAmount(String sourceCurrency, String defaultCurrency, Double amount) {
        CurrencyConversion currencyConversion =
                currencyConversionRepository.findBySourceCurrencyAndDestinationCurrency(
                       sourceCurrency, defaultCurrency)
        return amount * currencyConversion.conversionRate

    }
}
