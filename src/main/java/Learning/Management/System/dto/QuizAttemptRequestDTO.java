package Learning.Management.System.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class QuizAttemptRequestDTO {

    @NotEmpty
    private List<@Valid QuizAnswerRequestDTO> answers;
}
