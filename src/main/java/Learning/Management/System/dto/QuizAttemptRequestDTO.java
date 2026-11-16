package Learning.Management.System.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class QuizAttemptRequestDTO {

    private List<@Valid QuizAnswerRequestDTO> answers;

    private boolean timedOut;
}
