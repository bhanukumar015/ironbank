package hyperface.cms.controllers

import groovy.util.logging.Slf4j
import hyperface.cms.commands.AccountStatementsResponse
import hyperface.cms.commands.AccountSummaryResponse
import hyperface.cms.commands.FetchAccountStatementsRequest
import hyperface.cms.commands.FetchAccountTransactionsRequest
import hyperface.cms.commands.TransactionResponse
import hyperface.cms.commands.rewards.RewardsRequest
import hyperface.cms.commands.rewards.RewardsResponse
import hyperface.cms.domains.CardStatement
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.rewards.Reward
import hyperface.cms.repository.CardStatementRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.service.AccountService
import hyperface.cms.service.RewardService
import hyperface.cms.util.Response
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

import javax.validation.Valid

@RestController
@RequestMapping(value = "accounts")
@Slf4j
class AccountController {
    @Autowired
    private CreditAccountRepository creditAccountRepository

    @Autowired
    private RewardService rewardService

    @Autowired
    AccountService accountService

    @Autowired
    CardStatementRepository cardStatementRepository

    @GetMapping(value = "rewards/{accountId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity getRewardsSummary(@PathVariable(name = "accountId") String accountId) {
        // check if account exists with creditAccountID in request
        Optional<CreditAccount> creditAccountOptional = creditAccountRepository.findById(accountId)

        // return error response if credit_account_id isn't found.
        if (creditAccountOptional.isEmpty()) {
            String errorMessage = "Credit account with ID: [" + accountId + "] does not exist."
            log.error("Error occurred while fetching rewards summary for the accountID : [{}]. Exception: [{}]", accountId, errorMessage)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage)
        }

        RewardsResponse res = rewardService.getSummary(creditAccountOptional.get())
        return Response.returnSimpleJson(res)
    }

    @PostMapping(value = "rewards", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity processRewards(@Valid @RequestBody RewardsRequest request) {

        // check if account exists with creditAccountID in request
        Optional<CreditAccount> creditAccountOptional = creditAccountRepository.findById(request.getCreditAccountId())

        // return error response if credit_account_id isn't found.
        if (creditAccountOptional.isEmpty()) {
            String errorMessage = "Credit account with ID: [ " + request.getCreditAccountId() + " ] does not exist."
            log.error("Error occurred while processing rewards [{}] API. Exception: [{}]", request.getOperation().toString(), errorMessage)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage)
        }

        CreditAccount creditAccount = creditAccountOptional.get()
        Reward reward = creditAccount.getReward()

        // return error response if no rewards program is associated with this account
        if (null == reward) {
            String errorMessage = "No rewards program associated with this credit account."
            log.error("Error occurred while processing rewards [{}] API. Exception: [{}]", request.getOperation().toString(), errorMessage)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage)
        }

        RewardsResponse res = rewardService.executeOperation(request, creditAccount, reward)
        return Response.returnSimpleJson(res)
    }

    @GetMapping(value = "/{accountId}/transactions", consumes = MediaType.APPLICATION_JSON_VALUE
                , produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity getTransactions(@PathVariable(name = "accountId") String accountId
                                   , FetchAccountTransactionsRequest req){
        Optional<CreditAccount> accountOptional = creditAccountRepository.findById(accountId)
        if(!accountOptional.isPresent()){
            String errorMessage = "Credit account with ID: [ ${accountId} ] does not exist."
            log.error("Fetch transactions failed with error: ${errorMessage}")
            return Response.returnError(errorMessage)
        }
        req.account = accountOptional.get()
        List<TransactionResponse> transactions = accountService.fetchAccountTransactions(req)

        return Response.returnSimpleJson(accountService.getAccountTransactionResponse(req, transactions))
    }

    @GetMapping(value = "/{accountId}/statements", consumes = MediaType.APPLICATION_JSON_VALUE
                , produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity fetchAccountStatements(FetchAccountStatementsRequest req
                                          , @PathVariable(name = "accountId") String accountId){
        Optional<CreditAccount> accountOptional = creditAccountRepository.findById(accountId)
        if(!accountOptional.isPresent()){
            String errorMessage = "No credit account found with id: ${accountId}"
            log.error("Fetch account statements request failed with error: ${errorMessage}")
            return Response.returnError(errorMessage)
        }
        req.account = accountOptional.get()
        List<CardStatement> accountStatements = cardStatementRepository.findByAccountInRange(req.account, req.fromDate, req.toDate)
        AccountStatementsResponse response = new AccountStatementsResponse().tap{
            count = accountStatements.size()
            statements = accountStatements
        }
        return Response.returnSimpleJson(response)
    }

    @GetMapping(value = "/{accountId}/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity fetchAccountSummary(@PathVariable(name = "accountId") String accountId){
        Optional<CreditAccount> accountOptional = creditAccountRepository.findById(accountId)
        if(!accountOptional.isPresent()){
            String errorMessage = "No credit account found with id: ${accountId}"
            log.error("FetchAccountSummary request failed with error: ${errorMessage}")
            return Response.returnError(errorMessage)
        }
        CreditAccount account = accountOptional.get()
        return Response.returnSimpleJson(accountService.fetchAccountSummary(account))
    }
}
