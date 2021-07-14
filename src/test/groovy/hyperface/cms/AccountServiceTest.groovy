package hyperface.cms

import hyperface.cms.SwitchProvidersTests.Utility.MockObjects
import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.repository.CardProgramRepository
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

    static MockObjects mockObjects = new MockObjects()

    @Test
    void testCreateCreditAccount(){
        CreditCardProgram cardProgram = mockObjects.getTestCreditCardProgram()
        cardProgram.isActive = false
        cardProgram.disableLevel = CreditCardProgram.DisableLevel.MANUAL
        def response = accountService.createCreditAccount(mockObjects.getTestCustomer()
                , cardProgram, 0.0)
        assert response.isLeft()
    }

    @Test
    void testResetDailyAccountCount(){
        CreditCardProgram cardProgram = mockObjects.getTestCreditCardProgram()
        cardProgram.isActive = false
        cardProgram.disableLevel = CreditCardProgram.DisableLevel.DAILY
        cardProgram.currentDayAccountCount = cardProgram.dailyAccountLimit
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
