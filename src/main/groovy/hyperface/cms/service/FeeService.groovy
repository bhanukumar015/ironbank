package hyperface.cms.service

import hyperface.cms.domains.Card
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.CustomerTransaction
import hyperface.cms.domains.SystemTransaction
import hyperface.cms.domains.ledger.TransactionLedger
import hyperface.cms.model.enums.FeeType
import hyperface.cms.model.enums.LedgerTransactionType
import hyperface.cms.model.enums.MoneyMovementIndicator
import hyperface.cms.model.enums.TransactionSourceIndicator
import hyperface.cms.model.enums.TransactionType
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.repository.SystemTransactionRepository
import hyperface.cms.repository.TransactionLedgerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.time.ZonedDateTime

@Service
class FeeService {

    @Autowired
    CreditAccountRepository creditAccountRepository

    @Autowired
    SystemTransactionRepository systemTransactionRepository

    @Autowired
    TransactionLedgerRepository transactionLedgerRepository

    TransactionLedger createJoiningFeeEntry(CustomerTransaction txn){
        double joiningFee = txn.card.cardProgram.scheduleOfCharges.joiningFee.feeStrategy.getFee(0)
        CreditAccount account = txn.card.creditAccount
        SystemTransaction feeTxn = new SystemTransaction().tap{
            hasExecuted = true
            transactionReferenceId = txn.id
            feeType = FeeType.JOINING
            card = txn.card
            transactionAmount = joiningFee
            transactionCurrency = account.defaultCurrency
            transactionType = TransactionType.FEE
            txnDescription = String.format("Joining fee - %s", txn.card.cardProgram.name)
            txnSourceIndicator = TransactionSourceIndicator.SYSTEM_GENERATED
        }

        TransactionLedger feeEntry = new TransactionLedger().tap{
            transactionAmount = feeTxn.transactionAmount
            transactionType = LedgerTransactionType.FEE
            txnDescription = feeTxn.txnDescription
            postingDate = ZonedDateTime.now()
            openingBalance = account.currentBalance
            closingBalance = account.currentBalance - joiningFee
            moneyMovementIndicator = MoneyMovementIndicator.DEBIT
            transaction = feeTxn
        }

        systemTransactionRepository.save(feeTxn)
        transactionLedgerRepository.save(feeEntry)
        account.availableCreditLimit -= joiningFee
        account.currentBalance -= joiningFee
        creditAccountRepository.save(account)
        return feeEntry
    }

    TransactionLedger createMarkupFeeEntry(CustomerTransaction txn, double amount, Boolean isDebit){
        double markupFee = txn.card.cardProgram.scheduleOfCharges.forexFeeStrategy.getFee(amount)
        CreditAccount account = txn.card.creditAccount
        SystemTransaction feeTxn = new SystemTransaction().tap{
            hasExecuted = true
            transactionReferenceId = txn.id
            feeType = FeeType.FOREX_MARKUP
            card = txn.card
            transactionAmount = markupFee
            transactionCurrency = account.defaultCurrency
            txnDescription = String.format("Markup fee - %s", txn.card.cardProgram.name)
            txnSourceIndicator = TransactionSourceIndicator.SYSTEM_GENERATED
        }

        TransactionLedger feeEntry = new TransactionLedger().tap{
            transactionAmount = feeTxn.transactionAmount
            txnDescription = feeTxn.txnDescription
            postingDate = ZonedDateTime.now()
            openingBalance = account.currentBalance
            transaction = feeTxn
        }

        if(Boolean.TRUE == isDebit){
            feeTxn.transactionType = TransactionType.FEE
            feeEntry.transactionType = LedgerTransactionType.FEE
            feeEntry.closingBalance = account.currentBalance - markupFee
            feeEntry.moneyMovementIndicator = MoneyMovementIndicator.DEBIT
            account.availableCreditLimit -= markupFee
        }
        else{
            feeTxn.transactionType = TransactionType.FEE_REVERSAL
            feeEntry.transactionType = LedgerTransactionType.FEE_REVERSAL
            feeEntry.closingBalance = account.currentBalance + markupFee
            feeEntry.moneyMovementIndicator = MoneyMovementIndicator.CREDIT
            account.availableCreditLimit += markupFee
        }

        systemTransactionRepository.save(feeTxn)
        transactionLedgerRepository.save(feeEntry)
        account.currentBalance = feeEntry.closingBalance
        creditAccountRepository.save(account)
        return feeEntry
    }

    TransactionLedger createCashWithdrawalFeeEntry(CustomerTransaction txn, double amount, Boolean isDebit){
        double cashAdvanceFee = txn.card.cardProgram.scheduleOfCharges.cashAdvanceFeeStrategy.getFee(amount)
        CreditAccount account = txn.card.creditAccount
        SystemTransaction feeTxn = new SystemTransaction().tap{
            hasExecuted = true
            transactionReferenceId = txn.id
            feeType = FeeType.CASH_ADVANCE_FEE
            card = txn.card
            transactionAmount = cashAdvanceFee
            transactionCurrency = account.defaultCurrency
            txnDescription = String.format("Cash advance fee for transaction %s", txn.card.cardProgram.name)
            txnSourceIndicator = TransactionSourceIndicator.SYSTEM_GENERATED
        }

        TransactionLedger feeEntry = new TransactionLedger().tap{
            transactionAmount = feeTxn.transactionAmount
            txnDescription = feeTxn.txnDescription
            postingDate = ZonedDateTime.now()
            openingBalance = account.currentBalance
            transaction = feeTxn
        }

        if(Boolean.TRUE == isDebit){
            feeTxn.transactionType = TransactionType.FEE
            feeEntry.transactionType = LedgerTransactionType.FEE
            feeEntry.closingBalance = account.currentBalance - cashAdvanceFee
            feeEntry.moneyMovementIndicator = MoneyMovementIndicator.DEBIT
            account.availableCreditLimit -= cashAdvanceFee
        }
        else{
            feeTxn.transactionType = TransactionType.FEE_REVERSAL
            feeEntry.transactionType = LedgerTransactionType.FEE_REVERSAL
            feeEntry.closingBalance = account.currentBalance + cashAdvanceFee
            feeEntry.moneyMovementIndicator = MoneyMovementIndicator.CREDIT
            account.availableCreditLimit += cashAdvanceFee
        }

        systemTransactionRepository.save(feeTxn)
        transactionLedgerRepository.save(feeEntry)
        account.currentBalance = feeEntry.closingBalance
        creditAccountRepository.save(account)
        return feeEntry
    }

    TransactionLedger createAddOnCardFeeEntry(Card primaryCard){
        double addOnCardFee = primaryCard.cardProgram.scheduleOfCharges.addonCardFeeStrategy.getFee(0)
        CreditAccount account = primaryCard.creditAccount
        SystemTransaction feeTxn = new SystemTransaction().tap{
            hasExecuted = true
            feeType = FeeType.ADD_ONCARD
            card = primaryCard
            transactionAmount = addOnCardFee
            transactionCurrency = account.defaultCurrency
            transactionType = TransactionType.FEE
            txnDescription = String.format("Add-on card fee - %s", card.cardProgram.name)
            txnSourceIndicator = TransactionSourceIndicator.SYSTEM_GENERATED
        }

        TransactionLedger feeEntry = new TransactionLedger().tap{
            transactionAmount = feeTxn.transactionAmount
            transactionType = LedgerTransactionType.FEE
            txnDescription = feeTxn.txnDescription
            postingDate = ZonedDateTime.now()
            openingBalance = account.currentBalance
            closingBalance = account.currentBalance - addOnCardFee
            moneyMovementIndicator = MoneyMovementIndicator.DEBIT
            transaction = feeTxn
        }

        systemTransactionRepository.save(feeTxn)
        transactionLedgerRepository.save(feeEntry)
        account.availableCreditLimit -= addOnCardFee
        account.currentBalance -= addOnCardFee
        creditAccountRepository.save(account)
        return feeEntry
    }

    TransactionLedger createRewardRedemptionFeeEntry(CreditAccount account){
        CreditCardProgram cardProgram = account.cards.find {card -> card.isPrimaryCard}?.cardProgram
        double rewardFee = cardProgram.scheduleOfCharges.rewardRedemptionFeeStrategy.getFee(0)
        SystemTransaction feeTxn = new SystemTransaction().tap{
            hasExecuted = true
            feeType = FeeType.REWARDS_REDEMPTION
            transactionAmount = rewardFee
            transactionCurrency = account.defaultCurrency
            transactionType = TransactionType.FEE
            txnDescription = String.format("Rewards redemption fee - %s", cardProgram.name)
            txnSourceIndicator = TransactionSourceIndicator.SYSTEM_GENERATED
        }

        TransactionLedger feeEntry = new TransactionLedger().tap{
            transactionAmount = feeTxn.transactionAmount
            transactionType = LedgerTransactionType.FEE
            txnDescription = feeTxn.txnDescription
            postingDate = ZonedDateTime.now()
            openingBalance = account.currentBalance
            closingBalance = account.currentBalance - rewardFee
            moneyMovementIndicator = MoneyMovementIndicator.DEBIT
            transaction = feeTxn
        }

        systemTransactionRepository.save(feeTxn)
        transactionLedgerRepository.save(feeEntry)
        account.availableCreditLimit -= rewardFee
        account.currentBalance -= rewardFee
        creditAccountRepository.save(account)
        return feeEntry
    }

    TransactionLedger createOverlimitFeeEntry(CustomerTransaction txn, Boolean isDebit){
        Double amount = txn.billingAmount - txn.card.creditAccount.availableCreditLimit
        double overlimitFee = txn.card.cardProgram.scheduleOfCharges.overlimitFeeStrategy.getFee(amount)
        CreditAccount account = txn.card.creditAccount
        SystemTransaction feeTxn = new SystemTransaction().tap{
            hasExecuted = true
            transactionReferenceId = txn.id
            feeType = FeeType.OVERLIMIT
            card = txn.card
            transactionAmount = overlimitFee
            transactionCurrency = account.defaultCurrency
            txnDescription = String.format("Overlimit fee %s", txn.card.cardProgram.name)
            txnSourceIndicator = TransactionSourceIndicator.SYSTEM_GENERATED
        }

        TransactionLedger feeEntry = new TransactionLedger().tap{
            transactionAmount = feeTxn.transactionAmount
            txnDescription = feeTxn.txnDescription
            postingDate = ZonedDateTime.now()
            openingBalance = account.currentBalance
            transaction = feeTxn
        }

        if(Boolean.TRUE == isDebit){
            feeTxn.transactionType = TransactionType.FEE
            feeEntry.transactionType = LedgerTransactionType.FEE
            feeEntry.closingBalance = account.currentBalance - overlimitFee
            feeEntry.moneyMovementIndicator = MoneyMovementIndicator.DEBIT
            account.availableCreditLimit -= overlimitFee
        }
        else{
            feeTxn.transactionType = TransactionType.FEE_REVERSAL
            feeEntry.transactionType = LedgerTransactionType.FEE_REVERSAL
            feeEntry.closingBalance = account.currentBalance + overlimitFee
            feeEntry.moneyMovementIndicator = MoneyMovementIndicator.CREDIT
            account.availableCreditLimit += overlimitFee
        }

        systemTransactionRepository.save(feeTxn)
        transactionLedgerRepository.save(feeEntry)
        account.currentBalance = feeEntry.closingBalance
        creditAccountRepository.save(account)
        return feeEntry
    }
}
