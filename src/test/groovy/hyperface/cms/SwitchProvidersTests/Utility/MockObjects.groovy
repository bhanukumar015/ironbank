package hyperface.cms.SwitchProvidersTests.Utility

import hyperface.cms.Constants
import hyperface.cms.commands.CardChannelControlsRequest
import hyperface.cms.commands.CreateCardRequest
import hyperface.cms.domains.Address
import hyperface.cms.domains.Card
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.Customer
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

    public Customer getTestCustomer(){
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

    public CreateCardRequest getTestCreateCardRequest(){
        CreateCardRequest cardRequest = new CreateCardRequest()
        cardRequest.cardProgramId = '1'
        cardRequest.customerId = '1'
        cardRequest.creditAccountId = UUID.randomUUID().toString()
        cardRequest.cardType = Constants.CardType.Physical
        return cardRequest
    }

    public CreditCardProgram getTestCreditCardProgram(){
        CreditCardProgram creditCardProgram = new CreditCardProgram()
        creditCardProgram.id = '1'
        creditCardProgram.annualizedPercentageRateInBps = 36
        creditCardProgram.cardLogoId = '178'
        creditCardProgram.cardPlasticId = '750065000'
        creditCardProgram.baseCurrency = Constants.Currency.HKD
        creditCardProgram.defaultDailyCashWithdrawalLimit = 100
        creditCardProgram.defaultDailyTransactionLimit = 1000
        return creditCardProgram
    }

    public CreditAccount getTestCreditAccount(){
        CreditAccount mockCreditAccount = new CreditAccount()
        mockCreditAccount.id = '1'
        mockCreditAccount.defaultCurrency = Constants.Currency.HKD
        mockCreditAccount.approvedCreditLimit = 10000
        mockCreditAccount.availableCreditLimit = 10000
        mockCreditAccount.customer = this.getTestCustomer()
        return mockCreditAccount
    }

    public Card getTestCard(){
        Card card = new Card().tap {
            id = UUID.randomUUID().toString()
            cardExpiryMonth = 10
            cardExpiryYear = 2030
            switchCardId = UUID.randomUUID().toString()
            lastFourDigits = UUID.randomUUID().toString()
            physicallyIssued = true
            virtuallyIssued = true
            virtualCardActivatedByCustomer = false
            physicalCardActivatedByCustomer = false
            cardSuspendedByCustomer = false
            enableOverseasTransactions = false
            enableOfflineTransactions = false
            enableNFC = false
            enableOnlineTransactions = false
            enableCashWithdrawal = false
            enableMagStripe = false
            creditAccount = this.getTestCreditAccount()
        }
        return card
    }

    private String getRandomEmailAddress(){
        def length = 16
        def pool = ['a'..'z','A'..'Z',0..9,'.'].flatten()
        return ((0..length-1).collect{pool[random.nextInt(pool.size())]}).join().concat('1@gmail.com')
    }

    private String getRandomMobileNumber(){
        return (Math.abs(random.nextInt() % 9000000000) + 1000000).toString()
    }

    public String mockCreateCustomerResponse(){
        JSONObject response = new JSONObject()
                .put('customerHashId', UUID.randomUUID().toString())
                .put('walletHashId', UUID.randomUUID().toString())
        return response.toString()
    }

    public String mockCreateCardResponse(){
        JSONObject response = new JSONObject()
                .put('cardHashId', UUID.randomUUID().toString())
                .put('maskedCardNumber', '1234-56xx-xxxx-3456')
        return response
    }
    public HttpResponse<JsonNode> mockCreateCustomerResponseAsync(){
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

    public HttpResponse<JsonNode> mockAddCardResponseAsync(){
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

    public CardChannelControlsRequest getTestCardControlsRequest(){
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
}
