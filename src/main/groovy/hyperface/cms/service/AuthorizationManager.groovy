package hyperface.cms.service

import hyperface.cms.Constants
import hyperface.cms.commands.AuthorizationRequest
import hyperface.cms.commands.RejectTxnResponse
import hyperface.cms.domains.Card
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CustomerTxn
import hyperface.cms.domains.ledger.LedgerEntry
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.repository.CustomerTxnRepository
import hyperface.cms.repository.LedgerEntryRepository
import io.vavr.control.Either
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class AuthorizationManager {

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository

    @Autowired
    private CreditAccountRepository creditAccountRepository

    @Autowired
    private CustomerTxnRepository customerTxnRepository

    @Autowired
    PaymentService paymentService


    public Either<RejectTxnResponse, CustomerTxn> performAuth(AuthorizationRequest req) {
        Card card = req.card
        Optional<RejectTxnResponse> rejectTxnResponse = this.shouldRejectAuthTxn(req)
        if (rejectTxnResponse.isPresent()) {
            return Either.left(rejectTxnResponse.get())
        }
        else {
            CustomerTxn customerTxn = paymentService.processAuthorization(req)
            return Either.right(customerTxn)
        }
    }

    public Either<RejectTxnResponse, CustomerTxn> performRefund(AuthorizationRequest req) {
        Card card = req.card
        Optional<RejectTxnResponse> rejectTxnResponse = this.shouldRejectRefundTxn(req)
        if (rejectTxnResponse.isPresent()) {
            return Either.left(rejectTxnResponse.get())
        }
        else {
            CustomerTxn customerTxn = paymentService.processAuthorization(req)
            return Either.right(customerTxn)
        }
    }


    public Optional<RejectTxnResponse> shouldRejectAuthTxn(AuthorizationRequest req) {
        Card card = req.card
        Optional<RejectTxnResponse> rejectTxnResponse = new Optional<RejectTxnResponse>()
        def setRejectReason = { Constants.RejectionCode rc->
            rejectTxnResponse = new Optional<RejectTxnResponse>(new RejectTxnResponse(rc))
        }
        if (card.physicalCardActivatedByCustomer == false) {
            setRejectReason(Constants.RejectionCode.CARD_NOT_ACTIVATED)
        }
        if (req.billingAmount > card.creditAccount.availableCreditLimit) {
            setRejectReason(Constants.RejectionCode.INSUFFICIENT_FUNDS)
        }
        if (card.hotlisted == true) {
            setRejectReason(Constants.RejectionCode.CARD_HOTLISTED)
        }
        return rejectTxnResponse
    }

    public Optional<RejectTxnResponse> shouldRejectRefundTxn(AuthorizationRequest req) {
        Card card = req.card
        Optional<RejectTxnResponse> rejectTxnResponse = new Optional<RejectTxnResponse>()
        def setRejectReason = { Constants.RejectionCode rc->
            rejectTxnResponse = new Optional<RejectTxnResponse>(new RejectTxnResponse(rc))
        }
        // TODO - ensure that the amount requested for reversal is within bounds of the actual transaction
        if(card.hotlisted == true) {
            setRejectReason(Constants.RejectionCode.CARD_HOTLISTED)
        }
        return rejectTxnResponse
    }

}
