package Learning.Management.System.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AnnouncementResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String authorName;
    private Long courseId;
    private String courseName;
    private LocalDateTime createdAt;
}
