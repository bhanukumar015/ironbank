package hyperface.cms.domains.cardapplication

import hyperface.cms.commands.cardapplication.CardApplicationResponse
import hyperface.cms.commands.cardapplication.CardEligibilityResponse
import hyperface.cms.commands.cardapplication.CustomerBankVerificationResponse
import hyperface.cms.commands.cardapplication.FdBookingResponse
import hyperface.cms.commands.cardapplication.FixedDepositFundTransferResponse
import hyperface.cms.commands.cardapplication.NomineeInfoAndFatcaResponse

class CardApplicationFlowStatus {
    StatePair<Boolean, CardEligibilityResponse> eligibility
    StatePair<Boolean, CardApplicationResponse> applicationCapture
    StatePair<Boolean, String> kyc
    StatePair<Boolean, CustomerBankVerificationResponse> bankVerification
    StatePair<Boolean, FixedDepositFundTransferResponse> fundTransfer
    StatePair<Boolean, NomineeInfoAndFatcaResponse> fatcaDeclaration
    StatePair<Boolean, FdBookingResponse> fdBooking

    CardApplicationFlowStatus() {
        eligibility = new StatePair<Boolean, CardEligibilityResponse>(Boolean.FALSE, null)
        applicationCapture = new StatePair<Boolean, CardApplicationResponse>(Boolean.FALSE, null)
        kyc = new StatePair<Boolean, String>(Boolean.FALSE, null)
        bankVerification = new StatePair<Boolean, CustomerBankVerificationResponse>(Boolean.FALSE, null)
        fundTransfer = new StatePair<Boolean, FixedDepositFundTransferResponse>(Boolean.FALSE, null)
        fatcaDeclaration = new StatePair<Boolean, NomineeInfoAndFatcaResponse>(Boolean.FALSE, null)
        fdBooking = new StatePair<Boolean, FdBookingResponse>(Boolean.FALSE, null)
    }

}

class StatePair<L, R> {
    L state
    R response

    StatePair() {
    }

    StatePair(L state, R response) {
        this.state = state
        this.response = response
    }

    void set(L state, R response) {
        this.state = state
        this.response = response
    }
}
