package hyperface.cms.util

import hyperface.cms.commands.GenericErrorResponse
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

class Response {
    static ResponseEntity returnSimpleJson(def resultObj) {
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(resultObj)
    }

    static ResponseEntity returnError(String errorMessage) {
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new GenericErrorResponse(reason: errorMessage))
    }
}
