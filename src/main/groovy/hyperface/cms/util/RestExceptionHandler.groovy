package hyperface.cms.util

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Error> methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult()
        List<FieldError> fieldErrors = result.getFieldErrors()
        Error error = new Error(HttpStatus.BAD_REQUEST.value(), "Validation Error", fieldErrors.size())
        for (FieldError fieldError : fieldErrors) {
            error.addFieldError(fieldError.getField(), fieldError.getDefaultMessage())
        }
        return ResponseEntity
                .badRequest()
                .body(error)
    }

    static class Error {
        private class FieldError {
            final String path
            final String errorMessage

            FieldError(String path, String errorMessage) {
                this.path = path
                this.errorMessage = errorMessage
            }

            String getPath() {
                return path
            }

            String getErrorMessage() {
                return errorMessage
            }
        }
        private final int status
        private final String message
        private final int count
        private List<FieldError> fieldErrors = new ArrayList<>()

        Error(int status, String message, int count) {
            this.status = status
            this.message = message
            this.count = count
        }

        void addFieldError(String path, String message) {
            FieldError error = new FieldError(path, message)
            fieldErrors.add(error)
        }

        int getStatus() {
            return status
        }

        String getMessage() {
            return message
        }

        List<FieldError> getFieldErrors() {
            return fieldErrors
        }

        void setFieldErrors(List<FieldError> fieldErrors) {
            this.fieldErrors = fieldErrors
        }

        int getCount() {
            return count
        }
    }
}
