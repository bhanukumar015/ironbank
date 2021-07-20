package hyperface.cms.SwitchProvidersTests.Nium

import com.fasterxml.jackson.databind.ObjectMapper
import hyperface.cms.Utility.MockObjects
import hyperface.cms.commands.CardBlockActionRequest
import hyperface.cms.commands.CreateCardRequest
import hyperface.cms.commands.SetCardPinRequest
import hyperface.cms.domains.Card
import hyperface.cms.domains.Customer
import hyperface.cms.repository.CardProgramRepository
import hyperface.cms.repository.CardRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.repository.CustomerRepository
import hyperface.cms.service.CardService
import hyperface.cms.service.SwitchProviders.Nium.CardManagement.NiumCreateCardCallback
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
import java.security.InvalidParameterException

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
    CardService cardService

    @Autowired
    NiumCreateCardCallback createCardCallback

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
        cardService.creditAccountRepository = mockCreditAccountRepository
        Mockito.when(mockCreditAccountRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCreditAccount()))
        cardService.niumCardService.customerRepository = mockCustomerRepository
        Mockito.when(mockCustomerRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCustomer()))
        cardService.cardProgramRepository = mockCardProgramRepository
        Mockito.when(mockCardProgramRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCreditCardProgram()))
        cardService.cardRepository = mockCardRepository
        Mockito.when(mockCardRepository.save(Mockito.any())).thenReturn(null)
        niumCardService.niumSwitchProvider = mockNiumSwitchProvider
        Mockito.when(mockNiumSwitchProvider.executeHttpPostRequestSync(Mockito.anyString()
                , Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(mockObject.mockCreateCardResponse())

        mockClient.expect(HttpMethod.POST)
                .thenReturn(mockObject.mockCreateCardResponse())

        CreateCardRequest testCardRequest = mockObject.getTestCreateCardRequest()
        Card testCard = cardService.createCard(testCardRequest)

        // Assert the fields received from Nium switch
        assertNotNull(testCard.switchCardId)
        assertEquals(testCard.lastFourDigits, '3456')
    }

    @Test
    void testCreateCardSyncException(){
        cardService.creditAccountRepository = mockCreditAccountRepository
        Mockito.when(mockCreditAccountRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCreditAccount()))
        cardService.niumCardService.customerRepository = mockCustomerRepository
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

        assertThrows(IllegalArgumentException.class, () -> {
            CreateCardRequest testCardRequest = mockObject.getTestCreateCardRequest()
            Card testCard = cardService.createCard(testCardRequest)
        })
    }

    @Test
    void testCreateCardAsync(){
        createCardCallback.cardRepository = mockCardRepository
        Mockito.when(mockCardRepository.save(Mockito.any())).thenReturn(null)
        createCardCallback.creditAccountRepository = mockCreditAccountRepository
        Mockito.when(mockCreditAccountRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCreditAccount()))
        niumCardService.customerRepository = mockCustomerRepository
        Mockito.when(mockCustomerRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCustomer()))
        niumCardService.niumSwitchProvider = mockNiumSwitchProvider
        Mockito.when(mockNiumSwitchProvider.executeHttpPostRequestAsync(Mockito.any()
                , Mockito.any(), Mockito.any()))
                .thenAnswer{(createCardCallback)
                        .completed(mockObject.mockAddCardResponseAsync())}

        HttpStatus responseStatus = niumCardService.createCardAsync(mockObject.getTestCreateCardRequest()
                , mockObject.getTestCreditCardProgram())
        assert responseStatus == HttpStatus.OK
    }

    @Test
    void testCreateCardAsyncRetriesExhausted(){
        createCardCallback.cardRepository = mockCardRepository
        Mockito.when(mockCardRepository.save(Mockito.any())).thenReturn(null)
        createCardCallback.creditAccountRepository = mockCreditAccountRepository
        Mockito.when(mockCreditAccountRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCreditAccount()))
        niumCardService.customerRepository = mockCustomerRepository
        Mockito.when(mockCustomerRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCustomer()))
        niumCardService.niumSwitchProvider = mockNiumSwitchProvider
        Mockito.when(mockNiumSwitchProvider.executeHttpPostRequestAsync(Mockito.any()
                , Mockito.any(), Mockito.any()))
                .thenAnswer{
                    createCardCallback.retries = 0
                    (createCardCallback)
                        .failed(new UnirestException(""))}

        assertThrows(Exception.class, () -> {
            niumCardService.createCardAsync(mockObject.getTestCreateCardRequest()
                , mockObject.getTestCreditCardProgram())})
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
        niumCardService.niumSwitchProvider = mockNiumSwitchProvider
        Mockito.when(mockNiumSwitchProvider.executeHttpPostRequestSync(Mockito.anyString()
                , Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(new ObjectMapper().writeValueAsString(new Object(){
                    String status = 'Success'
                }))

        assertTrue(cardService.setCardPin(request))
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

        assertThrows(InvalidParameterException.class, () -> {cardService.setCardPin(request)})
    }

    @Test
    void testCardBlockAction(){
        cardService.cardRepository = mockCardRepository
        Mockito.when(mockCardRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCard()))
        niumCardService.niumSwitchProvider = mockNiumSwitchProvider
        Mockito.when(mockNiumSwitchProvider.executeHttpPostRequestSync(Mockito.anyString()
                , Mockito.any(), Mockito.anyInt()))
                .thenReturn(mockObject.mockBlockCardResponse())

        CardBlockActionRequest request = new CardBlockActionRequest().tap{
            cardId = UUID.randomUUID().toString()
            blockAction = CardBlockActionRequest.BlockAction.TEMPORARYBLOCK
            reason = CardBlockActionRequest.BlockActionReason.DAMAGED
        }
        Card card = cardService.invokeCardBlockAction(request)
        assertTrue(card.isLocked)
        assertFalse(card.hotlisted)
    }

    @Test
    void testUpdateCardControls(){
        cardService.cardRepository = mockCardRepository
        Mockito.when(mockCardRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(mockObject.getTestCard()))
        Card card = cardService.updateCardControls(mockObject.getTestCardControlsRequest())
        assertTrue(card.enableOnlineTransactions)
        assertTrue(card.enableOverseasTransactions)
        assertTrue(card.enableOfflineTransactions)
        assertFalse(card.enableNFC)
        assertFalse(card.enableMagStripe)
        assertFalse(card.enableCashWithdrawal)
    }

    @Test
    void testActivateCardSync(){
        cardService.cardRepository = mockCardRepository
        Mockito.when(mockCardRepository.findById(Mockito.any())).thenReturn(Optional.of(mockObject.getTestCard()))
        Mockito.when(mockCardRepository.save(Mockito.any())).thenReturn(null)
        niumCardService.niumSwitchProvider = mockNiumSwitchProvider
        Mockito.when(mockNiumSwitchProvider.executeHttpPostRequestSync(Mockito.anyString()
                , Mockito.any(), Mockito.anyInt()))
                .thenReturn(mockObject.mockActivateCardResponse())

        Card card = cardService.activateCard(UUID.randomUUID().toString())
        assertTrue(card.physicallyIssued)
        assertTrue(card.physicalCardActivatedByCustomer)
        assertFalse(card.virtuallyIssued)
        assertFalse(card.virtualCardActivatedByCustomer)
    }

    @Test
    void testActivateCardSyncFailure(){
        cardService.cardRepository = mockCardRepository
        Mockito.when(mockCardRepository.findById(Mockito.any())).thenReturn(Optional.of(mockObject.getTestCard()))
        Mockito.when(mockCardRepository.save(Mockito.any())).thenReturn(null)

        niumCardService.niumSwitchProvider = mockNiumSwitchProvider
        Mockito.when(mockNiumSwitchProvider.executeHttpPostRequestSync(Mockito.anyString()
                , Mockito.any(), Mockito.anyInt()))
                .thenReturn(mockObject.mockActivateCardResponseFailure())

        assertThrows(Exception.class, () -> {
            Card card = cardService.activateCard(UUID.randomUUID().toString())
        })
    }

    @Test
    void testCardLimitControls(){
        cardService.cardRepository = mockCardRepository
        Mockito.when(mockCardRepository.findById(Mockito.any())).thenReturn(Optional.of(mockObject.getTestCard()))
        Mockito.when(mockCardRepository.save(Mockito.any())).thenReturn(null)

        Card card = cardService.setCardLimits(mockObject.getTestCardLimitRequest())
        assert card.perTransactionLimit.value == 100.00
        assert card.monthlyTransactionLimit.isEnabled
        assert card.dailyTransactionLimit.additionalMarginPercentage == 5.00
    }
}
