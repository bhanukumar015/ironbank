package hyperface.cms.controllers

import hyperface.cms.Constants
import hyperface.cms.commands.CardTransaction
import hyperface.cms.commands.CreateCardRequest
import hyperface.cms.commands.CreateCreditAccountRequest
import hyperface.cms.commands.FetchCardTransactionsRequest
import hyperface.cms.domains.Card
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.Customer
import hyperface.cms.domains.CustomerTxn
import hyperface.cms.domains.ledger.LedgerEntry
import hyperface.cms.repository.CardRepository
import hyperface.cms.repository.CustomerRepository
import hyperface.cms.repository.CustomerTxnRepository
import hyperface.cms.repository.LedgerEntryRepository
import hyperface.cms.service.AccountService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/customers")
class CustomerController {

    @Autowired
    CustomerRepository customerRepository

    @Autowired
    AccountService accountService

    @Autowired
    CustomerTxnRepository customerTxnRepository

    @GetMapping(value = "/list")
    List<Customer> getCustomers() {
        return customerRepository.findAll()
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Customer createOrSave(Customer customer) {
        println customer.dump()
        return customerRepository.save(customer)
    }

    // this will be allowed only by
    @RequestMapping(value = "/createCreditAccount", method = RequestMethod.POST,
                    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    CreditAccount createCreditAccount(CreateCreditAccountRequest req) {
        println req.dump()
        String customerId = req.customerId
        Customer customer = customerRepository.findById(customerId).get()
        Integer approvedCreditLimit = req.approvedCreditLimit
        CreditAccount creditAccount = accountService.createCreditAccount(customer, Constants.Currency.INR, approvedCreditLimit)
        return creditAccount
    }

    @RequestMapping(value = "/createCard", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Card createCard(CreateCardRequest req) {
        println req.dump()

        // check if a card already exists for this customer under this program
        Card card = accountService.createCard(req)

        return card
    }

    @Autowired
    CardRepository cardRepository

    @Autowired
    LedgerEntryRepository ledgerEntryRepository

    @RequestMapping(value = "/fetchTransactions", method = RequestMethod.GET)
    List<CardTransaction> fetchCardTransactions(@RequestParam("cardId") String cardId) {
        println "Input cardId: ${cardId}"
        FetchCardTransactionsRequest req = new FetchCardTransactionsRequest(cardId: cardId)
        Date from = req.from?:getLastMonthBeginning()
        Date to = req.to?:(new Date())
        Card card = cardRepository.findById(req.cardId).get()
        CreditAccount creditAccount = card.creditAccount
        List<CustomerTxn> customerTxns = customerTxnRepository.findAllByAccountInRange(card.creditAccount, from, to)
        println customerTxns
        List<LedgerEntry> ledgerEntries = ledgerEntryRepository.findAllByCreditAccountInRange(creditAccount, from, to)
        println ledgerEntries
        // lets merge the two
        List<CustomerTxn> overlappingCustomerTxns = ledgerEntries.collect {it.customerTxn}
                                                        .findAll{it}
        customerTxns.removeAll(overlappingCustomerTxns)
        List<CardTransaction> cardTransactions = new ArrayList<>(ledgerEntries.size() + customerTxns.size())
        cardTransactions.addAll(customerTxns.collect {new CardTransaction(it)})
        cardTransactions.addAll(ledgerEntries.collect {new CardTransaction(it)})
        return cardTransactions.sort{it.transactedOn}.reverse()
    }

    private Date getLastMonthBeginning() {
        Calendar calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        return calendar.getTime()
    }
}