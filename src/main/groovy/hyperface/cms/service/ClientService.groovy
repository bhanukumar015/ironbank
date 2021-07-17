package hyperface.cms.service

import groovy.util.logging.Slf4j
import hyperface.cms.Constants
import hyperface.cms.domains.Client
import hyperface.cms.domains.ClientKey
import hyperface.cms.util.Utilities
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

@Service
@Slf4j
class ClientService {


    static String convertFileToBase64String(MultipartFile multipartFile) {
        if(!Constants.MIME_TYPES.contains(multipartFile.contentType)) {
            String errorMessage = "Input File type: ${multipartFile.contentType} is unsupported"
            log.info("Invalid request. Error: {}", errorMessage)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage)
        }

        if(multipartFile.getSize() > Constants.MAX_IMAGE_FILE_SIZE * 1000) {
            String errorMessage = "Input File size is exceeded max limit ${Constants.MAX_IMAGE_FILE_SIZE} Kb"
            log.info("Invalid request. Error: {}", errorMessage)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage)
        }

        byte[] fileByteArray = multipartFile.getBytes()
        return Base64.getEncoder().encodeToString(fileByteArray)
    }

     static ClientKey createClientKey(Client client) {
         //todo: may require to implement Kong Service here.
        ClientKey clientKey = new ClientKey()
        clientKey.secretKey = "secret_" + Utilities.generateUniqueReference(Constants.RANDOM_KEY_GENERATOR_LENGTH)
        clientKey.client = client
        return clientKey;
    }
}
