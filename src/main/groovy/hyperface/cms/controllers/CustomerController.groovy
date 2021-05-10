package hyperface.cms.controllers

import hyperface.cms.Constants
import hyperface.cms.commands.CreateCardRequest
import hyperface.cms.commands.CreateCreditAccountRequest
import hyperface.cms.domains.Card
import hyperface.cms.domains.CardProgram
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.Customer
import hyperface.cms.repository.CardBinRepository
import hyperface.cms.repository.CardProgramRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.repository.CustomerRepository
import hyperface.cms.service.AccountService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    CustomerRepository customerRepository

    @Autowired
    CreditAccountRepository creditAccountRepository

    @Autowired
    CardProgramRepository cardProgramRepository

    @Autowired
    AccountService accountService

    @GetMapping(value = "/list")
    public List<Customer> getCustomers() {
        return customerRepository.findAll()
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Customer createOrSave(Customer customer) {
        println customer.dump()
        return customerRepository.save(customer)
    }

    // this will be allowed only by
    @RequestMapping(value = "/createCreditAccount", method = RequestMethod.POST,
                    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CreditAccount createCreditAccount(CreateCreditAccountRequest req) {
        println req.dump()
        Long customerId = req.customerId
        Customer customer = customerRepository.findById(customerId).get()
        Integer approvedCreditLimit = req.approvedCreditLimit
        CreditAccount creditAccount = accountService.createCreditAccount(customer, Constants.Currency.INR, approvedCreditLimit)
        return creditAccount
    }

    @RequestMapping(value = "/createCard", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Card createCard(CreateCardRequest req) {
        println req.dump()
        Customer customer = customerRepository.findById(req.customerId).get()
        CreditAccount creditAccount = creditAccountRepository.findById(req.creditAccountId).get()
        CardProgram cardProgram = cardProgramRepository.findById(req.cardProgramId).get()

        // check if a card already exists for this customer under this program


        Card card = accountService.createCard(customer, creditAccount, cardProgram)

        return card
    }
}