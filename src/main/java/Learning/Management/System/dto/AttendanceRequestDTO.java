package Learning.Management.System.dto;

import Learning.Management.System.model.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AttendanceRequestDTO {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Required field")
    private LocalDate date;

    @NotNull(message = "Required field")
    private AttendanceStatus status;
}
