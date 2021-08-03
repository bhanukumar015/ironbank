package hyperface.cms.service.SwitchProviders.Nium.CardManagement

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import hyperface.cms.Constants
import hyperface.cms.commands.CreateCardRequest
import hyperface.cms.domains.Card
import hyperface.cms.domains.CardControl
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.TransactionLimit
import hyperface.cms.repository.CardControlRepository
import hyperface.cms.repository.CardRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.service.RestCallerService
import hyperface.cms.service.SwitchProviders.Nium.Utility.NiumObjectsCreation
import hyperface.cms.service.SwitchProviders.Nium.Utility.NiumRestUtils
import kong.unirest.Callback
import kong.unirest.HttpResponse
import kong.unirest.HttpStatus
import kong.unirest.JsonNode
import kong.unirest.UnirestException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class NiumCreateCardCallback implements Callback<JsonNode>{

    @Autowired
    NiumRestUtils niumRestUtils

    @Autowired
    CardRepository cardRepository

    @Autowired
    CreditAccountRepository creditAccountRepository

    @Autowired
    NiumObjectsCreation niumObjectsCreation

    @Autowired
    CardControlRepository cardControlRepository

    @Autowired
    RestCallerService restCallerService

    CreateCardRequest cardRequest
    CreditCardProgram cardProgram
    String endpoint
    int retries

    private Logger log = LoggerFactory.getLogger(NiumCreateCardCallback.class)
    private static ObjectMapper objectMapper = new ObjectMapper()

    @Override
    void completed(HttpResponse<JsonNode> response) {
        if(response.getStatus() == HttpStatus.OK) {
            log.info "POST request with request id ${response.getHeaders().get('x-request-id')} " +
                    "to Nium passed"
            CreditAccount creditAccount = creditAccountRepository.findById(cardRequest.creditAccountId)
                    .orElseThrow(() -> new IllegalArgumentException("No credit account found " +
                            "with the given Id ${cardRequest.creditAccountId}"))
            def switchCardMetadata = objectMapper.readValue(response.getBody().toString(),
                    new TypeReference<Map<String, Object>>() {})

            Card card = new Card()
            card.creditAccount = creditAccount
            card.cardProgram = cardProgram
            card.cardBin = cardProgram.cardBin
            card.cardType == cardRequest.cardType
            card.cardExpiryMonth = 10
            card.cardExpiryYear = 2030
            card.switchCardId = switchCardMetadata.get('cardHashId').toString()
            card.lastFourDigits = switchCardMetadata.get('maskedCardNumber').toString()[-4..-1]
            card.physicallyIssued = false
            card.virtuallyIssued = true
            card.virtualCardActivated = (card.cardType != Constants.CardType.Physical)
                    ? cardProgram.virtualCardActivation == CreditCardProgram.CardActivation.AUTO
                    : false
            card.physicalCardActivated = false
            CardControl cardControl = new CardControl().tap{
                cardSuspendedByCustomer = false
                enableOverseasTransactions = false
                enableOfflineTransactions = false
                enableNFC = false
                enableOnlineTransactions = !(card.cardType == Constants.CardType.Phygital)
                enableCashWithdrawal = false
                enableMagStripe = false
                dailyTransactionLimit = new TransactionLimit().tap{
                    value = cardProgram.defaultDailyTransactionLimit
                }
                dailyCashWithdrawalLimit = new TransactionLimit().tap{
                    value = cardProgram.defaultDailyCashWithdrawalLimit
                }
                perTransactionLimit = new TransactionLimit()
                monthlyTransactionLimit = new TransactionLimit()
                lifetimeTransactionLimit = new TransactionLimit()
            }
            cardControlRepository.save(cardControl)
            card.cardControl = cardControl
            cardRepository.save(card)
        }
        else if(retries > 0){
            // Slow down in case of status code 429(too many requests, rate limit hit)
            if(response.status == HttpStatus.TOO_MANY_REQUESTS) {sleep(2000)}
            log.info "Request to Nium failed with status code ${response.status}. Retrying..."
            String requestBody = niumObjectsCreation.createNiumRequestCard(cardRequest, cardProgram)
            this.retries -= 1
            restCallerService.executeHttpPostRequestAsync(niumRestUtils.prepareURL(endpoint), niumRestUtils.getHeaders(), requestBody, this)
        }
        else{
            log.info "Request to Nium failed with status code ${response.status}"
            throw new Exception("Retries exhausted. Request to create card failed!")
        }
    }

    @Override
    void failed(UnirestException e) {
        if(retries > 0){
            log.info "Request to Nium failed with exception ${e.message}. Retrying..."
            String requestBody = niumObjectsCreation.createNiumRequestCard(cardRequest, cardProgram)
            this.retries -= 1
            restCallerService.executeHttpPostRequestAsync(niumRestUtils.prepareURL(endpoint), niumRestUtils.getHeaders(), requestBody, this)
        }
        else{
            throw new Exception("Retries exhausted. Request to create card failed with message ${e.message}")
        }
    }

    @Override
    void cancelled() {
    }
}
