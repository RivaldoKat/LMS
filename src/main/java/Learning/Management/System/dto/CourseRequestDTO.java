package Learning.Management.System.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseRequestDTO {

    @NotBlank(message = "Course name is required")
    private String name;

    @NotBlank(message = "Course code is required")
    private String code;

    private String description;

    @NotBlank(message = "Term is required")
    private String term;

    @NotNull(message = "Teacher ID is required")
    private Long teacherId;
}
