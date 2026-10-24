package Learning.Management.System.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GradeRequestDTO {

    @NotNull(message = "Grade is required")
    private Double grade;

    @NotNull(message = "Feedback is required")
    private String feedback;
}
