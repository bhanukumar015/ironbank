package hyperface.cms.controllers

import hyperface.cms.domains.CreditCardProgram
import hyperface.cms.repository.CardProgramRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping

@Controller
class BankController {

    @Autowired
    CardProgramRepository cardProgramRepository

    @GetMapping("/cardPrograms")
    public String listCardPrograms(Model model) {
        List<CreditCardProgram> cardPrograms = cardProgramRepository.findAll()
        cardPrograms.each {
            println it.dump()
        }
        model.addAttribute("cardPrograms", cardPrograms)
        return "cardPrograms"
    }

    @GetMapping("/cardProgram")
    public String createCardProgram(Model model) {
        model.addAttribute("cardProgram", new CreditCardProgram())
        return "cardProgram"
    }

    @PostMapping("/cardProgram")
    public String submitCardProgram(@ModelAttribute CreditCardProgram cardProgram, Model model) {
        model.addAttribute("cardProgram", cardProgram)
        return "result"
    }
}
