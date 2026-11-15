package Learning.Management.System.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuizAnswerRequestDTO {

    @NotNull
    private Long questionId;

    @NotBlank
    private String responseText;
}
