package Learning.Management.System.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AssignmentResponseDTO {
    private Long id;
    private Long courseId;
    private String courseName;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Double maxScore;
}
