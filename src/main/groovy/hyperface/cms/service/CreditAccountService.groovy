package hyperface.cms.service

import hyperface.cms.domains.CreditAccount
import hyperface.cms.repository.CreditAccountRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.time.ZonedDateTime

@Service
class CreditAccountService {
    @Autowired
    CreditAccountRepository creditAccountRepository;

    List<CreditAccount> getAllCreditAccountsBillingCycleEndDate(ZonedDateTime start, ZonedDateTime end) {
        return creditAccountRepository.findAllByCurrentBillingEndDateGreaterThanEqualAndCurrentBillingEndDateLessThan(start, end)
    }
}
