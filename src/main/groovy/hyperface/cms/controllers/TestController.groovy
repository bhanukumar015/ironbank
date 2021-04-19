package hyperface.cms.controllers

import hyperface.cms.domains.TestItem
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView

@RestController
public class TestController {

    @RequestMapping("/test")
    public @ResponseBody TestItem getTestData() {
        TestItem testItem = new TestItem();
        testItem.title = "title"
        testItem.link = "link"
        return testItem;
    }
}