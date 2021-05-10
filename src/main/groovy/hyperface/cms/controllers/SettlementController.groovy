package hyperface.cms.controllers

import hyperface.cms.commands.SettlementDebitRequest
import hyperface.cms.domains.*
import hyperface.cms.repository.CardProgramRepository
import hyperface.cms.repository.CardRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.repository.CustomerRepository
import hyperface.cms.service.AccountService
import hyperface.cms.service.AuthorizationManager
import hyperface.cms.service.PaymentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settlements")
public class SettlementController {

    @Autowired
    CustomerRepository customerRepository

    @Autowired
    CreditAccountRepository creditAccountRepository

    @Autowired
    CardProgramRepository cardProgramRepository

    @Autowired
    AccountService accountService

    @Autowired
    PaymentService paymentService

    @Autowired
    CardRepository cardRepository

    @Autowired
    AuthorizationManager authorizationManager

    @RequestMapping(value = "/settlement-debit", method = RequestMethod.POST,
                    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, String> settlementDebit(SettlementDebitRequest req) {
        println req.dump()
        Card card = cardRepository.findById(req.cardId).get()
        req.card = card
        paymentService.processSettlementDebit(req)
        return ["responseCode": "00"]
    }

}