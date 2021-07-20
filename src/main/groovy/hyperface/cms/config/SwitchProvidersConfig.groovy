package hyperface.cms.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class SwitchProvidersConfig {

    @Value('${nium.api.key}')
    String niumAPIKey;

    @Value('${nium.client.name}')
    String niumClientName;

    @Value('${nium.url}')
    String niumUrl;
}
