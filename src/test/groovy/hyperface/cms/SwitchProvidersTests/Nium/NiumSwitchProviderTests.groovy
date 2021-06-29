package hyperface.cms.SwitchProvidersTests.Nium


import hyperface.cms.SwitchProvidersTests.Utility.MockObjects
import hyperface.cms.commands.CreateCardRequest
import hyperface.cms.domains.Card
import hyperface.cms.domains.Customer
import hyperface.cms.repository.CardProgramRepository
import hyperface.cms.repository.CardRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.repository.CustomerRepository
import hyperface.cms.service.AccountService
import hyperface.cms.service.SwitchProviders.Nium.CardManagement.NiumAddCardCallback
import hyperface.cms.service.SwitchProviders.Nium.CardManagement.NiumCardService
import hyperface.cms.service.SwitchProviders.Nium.CustomerManagement.NiumCreateCustomerCallback
import hyperface.cms.service.SwitchProviders.Nium.CustomerManagement.NiumCustomerService
import hyperface.cms.service.SwitchProviders.Nium.NiumSwitchProvider
import kong.unirest.HttpMethod
import kong.unirest.MockClient
import kong.unirest.UnirestException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class NiumSwitchProviderTests {

    MockClient mockClient

    private static MockObjects mockObject = new MockObjects()

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

    @Mock
    NiumSwitchProvider mockNiumSwitchProvider

    @Autowired
    NiumCustomerService niumCustomerService

    @Autowired
    NiumCardService niumCardService

    @Autowired
    AccountService accountService

    @Autowired
    NiumAddCardCallback addCardCallback

    @Autowired
    NiumCreateCustomerCallback createCustomerCallback

    @Test
    void testNiumSwitchCreateCustomer(){
        mockClient.expect(HttpMethod.POST, NiumSwitchProvider.niumUrl + niumCustomerService.createCustomerEndpoint)
                .thenReturn(mockObject.mockCreateCustomerResponse())
        niumCustomerService.customerRepository = mockCustomerRepository
        Mockito.when(mockCustomerRepository.save(Mockito.any(Customer.class))).thenReturn(null)

        Customer customer = mockObject.getTestCustomer()
        HttpStatus response = niumCustomerService.createCustomer(customer)
        // TODO: add better assert statements based on final response
        assert response == HttpStatus.OK
    }

    @Test
    void testNiumSwitchCreateCustomerAsync(){
        mockClient.expect(HttpMethod.POST, NiumSwitchProvider.niumUrl + niumCustomerService.createCustomerEndpoint)
                .thenReturn(mockObject.mockCreateCustomerResponse())
        createCustomerCallback.customerRepository = mockCustomerRepository
        Mockito.when(mockCustomerRepository.save(Mockito.any(Customer.class))).thenReturn(null)
        niumCustomerService.niumSwitchProvider = mockNiumSwitchProvider
        Mockito.when(mockNiumSwitchProvider.executeHttpPostRequestAsync(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer{(createCustomerCallback)
                        .completed(mockObject.mockCreateCustomerResponseAsync())}

        Customer customer = mockObject.getTestCustomer()
        HttpStatus response = niumCustomerService.createCustomerAsync(customer)
        // TODO: add better assert statements based on final response
        assert response == HttpStatus.OK
    }

    @Test
    void testCreateCardSync(){
        accountService.creditAccountRepository = mockCreditAccountRepository
        Mockito.when(mockCreditAccountRepository.findById(Mockito.any())).thenReturn(mockObject.getMockCreditAccount())
        accountService.niumCardService.customerRepository = mockCustomerRepository
        Mockito.when(mockCustomerRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCustomer()))
        accountService.cardProgramRepository = mockCardProgramRepository
        Mockito.when(mockCardProgramRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCreditCardProgram()))
        accountService.cardRepository = mockCardRepository
        Mockito.when(mockCardRepository.save(Mockito.any())).thenReturn(null)

        mockClient.expect(HttpMethod.POST)
                .thenReturn(mockObject.mockCreateCardResponse())

        CreateCardRequest testCardRequest = mockObject.getTestCreateCardRequest()
        Card testCard = accountService.createCard(testCardRequest)

        // Assert the fields received from Nium switch
        assertNotNull(testCard.switchCardId)
        assertEquals(testCard.lastFourDigits, '3456')
    }

    @Test
    void testCreateCardSyncException(){
        accountService.creditAccountRepository = mockCreditAccountRepository
        Mockito.when(mockCreditAccountRepository.findById(Mockito.any())).thenReturn(mockObject.getMockCreditAccount())
        accountService.niumCardService.customerRepository = mockCustomerRepository
        Mockito.when(mockCustomerRepository.findById(Mockito.any()))
                .thenReturn(Optional.empty())
        accountService.cardProgramRepository = mockCardProgramRepository
        Mockito.when(mockCardProgramRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCreditCardProgram()))
        accountService.cardRepository = mockCardRepository
        Mockito.when(mockCardRepository.save(Mockito.any())).thenReturn(null)
        Mockito.when(mockCardRepository.findByCreditAccount(Mockito.any())).thenReturn([])

        mockClient.expect(HttpMethod.POST)
                .thenReturn(mockObject.mockCreateCardResponse())

        assertThrows(IllegalArgumentException.class, () -> {
            CreateCardRequest testCardRequest = mockObject.getTestCreateCardRequest()
            Card testCard = accountService.createCard(testCardRequest)
        })
    }

    @Test
    void testCreateCardAsync(){
        addCardCallback.cardRepository = mockCardRepository
        Mockito.when(mockCardRepository.save(Mockito.any())).thenReturn(null)
        addCardCallback.creditAccountRepository = mockCreditAccountRepository
        Mockito.when(mockCreditAccountRepository.findById(Mockito.any())).thenReturn(mockObject.getMockCreditAccount())
        niumCardService.customerRepository = mockCustomerRepository
        Mockito.when(mockCustomerRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCustomer()))
        niumCardService.niumSwitchProvider = mockNiumSwitchProvider
        Mockito.when(mockNiumSwitchProvider.executeHttpPostRequestAsync(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer{(addCardCallback)
                        .completed(mockObject.mockAddCardResponseAsync())}

        HttpStatus responseStatus = niumCardService.createCardAsync(mockObject.getTestCreateCardRequest()
                , mockObject.getTestCreditCardProgram())
        assert responseStatus == HttpStatus.OK
    }

    @Test
    void testCreateCardAsyncRetriesExhausted(){
        addCardCallback.cardRepository = mockCardRepository
        Mockito.when(mockCardRepository.save(Mockito.any())).thenReturn(null)
        addCardCallback.creditAccountRepository = mockCreditAccountRepository
        Mockito.when(mockCreditAccountRepository.findById(Mockito.any())).thenReturn(mockObject.getMockCreditAccount())
        niumCardService.customerRepository = mockCustomerRepository
        Mockito.when(mockCustomerRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCustomer()))
        niumCardService.niumSwitchProvider = mockNiumSwitchProvider
        Mockito.when(mockNiumSwitchProvider.executeHttpPostRequestAsync(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenAnswer{
                    addCardCallback.retries = 0
                    (addCardCallback)
                        .failed(new UnirestException(""))}

        assertThrows(Exception.class, () -> {
            niumCardService.createCardAsync(mockObject.getTestCreateCardRequest()
                , mockObject.getTestCreditCardProgram())})
    }
}
