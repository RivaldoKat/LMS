package Learning.Management.System.dto;

import Learning.Management.System.entity.SchoolTerm;
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

    @NotBlank(message = "Required")
    private SchoolTerm schoolTerm;

    @NotNull(message = "Teacher ID is required")
    private Long teacherId;
}
