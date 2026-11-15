package Learning.Management.System.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuizCourseOptionDTO {
    private Long id;
    private String name;
    private String code;
}
