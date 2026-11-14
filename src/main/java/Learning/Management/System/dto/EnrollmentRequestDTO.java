package Learning.Management.System.dto;

import Learning.Management.System.entity.Grade;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnrollmentRequestDTO {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Grade is required")
    private Grade grade;
}
