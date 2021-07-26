package hyperface.cms.controllers

import groovy.util.logging.Slf4j
import hyperface.cms.appdata.TxnNotEligible
import hyperface.cms.commands.AuthSettlementRequest
import hyperface.cms.commands.GenericErrorResponse
import hyperface.cms.domains.*
import hyperface.cms.repository.CardRepository
import hyperface.cms.service.AuthorizationManager
import hyperface.cms.service.PaymentService
import hyperface.cms.util.Response
import io.vavr.control.Either
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

import javax.validation.Valid

@RestController
@RequestMapping("/settlements")
@Slf4j
public class SettlementController {

    @Autowired
    PaymentService paymentService

    @Autowired
    CardRepository cardRepository

    @Autowired
    AuthorizationManager authorizationManager

    @RequestMapping(value = "/debit", method = RequestMethod.POST,
                    consumes = MediaType.APPLICATION_JSON_VALUE,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity settlementDebit(@Valid @RequestBody AuthSettlementRequest req) {
        Optional<Card> card = cardRepository.findById(req.cardId)
        if ( !card.isPresent()) {
            String errorMessage = "Card not found"
            log.error("Failing card transaction for ${req.cardId} because ${errorMessage}")
            return Response.returnError(errorMessage)
        }
        req.card = card.get()
        Either<TxnNotEligible, Boolean> result = authorizationManager.checkEligibility(req)
        if(result.isRight()) {
            Either<GenericErrorResponse,CustomerTransaction> txnResult = paymentService.processSettlementDebit(req)
            if (txnResult.isRight()) {
                return Response.returnSimpleJson(paymentService.getCustomerTransactionResponse(txnResult.right().get()))
            } else {
                String reason = txnResult.left().get().reason
                log.error("Failing card transaction for ${req.cardId} because ${reason}")
                return Response.returnError(reason)
            }
        } else {
            String reason = result.left().get().reason
            log.error("Failing card transaction for ${req.cardId} because ${reason}")
            return Response.returnError(reason)
        }
    }

    @RequestMapping(value = "/credit", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity settlementCredit(@Valid @RequestBody AuthSettlementRequest req) {
        Optional<Card> card = cardRepository.findById(req.cardId)
        if ( !card.isPresent()) {
            String errorMessage = "Card not found"
            log.error("Failing card transaction for ${req.cardId} because ${errorMessage}")
            return Response.returnError(errorMessage)
        }
        req.card = card.get()
        Either<TxnNotEligible, Boolean> result = authorizationManager.checkEligibility(req)
        if(result.isRight()) {
            Either<GenericErrorResponse,CustomerTransaction> txnResult = paymentService.processSettlementCredit(req)
            if (txnResult.isRight()) {
                return Response.returnSimpleJson(paymentService.getCustomerTransactionResponse(txnResult.right().get()))
            } else {
                String reason = txnResult.left().get().reason
                log.error("Failing card transaction for ${req.cardId} because ${reason}")
                return Response.returnError(reason)
            }
        } else {
            String reason = result.left().get().reason
            log.error("Failing card transaction for ${req.cardId} because ${reason}")
            return Response.returnError(reason)
        }
    }
}