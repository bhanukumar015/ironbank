package hyperface.cms.interceptors

import groovy.util.logging.Slf4j
import hyperface.cms.Constants
import hyperface.cms.config.APIGatewayConfig
import hyperface.cms.domains.Client
import hyperface.cms.domains.ClientKey
import hyperface.cms.repository.ClientKeyRepository
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.lang.Nullable
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.HandlerInterceptor

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/** Interceptor for Client Authenticated routes */
@Component
@Slf4j
class ClientAuthInterceptor implements HandlerInterceptor {

    @Autowired
    APIGatewayConfig gatewayConfig

    @Autowired
    ClientKeyRepository clientKeyRepository

    @Override
    boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (StringUtils.isBlank(request.getHeader(Constants.API_KEY))) {
            String errorMessage = "Invalid request headers"
            log.error(errorMessage)
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, errorMessage)
        }

        String secretKey = request.getHeader(Constants.API_KEY)
        Optional<ClientKey> clientKeyOptional = clientKeyRepository.findBySecretKey(secretKey)

        if (!clientKeyOptional.isPresent()) {
            String errorMessage = "Invalid request headers"
            log.error(errorMessage)
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, errorMessage)
        }

        Client client = clientKeyOptional.get().getClient()

        request.getSession().setAttribute("client", client)

        return true
    }

    @Override
    void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        request.getSession().invalidate()
    }
}
