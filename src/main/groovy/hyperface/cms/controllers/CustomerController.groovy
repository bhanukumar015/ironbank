package hyperface.cms.controllers

import groovy.util.logging.Slf4j
import hyperface.cms.commands.CardTransaction
import hyperface.cms.commands.CreateCustomerRequest
import hyperface.cms.commands.FetchCardTransactionsRequest
import hyperface.cms.commands.GenericErrorResponse
import hyperface.cms.domains.Card
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.Customer
import hyperface.cms.domains.CustomerTxn
import hyperface.cms.domains.ledger.LedgerEntry
import hyperface.cms.repository.CardRepository
import hyperface.cms.repository.CustomerRepository
import hyperface.cms.repository.CustomerTxnRepository
import hyperface.cms.repository.LedgerEntryRepository
import hyperface.cms.service.CustomerService
import hyperface.cms.util.Response
import io.vavr.control.Either
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.validation.Valid

@RestController
@RequestMapping("/customers")
@Slf4j
class CustomerController {

    @Autowired
    CustomerRepository customerRepository

    @Autowired
    CustomerTxnRepository customerTxnRepository

    @Autowired
    CustomerService customerService

    @GetMapping(value = "/list")
    List<Customer> getCustomersList() {
        return customerRepository.findAll() as List<Customer>
    }

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity createCustomer(@Valid @RequestBody CreateCustomerRequest req){
        Either<GenericErrorResponse,Customer> customer = customerService.createCustomer(req)
        if(customer.isLeft()){
            String errorMessage = customer.left().get().reason
            log.error("Customer creation failed with error: ${errorMessage}")
            return Response.returnError(errorMessage)
        }
        return Response.returnSimpleJson(customer.right().get())
    }

    @GetMapping(value = "/{customerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity getCustomer(@PathVariable(name = "customerId", required = true) String customerId){
        Optional<Customer> customerOptional = customerRepository.findById(customerId)
        if(!customerOptional.isPresent()){
            String errorMessage = "No customer found with Id: ${customerId}"
            log.error("Request to get customer failed with error: ${errorMessage}")
            return Response.returnError(errorMessage)
        }
        Customer customer = customerOptional.get()
        return Response.returnSimpleJson(customer)
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