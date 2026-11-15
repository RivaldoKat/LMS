package Learning.Management.System;

import Learning.Management.System.dto.QuizAttemptRequestDTO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuizAttemptRequestDTOValidationTests {

    @Test
    void allowsEmptyAnswersForTimedOutAttemptsButRequiresAnswersField() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();
            QuizAttemptRequestDTO request = new QuizAttemptRequestDTO();
            request.setTimedOut(true);
            request.setAnswers(List.of());

            assertTrue(validator.validate(request).isEmpty());

            request.setAnswers(null);
            assertFalse(validator.validate(request).isEmpty());
        }
    }
}
