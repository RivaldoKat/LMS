package Learning.Management.System;

import Learning.Management.System.exceptions.GlobalExceptionHandler;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTests {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void missingEntitiesReturnNotFound() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleEntityNotFound(new EntityNotFoundException("Course not found"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().get("status"));
    }

    @Test
    void unexpectedFailuresDoNotExposeInternalMessages() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleGeneralException(new IllegalStateException("database password leaked"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().get("message"));
    }
}
