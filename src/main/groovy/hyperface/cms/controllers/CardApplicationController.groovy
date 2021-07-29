package hyperface.cms.controllers

import groovy.util.logging.Slf4j
import hyperface.cms.Constants
import hyperface.cms.commands.cardapplication.ApplicantDetails
import hyperface.cms.commands.cardapplication.CardApplicantAddress
import hyperface.cms.commands.cardapplication.CardApplicationRequest
import hyperface.cms.commands.cardapplication.CardApplicationResponse
import hyperface.cms.commands.cardapplication.CardEligibilityRequest
import hyperface.cms.commands.cardapplication.CardEligibilityResponse
import hyperface.cms.commands.cardapplication.CardEligibilityResponse.EligibilityStatus
import hyperface.cms.commands.cardapplication.CustomerBankVerificationRequest
import hyperface.cms.commands.cardapplication.CustomerBankVerificationResponse
import hyperface.cms.commands.cardapplication.FdBookingRequest
import hyperface.cms.commands.cardapplication.FdBookingResponse
import hyperface.cms.commands.cardapplication.FixedDepositFundTransferRequest
import hyperface.cms.commands.cardapplication.FixedDepositFundTransferResponse
import hyperface.cms.commands.cardapplication.NomineeInfoAndFatcaRequest
import hyperface.cms.commands.cardapplication.NomineeInfoAndFatcaResponse
import hyperface.cms.domains.Address
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.cardapplication.CardApplication
import hyperface.cms.domains.cardapplication.DemographicDetail
import hyperface.cms.domains.cardapplication.FixedDepositDetail
import hyperface.cms.domains.kyc.KycOption.KycType
import hyperface.cms.repository.CardProgramRepository
import hyperface.cms.repository.cardapplication.CardApplicationRepository
import hyperface.cms.repository.cardapplication.FixedDepositDetailRepository
import hyperface.cms.service.CardApplicationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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

    @Autowired
    private FixedDepositDetailRepository fixedDepositDetailRepository

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
        if (existingCardApplication != null) {
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

    @PostMapping(value = "bankverification", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CustomerBankVerificationResponse> verifyBank(@Valid @RequestBody CustomerBankVerificationRequest request) {
        //verify card application ID
        Optional<CardApplication> cardApplicationOptional = cardApplicationRepository.findById(request.getApplicationRefId())
        if (!cardApplicationOptional.isPresent()) {
            String errorMessage = "Application with reference ID: ${request.getApplicationRefId()} not found."
            log.error("Error - API:[bankverification] - {}", errorMessage)
            return this.buildBankVerificationResponse(null, CustomerBankVerificationResponse.VerificationStatus.FAILED, HttpStatus.BAD_REQUEST, errorMessage, null, null)
        }

        // process verification request and return response
        CustomerBankVerificationResponse response = cardApplicationService.verifyBank(request, cardApplicationOptional.get())
        return ResponseEntity
                .status(response.getStatus() == CustomerBankVerificationResponse.VerificationStatus.SUCCESS ? HttpStatus.OK : HttpStatus.FAILED_DEPENDENCY)
                .body(response)
    }

    @PostMapping(value = "fundtransfer", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<FixedDepositFundTransferResponse> transferFund(@Valid @RequestBody FixedDepositFundTransferRequest request) {
        //verify card application ID
        Optional<CardApplication> cardApplicationOptional = cardApplicationRepository.findById(request.getApplicationRefId())
        if (!cardApplicationOptional.isPresent()) {
            String errorMessage = "Application with reference ID: ${request.getApplicationRefId()} not found."
            log.error("Error - API:[fundtransfer] - {}", errorMessage)
            return this.buildFundTransferResponse(null, FixedDepositFundTransferResponse.TransferStatus.FAILED, HttpStatus.BAD_REQUEST, errorMessage, null, null, null)
        }

        // process fund transfer and return response
        FixedDepositFundTransferResponse response = cardApplicationService.processFundTransfer(request, cardApplicationOptional.get())

        return ResponseEntity
                .status(response.getStatus() == FixedDepositFundTransferResponse.TransferStatus.SUCCESS ? HttpStatus.OK : HttpStatus.FAILED_DEPENDENCY)
                .body(response)
    }

    @PostMapping(value = "declaration", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<NomineeInfoAndFatcaResponse> processDeclaration(@Valid @RequestBody NomineeInfoAndFatcaRequest request) {
        //verify card application ID
        Optional<CardApplication> cardApplicationOptional = cardApplicationRepository.findById(request.getApplicationRefId())
        if (!cardApplicationOptional.isPresent()) {
            String errorMessage = "Application with reference ID: ${request.getApplicationRefId()} not found."
            log.error("Error - API:[declaration] - {}", errorMessage)
            return this.buildNomineeAndFatcaResponse(null, FixedDepositFundTransferResponse.TransferStatus.FAILED, HttpStatus.BAD_REQUEST, errorMessage, null)
        }

        //verify FD reference ID
        Optional<FixedDepositDetail> fixedDepositDetailOptional = fixedDepositDetailRepository.findById(request.getFixedDepositRefId())
        FixedDepositDetail fdDetail = null
        if (!fixedDepositDetailOptional.isPresent()
                || (fdDetail = fixedDepositDetailOptional.get()).getCardApplication().getId() != request.getApplicationRefId()) {
            String errorMessage = "FixedDepositRefId: ${request.getFixedDepositRefId()} is not associated with ApplictionReferenceID: ${request.getApplicationRefId()}."
            log.error("Error - API:[declaration] - {}", errorMessage)
            return this.buildNomineeAndFatcaResponse(null, NomineeInfoAndFatcaResponse.FatcaStatus.FAILED, HttpStatus.BAD_REQUEST, errorMessage, null)
        }

        // check for FATCA confirmation
        if (!request.getFatcaConfirmed()) {
            String errorMessage = "Customer did not mandate the declaration for ApplictionReferenceID: ${request.getApplicationRefId()}."
            log.error("Error - API:[declaration] - {}", errorMessage)
            return this.buildNomineeAndFatcaResponse(null, NomineeInfoAndFatcaResponse.FatcaStatus.FAILED, HttpStatus.BAD_REQUEST, errorMessage, null)
        }

        // process nominee and fatca declaration and return response
        NomineeInfoAndFatcaResponse response = cardApplicationService.processNomineeAndFatca(request, cardApplicationOptional.get(), fdDetail)
        return ResponseEntity
                .status(response.getStatus() == NomineeInfoAndFatcaResponse.FatcaStatus.SUCCESS ? HttpStatus.OK : HttpStatus.FAILED_DEPENDENCY)
                .body(response)
    }

    @PostMapping(value = "fixeddeposit", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<FdBookingResponse> processFdBooking(@Valid @RequestBody FdBookingRequest request) {
        //verify card application ID
        Optional<CardApplication> cardApplicationOptional = cardApplicationRepository.findById(request.getApplicationRefId())
        if (!cardApplicationOptional.isPresent()) {
            String errorMessage = "Application with reference ID: ${request.getApplicationRefId()} not found."
            log.error("Error - API:[fixeddeposit] - {}", errorMessage)
            return this.buildFdBookingResponse(null, FdBookingResponse.FdBookingStatus.FAILED, HttpStatus.BAD_REQUEST, errorMessage, null, null)
        }

        //verify FD reference ID
        Optional<FixedDepositDetail> fixedDepositDetailOptional = fixedDepositDetailRepository.findById(request.getFixedDepositRefId())
        FixedDepositDetail fdDetail = null
        if (!fixedDepositDetailOptional.isPresent()
                || (fdDetail = fixedDepositDetailOptional.get()).getCardApplication().getId() != request.getApplicationRefId()) {
            String errorMessage = "FixedDepositRefId: ${request.getFixedDepositRefId()} is not associated with ApplictionReferenceID: ${request.getApplicationRefId()}."
            log.error("Error - API:[fixeddeposit] - {}", errorMessage)
            return this.buildFdBookingResponse(null, FdBookingResponse.FdBookingStatus.FAILED, HttpStatus.BAD_REQUEST, errorMessage, null, null)
        }

        //verify credit limit based on card program
        CardApplication cardApplication = cardApplicationOptional.get()
        CreditCardProgram creditCardProgram = cardProgramRepository.findById(cardApplication.getProgramId()).get()
        Double fdAmount = fdDetail.getFixedDepositAmount()
        if (request.getCreditLimit() < (fdAmount * creditCardProgram.getMinCreditLineFdPercentage()) / 100.0
                || request.getCreditLimit() > (fdAmount * creditCardProgram.getMaxCreditLineFdPercentage()) / 100.0) {
            String errorMessage = "Credit Limit is not in the defined range."
            log.error("Error - API:[fixeddeposit] - {}", errorMessage)
            return this.buildFdBookingResponse(null, FdBookingResponse.FdBookingStatus.FAILED, HttpStatus.BAD_REQUEST, errorMessage, null, null)
        }

        // process FD booking and return response
        FdBookingResponse response = cardApplicationService.processFd(request, cardApplication, fdDetail)
        return ResponseEntity
                .status(response.getStatus() == FdBookingResponse.FdBookingStatus.SUCCESS ? HttpStatus.OK : HttpStatus.FAILED_DEPENDENCY)
                .body(response)
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

    private ResponseEntity<CustomerBankVerificationResponse> buildBankVerificationResponse(String appRefId, CustomerBankVerificationResponse.VerificationStatus verificationStatus, HttpStatus httpStatus, String errReason, Double minCreditLineFdPer, Double maxCreditLineFdPer) {
        CustomerBankVerificationResponse response = new CustomerBankVerificationResponse()
                .tap {
                    status = verificationStatus
                    applicationRefId = appRefId
                    errorMessage = errReason
                    minCreditLineFdPercentage = minCreditLineFdPer
                    maxCreditLineFdPercentage = maxCreditLineFdPer
                }
        return ResponseEntity
                .status(httpStatus)
                .body(response)
    }

    private ResponseEntity<FixedDepositFundTransferResponse> buildFundTransferResponse(String appRefId, FixedDepositFundTransferResponse.TransferStatus transferStatus, HttpStatus httpStatus, String errReason, String txnRefId, Double amount, String fdRefId) {
        FixedDepositFundTransferResponse response = new FixedDepositFundTransferResponse()
                .tap {
                    status = transferStatus
                    applicationRefId = appRefId
                    errorMessage = errReason
                    transactionRefId
                    amountTransferred
                    fixedDepositRefId
                }
        return ResponseEntity
                .status(httpStatus)
                .body(response)
    }

    private ResponseEntity<NomineeInfoAndFatcaResponse> buildNomineeAndFatcaResponse(String appRefId, NomineeInfoAndFatcaResponse.FatcaStatus fatcaStatus, HttpStatus httpStatus, String errReason, String fdRefId) {
        NomineeInfoAndFatcaResponse response = new NomineeInfoAndFatcaResponse()
                .tap {
                    status = fatcaStatus
                    applicationRefId = appRefId
                    errorMessage = errReason
                    fixedDepositRefId = fdRefId
                }
        return ResponseEntity
                .status(httpStatus)
                .body(response)
    }

    private ResponseEntity<FdBookingResponse> buildFdBookingResponse(String appRefId, FdBookingResponse.FdBookingStatus fdBookingStatus, HttpStatus httpStatus, String errReason, String fdRefId, String fdAccNum) {
        FdBookingResponse response = new FdBookingResponse()
                .tap {
                    status = fdBookingStatus
                    applicationRefId = appRefId
                    errorMessage = errReason
                    fixedDepositRefId = fdRefId
                    fixedDepositAccountNumber = fdAccNum
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