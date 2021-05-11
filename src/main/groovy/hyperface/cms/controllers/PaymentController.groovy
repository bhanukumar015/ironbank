package hyperface.cms.controllers

import hyperface.cms.Constants
import hyperface.cms.commands.AuthorizationRequest
import hyperface.cms.commands.CreateCardRequest
import hyperface.cms.commands.CreateCreditAccountRequest
import hyperface.cms.commands.RejectTxnResponse
import hyperface.cms.domains.Card
import hyperface.cms.domains.CardProgram
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.Customer
import hyperface.cms.domains.CustomerTxn
import hyperface.cms.repository.CardProgramRepository
import hyperface.cms.repository.CardRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.repository.CustomerRepository
import hyperface.cms.service.AccountService
import hyperface.cms.service.AuthorizationManager
import hyperface.cms.service.PaymentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    CustomerRepository customerRepository

    @Autowired
    CreditAccountRepository creditAccountRepository

    @Autowired
    CardProgramRepository cardProgramRepository

    @Autowired
    AccountService accountService

    @Autowired
    PaymentService paymentService

    @Autowired
    CardRepository cardRepository

    @Autowired
    AuthorizationManager authorizationManager

    @RequestMapping(value = "/authorize", method = RequestMethod.POST,
                    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, String> authorize(AuthorizationRequest req) {
        println req.dump()
        Card card = cardRepository.findById(req.cardId).get()
        req.card = card
        if(req.transactionType == AuthorizationRequest.TransactionType.DEBIT) {
            return performAuthDebit(req)
        }
        else if(req.transactionType == AuthorizationRequest.TransactionType.REVERSAL) {
            return performAuthReversal(req)
        }
        return [responseCode: "01", 'partnerReferenceNumber': System.currentTimeMillis().toString()]
    }

    private Map<String, String> performAuthReversal(AuthorizationRequest req) {
        Optional<RejectTxnResponse> rejectResp = authorizationManager.shouldRejectReversalTxn(req)
        if(rejectResp.isPresent()) {
            RejectTxnResponse rtr = rejectResp.get()
            return [responseCode: rtr.rejectionCode, partnerReferenceNumber: System.currentTimeMillis().toString()]
        }
        else {
            CustomerTxn txn = paymentService.processReversalRequest(req)
            return ["responseCode": "00", "partnerReferenceNumber": txn.id.toString()]
        }
    }

    private Map<String, String> performAuthDebit(AuthorizationRequest req) {
        Optional<RejectTxnResponse> rejectResp = authorizationManager.shouldRejectAuthDebitTxn(req)
        if(rejectResp.isPresent()) {
            RejectTxnResponse rtr = rejectResp.get()
            return [responseCode: rtr.rejectionCode, partnerReferenceNumber: System.currentTimeMillis().toString()]
        }
        else {
            CustomerTxn txn = paymentService.processAuthorization(req)
            return ["responseCode": "00", "partnerReferenceNumber": txn.id.toString()]
        }
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