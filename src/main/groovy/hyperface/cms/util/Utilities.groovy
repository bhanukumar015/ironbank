package hyperface.cms.util

import groovy.util.logging.Slf4j
import hyperface.cms.Constants
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.http.HttpStatus
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

@Slf4j
class Utilities {

    private Utilities() {}

    static String generateUniqueReference(int length) {
        return RandomStringUtils.randomAlphanumeric(length).toLowerCase()
    }

    static String convertFileToBase64String(MultipartFile multipartFile) {
        if (!Constants.MIME_TYPES.contains(multipartFile.contentType)) {
            String errorMessage = "Input File type: ${multipartFile.contentType} is unsupported"
            log.info("Invalid request. Error: {}", errorMessage)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage)
        }

        if (multipartFile.getSize() > Constants.MAX_IMAGE_FILE_SIZE * 1000) {
            String errorMessage = "Input File size is exceeded max limit ${Constants.MAX_IMAGE_FILE_SIZE} Kb"
            log.info("Invalid request. Error: {}", errorMessage)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage)
        }

        byte[] fileByteArray = multipartFile.getBytes()
        return Base64.getEncoder().encodeToString(fileByteArray)
    }
}
