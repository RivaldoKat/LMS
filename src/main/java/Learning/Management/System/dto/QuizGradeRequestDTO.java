package Learning.Management.System.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuizGradeRequestDTO {

    @NotNull
    @DecimalMin("0.0")
    private Double grade;

    private String feedback;
}
