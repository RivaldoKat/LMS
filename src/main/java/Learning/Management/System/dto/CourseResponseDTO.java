package Learning.Management.System.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseResponseDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String term;
    private String teacherName;
}
