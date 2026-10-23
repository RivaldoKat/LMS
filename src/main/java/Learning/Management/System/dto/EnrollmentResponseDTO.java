package Learning.Management.System.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class EnrollmentResponseDTO {
    private Long id;
    private Long courseId;
    private String courseName;
    private Long studentId;
    private String studentName;
    private LocalDateTime enrollmentAt;
}
