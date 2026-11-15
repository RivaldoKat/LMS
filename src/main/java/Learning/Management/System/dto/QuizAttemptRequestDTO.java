package Learning.Management.System.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class QuizAttemptRequestDTO {

    @NotNull
    private List<@Valid QuizAnswerRequestDTO> answers;

    private boolean timedOut;
}
