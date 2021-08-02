package hyperface.cms.controllers

import groovy.util.logging.Slf4j
import hyperface.cms.commands.GenericErrorResponse
import hyperface.cms.commands.rewards.RewardsRequest
import hyperface.cms.commands.rewards.RewardsResponse
import hyperface.cms.domains.CardStatement
import hyperface.cms.domains.CreditAccount
import hyperface.cms.domains.rewards.Reward
import hyperface.cms.repository.CardStatementRepository
import hyperface.cms.repository.CreditAccountRepository
import hyperface.cms.service.RewardService
import hyperface.cms.service.StatementService
import hyperface.cms.util.Response
import io.vavr.control.Either
import org.apache.pdfbox.pdmodel.PDDocument
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
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
@RequestMapping(value = "account")
@Slf4j
class AccountController {
    @Autowired
    private CreditAccountRepository creditAccountRepository

    @Autowired
    private RewardService rewardService

    @Autowired
    StatementService statementService

    @Autowired
    CardStatementRepository cardStatementRepository

    @GetMapping(value = "rewards/{accountId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RewardsResponse> getRewardsSummary(@PathVariable(name = "accountId") String accountId) {
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
    ResponseEntity<RewardsResponse> processRewards(@Valid @RequestBody RewardsRequest request) {

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

    @RequestMapping(value = "/downloadStatement/{statementId}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_PDF_VALUE)
    ResponseEntity<InputStreamResource> downloadStatement(@PathVariable String statementId) throws IOException {
        Optional<CardStatement> statement = cardStatementRepository.findById(statementId)

        String errorMessage = null
        if (!statement.isPresent()) {
            errorMessage = "Statement record with id: ${statementId} is not found"
            log.error("Statement record with id: ${statementId} is not found")
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage)
        }

        ByteArrayInputStream inputStream = null
        try {
            Either<GenericErrorResponse, PDDocument> statementResult = statementService.generateStatement(statement.get())
            if (statementResult.isLeft()) {
                errorMessage = statementResult.left().get().reason
                log.error("Failed to generate downloadable statement: ${statementId} due to ${errorMessage}")
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage)
            }

            PDDocument document = statementResult.right().get()

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

            document.save(outputStream)
            document.close()

            HttpHeaders headers = new HttpHeaders()
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=${statementId}.pdf")

            byte[] contentBytes = outputStream.toByteArray()
            inputStream = new ByteArrayInputStream(contentBytes)
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(inputStream))
        } catch (Exception e) {
            println e.printStackTrace()
            log.error("Error occurred while generating statement: ${e.message}")
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage)
        } finally {
            if (inputStream != null) {
                inputStream.close()
            }
        }
    }
}
