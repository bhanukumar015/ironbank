package hyperface.cms.config;

import hyperface.cms.service.pdfbox.PDFBoxService;
import hyperface.cms.service.pdfbox.PDFBoxServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    PDFBoxService pdfBoxService() {
        return new PDFBoxServiceImpl();
    }
}
