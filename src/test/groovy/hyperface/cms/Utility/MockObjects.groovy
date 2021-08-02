package hyperface.cms.Utility

import hyperface.cms.Constants
import hyperface.cms.commands.CardChannelControlsRequest
import hyperface.cms.commands.CardLimitsRequest
import hyperface.cms.commands.CreateCardRequest
import hyperface.cms.commands.CustomerTransactionRequest
import hyperface.cms.domains.Address
import hyperface.cms.domains.Card
import hyperface.cms.domains.CardControl
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.CreditCardScheduleOfCharges
import hyperface.cms.domains.Customer
import hyperface.cms.domains.CustomerTransaction
import hyperface.cms.domains.TransactionLimit
import hyperface.cms.domains.fees.FlatFeeStrategy
import hyperface.cms.domains.fees.HigherOfPctOrMinValueStrategy
import hyperface.cms.domains.fees.JoiningFee
import hyperface.cms.domains.fees.PercentFeeStrategy
import kong.unirest.Cookies
import kong.unirest.Headers
import kong.unirest.HttpResponse
import kong.unirest.JsonNode
import kong.unirest.UnirestParsingException
import org.json.JSONObject

import java.util.function.Consumer
import java.util.function.Function

class MockObjects {

    private static Random random = new Random(System.currentTimeMillis())

    Customer getTestCustomer(){
        Customer customer = new Customer()
        customer.firstName = "John"
        customer.middleName = ""
        customer.lastName = "Smith"
        customer.preferredName = customer.firstName
        customer.dateOfBirth = "2000-01-01"
        customer.email = getRandomEmailAddress()
        customer.mobile = getRandomMobileNumber()
        customer.countryCode = "IN"
        customer.nationality = "IN"
        customer.currentAddress = new Address()
        customer.currentAddress.city = "Bengaluru"
        customer.currentAddress.pincode = "560001"
        customer.currentAddress.line1 = "1, MG Road"
        customer.currentAddress.line2 = "Bengaluru Urban"
        Map<String,Object> metadata = new HashMap<String,Object>()
        metadata.put('nium.customerHashId', UUID.randomUUID().toString())
        metadata.put('nium.walletId', UUID.randomUUID().toString())
        customer.switchMetadata = metadata
        return customer
    }

    CreateCardRequest getTestCreateCardRequest(){
        CreateCardRequest cardRequest = new CreateCardRequest()
        cardRequest.isAddOn = false
        cardRequest.cardProgramId = '1'
        cardRequest.customerId = '1'
        cardRequest.creditAccountId = UUID.randomUUID().toString()
        cardRequest.cardType = Constants.CardType.Physical
        return cardRequest
    }

    CreditCardProgram getTestCreditCardProgram(){
        CreditCardProgram creditCardProgram = new CreditCardProgram().tap {
            annualizedPercentageRateInBps = 36
            cardLogoId = '178'
            cardPlasticId = '750065000'
            baseCurrency = Constants.Currency.HKD
            defaultDailyCashWithdrawalLimit = 100
            defaultDailyTransactionLimit = 1000
            isActive = true
            disableLevel = null
            currentDayAccountCount = 0
            dailyAccountLimit = 100
            currentWeekAccountCount = 0
            weeklyAccountLimit = 500
            currentMonthAccountCount = 0
            monthlyAccountLimit = 1000
            lifetimeAccountCount = 0
            lifetimeAccountLimit = 10000
            scheduleOfCharges = this.getTestScheduleOfCharges()
        }
        return creditCardProgram
    }

    CreditCardScheduleOfCharges getTestScheduleOfCharges(){
        return new CreditCardScheduleOfCharges().tap{
            joiningFee = new JoiningFee().tap{
                applicationTrigger = JoiningFee.ApplicationTrigger.AFTER_FIRST_PURCHASE_TXN
                feeStrategy = new FlatFeeStrategy(valueTobeCharged: 100)
            }
            forexFeeStrategy = new PercentFeeStrategy(percentageToBeCharged: 2)
            cashAdvanceFeeStrategy = new HigherOfPctOrMinValueStrategy(minTobeCharged: 100, percentage: 3)
            addonCardFeeStrategy = new FlatFeeStrategy(valueTobeCharged: 100)
            rewardRedemptionFeeStrategy = new FlatFeeStrategy(valueTobeCharged: 50)
            overlimitFeeStrategy = new HigherOfPctOrMinValueStrategy(minTobeCharged: 100, percentage: 3)
        }
    }

    CreditAccount getTestCreditAccount(){
        CreditAccount mockCreditAccount = new CreditAccount()
        mockCreditAccount.id = '1'
        mockCreditAccount.defaultCurrency = Constants.Currency.HKD
        mockCreditAccount.currentBalance = 10000
        mockCreditAccount.approvedCreditLimit = 10000
        mockCreditAccount.availableCreditLimit = 10000
        mockCreditAccount.customer = this.getTestCustomer()
        return mockCreditAccount
    }

    Card getTestCard(){
        Card card = new Card().tap {
            id = UUID.randomUUID().toString()
            cardExpiryMonth = 10
            cardExpiryYear = 2030
            switchCardId = UUID.randomUUID().toString()
            lastFourDigits = UUID.randomUUID().toString()
            physicallyIssued = true
            virtuallyIssued = false
            virtualCardActivated = false
            physicalCardActivated = false
            cardType = Constants.CardType.Physical
            isPrimaryCard = true
            CardControl mockCardControl = new CardControl().tap{
                cardSuspendedByCustomer = false
                enableOverseasTransactions = false
                enableOfflineTransactions = false
                enableNFC = false
                enableOnlineTransactions = false
                enableCashWithdrawal = false
                enableMagStripe = false
                dailyTransactionLimit = new TransactionLimit().tap{
                    limit = 100.00
                    currentValue = 0.00
                    additionalMarginPercentage = 5.00
                    isEnabled = true
                }
                dailyCashWithdrawalLimit = new TransactionLimit().tap{
                    limit = 10
                    currentValue = 0.00
                    additionalMarginPercentage = 5.00
                    isEnabled = true
                }
                perTransactionLimit = new TransactionLimit().tap{
                    limit = 10
                    currentValue = 0.00
                    additionalMarginPercentage = 5.00
                    isEnabled = true
                }
                monthlyTransactionLimit = new TransactionLimit().tap{
                    limit = 1000
                    currentValue = 0.00
                    additionalMarginPercentage = 5.00
                    isEnabled = true
                }
            }
            cardControl = mockCardControl
            cardProgram = this.getTestCreditCardProgram()
            creditAccount = this.getTestCreditAccount()
        }
        return card
    }

    CardLimitsRequest getTestCardLimitRequest(){
        return new CardLimitsRequest().tap {
            cardId = UUID.randomUUID().toString()
            cardLimits = new LinkedList<CardLimitsRequest.CardLimit>()
            cardLimits.add(new CardLimitsRequest.CardLimit().tap {
                type = CardLimitsRequest.CardLimit.TransactionLimitType.DAILY_LIMIT
                value = 1000.00
                additionalMarginPercentage = 5.0
                isEnabled = true
            })
            cardLimits.add(new CardLimitsRequest.CardLimit().tap {
                type = CardLimitsRequest.CardLimit.TransactionLimitType.PER_TRANSACTION_LIMIT
                value = 100.00
                additionalMarginPercentage = 5.0
                isEnabled = false
            })
            cardLimits.add(new CardLimitsRequest.CardLimit().tap {
                type = CardLimitsRequest.CardLimit.TransactionLimitType.MONTHLY_LIMIT
                value = 10000.00
                additionalMarginPercentage = 5.0
                isEnabled = true
            })
        }
    }

    private String getRandomEmailAddress(){
        def length = 16
        def pool = ['a'..'z','A'..'Z',0..9,'.'].flatten()
        return ((0..length-1).collect{pool[random.nextInt(pool.size())]}).join().concat('1@gmail.com')
    }

    private String getRandomMobileNumber(){
        return (Math.abs(random.nextInt() % 9000000000) + 1000000).toString()
    }

    String mockCreateCustomerResponse(){
        JSONObject response = new JSONObject()
                .put('customerHashId', UUID.randomUUID().toString())
                .put('walletHashId', UUID.randomUUID().toString())
        return response.toString()
    }

    String mockCreateCardResponse(){
        JSONObject response = new JSONObject()
                .put('cardHashId', UUID.randomUUID().toString())
                .put('maskedCardNumber', '1234-56xx-xxxx-3456')
        return response
    }
    HttpResponse<JsonNode> mockCreateCustomerResponseAsync(){
        return new HttpResponse(){
            @Override
            int getStatus() {
                return 200
            }

            @Override
            String getStatusText() {
                return null
            }

            @Override
            Headers getHeaders() {
                Headers headers = new Headers()
                headers.add('x-request-id', UUID.randomUUID().toString())
                return headers
            }

            String getBody(){
                return mockCreateCustomerResponse()
            }

            @Override
            Optional<UnirestParsingException> getParsingError() {
                return null
            }

            @Override
            boolean isSuccess() {
                return false
            }

            @Override
            Cookies getCookies() {
                return null
            }

            @Override
            Object mapError(Class errorClass) {
                return null
            }

            @Override
            HttpResponse ifFailure(Class errorClass, Consumer consumer) {
                return null
            }

            @Override
            HttpResponse ifFailure(Consumer consumer) {
                return null
            }

            @Override
            HttpResponse ifSuccess(Consumer consumer) {
                return null
            }

            @Override
            HttpResponse<JsonNode> map(Function func) {
                return null
            }

            @Override
            JsonNode mapBody(Function func) {
                return null
            }
        }
    }

    HttpResponse<JsonNode> mockAddCardResponseAsync(){
        return new HttpResponse(){
            @Override
            int getStatus() {
                return 200
            }

            @Override
            String getStatusText() {
                return null
            }

            @Override
            Headers getHeaders() {
                Headers headers = new Headers()
                headers.add('x-request-id', UUID.randomUUID().toString())
                return headers
            }

            String getBody(){
                return mockCreateCardResponse()
            }

            @Override
            Optional<UnirestParsingException> getParsingError() {
                return null
            }

            @Override
            boolean isSuccess() {
                return false
            }

            @Override
            Cookies getCookies() {
                return null
            }

            @Override
            Object mapError(Class errorClass) {
                return null
            }

            @Override
            HttpResponse ifFailure(Class errorClass, Consumer consumer) {
                return null
            }

            @Override
            HttpResponse ifFailure(Consumer consumer) {
                return null
            }

            @Override
            HttpResponse ifSuccess(Consumer consumer) {
                return null
            }

            @Override
            HttpResponse<JsonNode> map(Function func) {
                return null
            }

            @Override
            JsonNode mapBody(Function func) {
                return null
            }
        }
    }

    CardChannelControlsRequest getTestCardControlsRequest(){
        return new CardChannelControlsRequest().tap{
            cardId = UUID.randomUUID().toString()
            enableMagStripe = false
            enableOfflineTransactions = true
            enableNFC = false
            enableCashWithdrawl = false
            enableOverseasTransactions = true
            enableOnlineTransactions = true
        }
    }

    String mockActivateCardResponse(){
        JSONObject response = new JSONObject()
                .put('status', 'Active')
        return response.toString()
    }

    String mockBlockCardResponse(){
        JSONObject response = new JSONObject()
                .put('status', 'Success')
        return response.toString()
    }

    String mockActivateCardResponseFailure(){
        JSONObject response = new JSONObject()
                .put('status', 'BAD_REQUEST')
                .put('errors', ["Card already activated!"])
        return response.toString()
    }

    CustomerTransactionRequest getTestCustomerDomesticTransactionResquest() {
        return new CustomerTransactionRequest()
            .tap {
                cardId = "card_j3rMwLlbL5uAftf2"
                transactionAmount = 1500.00
                transactionCurrency = "INR"
                transactionDescription = "Sample Txn 1"
                merchantCategoryCode = "120"
                merchantTerminalId = "123"
                transactionType = "SETTLEMENT_CREDIT"
            }
    }

    CustomerTransactionRequest getTestCustomerInternationalTransactionResquest() {
        return new CustomerTransactionRequest()
            .tap {
                cardId = "card_j3rMwLlbL5uAftf2"
                transactionAmount = 15.00
                transactionCurrency = "USD"
                transactionDescription = "Sample Txn 2"
                merchantCategoryCode = "120"
                merchantTerminalId = "123"
                transactionType = "SETTLEMENT_DEBIT"
            }
    }

    CustomerTransaction getTestCustomerTransaction(){
        return new CustomerTransaction().tap{
            card = this.getTestCard()
            id = 1
            billingAmount = 15000
        }
    }
}
