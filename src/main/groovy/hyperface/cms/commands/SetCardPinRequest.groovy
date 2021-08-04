package hyperface.cms.commands

import hyperface.cms.domains.Card

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

class SetCardPinRequest {
    @NotBlank
    @Pattern(regexp = /^\d\d\d\d\d\d$/, message = "Card pin should be of length 6 containing only digits")
    // card pin
    String cardPin
    
    // Derived object
    Card card
}
