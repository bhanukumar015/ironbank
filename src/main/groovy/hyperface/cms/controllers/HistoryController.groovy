package hyperface.cms.controllers

import hyperface.cms.Constants
import hyperface.cms.commands.AuthorizationRequest
import hyperface.cms.commands.CreateCardRequest
import hyperface.cms.commands.CreateCreditAccountRequest
import hyperface.cms.commands.RejectTxnResponse
import hyperface.cms.domains.*
import hyperface.cms.repository.CardProgramRepository
import hyperface.cms.repository.CardRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.repository.CustomerRepository
import hyperface.cms.service.AccountService
import hyperface.cms.service.AuthorizationManager
import hyperface.cms.service.PaymentService
import io.vavr.control.Either
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/history")
public class HistoryController {

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

    @RequestMapping(value = "/createTxn", method = RequestMethod.POST,
                    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, String> createTxn(AuthorizationRequest req) {
        println req.dump()
        Card card = cardRepository.findById(req.cardId).get()
        req.card = card
        if(req.transactionType == Constants.TxnType.AUTH) {
            return performAuth(req)
        }
        else if(req.transactionType == Constants.TxnType.REFUND) {
            return performRefund(req)
        }
        else if(req.transactionType == Constants.TxnType.SETTLE_DEBIT) {
            // TODO - implementation
            // create CustomerTxn if not exists
            // Process settlement
        }
        return [responseCode: "01", 'partnerReferenceNumber': req.internalReferenceId]
    }

    private Map<String, String> performRefund(AuthorizationRequest req) {
        Either<RejectTxnResponse, CustomerTxn> refundResp = authorizationManager.performRefund(req)
        return handleAuthResponse(refundResp)
    }

    private Map<String, String> performAuth(AuthorizationRequest req) {
        Either<RejectTxnResponse, CustomerTxn> authDebitResp = authorizationManager.performAuth(req)
        return handleAuthResponse(authDebitResp)
    }

    private Map<String, String> handleAuthResponse(Either<RejectTxnResponse, CustomerTxn> authResponse) {
        if(authResponse.isLeft()) {
            RejectTxnResponse rtr = authResponse.getLeft()
            return [responseCode: rtr.rejectionCode, partnerReferenceNumber: System.currentTimeMillis().toString()]
        }
        else {
            CustomerTxn txn = authResponse.get()
            return ["responseCode": "00", "partnerReferenceNumber": txn.id.toString()]
        }
    }
}