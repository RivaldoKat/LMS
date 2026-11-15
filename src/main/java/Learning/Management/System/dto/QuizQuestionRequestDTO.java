package Learning.Management.System.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class QuizQuestionRequestDTO {

    @NotBlank
    private String prompt;

    @NotNull
    @Positive
    private Double maxPoints;
}
