package Learning.Management.System.dto;

import Learning.Management.System.model.AttendanceStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AttendanceRequestDTO {
    private Long courseId;
    private Long studentId;
    private LocalDate date;
    private AttendanceStatus status;
}
