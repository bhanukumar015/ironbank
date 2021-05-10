package hyperface.cms.commands

import hyperface.cms.Constants

class RejectTxnResponse {
    String rejectionCode
    String rejectReason

    public RejectTxnResponse() {}
    public RejectTxnResponse(Constants.RejectionCode rc) {
        this.rejectionCode = rc.code
        this.rejectReason = rc.description
    }
}
