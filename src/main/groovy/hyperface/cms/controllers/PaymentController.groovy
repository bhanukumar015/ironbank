package hyperface.cms.controllers

import groovy.util.logging.Slf4j
import hyperface.cms.Constants
import hyperface.cms.appdata.TxnNotEligible
import hyperface.cms.commands.AuthorizationRequest
import hyperface.cms.commands.CustomerTransactionRequest
import hyperface.cms.commands.CustomerTransactionResponse
import hyperface.cms.commands.GenericErrorResponse
import hyperface.cms.commands.RejectTxnResponse
import hyperface.cms.domains.Card
import hyperface.cms.domains.CustomerTransaction
import hyperface.cms.domains.CustomerTxn
import hyperface.cms.repository.CardRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.service.AuthorizationManager
import hyperface.cms.service.PaymentService
import io.vavr.control.Either
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
@Slf4j
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
    public ResponseEntity performTransaction(@RequestBody CustomerTransactionRequest req) {
        Card card = cardRepository.findById(req.cardId).get()
        if (card == null) { // this validation should ideally move to CustomerTransactionRequest
            String errorMessage = "Card with ID ${req.cardId} does not exist."
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage)
        }
        req.card = card
        Either<TxnNotEligible, Boolean> result = paymentService.checkEligibility(req)
        if(result.isRight()) {
            return result
                    .map({return paymentService.createCustomerTxn(req)})
                    .map({return paymentService.getCustomerTransactionResponse(it)})
                    .map({returnSimpleJson(it) })
                    .get()
        }
        else {
            String reason = result.left().get().reason
            log.error("Failing card transaction for ${req.cardId} because ${reason}")
            return returnError(reason)
        }
    }

    private ResponseEntity returnSimpleJson(def resultObj) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(resultObj)
    }

    private ResponseEntity returnError(String errorMessage) {
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new GenericErrorResponse(reason: errorMessage))
    }
}