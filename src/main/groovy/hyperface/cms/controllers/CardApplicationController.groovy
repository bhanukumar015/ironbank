package hyperface.cms.controllers

import groovy.util.logging.Slf4j
import hyperface.cms.Constants
import hyperface.cms.commands.cardapplication.ApplicantDetails
import hyperface.cms.commands.cardapplication.CardApplicantAddress
import hyperface.cms.commands.cardapplication.CardApplicationRequest
import hyperface.cms.commands.cardapplication.CardApplicationResponse
import hyperface.cms.commands.cardapplication.CardEligibilityRequest
import hyperface.cms.commands.cardapplication.CardEligibilityResponse
import hyperface.cms.domains.Address
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.cardapplication.CardApplication
import hyperface.cms.domains.cardapplication.DemographicDetail
import hyperface.cms.domains.kyc.KycOption.KycType
import hyperface.cms.repository.CardProgramRepository
import hyperface.cms.repository.cardapplication.CardApplicationRepository
import hyperface.cms.service.CardApplicationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import hyperface.cms.commands.cardapplication.CardEligibilityResponse.EligibilityStatus

import javax.validation.Valid

@RestController
@RequestMapping(value = "card-application")
@Slf4j
class CardApplicationController {

    @Autowired
    private CardProgramRepository cardProgramRepository

    @Autowired
    private CardApplicationRepository cardApplicationRepository

    @Autowired
    private CardApplicationService cardApplicationService

    @PostMapping(value = "eligibility", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CardEligibilityResponse> processEligibility(@Valid @RequestBody CardEligibilityRequest request) {

        if (!request.getIsMobileNumberVerified()) {
            String errorMessage = "Mobile number has not been verified yet. Please verify the mobileNumber or invoke mobile verification API on Hyperface, before calling this API."
            log.error("Error occurred while processing card application eligibility API. Exception: [{}]", errorMessage)
            return this.buildEligibilityResponse(null, EligibilityStatus.PENDING_MOBILE_VERIFICATION, HttpStatus.BAD_REQUEST, errorMessage)
        }
        //verify clientId and programId
        Optional<CreditCardProgram> creditCardProgramOptional = cardProgramRepository.findById(request.getProgramId())
        if (!creditCardProgramOptional.isPresent()) {
            String errorMessage = "Program not found."
            return this.buildEligibilityResponse(null, EligibilityStatus.REJECTED, HttpStatus.BAD_REQUEST, errorMessage)
        }
        CreditCardProgram ccProgram = creditCardProgramOptional.get()
        if (ccProgram.getClient()?.getId() != request.getClientId()) {
            String errorMessage = "Invalid Program Id"
            return this.buildEligibilityResponse(null, EligibilityStatus.REJECTED, HttpStatus.BAD_REQUEST, errorMessage)
        }
        CardApplication existingCardApplication = cardApplicationRepository.findApplicationByMobileClientAndProgram(request.getClientId(),
                request.getProgramId(), request.getMobileNumber())
        if(existingCardApplication != null) {
            log.info("Idempotent request received for Eligibility API. CardApplicationId: {}", existingCardApplication.getId())
            return this.buildEligibilityResponse(existingCardApplication.getId(), EligibilityStatus.APPROVED, HttpStatus.OK, null)
        }

        //TODO: Trigger eligibility module and if success, only then continue with below stub
        CardApplication savedCardApplication = cardApplicationService.createCardApplication(request)
        return this.buildEligibilityResponse(savedCardApplication.getId(), EligibilityStatus.APPROVED, HttpStatus.CREATED, null)
    }


    @PostMapping(value = "apply", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CardApplicationResponse> processApplication(@Valid @RequestBody CardApplicationRequest request) {
        Optional<CardApplication> cardApplicationOptional = cardApplicationRepository.findById(request.getApplicationRefId())
        if (cardApplicationOptional.isPresent()) {
            CardApplication cardApplication = cardApplicationOptional.get()
            //check for idempotency
            if (cardApplication.getStatus() != CardApplication.ApplicationStatus.INITIATED) {
                log.info("Idempotent request received for Card Application Capture API. CardApplicationId: {}", cardApplication.getId())
                return this.buildCardApplicationResponse(cardApplication.getId(), (CardApplicationResponse.CardApplicationStatus) cardApplication.getStatus().toString(), HttpStatus.OK, null, cardApplication.getHyperfaceCustId(), cardApplication.getKycType(), cardApplication.getBankCustId())
            }
            //verify pincode from residential address, which should match with the value from Eligibility API request
            if (cardApplication.getApplicantDetails().getResidentialAddress().getPincode() != request.getApplicantDetails().getResidentialAddress().pincode) {
                String errorMessage = "Pincode mismatch for the residential address. It must be same as the value in Eligibility API request."
                log.error("Error occurred while processing card application capture API. Exception: [{}]", errorMessage)
                return this.buildCardApplicationResponse(null, CardApplicationResponse.CardApplicationStatus.FAILED, HttpStatus.BAD_REQUEST, errorMessage, null, null, null)
            }
            //populate rest of the fields
            enrichCardApplicationEntity(cardApplication, request)
            CardApplicationResponse response = cardApplicationService.orchestrate(cardApplication)
            return ResponseEntity
                    .status(response.getStatus() == CardApplicationResponse.CardApplicationStatus.PENDING ? HttpStatus.CREATED : HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response)
        }
        String errorMessage = "Record with applicationRefId: [${request.getApplicationRefId()}] is not found. Please use the applicationRefId from the Eligibility API response for this customer."
        log.error("Error occurred while processing card application capture API. Exception: [{}]", errorMessage)
        return this.buildCardApplicationResponse(null, CardApplicationResponse.CardApplicationStatus.FAILED, HttpStatus.BAD_REQUEST, errorMessage, null, null, null)

    }

    private ResponseEntity<CardEligibilityResponse> buildEligibilityResponse(String appRefId, CardEligibilityResponse.EligibilityStatus eligibilityStatus, HttpStatus httpStatus, String errReason) {
        CardEligibilityResponse response = new CardEligibilityResponse()
                .tap {
                    status = eligibilityStatus
                    applicationRefId = appRefId
                    failureReason = errReason
                }
        return ResponseEntity
                .status(httpStatus)
                .body(response)
    }

    private ResponseEntity<CardApplicationResponse> buildCardApplicationResponse(String appRefId, CardApplicationResponse.CardApplicationStatus cardApplicationStatus, HttpStatus httpStatus, String errReason, String hfCustId, KycType kycType, String bankCustId) {
        CardApplicationResponse response = new CardApplicationResponse()
                .tap {
                    status = cardApplicationStatus
                    applicationRefId = appRefId
                    failureReason = errReason
                    hyperfaceCustomerId = hfCustId
                    kycMethod = kycType
                    bankCustomerId = bankCustId
                }
        return ResponseEntity
                .status(httpStatus)
                .body(response)
    }

    private void enrichCardApplicationEntity(final CardApplication cardApplication, final CardApplicationRequest cardApplicationRequest) {
        DemographicDetail demographicDetail = cardApplication.getApplicantDetails()
        ApplicantDetails applicantDetails = cardApplicationRequest.getApplicantDetails()
        CardApplicantAddress residentAdd = applicantDetails.getResidentialAddress()
        CardApplicantAddress permAdd = applicantDetails.getPermanentAddress()
        Address resAddEntity = demographicDetail.getResidentialAddress()
        Address permAddEntity = demographicDetail.getPermanentAddress()

        resAddEntity
                .tap {
                    line1 = residentAdd.getLine1()
                    line2 = residentAdd.getLine2()
                    line3 = residentAdd.getLine3()
                    city = residentAdd.getCity()
                    pincode = residentAdd.getPincode()
                    state = residentAdd.getState()
                    country = residentAdd.getCountry()
                    landmark = residentAdd.getLandmark()
                    countryCodeIso = residentAdd.getCountryCodeIso()
                }
        permAddEntity
                .tap {
                    line1 = permAdd.getLine1()
                    line2 = permAdd.getLine2()
                    line3 = permAdd.getLine3()
                    city = permAdd.getCity()
                    pincode = permAdd.getPincode()
                    state = permAdd.getState()
                    country = permAdd.getCountry()
                    landmark = permAdd.getLandmark()
                    countryCodeIso = permAdd.getCountryCodeIso()
                }
        demographicDetail
                .tap {
                    salutation = (Constants.Salutation) applicantDetails.getSalutation()
                    firstName = applicantDetails.getFirstName()
                    middleName = applicantDetails.getMiddleName()
                    lastName = applicantDetails.getLastName()
                    nameOnCard = applicantDetails.getNameOnCard()
                    gender = (Constants.Gender) applicantDetails.getGender()
                    educationalQualification = applicantDetails.getEducationalQualification()
                    maritalStatus = (Constants.MaritalStatus) applicantDetails.getMaritalStatus()
                    phoneNumber = applicantDetails.getPhoneNumber()
                    emailId = applicantDetails.getEmailId()
                    employerName = applicantDetails.getEmployerName()
                }

        cardApplication
                .tap {
                    bankAccountNumber = cardApplicationRequest.getBankDetails() == null ? null : cardApplicationRequest.getBankDetails().getBankAccountNumber()
                    clientRelationshipNumber = cardApplicationRequest.getBankDetails() == null ? null : cardApplicationRequest.getBankDetails().getClientRelationshipNumber()
                    ipAddress = cardApplicationRequest.getIpAddress()
                    clientPartnerRefId = cardApplicationRequest.getBankDetails() == null ? null : cardApplicationRequest.getBankDetails().getClientPartnerRefId()
                    isKycComplete = Boolean.FALSE
                    isEtbNtbCheckComplete = Boolean.FALSE
                }
    }
}