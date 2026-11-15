package Learning.Management.System.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CourseResourceResponseDTO {
    private Long id;
    private Long courseId;
    private String courseName;
    private String title;
    private String originalFilename;
    private String contentType;
    private LocalDateTime uploadedAt;
    private String downloadUrl;
}
