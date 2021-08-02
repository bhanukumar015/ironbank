package hyperface.cms.service

import groovy.util.logging.Slf4j
import hyperface.cms.Constants
import hyperface.cms.domains.CardStatement
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.CreditCardScheduleOfCharges
import hyperface.cms.domains.interest.Condition
import hyperface.cms.domains.interest.InterestCriteria
import hyperface.cms.domains.ledger.TransactionLedger
import hyperface.cms.model.enums.BillingStatus
import hyperface.cms.model.enums.LedgerTransactionType
import hyperface.cms.model.enums.MoneyMovementIndicator
import hyperface.cms.model.enums.RepaymentIndicator
import hyperface.cms.repository.CardStatementRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.repository.TransactionLedgerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.time.ZonedDateTime

@Service
@Slf4j
class BillingService {

    @Autowired
    CardStatementRepository cardStatementRepository

    @Autowired
    PaymentService paymentService

    @Autowired
    CreditAccountRepository creditAccountRepository

    @Autowired
    TransactionLedgerRepository transactionLedgerRepository

    public Boolean generateStatement(CreditAccount account) {
        Double billedInterest = 0.0
        Double waivedInterest = 0.0
        Double deferredInterest = 0.0
        Double residualInterest = 0.0
        Double interestAmount = 0.0
        Double totalDebits = 0.0
        Double totalCredits = 0.0
        Double netCashback = 0.0
        Double netFeeCharges = 0.0
        Double netFinanceCharges = 0.0
        Double netPurchases = 0.0
        Double netRepayments = 0.0
        Double refunds = 0.0
        Double openingBalance = 0.0
        Double closingBalance = 0.0
        CardStatement lastStatement = null
        if (account.lastStatementId) {
            lastStatement = cardStatementRepository.findById(account.lastStatementId).get()
        }
        switch (account.billingStatus) {
            case BillingStatus.BILLING_INITIATED:
                changeRepaymentIndicator(account)
                account.billingStatus = BillingStatus.CHANGED_REPAYMENT_INDICATOR
                creditAccountRepository.save(account)

            case BillingStatus.CHANGED_REPAYMENT_INDICATOR:
                //Fee Module
                account.billingStatus = BillingStatus.FEE_BILLED
                creditAccountRepository.save(account)
            case BillingStatus.FEE_BILLED:
                List<TransactionLedger> transactionLedgerList = transactionLedgerRepository.findAllByCreditAccountInRange(account,account.currentBillingStartDate,account.currentBillingEndDate).toList()
                for (def transactionLedger : transactionLedgerList) {
                    interestAmount += paymentService.calculateInterest(account,transactionLedger)
                }
                if (account.previousCycleRepaymentIndicator == RepaymentIndicator.TRANSACTOR
                        && account.currentCycleRepaymentIndicator == RepaymentIndicator.TRANSACTOR ) {
                    if (account.lastStatementId) {
                        waivedInterest = lastStatement.deferredInterest
                        residualInterest = paymentService.calculateInterestByDate(account.currentBillingStartDate,account.currentBillingEndDate,lastStatement.unpaidResidualBalance)
                    }
                    deferredInterest = Math.max(0.0, interestAmount)
                } else if (account.previousCycleRepaymentIndicator == RepaymentIndicator.REVOLVER
                        && account.currentCycleRepaymentIndicator == RepaymentIndicator.TRANSACTOR ) {
                    residualInterest = paymentService.calculateInterestByDate(account.currentBillingStartDate,account.currentBillingEndDate,lastStatement.unpaidResidualBalance)
                    billedInterest += lastStatement.deferredInterest + interestAmount + residualInterest
                } else if (account.previousCycleRepaymentIndicator == RepaymentIndicator.TRANSACTOR
                        && account.currentCycleRepaymentIndicator == RepaymentIndicator.REVOLVER ) {
                    Double repaymentCreditInterest = 0.0
                    Double repaymentCreditAmount = 0.0
                    List<TransactionLedger> creditTransactions = transactionLedgerRepository.findAllByCreditAccountInRange(account,account.currentBillingStartDate,lastStatement.dueDate,MoneyMovementIndicator.CREDIT).toList()
                    for (def creditTransaction : creditTransactions) {
                        repaymentCreditAmount += creditTransaction.transactionAmount
                        if (repaymentCreditAmount >= lastStatement.totalAmountDue)
                            break
                    }
                    billedInterest += residualInterest + repaymentCreditInterest
                    deferredInterest = Math.max(0.0, interestAmount - repaymentCreditInterest)
                } else {
                    residualInterest = paymentService.calculateInterestByDate(account.currentBillingStartDate,account.currentBillingEndDate,lastStatement.unpaidResidualBalance)
                    billedInterest += residualInterest + interestAmount
                }

                // Create systemTransaction to billed the interest
                account.billingStatus = BillingStatus.INTEREST_BILLED

            case BillingStatus.INTEREST_BILLED:
                List<TransactionLedger> transactionLedgerList = transactionLedgerRepository.findAllByCreditAccountInRange(account,account.currentBillingStartDate,account.currentBillingEndDate).toList()
                Integer firstIndex = 0
                Integer lastIndex = transactionLedgerList.size() - 1
                Integer index = 0
                for (def transactionLedger : transactionLedgerList) {
                    if (index == lastIndex) {
                        openingBalance = transactionLedger.openingBalance
                    }
                    if (index == firstIndex) {
                        closingBalance = transactionLedger.closingBalance
                    }
                    if (transactionLedger.moneyMovementIndicator == MoneyMovementIndicator.CREDIT) {
                        totalCredits += transactionLedger.transactionAmount
                    } else {
                        totalDebits += transactionLedger.transactionAmount
                    }
                    switch(transactionLedger.transactionType) {
                        case LedgerTransactionType.PURCHASE:
                        case LedgerTransactionType.CASH_WITHDRAWAL:
                            netPurchases += transactionLedger.transactionAmount
                            break
                        case LedgerTransactionType.PURCHASE_REVERSAL:
                        case LedgerTransactionType.CASH_WITHDRAWAL_REFUND:
                            netPurchases -= transactionLedger.transactionAmount
                            refunds += transactionLedger.transactionAmount
                            break
                        case LedgerTransactionType.REPAYMENT:
                            netRepayments += transactionLedger.transactionAmount
                            break
                        case LedgerTransactionType.REPAYMENT_REVERSAL:
                            netRepayments -= transactionLedger.transactionAmount
                            break
                        case LedgerTransactionType.INTEREST:
                            netFinanceCharges += transactionLedger.transactionAmount
                            break
                        case LedgerTransactionType.INTEREST_REVERSAL:
                            netFinanceCharges -= transactionLedger.transactionAmount
                            break
                        case LedgerTransactionType.CASHBACK:
                            netCashback += transactionLedger.transactionAmount
                            break
                        case LedgerTransactionType.CASHBACK_REVERSAL:
                            netCashback -= transactionLedger.transactionAmount
                            break
                        case LedgerTransactionType.FEE:
                            netFeeCharges += transactionLedger.transactionAmount
                            break
                        case LedgerTransactionType.FEE_REVERSAL:
                            netFeeCharges -= transactionLedger.transactionAmount
                            break
                    }
                    index++
                }

                //When Current Billing Cycle has zero transaction
                if (transactionLedgerList.size() == 0 && account.lastStatementId) {
                    openingBalance = lastStatement.closingBalance
                    closingBalance = lastStatement.closingBalance
                }
                Double totalAmountDue = Math.abs(openingBalance) + totalDebits - totalCredits
                Double minAmountDue = totalAmountDue * 0.05 + transactionLedgerRepository.getSumByCreditAccountAndTxnTypeInRange(account,LedgerTransactionType.TAX,account.currentBillingStartDate,account.currentBillingEndDate) ?: 0.0
                CardStatement cardStatement = new CardStatement()
                cardStatement.totalAmountDue = totalAmountDue
                cardStatement.minAmountDue = Math.max(minAmountDue, account.cards[0].cardProgram.minimumAmountDueFloor)
                cardStatement.dueDate = ZonedDateTime.now().plusDays(account.cards[0].cardProgram.gracePeriodInDays)
                cardStatement.totalCredits = totalDebits
                cardStatement.totalDebits = totalCredits
                cardStatement.openingBalance = openingBalance
                cardStatement.closingBalance = closingBalance
                cardStatement.deferredInterest = deferredInterest
                cardStatement.residualInterest = residualInterest
                cardStatement.unpaidResidualBalance = closingBalance
                cardStatement.billedInterest = billedInterest
                cardStatement.waivedInterest = waivedInterest
                cardStatement.netTaxOnInterest = netFinanceCharges * 0.18
                cardStatement.netTaxOnFees = netFeeCharges * 0.18
                cardStatement.netFinanceCharges = netFinanceCharges
                cardStatement.netFeeCharges = netFeeCharges
                cardStatement.netRepayments = netRepayments
                cardStatement.refunds = refunds
                cardStatement.netCashback = netCashback
                cardStatement.netPurchases = netPurchases
                cardStatement.billingCycleNumber = account.currentBillingCycle + 1
                cardStatement.generatedOn = ZonedDateTime.now()
                cardStatement.creditAccount = account

                account.currentBillingCycle += 1
                account.currentBillingStartDate = account.currentBillingStartDate.plusMonths(1)
                account.currentBillingEndDate = account.currentBillingEndDate.plusMonths(1)
                account.billingStatus = BillingStatus.BILLED
                cardStatementRepository.save(cardStatement)
                creditAccountRepository.save(account)

        }
        return true
    }

    void changeRepaymentIndicator(CreditAccount account) {
        account.previousCycleRepaymentIndicator = account.currentCycleRepaymentIndicator
        if (account.lastStatementId) {
            CardStatement lastStatement = cardStatementRepository.findById(account.lastStatementId).get()
            Double totalRepayment = 0.0
            List<TransactionLedger> transactionLedgerList = transactionLedgerRepository.findAllByCreditAccountInRange(account,account.currentBillingStartDate,lastStatement.dueDate).toList()
            for (def transactionLedger : transactionLedgerList) {
                totalRepayment += transactionLedger.transactionAmount
            }
            if (totalRepayment < lastStatement.totalAmountDue) {
                account.currentCycleRepaymentIndicator = RepaymentIndicator.TRANSACTOR
            } else {
                account.currentCycleRepaymentIndicator = RepaymentIndicator.REVOLVER
            }
        }
        creditAccountRepository.save(account)
    }
}
