package hyperface.cms.domains.SwitchProviders

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class NiumSwitchMetadata {

    String customerHashId

    String walletHashId
}
