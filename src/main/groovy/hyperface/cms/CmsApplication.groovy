
package hyperface.cms

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.context.annotation.ComponentScan
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.sql.DataSource

@RestController
@SpringBootApplication
@EnableAutoConfiguration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
@EnableAsync
@EnableJpaAuditing
@EnableTransactionManagement
@EnableConfigurationProperties
@ComponentScan(value = "hyperface.cms.*")
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

	@Bean
	public LockProvider lockProvider(DataSource dataSource) {
		return new JdbcTemplateLockProvider(
				JdbcTemplateLockProvider.Configuration.builder()
						.withJdbcTemplate(new JdbcTemplate(dataSource))
						.usingDbTime()
						.build()
		)
	}
}
