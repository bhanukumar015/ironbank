package hyperface.cms

import com.fasterxml.jackson.databind.ObjectMapper
import hyperface.cms.commands.CreateCardRequest
import hyperface.cms.domains.Address
import hyperface.cms.domains.Card
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.Customer
import hyperface.cms.repository.CardProgramRepository
import hyperface.cms.repository.CardRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.repository.CustomerRepository
import hyperface.cms.service.AccountService
import hyperface.cms.service.SwitchProviders.Nium.NiumSwitchProvider
import kong.unirest.HttpMethod
import kong.unirest.MockClient
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class NiumSwitchProviderTests {

    MockClient mockClient

    @BeforeEach
    void setup(){
        mockClient = MockClient.register()
    }

    @AfterEach
    void tearDown(){
        mockClient.clear()
    }

    @Test
    void contextLoads() {
    }

    @Mock
    CustomerRepository mockCustomerRepository

    @Mock
    CreditAccountRepository mockCreditAccountRepository

    @Mock
    CardProgramRepository mockCardProgramRepository

    @Mock
    CardRepository mockCardRepository

    @Autowired
    NiumSwitchProvider niumSwitchProvider

    @Autowired
    AccountService accountService

    private static Random random = new Random(System.currentTimeMillis())

    @Test
    void testNiumSwitchCreateCustomer(){
        mockClient.expect(HttpMethod.POST, NiumSwitchProvider.niumUrl+NiumSwitchProvider.createCustomerEndpoint)
                .thenReturn(mockCreateCustomerResponse())
        niumSwitchProvider.customerRepository = mockCustomerRepository
        Mockito.when(mockCustomerRepository.save(Mockito.any(Customer.class))).thenReturn(null)

        Customer customer = this.getTestCustomer()
        HttpStatus response = niumSwitchProvider.createCustomer(customer)
        // TODO: add better assert statements based on final response
        assert response == HttpStatus.OK
    }

    @Test
    void testNiumSwitchCreateCustomerAsync(){
        mockClient.expect(HttpMethod.POST, NiumSwitchProvider.niumUrl+NiumSwitchProvider.createCustomerEndpoint)
                .thenReturn(mockCreateCustomerResponse())
        niumSwitchProvider.customerRepository = mockCustomerRepository
        Mockito.when(mockCustomerRepository.save(Mockito.any(Customer.class))).thenReturn(null)
        Customer customer = this.getTestCustomer()
        HttpStatus response = niumSwitchProvider.createCustomerAsync(customer)
        sleep(10000)
        // TODO: add better assert statements based on final response
        assert response == HttpStatus.OK
    }

    @Test
    void testCreateCardSync(){
        accountService.creditAccountRepository = mockCreditAccountRepository
        Mockito.when(mockCreditAccountRepository.findById(Mockito.any())).thenReturn(this.getMockCreditAccount())
        accountService.niumSwitchProvider.customerRepository = mockCustomerRepository
        Mockito.when(mockCustomerRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(this.getTestCustomer()))
        accountService.cardProgramRepository = mockCardProgramRepository
        Mockito.when(mockCardProgramRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(this.getTestCreditCardProgram()))
        accountService.cardRepository = mockCardRepository
        Mockito.when(mockCardRepository.save(Mockito.any())).thenReturn(null)

        mockClient.expect(HttpMethod.POST)
                .thenReturn(mockCreateCardResponse())

        CreateCardRequest testCardRequest = this.getTestCreateCardRequest()
        Card testCard = accountService.createCard(testCardRequest)
    }

    //Utility methods to create mock test data
    private Customer getTestCustomer(){
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

    private CreateCardRequest getTestCreateCardRequest(){
        CreateCardRequest cardRequest = new CreateCardRequest()
        cardRequest.cardProgramId = '1'
        cardRequest.customerId = '1'
        cardRequest.creditAccountId = UUID.randomUUID().toString()
        cardRequest.cardType = 'Physical'
        println(new ObjectMapper().writeValueAsString(cardRequest))
        return cardRequest
    }

    private CreditCardProgram getTestCreditCardProgram(){
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

    private Optional<CreditAccount> getMockCreditAccount(){
        CreditAccount mockCreditAccount = new CreditAccount()
        mockCreditAccount.id = '1'
        mockCreditAccount.defaultCurrency = Constants.Currency.HKD
        mockCreditAccount.approvedCreditLimit = 10000
        mockCreditAccount.availableCreditLimit = 10000
        return Optional.of(mockCreditAccount)
    }

    // TODO: move to test utils? can be used by other tests
    private String getRandomEmailAddress(){
        def length = 16
        def pool = ['a'..'z','A'..'Z',0..9,'.'].flatten()
        return ((0..length-1).collect{pool[random.nextInt(pool.size())]}).join().concat('1@gmail.com')
    }

    private String getRandomMobileNumber(){
        return (Math.abs(random.nextInt() % 9000000000) + 1000000).toString()
    }

    private String mockCreateCustomerResponse(){
        JSONObject response = new JSONObject()
                .put('customerHashId', UUID.randomUUID().toString())
                .put('walletHashId', UUID.randomUUID().toString())
        return response.toString()
    }

    private String mockCreateCardResponse(){
        JSONObject response = new JSONObject()
                .put('cardHashId', UUID.randomUUID().toString())
                .put('maskedCardNumber', '1234-56xx-xxxx-3456')
        return response
    }
}
