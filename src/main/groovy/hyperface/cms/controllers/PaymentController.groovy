package hyperface.cms.controllers

import hyperface.cms.Constants
import hyperface.cms.commands.AuthorizationRequest
import hyperface.cms.commands.CustomerTransactionRequest
import hyperface.cms.commands.CustomerTransactionResponse
import hyperface.cms.commands.RejectTxnResponse
import hyperface.cms.domains.Card
import hyperface.cms.domains.CustomerTransaction
import hyperface.cms.domains.CustomerTxn
import hyperface.cms.repository.CardRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.service.AuthorizationManager
import hyperface.cms.service.PaymentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    PaymentService paymentService

    @Autowired
    CardRepository cardRepository

    @Autowired
    AuthorizationManager authorizationManager

    @Autowired
    CreditAccountRepository creditAccountRepository

    @RequestMapping(value = "/authorize", method = RequestMethod.POST,
                    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, String> authorize(AuthorizationRequest req) {
        println req.dump()
        Card card = cardRepository.findById(req.cardId).get()
        req.card = card
        if(req.transactionType == Constants.TxnType.AUTH) {
            return performAuthDebit(req)
        }
        else if(req.transactionType == Constants.TxnType.AUTH_REVERSAL) {
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

    @RequestMapping(value = "/performTransaction", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomerTransactionResponse> performTransaction(@RequestBody CustomerTransactionRequest req) {

        println req.dump()
        Card card = cardRepository.findById(req.cardId).get()
        if (req.card == null) {
            String errorMessage = "Card with ID: [" + req.cardId + "] does not exist."
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage)
        }
        req.card = card
        CustomerTransaction txn
        if ( paymentService.checkTransactionEligibility(req) )
            txn = paymentService.createCustomerTxn(req)
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(paymentService.getCustomerTransactionResponse(txn))
    }
}