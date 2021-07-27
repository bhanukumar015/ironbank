package hyperface.cms.service

import groovy.util.logging.Slf4j
import hyperface.cms.Constants
import hyperface.cms.commands.cardapplication.CardApplicationResponse
import hyperface.cms.commands.cardapplication.CardEligibilityRequest
import hyperface.cms.domains.Address
import hyperface.cms.domains.Client
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.Customer
import hyperface.cms.domains.cardapplication.CardApplication
import hyperface.cms.domains.cardapplication.DemographicDetail
import hyperface.cms.domains.kyc.KycOption
import hyperface.cms.repository.CardProgramRepository
import hyperface.cms.repository.ClientRepository
import hyperface.cms.repository.CustomerRepository
import hyperface.cms.repository.cardapplication.CardApplicationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@Slf4j
class CardApplicationService {
    @Autowired
    private CardApplicationRepository cardApplicationRepository

    @Autowired
    private ClientRepository clientRepository

    @Autowired
    private CustomerRepository customerRepository

    @Autowired
    private CardProgramRepository cardProgramRepository

    CardApplication createCardApplication(CardEligibilityRequest request) {
        Address residentAddress = new Address()
                .tap {
                    pincode = request.getCurrentResidencePincode()
                }
        DemographicDetail demographicDetail = new DemographicDetail()
                .tap {
                    residentialAddress = residentAddress
                    permanentAddress = new Address()
                    profession = Constants.Profession.valueOf(request.getProfession())
                    dob = request.getDob()
                    grossAnnualIncome = request.getGrossAnnualIncome()
                    mobileNumber = request.getMobileNumber()
                    countryCode = request.getMobileNumberCountryCode()
                    nationality = request.getNationality()
                }
        CardApplication cardApplication = new CardApplication()
                .tap {
                    clientId = request.getClientId()
                    programId = request.getProgramId()
                    applicantDetails = demographicDetail
                    isMobileNumberVerified = Boolean.TRUE
                    isEligibilityCheckComplete = Boolean.TRUE
                    status = CardApplication.ApplicationStatus.INITIATED
                }
        CardApplication savedCardApplication = cardApplicationRepository.save(cardApplication)
        log.info("Card Application created successfully for ${request.mobileNumber} under ${request.getProgramId()}")
        return savedCardApplication
    }

    CardApplicationResponse orchestrate(final CardApplication cardApplication) {
        final CardApplicationResponse response = new CardApplicationResponse()
        try {
            // Step 1: create customer
            DemographicDetail demographicDetail = cardApplication.getApplicantDetails()
            Client cl = clientRepository.findById(cardApplication.getClientId()).get()
            Customer customer = new Customer()
                    .tap {
                        firstName = demographicDetail.getFirstName()
                        middleName = demographicDetail.getMiddleName()
                        lastName = demographicDetail.getLastName()
                        preferredName = demographicDetail.getFirstName()
                        dateOfBirth = demographicDetail.getDob()
                        email = demographicDetail.getEmailId()
                        mobile = demographicDetail.getMobileNumber()
                        countryCode = demographicDetail.getCountryCode()
                        nationality = demographicDetail.getNationality()
                        currentAddress = demographicDetail.getResidentialAddress()
                        permanentAddress = demographicDetail.getPermanentAddress()
                        client = cl
                    }
            Customer savedCustomer = customerRepository.save(customer)
            response.setHyperfaceCustomerId(savedCustomer.getId())

            // Step 2: ETB/NTB check
            //TODO: set bankCustomerId or whatever relevant information comes back after ETB/NTB check

            //Step 3: Extract KYC method
            CreditCardProgram cardProgram = cardProgramRepository.findById(cardApplication.getProgramId()).get()
            Optional<KycOption> kycOption = cardProgram
                    .getKycOptions()
                    .stream()
                    .sorted((op1, op2) -> op1.getPriority() < op2.getPriority())
                    .findFirst()
            kycOption.ifPresent(op -> {
                response.setKycMethod(op.getKycType())
                cardApplication.setKycType(op.getKycType())
            })

            // Step 4: Save enriched cardApplication entity
            cardApplication.tap {
                status = CardApplication.ApplicationStatus.PENDING
                hyperfaceCustId = savedCustomer.getId()
            }
            cardApplicationRepository.save(cardApplication)
            response.setApplicationRefId(cardApplication.getId())

            response.setStatus(CardApplicationResponse.CardApplicationStatus.PENDING)

        } catch (Exception e) {
            log.error("Exception occurred while orchestrating card application. Error: {}", e.getLocalizedMessage())
            if (null != response.getHyperfaceCustomerId()) {
                customerRepository.deleteById(response.getHyperfaceCustomerId())
            }
            response
                    .tap {
                        hyperfaceCustomerId = null
                        kycMethod = null
                        bankCustomerId = null
                        status = CardApplicationResponse.CardApplicationStatus.FAILED
                        failureReason = "Exception occurred while orchestrating card application"
                        applicationRefId = cardApplication.getId()
                    }
        }
        return response
    }
}
