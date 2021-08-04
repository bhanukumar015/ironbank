package hyperface.cms.controllers

import groovy.util.logging.Slf4j
import hyperface.cms.commands.CardBlockActionRequest
import hyperface.cms.commands.CardChannelControlsRequest
import hyperface.cms.commands.CardLimitsRequest
import hyperface.cms.commands.CreateCardRequest
import hyperface.cms.commands.GenericErrorResponse
import hyperface.cms.commands.GenericSuccessResponse
import hyperface.cms.commands.SetCardPinRequest
import hyperface.cms.domains.Card
import hyperface.cms.repository.CardRepository
import hyperface.cms.service.CardService
import hyperface.cms.util.Response
import io.vavr.control.Either
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.validation.Valid

@RestController
@RequestMapping("/creditCards")
@Slf4j
class CardController {

    @Autowired
    CardService cardService

    @Autowired
    CardRepository cardRepository

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity createCard(@Valid @RequestBody CreateCardRequest req) {
        Either<GenericErrorResponse, List<Card>> cards = cardService.createCard(req)
        if(cards.isRight()){
            return Response.returnSimpleJson(cards.right().get())
        }
        else{
            String errorMessage = cards.left().get().reason
            log.error("CreateCard request failed with error: ${errorMessage}")
            return Response.returnError(errorMessage)
        }
    }

    @GetMapping(value = "/{cardId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity getCard(@PathVariable(name = "cardId") String cardId){
        Optional<Card> cardOptional = cardRepository.findById(cardId)
        if(!cardOptional.isPresent()){
            String errorMessage = "No card found with id: ${cardId}"
            log.error("Get card request failed with error: ${errorMessage}")
            return Response.returnError(errorMessage)
        }
        return Response.returnSimpleJson(cardOptional.get())
    }

    @PostMapping(value = "/{cardId}/activate", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity activateCard(@PathVariable(name = "cardId") String cardId){
        Either<GenericErrorResponse, String> cardActivation = cardService.activateCard(cardId)
        if(cardActivation.isRight()){
            return Response.returnSimpleJson(new GenericSuccessResponse(status: cardActivation.right().get()))
        }
        else{
            String errorMessage = cardActivation.left().get().reason
            log.error("Card activation failed with error: ${errorMessage}")
            return Response.returnError(errorMessage)
        }
    }

    @PostMapping(value = "/{cardId}/pin", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity setPin(@PathVariable(name = "cardId") String cardId
                          , @Valid @RequestBody SetCardPinRequest request){
        Optional<Card> cardOptional = cardRepository.findById(cardId)
        if(!cardOptional.isPresent()){
            String errorMessage = "No card found with id: ${cardId}"
            log.error("Set card pin request failed with error: ${errorMessage}")
            return Response.returnError(errorMessage)
        }
        request.card = cardOptional.get()
        Either<GenericErrorResponse,String> pinSetResponse = cardService.setCardPin(request)
        if(pinSetResponse.right()){
            return Response.returnSimpleJson(new GenericSuccessResponse(status: pinSetResponse.right().get()))
        }
        else{
            String errorMessage = pinSetResponse.left().get().reason
            log.error("Card pin update failed with error: ${errorMessage}")
            return Response.returnError(errorMessage)
        }
    }

    @PostMapping(value = "/{cardId}/lock", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity lockCard(@Valid @RequestBody CardBlockActionRequest req
                            , @PathVariable(name = "cardId") String cardId){
        Optional<Card> cardOptional = cardRepository.findById(cardId)
        if(!cardOptional.isPresent()){
            String errorMessage = "No card found with id: ${cardId}"
            log.error("Card lock action failed with error: ${errorMessage}")
            return Response.returnError(errorMessage)
        }
        req.card = cardOptional.get()
        Either<GenericErrorResponse,String> lockResponse = cardService.invokeCardBlockAction(req)
        if(lockResponse.isRight()){
             return Response.returnSimpleJson(new GenericSuccessResponse(status: lockResponse.right().get()))
        }
        String errorMessage = lockResponse.left().get().reason
        log.error("Card lock action failed with error: ${errorMessage}")
        return Response.returnError(errorMessage)
    }

    @PostMapping(value = "/{cardId}/cardControls", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity updateCardControls(@Valid @RequestBody CardChannelControlsRequest req
                                      , @PathVariable(name = "cardId") String cardId){
        Optional<Card> cardOptional = cardRepository.findById(cardId)
        if(!cardOptional.isPresent()){
            String errorMessage = "No card found with id: ${cardId}"
            log.error("Card controls update request failed with error: ${errorMessage}")
            return Response.returnError(errorMessage)
        }
        req.card = cardOptional.get()
        Either<GenericErrorResponse, Card> card = cardService.updateCardControls(req)
        if(card.isRight()){
            return Response.returnSimpleJson(card.right().get())
        }
        String errorMessage = card.left().get().reason
        log.error("Card activation failed with error: ${errorMessage}")
        return Response.returnError(errorMessage)
    }

    @PostMapping(value = "/{cardId}/cardLimits", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity updateCardLimits(@Valid @RequestBody CardLimitsRequest req
                                    , @PathVariable(name = "cardId") String cardId){
        Optional<Card> cardOptional = cardRepository.findById(cardId)
        if(!cardOptional.isPresent()){
            String errorMessage = "No card found with id: ${cardId}"
            log.error("Update card Limit request failed with error: ${errorMessage}")
            return Response.returnError(errorMessage)
        }
        req.card = cardOptional.get()
        Either<GenericErrorResponse, Card> updatedCard = cardService.setCardLimits(req)
        if(updatedCard.isRight()){
            return Response.returnSimpleJson(updatedCard.right().get())
        }
        String errorMessage = updatedCard.left().get().reason
        log.error("Update card limit request failed with error: ${errorMessage}")
        return Response.returnError(errorMessage)
    }
}
