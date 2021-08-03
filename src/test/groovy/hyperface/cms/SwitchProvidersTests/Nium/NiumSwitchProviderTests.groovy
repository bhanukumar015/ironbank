package hyperface.cms.SwitchProvidersTests.Nium

import com.fasterxml.jackson.databind.ObjectMapper
import hyperface.cms.Utility.MockObjects
import hyperface.cms.commands.CardBlockActionRequest
import hyperface.cms.commands.CreateCardRequest
import hyperface.cms.commands.SetCardPinRequest
import hyperface.cms.config.SwitchProvidersConfig
import hyperface.cms.domains.Card
import hyperface.cms.domains.Customer
import hyperface.cms.repository.CardProgramRepository
import hyperface.cms.repository.CardRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.repository.CustomerRepository
import hyperface.cms.service.CardService
import hyperface.cms.service.RestCallerService
import hyperface.cms.service.SwitchProviders.Nium.CardManagement.NiumCreateCardCallback
import hyperface.cms.service.SwitchProviders.Nium.CardManagement.NiumCardService
import hyperface.cms.service.SwitchProviders.Nium.CustomerManagement.NiumCreateCustomerCallback
import hyperface.cms.service.SwitchProviders.Nium.CustomerManagement.NiumCustomerService
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
    RestCallerService mockRestCallerService

    @Autowired
    SwitchProvidersConfig switchProvidersConfig

    @Autowired
    NiumCustomerService niumCustomerService

    @Autowired
    NiumCardService niumCardService

    @Autowired
    CardService cardService

    @Autowired
    NiumCreateCardCallback createCardCallback

    @Autowired
    NiumCreateCustomerCallback createCustomerCallback

    @Test
    void testNiumSwitchCreateCustomer(){
        mockClient.expect(HttpMethod.POST, switchProvidersConfig.niumUrl + niumCustomerService.createCustomerEndpoint)
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
        mockClient.expect(HttpMethod.POST, switchProvidersConfig.niumUrl + niumCustomerService.createCustomerEndpoint)
                .thenReturn(mockObject.mockCreateCustomerResponse())
        createCustomerCallback.customerRepository = mockCustomerRepository
        Mockito.when(mockCustomerRepository.save(Mockito.any(Customer.class))).thenReturn(null)
        niumCustomerService.restCallerService = mockRestCallerService
        Mockito.when(mockRestCallerService.executeHttpPostRequestAsync(Mockito.anyString(), Mockito.any(Map.class), Mockito.anyString(), Mockito.any()))
                .thenAnswer{(createCustomerCallback)
                        .completed(mockObject.mockCreateCustomerResponseAsync())}

        Customer customer = mockObject.getTestCustomer()
        HttpStatus response = niumCustomerService.createCustomerAsync(customer)
        // TODO: add better assert statements based on final response
        assert response == HttpStatus.OK
    }

    @Test
    void testCreateCardSync(){
        cardService.creditAccountRepository = mockCreditAccountRepository
        Mockito.when(mockCreditAccountRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCreditAccount()))
        cardService.customerRepository = mockCustomerRepository
        Mockito.when(mockCustomerRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCustomer()))
        cardService.cardProgramRepository = mockCardProgramRepository
        Mockito.when(mockCardProgramRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCreditCardProgram()))
        cardService.cardRepository = mockCardRepository
        Mockito.when(mockCardRepository.save(Mockito.any())).thenReturn(null)
        niumCardService.restCallerService = mockRestCallerService
        Mockito.when(mockRestCallerService.executeHttpPostRequestSync(Mockito.anyString(), Mockito.any(Map.class)
                , Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(mockObject.mockCreateCardResponse())

        mockClient.expect(HttpMethod.POST)
                .thenReturn(mockObject.mockCreateCardResponse())

        CreateCardRequest testCardRequest = mockObject.getTestCreateCardRequest()
        def response = cardService.createCard(testCardRequest)
        assertTrue(response.isRight())
        List<Card> cards = response.right().get()
        // Assert the fields received from Nium switch
        assertNotNull(cards.first().switchCardId)
        assertEquals(cards.first().lastFourDigits, '3456')
    }

    @Test
    void testCreateCardSyncFailure(){
        cardService.creditAccountRepository = mockCreditAccountRepository
        Mockito.when(mockCreditAccountRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCreditAccount()))
        cardService.customerRepository = mockCustomerRepository
        Mockito.when(mockCustomerRepository.findById(Mockito.any()))
                .thenReturn(Optional.empty())
        cardService.cardProgramRepository = mockCardProgramRepository
        Mockito.when(mockCardProgramRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCreditCardProgram()))
        cardService.cardRepository = mockCardRepository
        Mockito.when(mockCardRepository.save(Mockito.any())).thenReturn(null)
        Mockito.when(mockCardRepository.findByCreditAccount(Mockito.any())).thenReturn([])

        mockClient.expect(HttpMethod.POST)
                .thenReturn(mockObject.mockCreateCardResponse())

        assertTrue(cardService.createCard(mockObject.getTestCreateCardRequest()).isLeft())
    }

    @Test
    void testCreateCardAsync(){
        createCardCallback.cardRepository = mockCardRepository
        Mockito.when(mockCardRepository.save(Mockito.any())).thenReturn(null)
        createCardCallback.creditAccountRepository = mockCreditAccountRepository
        Mockito.when(mockCreditAccountRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCreditAccount()))
        niumCardService.restCallerService = mockRestCallerService
        Mockito.when(mockRestCallerService.executeHttpPostRequestAsync(Mockito.anyString(), Mockito.any(Map.class)
                , Mockito.anyString(), Mockito.any()))
                .thenAnswer{(createCardCallback)
                        .completed(mockObject.mockAddCardResponseAsync())}
        Customer customer = mockObject.getTestCustomer()
        HttpStatus responseStatus = niumCardService.createCardAsync(mockObject.getTestCreateCardRequest()
                , mockObject.getTestCreditCardProgram(), customer.switchMetadata)
        assert responseStatus == HttpStatus.OK
    }

    @Test
    void testCreateCardAsyncRetriesExhausted(){
        createCardCallback.cardRepository = mockCardRepository
        Mockito.when(mockCardRepository.save(Mockito.any())).thenReturn(null)
        createCardCallback.creditAccountRepository = mockCreditAccountRepository
        Mockito.when(mockCreditAccountRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCreditAccount()))
        niumCardService.restCallerService = mockRestCallerService
        Mockito.when(mockRestCallerService.executeHttpPostRequestAsync(Mockito.anyString(), Mockito.any(Map.class)
                , Mockito.anyString(), Mockito.any()))
                .thenAnswer{
                    createCardCallback.retries = 0
                    (createCardCallback)
                        .failed(new UnirestException(""))}
        Customer customer = mockObject.getTestCustomer()
        assertThrows(Exception.class, () -> {
            niumCardService.createCardAsync(mockObject.getTestCreateCardRequest()
                , mockObject.getTestCreditCardProgram(), customer.switchMetadata)})
    }

    @Test
    void testSetCardPin(){
        cardService.cardRepository = mockCardRepository
        Mockito.when(mockCardRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCard()))
        SetCardPinRequest request = new SetCardPinRequest().tap {
            cardId = UUID.randomUUID().toString()
            cardPin = '1234'
        }
        niumCardService.restCallerService = mockRestCallerService
        Mockito.when(mockRestCallerService.executeHttpPostRequestSync(Mockito.anyString(), Mockito.any(Map.class)
                , Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(new ObjectMapper().writeValueAsString(new Object(){
                    String status = 'Success'
                }))
        assertTrue(cardService.setCardPin(request).right().get())
    }

    @Test
    void testSetCardPinInvalidPin(){
        cardService.cardRepository = mockCardRepository
        Mockito.when(mockCardRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCard()))
        SetCardPinRequest request = new SetCardPinRequest().tap {
            cardId = UUID.randomUUID().toString()
            cardPin = '123A'
        }

        assertTrue(cardService.setCardPin(request).isLeft())
    }

    @Test
    void testCardBlockAction(){
        cardService.cardRepository = mockCardRepository
        Mockito.when(mockCardRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCard()))
        niumCardService.restCallerService = mockRestCallerService
        Mockito.when(mockRestCallerService.executeHttpPostRequestSync(Mockito.anyString(), Mockito.any(Map.class)
                , Mockito.any(), Mockito.anyInt()))
                .thenReturn(mockObject.mockBlockCardResponse())

        CardBlockActionRequest request = new CardBlockActionRequest().tap{
            cardId = UUID.randomUUID().toString()
            blockAction = CardBlockActionRequest.BlockAction.TEMPORARYBLOCK
            reason = CardBlockActionRequest.BlockActionReason.DAMAGED
        }
        def response = cardService.invokeCardBlockAction(request)
        assertTrue(response.isRight())
        Card card = response.right().get()
        assertTrue(card.isLocked)
        assertFalse(card.hotlisted)
    }

    @Test
    void testUpdateCardControls(){
        cardService.cardRepository = mockCardRepository
        Mockito.when(mockCardRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCard()))
        def response = cardService.updateCardControls(mockObject.getTestCardControlsRequest())
        assertTrue(response.isRight())
        Card card = response.right().get()
        assertTrue(card.cardControl.enableOnlineTransactions)
        assertTrue(card.cardControl.enableOverseasTransactions)
        assertTrue(card.cardControl.enableOfflineTransactions)
        assertFalse(card.cardControl.enableNFC)
        assertFalse(card.cardControl.enableMagStripe)
        assertFalse(card.cardControl.enableCashWithdrawal)
    }

    @Test
    void testActivateCardSync(){
        cardService.cardRepository = mockCardRepository
        Mockito.when(mockCardRepository.findById(Mockito.any())).thenReturn(Optional.of(mockObject.getTestCard()))
        Mockito.when(mockCardRepository.save(Mockito.any())).thenReturn(null)
        niumCardService.restCallerService = mockRestCallerService
        Mockito.when(mockRestCallerService.executeHttpPostRequestSync(Mockito.anyString(), Mockito.any(Map.class)
                , Mockito.any(), Mockito.anyInt()))
                .thenReturn(mockObject.mockActivateCardResponse())

        def response = cardService.activateCard(UUID.randomUUID().toString())
        assertTrue(response.isRight())
        Card card = response.right().get()
        assertTrue(card.physicallyIssued)
        assertTrue(card.physicalCardActivated)
        assertFalse(card.virtuallyIssued)
        assertFalse(card.virtualCardActivated)
    }

    @Test
    void testActivateCardSyncFailure(){
        cardService.cardRepository = mockCardRepository
        Mockito.when(mockCardRepository.findById(Mockito.any())).thenReturn(Optional.of(mockObject.getTestCard()))
        Mockito.when(mockCardRepository.save(Mockito.any())).thenReturn(null)

        niumCardService.restCallerService = mockRestCallerService
        Mockito.when(mockRestCallerService.executeHttpPostRequestSync(Mockito.anyString(), Mockito.any(Map.class)
                , Mockito.any(), Mockito.anyInt()))
                .thenReturn(mockObject.mockActivateCardResponseFailure())

        assertThrows(Exception.class, () -> {
            cardService.activateCard(UUID.randomUUID().toString())
        })
    }

    @Test
    void testCardLimitControls(){
        cardService.cardRepository = mockCardRepository
        Mockito.when(mockCardRepository.findById(Mockito.any())).thenReturn(Optional.of(mockObject.getTestCard()))
        Mockito.when(mockCardRepository.save(Mockito.any())).thenReturn(null)

        def response = cardService.setCardLimits(mockObject.getTestCardLimitRequest())
        assertTrue(response.isRight())
        Card card = response.right().get()
        assert card.cardControl.perTransactionLimit.value == 100.00
        assert card.cardControl.monthlyTransactionLimit.isEnabled
        assert card.cardControl.dailyTransactionLimit.additionalMarginPercentage == 5.00
    }
}
