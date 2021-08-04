package hyperface.cms.controllers

import groovy.util.logging.Slf4j
import hyperface.cms.commands.CreateCreditAccountRequest
import hyperface.cms.commands.GenericErrorResponse
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.domains.Customer
import hyperface.cms.repository.CardProgramRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.repository.CustomerRepository
import hyperface.cms.service.AccountService
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
@RequestMapping("/creditAccounts")
@Slf4j
class CreditAccountController {

    @Autowired
    CustomerRepository customerRepository

    @Autowired
    CardProgramRepository cardProgramRepository

    @Autowired
    CreditAccountRepository creditAccountRepository

    @Autowired
    AccountService accountService

    @GetMapping(value = "/{accountId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity getCreditAccount(@PathVariable(name = "accountId", required = true) String accountId){
        Optional<CreditAccount> creditAccountOptional = creditAccountRepository.findById(accountId)
        if(!creditAccountOptional.isPresent()){
            String errorMessage = "No credit account found with Id ${accountId}"
            log.error("Request to get credit account failed due to error: ${errorMessage}")
            return Response.returnError(errorMessage)
        }
        return Response.returnSimpleJson(creditAccountOptional.get())
    }

    // this will be allowed only by
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity createCreditAccount(@Valid @RequestBody CreateCreditAccountRequest req) {
        Optional<Customer> customerOptional = customerRepository.findById(req.customerId)
        if(!customerOptional.isPresent()){
            String errorMessage = "Customer not found with id ${req.customerId}"
            log.error("Failed to create credit account due to: ${errorMessage}")
            return Response.returnError(errorMessage)
        }
        req.customer = customerOptional.get()
        Optional<CreditCardProgram> cardProgramOptional = cardProgramRepository.findById(req.cardProgramId)
        if(!cardProgramOptional.isPresent()){
            String errorMessage = "Credit card program not found with id ${req.cardProgramId}"
            log.error("Failed to create credit account due to: ${errorMessage}")
            return Response.returnError(errorMessage)
        }
        req.cardProgram = cardProgramOptional.get()
        Either<GenericErrorResponse, CreditAccount> creditAccount = accountService.createCreditAccount(req)
        if(creditAccount.isRight()){
            return Response.returnSimpleJson(creditAccount.right().get())
        }
        else{
            String errorMessage = creditAccount.left().get().reason
            log.error("Credit account creation failed with error: ${errorMessage}")
            return Response.returnError(errorMessage)
        }
    }
}
