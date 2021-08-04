package hyperface.cms

import hyperface.cms.Utility.MockObjects
import hyperface.cms.commands.CreateCreditAccountRequest
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.repository.CardProgramRepository
import hyperface.cms.repository.CreditCardScheduleOfChargesRepository
import hyperface.cms.service.AccountService
import hyperface.cms.util.CardProgramManagement
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class AccountTest {
    @Autowired
    CardProgramRepository cardProgramRepository
    
    @Mock
    CardProgramRepository mockCardProgramRepository

    @Autowired
    AccountService accountService

    @Autowired
    CardProgramManagement cardProgramManagement

    @Autowired
    CreditCardScheduleOfChargesRepository scheduleOfChargesRepository

    static MockObjects mockObjects = new MockObjects()

    @Test
    void testCreateCreditAccount(){
        CreditCardProgram creditCardProgram = mockObjects.getTestCreditCardProgram()
        creditCardProgram.isActive = false
        creditCardProgram.disableLevel = CreditCardProgram.DisableLevel.MANUAL
        def response = accountService.createCreditAccount(new CreateCreditAccountRequest().tap{
            customer = mockObjects.getTestCustomer()
            cardProgram = creditCardProgram
            approvedCreditLimit = 0})
        assert response.isLeft()
    }

    @Test
    void testResetDailyAccountCount(){
        CreditCardProgram cardProgram = mockObjects.getTestCreditCardProgram()
        cardProgram.isActive = false
        cardProgram.disableLevel = CreditCardProgram.DisableLevel.DAILY
        cardProgram.currentDayAccountCount = cardProgram.dailyAccountLimit
        scheduleOfChargesRepository.save(cardProgram.scheduleOfCharges)
        cardProgramRepository.save(cardProgram)

        CreditCardProgram savedCardProgram = cardProgramRepository.findById(cardProgram.id).get()
        assert(!savedCardProgram.isActive)
        assert(savedCardProgram.currentDayAccountCount == cardProgram.currentDayAccountCount)

        cardProgramManagement.resetDailyAccountCount()
        CreditCardProgram updatedCardProgram = cardProgramRepository.findById(cardProgram.id).get()
        assert(updatedCardProgram.isActive)
        assert(updatedCardProgram.currentDayAccountCount == 0)
    }

    @Test
    void testResetWeeklyAccountCount(){
        CreditCardProgram cardProgram = mockObjects.getTestCreditCardProgram()
        cardProgram.isActive = false
        cardProgram.disableLevel = CreditCardProgram.DisableLevel.WEEKLY
        cardProgram.currentWeekAccountCount = cardProgram.weeklyAccountLimit
        scheduleOfChargesRepository.save(cardProgram.scheduleOfCharges)
        cardProgramRepository.save(cardProgram)

        CreditCardProgram savedCardProgram = cardProgramRepository.findById(cardProgram.id).get()
        assert(!savedCardProgram.isActive)
        assert(savedCardProgram.currentWeekAccountCount == cardProgram.currentWeekAccountCount)

        cardProgramManagement.resetWeeklyAccountCount()
        CreditCardProgram updatedCardProgram = cardProgramRepository.findById(cardProgram.id).get()
        assert(updatedCardProgram.isActive)
        assert(updatedCardProgram.currentWeekAccountCount == 0)
    }

    @Test
    void testResetMonthlyAccountCount(){
        CreditCardProgram cardProgram = mockObjects.getTestCreditCardProgram()
        cardProgram.isActive = false
        cardProgram.disableLevel = CreditCardProgram.DisableLevel.MONTHLY
        cardProgram.currentMonthAccountCount = cardProgram.monthlyAccountLimit
        scheduleOfChargesRepository.save(cardProgram.scheduleOfCharges)
        cardProgramRepository.save(cardProgram)

        CreditCardProgram savedCardProgram = cardProgramRepository.findById(cardProgram.id).get()
        assert(!savedCardProgram.isActive)
        assert(savedCardProgram.currentMonthAccountCount == cardProgram.currentMonthAccountCount)

        cardProgramManagement.resetMonthlyAccountCount()
        CreditCardProgram updatedCardProgram = cardProgramRepository.findById(cardProgram.id).get()
        assert(updatedCardProgram.isActive)
        assert(updatedCardProgram.currentMonthAccountCount == 0)
    }
}
