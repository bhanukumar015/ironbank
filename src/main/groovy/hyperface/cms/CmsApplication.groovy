
package hyperface.cms

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@SpringBootApplication
@EnableAutoConfiguration
class CmsApplication {

	@RequestMapping("/")
	String home() {
		return "Hello World!!!"
	}

	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
		return String.format("Hello %s!", name)
	}

	static void main(String[] args) {
		SpringApplication.run(CmsApplication, args)
	}

}
