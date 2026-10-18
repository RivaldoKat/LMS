package Learning.Management.System.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AssignmentRequestDTO {
    private Long courseId;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Double maxScore;
}
