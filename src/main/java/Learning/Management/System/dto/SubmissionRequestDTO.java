package Learning.Management.System.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmissionRequestDTO {

    @NotNull(message = "Assignment ID is required")
    private Long assignmentId;

    private String fileUrl;
}
