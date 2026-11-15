package Learning.Management.System.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import Learning.Management.System.entity.QuizQuestionType;

import java.util.List;

@Data
public class QuizQuestionRequestDTO {

    @NotBlank
    private String prompt;

    @NotNull
    @Positive
    private Double maxPoints;

    @NotNull
    private QuizQuestionType questionType = QuizQuestionType.WRITTEN;

    private List<@NotBlank String> options;

    private Integer correctOptionIndex;
}
