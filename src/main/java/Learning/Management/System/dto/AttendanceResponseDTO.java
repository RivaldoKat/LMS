package Learning.Management.System.dto;

import Learning.Management.System.model.AttendanceStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class AttendanceResponseDTO {
    private Long id;
    private Long courseId;
    private String courseName;
    private Long studentId;
    private String studentName;
    private LocalDate date;
    private AttendanceStatus status;
}