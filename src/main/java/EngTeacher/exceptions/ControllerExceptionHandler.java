package EngTeacher.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException ex) {
        log.error("NotFoundException occurred", ex);
        return ResponseEntity
                .status(404)
                .body(Map.of(
                    "error", "NOT_FOUND",
                        "message", String.format("Not found: %s", ex.getMessage())
                ));
    }

    @ExceptionHandler(ImproperApiUsageException.class)
    public ResponseEntity<?> handleNotFound(ImproperApiUsageException ex) {
        return ResponseEntity
                .status(404)
                .body(Map.of(
                        "error", "IMPROPER_USAGE_ERROR",
                        "message", String.format("API was used improper: %s", ex.getMessage())
                ));
    }

    @ExceptionHandler(AgentResponseParsingException.class)
    public ResponseEntity<?> handleNotFound(AgentResponseParsingException ex) {
        log.error("The response of the agent couldn't be parsed", ex);
        return ResponseEntity
                .status(404)
                .body(Map.of(
                    "error", "INTERNAL_ERROR",
                    "message", "Something went wrong"
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return ResponseEntity
                .status(500)
                .body(Map.of(
                    "error", "INTERNAL_ERROR",
                    "message", "Something went wrong"
                ));
    }
}
