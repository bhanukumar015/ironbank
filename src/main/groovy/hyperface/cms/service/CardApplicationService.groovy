package hyperface.cms.service

import groovy.util.logging.Slf4j
import hyperface.cms.Constants
import hyperface.cms.commands.cardapplication.CardApplicationResponse
import hyperface.cms.commands.cardapplication.CardEligibilityRequest
import hyperface.cms.commands.cardapplication.CardEligibilityResponse
import hyperface.cms.commands.cardapplication.CustomerBankVerificationRequest
import hyperface.cms.commands.cardapplication.CustomerBankVerificationResponse
import hyperface.cms.commands.cardapplication.FdBookingRequest
import hyperface.cms.commands.cardapplication.FdBookingResponse
import hyperface.cms.commands.cardapplication.FixedDepositFundTransferRequest
import hyperface.cms.commands.cardapplication.FixedDepositFundTransferResponse
import hyperface.cms.commands.cardapplication.NomineeInfoAndFatcaRequest
import hyperface.cms.commands.cardapplication.NomineeInfoAndFatcaResponse
import hyperface.cms.domains.Address
import hyperface.cms.domains.Bank
import hyperface.cms.domains.Client
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.Customer
import hyperface.cms.domains.cardapplication.CardApplication
import hyperface.cms.domains.cardapplication.CardApplicationFlowStatus
import hyperface.cms.domains.cardapplication.DemographicDetail
import hyperface.cms.domains.cardapplication.FixedDepositDetail
import hyperface.cms.domains.cardapplication.StatePair
import hyperface.cms.domains.kyc.KycOption
import hyperface.cms.repository.CardProgramRepository
import hyperface.cms.repository.ClientRepository
import hyperface.cms.repository.CustomerRepository
import hyperface.cms.repository.cardapplication.CardApplicationRepository
import hyperface.cms.repository.cardapplication.FixedDepositDetailRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.time.ZoneId
import java.time.ZonedDateTime

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

    @Autowired
    private FixedDepositDetailRepository fixedDepositDetailRepository

    CardEligibilityResponse createCardApplication(CardEligibilityRequest request) {
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
                    status = CardApplication.ApplicationStatus.INITIATED
                }

        CardApplication savedCardApplication = cardApplicationRepository.save(cardApplication)
        log.info("Card Application created successfully for ${request.mobileNumber} under ${request.getProgramId()}")
        CardEligibilityResponse response = new CardEligibilityResponse()
                .tap {
                    status = CardEligibilityResponse.EligibilityStatus.APPROVED
                    applicationRefId = savedCardApplication.getId()
                }
        savedCardApplication.tap {
            cardApplicationFlowStatus = new CardApplicationFlowStatus()
                    .tap {
                        eligibility = new StatePair<>(Boolean.TRUE, response)
                    }
        }
        cardApplicationRepository.save(savedCardApplication)
        return response
    }

    CardApplicationResponse orchestrate(final CardApplication cardApplication) {
        CardApplicationResponse response = new CardApplicationResponse()
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
            response.setApplicationRefId(cardApplication.getId())

            response.setStatus(CardApplicationResponse.CardApplicationStatus.PENDING)
            cardApplication.getCardApplicationFlowStatus().getApplicationCapture().set(Boolean.TRUE, response)
            cardApplicationRepository.save(cardApplication)

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

    CustomerBankVerificationResponse verifyBank(CustomerBankVerificationRequest request, CardApplication cardApplication) {
        // store customer's FD funding bank details
        cardApplication
                .tap {
                    custSavingsBankAccNumber = request.getAccountNumber()
                    custSavingsBankIfsCode = request.getIfsCode()
                }


        //TODO: initiate penny drop transaction

        // return response
        log.info("API:[bankverification] - Bank verification complete for applicationID: {}", request.getApplicationRefId())
        CreditCardProgram cardProgram = cardProgramRepository.findById(cardApplication.getProgramId()).get()
        CustomerBankVerificationResponse response = new CustomerBankVerificationResponse()
                .tap {
                    status = CustomerBankVerificationResponse.VerificationStatus.SUCCESS
                    applicationRefId = cardApplication.getId()
                    minCreditLineFdPercentage = cardProgram.getMinCreditLineFdPercentage()
                    maxCreditLineFdPercentage = cardProgram.getMaxCreditLineFdPercentage()
                }
        cardApplication.getCardApplicationFlowStatus().getBankVerification().set(Boolean.TRUE, response)
        cardApplicationRepository.save(cardApplication)
        return response
    }

    FixedDepositFundTransferResponse processFundTransfer(FixedDepositFundTransferRequest request, CardApplication application) {
        CreditCardProgram cardProgram = cardProgramRepository.findById(application.getProgramId()).get()
        Bank issuingBank = cardProgram.getBank()

        // collect data for fulfilling fund transfer
        String issuingBankOmnibusAccountNumber = issuingBank.getOmnibusAccountNumber()
        String issuingBankIfsCode = issuingBank.getIfsCode()
        String fundingBankAccountNumber = application.getCustSavingsBankAccNumber()
        String fundingBankIfsCode = application.getCustSavingsBankIfsCode()
        Double fdAmount = request.getFixedDepositAmount()

        //TODO: invoke PG for IACH transaction using above information

        String txnRefId = "" // will be populated from above call

        // populate and save FD details
        FixedDepositDetail fixedDepositDetail = new FixedDepositDetail()
                .tap {
                    fixedDepositAmount = request.getFixedDepositAmount()
                    fdStatus = FixedDepositDetail.FdStatus.PENDING
                    cardApplication = application
                    fatcaConfirmed = Boolean.FALSE
                    lienStatus = FixedDepositDetail.LienStatus.UNMARKED
                }
        FixedDepositDetail savedFdDetail = fixedDepositDetailRepository.save(fixedDepositDetail)

        // return response
        log.info("API:[fundtransfer] - Fund transfer complete for applicationID: {}", request.getApplicationRefId())
        FixedDepositFundTransferResponse response = new FixedDepositFundTransferResponse()
                .tap {
                    status = FixedDepositFundTransferResponse.TransferStatus.SUCCESS
                    applicationRefId = application.getId()
                    transactionRefId = txnRefId
                    amountTransferred = request.getFixedDepositAmount()
                    fixedDepositRefId = savedFdDetail.getId()
                }

        application.getCardApplicationFlowStatus().getFundTransfer().set(Boolean.TRUE, response)

        cardApplicationRepository.save(application)
        return response
    }

    NomineeInfoAndFatcaResponse processNomineeAndFatca(NomineeInfoAndFatcaRequest request, CardApplication cardApplication, FixedDepositDetail fdDetail) {
        // store information
        fdDetail
                .tap {
                    nomineeName = request.getNomineeName()
                    nomineeDob = request.getNomineeDob()
                    nomineeGuardian = request.getNomineeGuardian()
                    motherMaidenName = request.getMotherMaidenName()
                    fatcaConfirmed = Boolean.TRUE
                }
        fixedDepositDetailRepository.save(fdDetail)

        // return response
        log.info("API:[FATCA declaration] - FATCA and Nominee declaration complete for applicationID: {}", request.getApplicationRefId())
        NomineeInfoAndFatcaResponse response = new NomineeInfoAndFatcaResponse()
                .tap {
                    status = NomineeInfoAndFatcaResponse.FatcaStatus.SUCCESS
                    applicationRefId = cardApplication.getId()
                    fixedDepositRefId = fdDetail.getId()
                }

        cardApplication.getCardApplicationFlowStatus().getFatcaDeclaration().set(Boolean.TRUE, response)

        cardApplicationRepository.save(cardApplication)
        return response

    }

    FdBookingResponse processFd(FdBookingRequest request, CardApplication cardApplication, FixedDepositDetail fixedDepositDetail) {
        // collect data for invoking FD booking API on the bank
        String creditLimit = request.getCreditLimit()
        String custBankAccountNumber = cardApplication.getCustSavingsBankAccNumber()
        String custBankIfsCode = cardApplication.getCustSavingsBankIfsCode()
        String fdAmount = fixedDepositDetail.getFixedDepositAmount()
        // ...
        // and rest of Nominee FATCA details from "fixedDepositDetail"

        //TODO: invoke Bank FD booking API
        // will get these as part of the above API response
        String fdAccountNumber = ""
        ZonedDateTime fdMaturityDate = ZonedDateTime.now(ZoneId.of("UTC")).plusYears(2)

        // store FD Account Number
        fixedDepositDetail
                .tap {
                    accountNumber = fdAccountNumber
                    maturityDate = fdMaturityDate
                }
        fixedDepositDetailRepository.save(fixedDepositDetail)

        // return response
        log.info("API:[FDBooking] - FD booking complete for applicationID: {}", request.getApplicationRefId())
        FdBookingResponse response = new FdBookingResponse()
                .tap {
                    status = FdBookingResponse.FdBookingStatus.SUCCESS
                    applicationRefId = cardApplication.getId()
                    fixedDepositRefId = fixedDepositDetail.getId()
                    fixedDepositAccountNumber = fdAccountNumber
                    fixedDepositMaturityDate = fdMaturityDate.toString()
                }

        cardApplication.getCardApplicationFlowStatus().getFdBooking().set(Boolean.TRUE, response)

        cardApplicationRepository.save(cardApplication)
        return response
    }
}
