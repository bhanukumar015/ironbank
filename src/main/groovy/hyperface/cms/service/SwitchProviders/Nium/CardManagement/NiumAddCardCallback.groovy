package hyperface.cms.service.SwitchProviders.Nium.CardManagement

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import hyperface.cms.commands.CreateCardRequest
import hyperface.cms.domains.Card
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.repository.CardProgramRepository
import hyperface.cms.repository.CardRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.service.SwitchProviders.Nium.NiumSwitchProvider
import hyperface.cms.service.SwitchProviders.Nium.Utility.NiumObjectsCreation
import kong.unirest.Callback
import kong.unirest.HttpResponse
import kong.unirest.JsonNode
import kong.unirest.UnirestException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class NiumAddCardCallback implements Callback<JsonNode>{

    @Autowired
    NiumSwitchProvider niumSwitchProvider

    @Autowired
    CardRepository cardRepository

    @Autowired
    CreditAccountRepository creditAccountRepository

    @Autowired
    CardProgramRepository cardProgramRepository

    @Autowired
    NiumObjectsCreation niumObjectsCreation

    CreateCardRequest cardRequest
    CreditCardProgram cardProgram
    String endpoint
    int retries

    private Logger log = LoggerFactory.getLogger(NiumAddCardCallback.class)
    private static ObjectMapper objectMapper = new ObjectMapper()

    @Override
    void completed(HttpResponse<JsonNode> response) {
        if(response.getStatus() == 200) {
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
            card.cardExpiryMonth = 10
            card.cardExpiryYear = 2030
            card.switchCardId = switchCardMetadata.get('cardHashId').toString()
            card.lastFourDigits = switchCardMetadata.get('maskedCardNumber').toString()[-4..-1]
            card.physicallyIssued = false
            card.virtuallyIssued = true
            card.virtualCardActivatedByCustomer = false
            card.physicalCardActivatedByCustomer = false
            card.cardSuspendedByCustomer = false
            card.enableOverseasTransactions = false
            card.enableDomesticTransactions = false
            card.enableNFC = false
            card.enableOnlineTransactions = false
            card.enableCashWithdrawal = false

            card.dailyTransactionLimit = cardProgram.defaultDailyTransactionLimit
            card.dailyCashWithdrawalLimit = cardProgram.defaultDailyCashWithdrawalLimit

            cardRepository.save(card)
        }
        else if(retries > 0){
            // Slow down in case of status code 429(too many requests, rate limit hit)
            if(response.status == 429) {sleep(2000)}
            log.info "Request to Nium failed with status code ${response.status}. Retrying..."
            String requestBody = niumObjectsCreation.createNiumRequestCard(cardRequest, cardProgram)
            this.retries -= 1
            niumSwitchProvider.executeHttpPostRequestAsync(endpoint, requestBody, this)
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
            niumSwitchProvider.executeHttpPostRequestAsync(endpoint, requestBody, this)
        }
        else{
            throw new Exception("Retries exhausted. Request to create card failed with message ${e.message}")
        }
    }

    @Override
    void cancelled() {
    }
}
