package hyperface.cms.domains.cardapplication

import org.hibernate.annotations.GenericGenerator

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
class KycProof {
    @Id
    @GenericGenerator(name = "kyc_proof_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "kyc_proof_id")
    String id

    String proofType
    String proofNumber
}
