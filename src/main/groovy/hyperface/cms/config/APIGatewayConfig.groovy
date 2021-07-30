package hyperface.cms.config

import hyperface.cms.Constants
import hyperface.cms.interceptors.ClientAuthInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class APIGatewayConfig implements WebMvcConfigurer {

    @Value('${kong.gateway.enabled}')
    Boolean isServiceEnabled

    @Value('${kong.gateway.admin.url}')
    String adminURL

    @Value('${kong.gateway.proxy.url}')
    String proxyURL

    @Value('${kong.gateway.services.client}')
    String clientService

    @Autowired
    ClientAuthInterceptor clientAuthInterceptor

    @Override
    void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(clientAuthInterceptor).addPathPatterns(Constants.CLIENT_AUTH_ROUTES)
    }

}

