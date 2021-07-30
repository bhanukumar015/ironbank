package hyperface.cms.commands.cardapplication

import org.apache.commons.lang3.StringUtils
import org.springframework.format.annotation.DateTimeFormat

import javax.validation.constraints.AssertFalse
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import java.time.LocalDate

class NomineeInfoAndFatcaRequest {
    @NotBlank(message = "applicationRefId must not be null/empty")
    String applicationRefId

    @NotBlank(message = "fdRefId must not be null/empty")
    String fixedDepositRefId

    @NotBlank(message = "nomineeName must not be null/empty")
    String nomineeName

    @NotBlank(message = "nomineeDob must not be null/empty")
    @DateTimeFormat(pattern = "yyyyMMdd")
    String nomineeDob

    String nomineeGuardian

    @NotNull(message = "fatcaConfirmed must not be null/empty")
    Boolean fatcaConfirmed

    @NotBlank(message = "motherMaidenName must not be null/empty")
    String motherMaidenName


    @AssertFalse(message = "nomineeGuardian is required since nominee is a minor")
    private boolean isGuardianNeeded() {
        int nomineeBirthYear = Integer.parseInt(nomineeDob.substring(0, 4))

        return (LocalDate.now().getYear() - nomineeBirthYear) < 18
                && StringUtils.isBlank(nomineeGuardian)
    }
}
