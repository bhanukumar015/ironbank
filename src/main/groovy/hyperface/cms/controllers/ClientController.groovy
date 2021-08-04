package hyperface.cms.controllers

import groovy.util.logging.Slf4j
import hyperface.cms.domains.Client
import hyperface.cms.domains.ClientKey
import hyperface.cms.repository.ClientKeyRepository
import hyperface.cms.repository.ClientRepository
import hyperface.cms.service.ClientService
import hyperface.cms.util.Response
import hyperface.cms.util.Utilities
import org.apache.commons.lang3.ObjectUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import hyperface.cms.Constants

@RestController
@RequestMapping(Constants.PATH_CLIENT_BASE_URL)
@Slf4j
class ClientController {

    @Autowired
    ClientRepository clientRepository

    @Autowired
    ClientKeyRepository clientKeyRepository

    @Autowired
    ClientService clientService

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Client> createOrSave(@RequestBody Client client, @RequestParam(name = "logoFile", required = false) MultipartFile multipartFile) throws IOException {
        if (ObjectUtils.isNotEmpty(multipartFile)) {
            String logo = Utilities.convertFileToBase64String(multipartFile)
            client.setLogo(logo)
        }
        client = clientRepository.save(client)

        ClientKey clientKey = clientService.createClientKey(client)
        clientKeyRepository.save(clientKey)
        return ResponseEntity.ok(client)
    }

    @PostMapping(value = "/{clientId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity get(@PathVariable(name = "clientId") String clientId) {
        Optional<Client> client = clientRepository.findById(clientId)
        if (!client.isPresent()) {
            String errorMessage = "Client record with id: ${clientId} is not found"
            log.error("Request to get client failed die to error: ${errorMessage}")
            return Response.returnError(errorMessage)
        }
        return Response.returnSimpleJson(client.get())
    }

    @PutMapping(value = "/{clientId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity update(Client client, @PathVariable(name = "clientId") String clientId, @RequestParam(name = "logoFile", required = false) MultipartFile multipartFile) throws IOException {
        Optional<Client> clientOptional = clientRepository.findById(clientId)
        if (!clientOptional.isPresent()) {
            String errorMessage = "Client record with id: ${clientId} is not found"
            log.error(errorMessage)
            return Response.returnError(errorMessage)
        }

        Client existingClient = clientOptional.get()
        if (ObjectUtils.isNotEmpty(multipartFile)) {
            String logo = Utilities.convertFileToBase64String(multipartFile)
            client.setLogo(logo)
        }

        existingClient.with {
            emailAddress = client.emailAddress
            logoUrl = client.logoUrl
            logo = client.logo ?: existingClient.logo
        }
        clientRepository.save(existingClient)

        return Response.returnSimpleJson(existingClient)
    }

    @DeleteMapping(value = "/{clientId}")
    ResponseEntity delete(@PathVariable(name = "clientId") String clientId) {
        clientRepository.deleteById(clientId)
        return Response.returnSimpleJson(null)
    }
}