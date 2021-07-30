package hyperface.cms.domains.cardapplication


import hyperface.cms.domains.converters.CardApplicationFlowStatusConverter
import hyperface.cms.domains.kyc.KycOption
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.GenericGenerator

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import java.time.ZonedDateTime

@Entity
class CardApplication {
    // NOTE: ApplicationStatus = COMPLETE, should only be set when application moves for
    // card issuance APIs. Until then, the ApplicationStatus must be PENDING.
    enum ApplicationStatus {
        INITIATED,
        PENDING,
        COMPLETE,
        FAILED
    }

    @Id
    @GenericGenerator(name = "card_application_id", strategy = "hyperface.cms.util.UniqueIdGenerator")
    @GeneratedValue(generator = "card_application_id")
    String id

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "demographic_detail_id", referencedColumnName = "id")
    DemographicDetail applicantDetails

    String clientId
    String programId
    String bankAccountNumber
    String clientRelationshipNumber
    Boolean isMobileNumberVerified
    Boolean isEtbNtbCheckComplete
    String ipAddress
    String clientPartnerRefId
    String hyperfaceCustId
    KycOption.KycType kycType
    String bankCustId
    String hyperfaceCardAccountId
    String custSavingsBankAccNumber
    String custSavingsBankIfsCode

    @OneToMany(mappedBy = "cardApplication", cascade = CascadeType.REMOVE)
    List<FixedDepositDetail> fdDetails

    @CreationTimestamp
    ZonedDateTime capturedOn

    @Enumerated(EnumType.STRING)
    ApplicationStatus status

    @Convert(converter = CardApplicationFlowStatusConverter.class)
    @Column(columnDefinition = "TEXT")
    CardApplicationFlowStatus cardApplicationFlowStatus
}
