package Learning.Management.System.dto;

import Learning.Management.System.entity.Grade;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EnrollmentResponseDTO {
    private Long id;
    private Long courseId;
    private String courseName;
    private Long studentId;
    private String studentName;
    private Grade grade;
    private LocalDateTime enrollmentAt;
}
